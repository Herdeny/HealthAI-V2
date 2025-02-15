package com.github.herdeny.healthaiv2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Paths;

@SpringBootApplication
public class HealthAiv2Application extends SpringBootServletInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(HealthAiv2Application.class);

    @Value("${PROJECT_PATH:.}")
    private String projectPath;

    @Value("${MODEL_PATH}")
    private String modelPath;

    @Value("${SCRIPT_PATH}")
    private String scriptPath;

    @Value("${DATA_PATH}")
    private String dataPath;

    public static void main(String[] args) {

        SpringApplication.run(HealthAiv2Application.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application){
        return application.sources(HealthAiv2Application.class);
    }

    @Override
    public void run(String... args) throws Exception {
        String project_Path = Paths.get(projectPath).toAbsolutePath().toString();
        logger.info("Project Path: " + project_Path);
        String model_Path = Paths.get(modelPath).toAbsolutePath().toString();
        logger.info("Model Path: " + model_Path);
        String script_Path = Paths.get(scriptPath).toAbsolutePath().toString();
        logger.info("Script Path: " + script_Path);
        String data_Path = Paths.get(dataPath).toAbsolutePath().toString();
        logger.info("Data Path: " + data_Path);
    }

}
