package com.behalf.migration.dataflow.salesforce;

import com.behalf.migration.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static spark.Spark.get;
import static spark.Spark.port;

/**
 * Created by Chaim on 15/03/2017.
 * Class for Dataflow Schedualing from cron
  */
public class ScheduleDataflow {
    private static final Logger LOG = LoggerFactory.getLogger(ScheduleDataflow.class);
    public static final String PIPELINE = "pipeline";

    public static void main(String[] args) {
        port(8080);

        get("/dataflow/execute", (req, res) -> {
            try {
                LOG.info(LogUtils.prefixLog("start pipeline"));
                String pipline = req.queryParams(PIPELINE);
                if (pipline==null)
                    throw new RuntimeException("pipeline parameter is missing");
                Class<?> aClass = Class.forName(pipline);
                Method method = aClass.getMethod("main", String[].class);

                List<String> appArgs = getArgs(req,Arrays.asList(PIPELINE));
                String[] appParams = new String[appArgs.size()];
                appParams = appArgs.toArray(appParams);
                LOG.info(LogUtils.prefixLog("Running dataflow with {}"), appArgs.toString());

                method.invoke(null,(Object)appParams);

                return "Running with params:" + appArgs.toString();
            } catch (Exception e) {
                LOG.error(LogUtils.prefixLog(e.getMessage()));
                return "error: " + e.getMessage();
            }
        });

        get("/", (req, res) -> {
            LOG.info(LogUtils.prefixLog("request main"));
            return "Scheduler Running";
        });

        get("/_ah/health", (req, res) -> "OK");


    }

    private static List<String> getArgs(Request req, List<String> ignore) {
        List<String> appArgs = new LinkedList<>();
        for (String param : req.queryParams()) {
            String paramValue = req.queryParams(param);
            if (ignore.indexOf(param) ==-1 && paramValue != null) {
                appArgs.add(String.format("--%s=%s", param, paramValue));
            }
        }
        return appArgs;
    }
}
