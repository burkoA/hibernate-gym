package epam.arsen.burko.gym.service;

import epam.arsen.burko.gym.metrics.GymMetrics;
import org.springframework.stereotype.Service;

@Service
public class MetricsTrackingService {

    private final GymMetrics gymMetrics;

    public MetricsTrackingService(GymMetrics gymMetrics) {
        this.gymMetrics = gymMetrics;
    }

    public void trackTraineeRegistration() {
        gymMetrics.recordTraineeRegistration();
        gymMetrics.incrementActiveTrainees();
    }

    public void trackTraineeInactive() {
        gymMetrics.decrementActiveTrainees();
    }

    public void trackTrainerRegistration() {
        gymMetrics.recordTrainerRegistration();
        gymMetrics.incrementActiveTrainers();
    }

    public void trackTrainerInactive() {
        gymMetrics.decrementActiveTrainers();
    }

    public void trackTrainingSession(long durationSeconds, long caloriesBurned) {
        gymMetrics.recordTrainingSession(durationSeconds);
        gymMetrics.addCaloriesBurned(caloriesBurned);
    }

    public String getMetricsSummary() {
        return String.format(
                "Active Trainees: %d, Active Trainers: %d, Total Calories Burned: %d",
                gymMetrics.getActiveTraineesCount(),
                gymMetrics.getActiveTrainersCount(),
                gymMetrics.getTotalCaloriesBurned()
        );
    }
}

