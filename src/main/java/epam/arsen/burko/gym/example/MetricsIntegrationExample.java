package epam.arsen.burko.gym.example;

import epam.arsen.burko.gym.metrics.GymMetrics;
import epam.arsen.burko.gym.service.MetricsTrackingService;
import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MetricsIntegrationExample {

    @Autowired
    private MetricsTrackingService metricsTrackingService;

    @Autowired
    private GymMetrics gymMetrics;

    public void registerNewTrainee(String traineeName) {
        System.out.println("Registering trainee: " + traineeName);
        metricsTrackingService.trackTraineeRegistration();
    }

    public void registerNewTrainer(String trainerName) {
        System.out.println("Registering trainer: " + trainerName);
        metricsTrackingService.trackTrainerRegistration();
    }

    @Timed(value = "gym.training.session.complete", description = "Time taken to complete training session")
    public void completeTrainingSession(long durationMinutes, long caloriesBurned) {
        System.out.println("Completing training session");
        long durationSeconds = durationMinutes * 60;
        metricsTrackingService.trackTrainingSession(durationSeconds, caloriesBurned);
    }

    public void directMetricManipulation() {
        gymMetrics.incrementActiveTrainees();
        int activeTrainees = gymMetrics.getActiveTraineesCount();
        int activeTrainers = gymMetrics.getActiveTrainersCount();
        long totalCalories = gymMetrics.getTotalCaloriesBurned();

        System.out.println("Active Trainees: " + activeTrainees);
        System.out.println("Active Trainers: " + activeTrainers);
        System.out.println("Total Calories: " + totalCalories);
    }

    @Timed(value = "gym.example.process", description = "Process execution time")
    public void processWithAutomaticTiming() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void handleTraineeInactive(String traineeName) {
        System.out.println("Trainee " + traineeName + " is now inactive");
        metricsTrackingService.trackTraineeInactive();
    }

    public void handleTrainerInactive(String trainerName) {
        System.out.println("Trainer " + trainerName + " is now inactive");
        metricsTrackingService.trackTrainerInactive();
    }

    public String getApplicationMetrics() {
        return metricsTrackingService.getMetricsSummary();
    }
}

