package epam.arsen.burko.gym.service;

import epam.arsen.burko.gym.config.EnvironmentConfiguration;
import epam.arsen.burko.gym.config.EnvironmentProperties;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EnvironmentService {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentService.class);
    private final Environment environment;
    private final EnvironmentProperties environmentProperties;
    private final EnvironmentConfiguration.EnvironmentConfig environmentConfig;

    public EnvironmentService(Environment environment,
                             EnvironmentProperties environmentProperties,
                             EnvironmentConfiguration.EnvironmentConfig environmentConfig) {
        this.environment = environment;
        this.environmentProperties = environmentProperties;
        this.environmentConfig = environmentConfig;
        logger.info("EnvironmentService initialized for environment: {}", getCurrentEnvironment());
    }

    public String getCurrentEnvironment() {
        return environmentProperties.getEnvironment();
    }

    public boolean isLocalEnvironment() {
        return "local".equalsIgnoreCase(getCurrentEnvironment());
    }

    public boolean isDevEnvironment() {
        return "dev".equalsIgnoreCase(getCurrentEnvironment());
    }

    public boolean isStagingEnvironment() {
        return "stg".equalsIgnoreCase(getCurrentEnvironment());
    }

    public boolean isProductionEnvironment() {
        return "prod".equalsIgnoreCase(getCurrentEnvironment());
    }

    public String[] getActiveProfiles() {
        String[] profiles = environment.getActiveProfiles();
        return profiles.length == 0 ? environment.getDefaultProfiles() : profiles;
    }

    public boolean isProfileActive(String profile) {
        return environment.acceptsProfiles(Profiles.of(profile));
    }

    public boolean isDebugMode() {
        return environmentProperties.isDebugMode();
    }

    public boolean isLogRequests() {
        return environmentProperties.isLogRequests();
    }

    public boolean isCacheEnabled() {
        return environmentProperties.isCacheEnabled();
    }

    public long getRequestTimeout() {
        return environmentProperties.getRequestTimeout();
    }

    public String getApplicationName() {
        return environmentProperties.getName();
    }

    public String getApplicationVersion() {
        return environmentProperties.getVersion();
    }

    public String getDatabaseUrl() {
        return environmentConfig.getDatabaseUrl();
    }

    public boolean isSslEnabled() {
        return environmentConfig.isSslEnabled();
    }

    public boolean isH2ConsoleEnabled() {
        return environmentConfig.isH2ConsoleEnabled();
    }

    public String getProperty(String key, String defaultValue) {
        String value = environment.getProperty(key, defaultValue);
        logger.debug("Retrieved property '{}' = {}", key, maskSensitiveInfo(value));
        return value;
    }

    public String getProperty(String key) {
        String value = environment.getProperty(key);
        logger.debug("Retrieved property '{}' = {}", key, maskSensitiveInfo(value));
        return value;
    }

    public EnvironmentProperties getEnvironmentProperties() {
        return environmentProperties;
    }

    public void logEnvironmentSummary() {
        logger.info("Environment Summary:");
        logger.info("  Current Environment: {}", getCurrentEnvironment());
        logger.info("  Active Profiles: {}", String.join(", ", getActiveProfiles()));
        logger.info("  Application: {} v{}", getApplicationName(), getApplicationVersion());
        logger.info("  Debug Mode: {}", isDebugMode());
        logger.info("  Log Requests: {}", isLogRequests());
        logger.info("  Cache Enabled: {}", isCacheEnabled());
        logger.info("  SSL Enabled: {}", isSslEnabled());
        logger.info("  Request Timeout: {}ms", getRequestTimeout());
    }

    @Override
    public String toString() {
        return "EnvironmentService{" +
                "environment='" + getCurrentEnvironment() + '\'' +
                ", debugMode=" + isDebugMode() +
                ", logRequests=" + isLogRequests() +
                ", cacheEnabled=" + isCacheEnabled() +
                ", sslEnabled=" + isSslEnabled() +
                '}';
    }

    private String maskSensitiveInfo(String value) {
        if (value == null) {
            return null;
        }
        if (value.toLowerCase().contains("password")) {
            return "****";
        }
        return value;
    }
}

