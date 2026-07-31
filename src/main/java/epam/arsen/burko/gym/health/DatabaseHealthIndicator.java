package epam.arsen.burko.gym.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component("databaseHealth")
public class DatabaseHealthIndicator implements HealthIndicator {

    @Autowired
    private DataSource dataSource;

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection != null && !connection.isClosed()) {
                return Health.up()
                        .withDetail("database", "H2")
                        .withDetail("status", "Database connection is available")
                        .build();
            } else {
                return Health.down()
                        .withDetail("database", "H2")
                        .withDetail("error", "Connection is closed")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "H2")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}

