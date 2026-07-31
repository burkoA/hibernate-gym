package epam.arsen.burko.gym.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class EnvironmentProperties {

    private String name;
    private String version;
    private String environment;
    private boolean debugMode;
    private boolean logRequests;
    private long requestTimeout;
    private boolean cacheEnabled;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public boolean isLogRequests() {
        return logRequests;
    }

    public void setLogRequests(boolean logRequests) {
        this.logRequests = logRequests;
    }

    public long getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(long requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    @Override
    public String toString() {
        return "EnvironmentProperties{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", environment='" + environment + '\'' +
                ", debugMode=" + debugMode +
                ", logRequests=" + logRequests +
                ", requestTimeout=" + requestTimeout +
                ", cacheEnabled=" + cacheEnabled +
                '}';
    }
}

