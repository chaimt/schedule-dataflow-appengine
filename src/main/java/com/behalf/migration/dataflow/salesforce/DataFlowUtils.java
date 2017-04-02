package com.behalf.migration.dataflow.salesforce;

import com.behalf.migration.utils.LogUtils;
import com.google.api.services.dataflow.model.Job;
import com.google.api.services.dataflow.model.ListJobsResponse;
import javaslang.control.Try;
import org.apache.beam.runners.dataflow.DataflowClient;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Chaim on 06/03/2017.
 * Utilities for Dataflow processes
 */
public class DataFlowUtils {
    public enum JobStatus{
        Running,
        QuitePeriod,
        Nothing
    }

    public interface JobRunOptions  extends DataflowPipelineOptions{
        @Description("Quite Period from last job start. In minutes")
        @Default.Integer(5)
        Integer getQuitePeriod();

        void setQuitePeriod(Integer value);
    }


    public static Pair<JobStatus,String> isJobRunning(DataflowPipelineOptions options, String jobName, int jobIntervalinMinutes) throws IOException {
        DataflowClient dataflowClient = DataflowClient.create(options);
        ListJobsResponse currentJobs = dataflowClient.listJobs(null);
        final List<Job> jobs = currentJobs.getJobs();
        if (jobs!=null) {
            List<Job> runningJobs = jobs.stream()
                    .filter(job -> job.getName().startsWith(jobName))
                    .filter(job -> job.getCurrentState().equals("JOB_STATE_RUNNING"))
                    .collect(Collectors.toList());
            //check if x minutes have passed sine last run
            if (runningJobs.size() == 0) {
                Optional<Job> job_state_done = jobs.stream()
                        .filter(job -> job.getName().startsWith(jobName))
                        .filter(job -> job.getCurrentState().equals("JOB_STATE_DONE"))
                        .max(Comparator.comparingLong(p -> ISODateTimeFormat.dateTimeParser().parseDateTime(p.getCreateTime()).getMillis()));
                if (job_state_done.isPresent()) {
                    long millis = ISODateTimeFormat.dateTimeParser().parseDateTime(job_state_done.get().getCreateTime()).getMillis();
                    long passedMinutes = (System.currentTimeMillis() - millis) / 1000 / 60;
                    if (passedMinutes < jobIntervalinMinutes)
                        return Pair.of(JobStatus.QuitePeriod,job_state_done.get().getCreateTime());
                    else
                        return Pair.of(JobStatus.Nothing,"");
                }

            } else
                return Pair.of(JobStatus.Running,runningJobs.get(0).getCreateTime());
        }
        return Pair.of(JobStatus.Nothing,"");
    }

    public static Try<Boolean> canJobRun(JobRunOptions options, Class<?> jobClass, int quitePeriod, Logger LOG)  {
        return Try.of(() -> {
            int lastPackage = jobClass.getCanonicalName().lastIndexOf(".");
            String className = jobClass.getCanonicalName().substring(lastPackage+1, jobClass.getCanonicalName().length());
            Pair<JobStatus, String> jobRunning = DataFlowUtils.isJobRunning(options, className.toLowerCase(), quitePeriod);
            if (jobRunning.getKey() != DataFlowUtils.JobStatus.Nothing) {
                LOG.warn(LogUtils.prefixLog("job {} {} [{}min QP - {}]"), className,jobRunning.getKey(),options.getQuitePeriod(),jobRunning.getValue());
                return false;
            }
            return true;
        });
    }



}
