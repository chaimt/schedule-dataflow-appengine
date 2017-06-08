package com.turel.migration.dataflow.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Chaim on 07/06/2017.
 */
@RestController
@EnableAutoConfiguration
public class ScheduleDataflowMVC {
    private static final Logger LOG = LoggerFactory.getLogger(ScheduleDataflowMVC.class);
    public static final String PIPELINE = "pipeline";
    public static final String JOB_NAME = "jobName";
    public static final String PCS_PATH = "gcsPath";

    @RequestMapping("/")
    public String home() {
        return "Hello World!";
    }

    @RequestMapping("/dataflow/execute")
    public String executeDataflow(HttpServletRequest request){
        try {
            LOG.info("start pipeline spring boot");
            Map<String, String[]> parameters = request.getParameterMap();
            List<String> appArgs = parameters.keySet().stream().filter(key -> !key.equalsIgnoreCase(PIPELINE)).map(key -> String.format("--%s=%s",key,parameters.get(key)[0])).collect(Collectors.toList());
            String[] appParams = appArgs.toArray(new String[appArgs.size()]);
            String[] pipline = parameters.get(PIPELINE);
            if (pipline == null || pipline.length==0)
                throw new RuntimeException("pipeline parameter is missing");
            Class<?> aClass = Class.forName(pipline[0]);
            Method method = aClass.getMethod("main", String[].class);
            LOG.info("Running dataflow with " + appArgs.toString());
            method.invoke(null, (Object) appParams);
            return "Running with params:" + appArgs.toString();
        } catch (Exception e) {
            LOG.error("execution error: " + e.getMessage());
            return "error: " + e.getMessage();
        }
    }


    public static void main(String[] args) throws Exception {
        SpringApplication.run(ScheduleDataflowMVC.class, args);
    }
}
