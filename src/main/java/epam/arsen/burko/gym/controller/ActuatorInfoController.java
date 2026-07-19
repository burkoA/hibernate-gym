package epam.arsen.burko.gym.controller;

import epam.arsen.burko.gym.metrics.GymMetrics;
import epam.arsen.burko.gym.service.MetricsTrackingService;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/actuator-info")
public class ActuatorInfoController {

    private final GymMetrics gymMetrics;
    private final MetricsTrackingService metricsTrackingService;
    private final HealthEndpoint healthEndpoint;

    public ActuatorInfoController(GymMetrics gymMetrics, MetricsTrackingService metricsTrackingService,
                                  HealthEndpoint healthEndpoint) {
        this.gymMetrics = gymMetrics;
        this.metricsTrackingService = metricsTrackingService;
        this.healthEndpoint = healthEndpoint;
    }

    @GetMapping("/metrics-summary")
    public ResponseEntity<Map<String, Object>> getMetricsSummary() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("summary", metricsTrackingService.getMetricsSummary());
        response.put("activeTrainees", gymMetrics.getActiveTraineesCount());
        response.put("activeTrainers", gymMetrics.getActiveTrainersCount());
        response.put("totalCaloriesBurned", gymMetrics.getTotalCaloriesBurned());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health-status")
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        HealthComponent health = healthEndpoint.health();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", health.getStatus().toString());
        response.put("details", Map.of("health", health));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health-check")
    public ResponseEntity<String> healthCheck() {
        HealthComponent health = healthEndpoint.health();
        String status = health.getStatus().toString();
        if ("UP".equals(status)) {
            return ResponseEntity.ok("Application is healthy");
        } else if ("OUT_OF_SERVICE".equals(status)) {
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body("Application is degraded");
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Application is down");
        }
    }
}


