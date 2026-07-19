package epam.arsen.burko.gym.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableConfigurationProperties(EnvironmentProperties.class)
public class EnvironmentConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentConfiguration.class);
    private final Environment environment;
    private final EnvironmentProperties envProperties;

    public EnvironmentConfiguration(Environment environment, EnvironmentProperties envProperties) {
        this.environment = environment;
        this.envProperties = envProperties;
        logEnvironmentInfo();
    }

    private void logEnvironmentInfo() {
        String activeProfiles = String.join(", ", environment.getActiveProfiles());
        logger.info("=================================================");
        logger.info("Gym Application Initialization");
        logger.info("=================================================");
        logger.info("Application Name: {}", envProperties.getName());
        logger.info("Application Version: {}", envProperties.getVersion());
        logger.info("Active Profiles: {}", activeProfiles.isEmpty() ? "default" : activeProfiles);
        logger.info("Environment: {}", envProperties.getEnvironment());
        logger.info("Debug Mode: {}", envProperties.isDebugMode());
        logger.info("Log Requests: {}", envProperties.isLogRequests());
        logger.info("Cache Enabled: {}", envProperties.isCacheEnabled());
        logger.info("Request Timeout: {}ms", envProperties.getRequestTimeout());

        String dbUrl = environment.getProperty("spring.datasource.url", "Not configured");
        String dbDriver = environment.getProperty("spring.datasource.driver-class-name", "Not configured");
        logger.info("Database URL: {}", maskSensitiveInfo(dbUrl));
        logger.info("Database Driver: {}", dbDriver);

        boolean sslEnabled = Boolean.parseBoolean(environment.getProperty("server.ssl.enabled", "false"));
        logger.info("SSL Enabled: {}", sslEnabled);

        logger.info("=================================================");
    }

    private String maskSensitiveInfo(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        return url.replaceAll("password=[^&;]*", "password=****");
    }

    @Bean
    @Profile("local")
    public EnvironmentConfig localEnvironmentConfig() {
        logger.info("[LOCAL] Loading LOCAL environment configuration");
        logger.info("[LOCAL] H2 In-Memory Database configured");
        logger.info("[LOCAL] H2 Console enabled at /h2-console");
        return new EnvironmentConfig(
                "LOCAL",
                true,
                true,
                true,
                false,
                false,
                "jdbc:h2:mem:gymdb"
        );
    }

    @Bean
    @Profile("dev")
    public EnvironmentConfig devEnvironmentConfig() {
        String url = environment.getProperty("spring.datasource.url", "jdbc:mysql://localhost:3306/gym_dev");
        logger.info("[DEV] Loading DEVELOPMENT environment configuration");
        logger.info("[DEV] MySQL Database configured at: {}", maskSensitiveInfo(url));
        logger.info("[DEV] Request logging enabled");
        logger.info("[DEV] Schema validation mode");
        return new EnvironmentConfig(
                "DEV",
                false,
                true,
                false,
                false,
                false,
                url
        );
    }

    @Bean
    @Profile("stg")
    public EnvironmentConfig stagingEnvironmentConfig() {
        String url = environment.getProperty("spring.datasource.url", "jdbc:mysql://stg-db-server:3306/gym_stg");
        logger.info("[STG] Loading STAGING environment configuration");
        logger.info("[STG] MySQL Database configured at: {}", maskSensitiveInfo(url));
        logger.info("[STG] Cache enabled for improved performance");
        logger.info("[STG] Connection pool size: 20");
        return new EnvironmentConfig(
                "STG",
                false,
                true,
                false,
                false,
                true,
                url
        );
    }

    @Bean
    @Profile("prod")
    public EnvironmentConfig productionEnvironmentConfig() {
        String url = environment.getProperty("spring.datasource.url", "jdbc:mysql://prod-db-server:3306/gym_prod");
        logger.warn("[PROD] ========== PRODUCTION ENVIRONMENT INITIALIZED ==========");
        logger.warn("[PROD] MySQL Database configured at: {}", maskSensitiveInfo(url));
        logger.warn("[PROD] SSL/TLS enabled for secure communication");
        logger.warn("[PROD] Cache enabled (required for production)");
        logger.warn("[PROD] Connection pool size: 50");
        logger.warn("[PROD] Request logging disabled for performance");
        logger.warn("[PROD] Error logging level only");
        logger.warn("[PROD] ========================================================");
        return new EnvironmentConfig(
                "PROD",
                false,
                false,
                false,
                true,
                true,
                url
        );
    }

    @Bean
    public EnvironmentProperties getEnvironmentProperties() {
        return envProperties;
    }

    public static class EnvironmentConfig {
        private final String name;
        private final boolean debugMode;
        private final boolean logRequests;
        private final boolean h2ConsoleEnabled;
        private final boolean sslEnabled;
        private final boolean cacheEnabled;
        private final String databaseUrl;

        public EnvironmentConfig(String name, boolean debugMode, boolean logRequests,
                                boolean h2ConsoleEnabled, boolean sslEnabled,
                                boolean cacheEnabled, String databaseUrl) {
            this.name = name;
            this.debugMode = debugMode;
            this.logRequests = logRequests;
            this.h2ConsoleEnabled = h2ConsoleEnabled;
            this.sslEnabled = sslEnabled;
            this.cacheEnabled = cacheEnabled;
            this.databaseUrl = databaseUrl;
        }

        public String getName() {
            return name;
        }

        public boolean isDebugMode() {
            return debugMode;
        }

        public boolean isLogRequests() {
            return logRequests;
        }

        public boolean isH2ConsoleEnabled() {
            return h2ConsoleEnabled;
        }

        public boolean isSslEnabled() {
            return sslEnabled;
        }

        public boolean isCacheEnabled() {
            return cacheEnabled;
        }

        public String getDatabaseUrl() {
            return databaseUrl;
        }

        @Override
        public String toString() {
            return "EnvironmentConfig{" +
                    "name='" + name + '\'' +
                    ", debugMode=" + debugMode +
                    ", logRequests=" + logRequests +
                    ", h2ConsoleEnabled=" + h2ConsoleEnabled +
                    ", sslEnabled=" + sslEnabled +
                    ", cacheEnabled=" + cacheEnabled +
                    ", databaseUrl='" + databaseUrl + '\'' +
                    '}';
        }
    }
}

