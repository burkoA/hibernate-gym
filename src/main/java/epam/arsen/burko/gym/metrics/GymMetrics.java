package epam.arsen.burko.gym.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class GymMetrics {

    private final MeterRegistry meterRegistry;
    private final Counter traineeRegistrationCounter;
    private final Counter trainerRegistrationCounter;
    private final Counter trainingSessionCounter;
    private final Timer trainingSessionDuration;
    private final AtomicInteger activeTrainees;
    private final AtomicInteger activeTrainers;
    private final AtomicLong totalCaloriesBurned;

    public GymMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.traineeRegistrationCounter = Counter.builder("gym.trainee.registrations.total")
                .description("Total number of trainee registrations")
                .tag("component", "gym")
                .register(meterRegistry);

        this.trainerRegistrationCounter = Counter.builder("gym.trainer.registrations.total")
                .description("Total number of trainer registrations")
                .tag("component", "gym")
                .register(meterRegistry);

        this.trainingSessionCounter = Counter.builder("gym.training.sessions.total")
                .description("Total number of training sessions completed")
                .tag("component", "gym")
                .register(meterRegistry);

        this.trainingSessionDuration = Timer.builder("gym.training.session.duration")
                .description("Duration of training sessions in seconds")
                .tag("component", "gym")
                .register(meterRegistry);
        this.activeTrainees = new AtomicInteger(0);
        this.activeTrainers = new AtomicInteger(0);
        this.totalCaloriesBurned = new AtomicLong(0);
        Gauge.builder("gym.trainees.active", activeTrainees, AtomicInteger::get)
                .description("Current number of active trainees")
                .tag("component", "gym")
                .register(meterRegistry);

        Gauge.builder("gym.trainers.active", activeTrainers, AtomicInteger::get)
                .description("Current number of active trainers")
                .tag("component", "gym")
                .register(meterRegistry);

        Gauge.builder("gym.calories.burned.total", totalCaloriesBurned, AtomicLong::get)
                .description("Total calories burned by all trainees")
                .tag("component", "gym")
                .baseUnit("calories")
                .register(meterRegistry);
    }

    public void recordTraineeRegistration() {
        traineeRegistrationCounter.increment();
    }

    public void recordTrainerRegistration() {
        trainerRegistrationCounter.increment();
    }

    public void recordTrainingSession(long durationSeconds) {
        trainingSessionCounter.increment();
        trainingSessionDuration.record(durationSeconds, java.util.concurrent.TimeUnit.SECONDS);
    }

    public void incrementActiveTrainees() {
        activeTrainees.incrementAndGet();
    }

    public void decrementActiveTrainees() {
        activeTrainees.decrementAndGet();
    }

    public void incrementActiveTrainers() {
        activeTrainers.incrementAndGet();
    }

    public void decrementActiveTrainers() {
        activeTrainers.decrementAndGet();
    }

    public void addCaloriesBurned(long calories) {
        totalCaloriesBurned.addAndGet(calories);
    }

    public int getActiveTraineesCount() {
        return activeTrainees.get();
    }

    public int getActiveTrainersCount() {
        return activeTrainers.get();
    }

    public long getTotalCaloriesBurned() {
        return totalCaloriesBurned.get();
    }
}

