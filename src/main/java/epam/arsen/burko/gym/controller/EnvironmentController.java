package epam.arsen.burko.gym.controller;

import epam.arsen.burko.gym.service.EnvironmentService;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/environment")
public class EnvironmentController {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentController.class);

    private final EnvironmentService environmentService;
    private final HealthEndpoint healthEndpoint;

    public EnvironmentController(EnvironmentService environmentService, HealthEndpoint healthEndpoint) {
        this.environmentService = environmentService;
        this.healthEndpoint = healthEndpoint;
        logger.info("EnvironmentController initialized");
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getEnvironmentInfo() {
        logger.info("Environment info requested");
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("environment", environmentService.getCurrentEnvironment());
        response.put("activeProfiles", environmentService.getActiveProfiles());
        response.put("applicationName", environmentService.getApplicationName());
        response.put("applicationVersion", environmentService.getApplicationVersion());

        logger.debug("Returning environment info: {}", environmentService.getCurrentEnvironment());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/settings")
    public ResponseEntity<Map<String, Object>> getEnvironmentSettings() {
        logger.info("Environment settings requested");
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("environment", environmentService.getCurrentEnvironment());

        Map<String, Object> settings = new LinkedHashMap<>();
        settings.put("debugMode", environmentService.isDebugMode());
        settings.put("logRequests", environmentService.isLogRequests());
        settings.put("cacheEnabled", environmentService.isCacheEnabled());
        settings.put("sslEnabled", environmentService.isSslEnabled());
        settings.put("h2ConsoleEnabled", environmentService.isH2ConsoleEnabled());
        settings.put("requestTimeout", environmentService.getRequestTimeout() + "ms");

        response.put("settings", settings);

        Map<String, Boolean> environmentFlags = new LinkedHashMap<>();
        environmentFlags.put("isLocal", environmentService.isLocalEnvironment());
        environmentFlags.put("isDev", environmentService.isDevEnvironment());
        environmentFlags.put("isStaging", environmentService.isStagingEnvironment());
        environmentFlags.put("isProduction", environmentService.isProductionEnvironment());

        response.put("environmentFlags", environmentFlags);

        logger.debug("Returning environment settings for: {}", environmentService.getCurrentEnvironment());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getEnvironmentStatus() {
        logger.info("Environment status requested");
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("environment", environmentService.getCurrentEnvironment());
        response.put("applicationName", environmentService.getApplicationName());
        response.put("applicationVersion", environmentService.getApplicationVersion());

        var health = healthEndpoint.health();
        response.put("healthStatus", health.getStatus().toString());

        Map<String, Object> statusDetails = new LinkedHashMap<>();
        statusDetails.put("debugMode", environmentService.isDebugMode());
        statusDetails.put("cacheEnabled", environmentService.isCacheEnabled());
        statusDetails.put("loggingEnabled", environmentService.isLogRequests());

        response.put("statusDetails", statusDetails);

        logger.debug("Environment status - Health: {}, Environment: {}", health.getStatus(), environmentService.getCurrentEnvironment());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profiles")
    public ResponseEntity<Map<String, Object>> getEnvironmentProfiles() {
        logger.info("Environment profiles requested");
        Map<String, Object> response = new LinkedHashMap<>();

        String[] activeProfiles = environmentService.getActiveProfiles();
        response.put("activeProfiles", activeProfiles);
        response.put("activeProfilesCount", activeProfiles.length);

        Map<String, Boolean> profileStatus = new LinkedHashMap<>();
        profileStatus.put("local", environmentService.isProfileActive("local"));
        profileStatus.put("dev", environmentService.isProfileActive("dev"));
        profileStatus.put("stg", environmentService.isProfileActive("stg"));
        profileStatus.put("prod", environmentService.isProfileActive("prod"));

        response.put("profileStatus", profileStatus);

        logger.debug("Returning profile status: {}", String.join(", ", activeProfiles));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getEnvironmentSummary() {
        logger.info("Environment summary requested");
        Map<String, Object> summary = new LinkedHashMap<>();

        Map<String, Object> basicInfo = new LinkedHashMap<>();
        basicInfo.put("environment", environmentService.getCurrentEnvironment());
        basicInfo.put("name", environmentService.getApplicationName());
        basicInfo.put("version", environmentService.getApplicationVersion());
        summary.put("basicInfo", basicInfo);

        summary.put("activeProfiles", environmentService.getActiveProfiles());

        Map<String, Object> configuration = new LinkedHashMap<>();
        configuration.put("debugMode", environmentService.isDebugMode());
        configuration.put("logRequests", environmentService.isLogRequests());
        configuration.put("cacheEnabled", environmentService.isCacheEnabled());
        configuration.put("sslEnabled", environmentService.isSslEnabled());
        configuration.put("requestTimeout", environmentService.getRequestTimeout() + "ms");
        summary.put("configuration", configuration);

        Map<String, Boolean> flags = new LinkedHashMap<>();
        flags.put("local", environmentService.isLocalEnvironment());
        flags.put("dev", environmentService.isDevEnvironment());
        flags.put("staging", environmentService.isStagingEnvironment());
        flags.put("production", environmentService.isProductionEnvironment());
        summary.put("environmentFlags", flags);

        var health = healthEndpoint.health();
        summary.put("healthStatus", health.getStatus().toString());

        logger.debug("Environment summary retrieved for: {}", environmentService.getCurrentEnvironment());
        return ResponseEntity.ok(summary);
    }
}

