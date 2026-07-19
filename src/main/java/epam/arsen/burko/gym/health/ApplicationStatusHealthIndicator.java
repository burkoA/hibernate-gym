package epam.arsen.burko.gym.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

@Component("applicationStatus")
public class ApplicationStatusHealthIndicator implements HealthIndicator {

    private static final float MEMORY_THRESHOLD = 0.85f;

    @Override
    public Health health() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long heapMemoryUsed = memoryBean.getHeapMemoryUsage().getUsed();
        long heapMemoryMax = memoryBean.getHeapMemoryUsage().getMax();

        float memoryUsagePercentage = (float) heapMemoryUsed / heapMemoryMax;

        if (memoryUsagePercentage > MEMORY_THRESHOLD) {
            return Health.outOfService()
                    .withDetail("application", "Gym Application")
                    .withDetail("status", "Running with high memory usage")
                    .withDetail("heapMemoryUsed", formatBytes(heapMemoryUsed))
                    .withDetail("heapMemoryMax", formatBytes(heapMemoryMax))
                    .withDetail("memoryUsagePercentage", String.format("%.2f%%", memoryUsagePercentage * 100))
                    .build();
        }

        return Health.up()
                .withDetail("application", "Gym Application")
                .withDetail("status", "Running normally")
                .withDetail("heapMemoryUsed", formatBytes(heapMemoryUsed))
                .withDetail("heapMemoryMax", formatBytes(heapMemoryMax))
                .withDetail("memoryUsagePercentage", String.format("%.2f%%", memoryUsagePercentage * 100))
                .build();
    }

    private String formatBytes(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}


