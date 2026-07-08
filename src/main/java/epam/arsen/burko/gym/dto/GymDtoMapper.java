package epam.arsen.burko.gym.dto;

import epam.arsen.burko.gym.entity.Trainee;
import epam.arsen.burko.gym.entity.Trainer;
import epam.arsen.burko.gym.entity.Training;

import java.util.List;

public final class GymDtoMapper {

    private GymDtoMapper() {
    }

    public static TraineeDto toDto(Trainee trainee) {
        return new TraineeDto(
                trainee.getId(),
                trainee.getFirstName(),
                trainee.getLastName(),
                trainee.getUsername(),
                trainee.getPassword(),
                trainee.getIsActive(),
                trainee.getDateOfBirth(),
                trainee.getAddress()
        );
    }

    public static TraineeProfileResponse toProfileResponse(Trainee trainee) {
        List<TrainerSummaryDto> trainerSummaries = trainee.getTrainers().stream()
                .map(trainer -> new TrainerSummaryDto(
                        trainer.getUsername(),
                        trainer.getFirstName(),
                        trainer.getLastName(),
                        trainer.getSpecialization() != null ? trainer.getSpecialization().getId() : null,
                        trainer.getSpecialization() != null ? trainer.getSpecialization().getTrainingTypeName() : null
                ))
                .toList();

        return new TraineeProfileResponse(
                trainee.getFirstName(),
                trainee.getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                trainee.getIsActive(),
                trainerSummaries
        );
    }

    public static TraineeUpdateResponse toUpdateResponse(Trainee trainee) {
        List<TrainerSummaryDto> trainerSummaries = trainee.getTrainers().stream()
                .map(trainer -> new TrainerSummaryDto(
                        trainer.getUsername(),
                        trainer.getFirstName(),
                        trainer.getLastName(),
                        trainer.getSpecialization() != null ? trainer.getSpecialization().getId() : null,
                        trainer.getSpecialization() != null ? trainer.getSpecialization().getTrainingTypeName() : null
                ))
                .toList();

        return new TraineeUpdateResponse(
                trainee.getUsername(),
                trainee.getFirstName(),
                trainee.getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                trainee.getIsActive(),
                trainerSummaries
        );
    }

    public static TrainerProfileResponse toProfileResponse(Trainer trainer) {
        List<TraineeSummaryDto> traineeSummaries = trainer.getTrainees().stream()
                .map(trainee -> new TraineeSummaryDto(
                        trainee.getUsername(),
                        trainee.getFirstName(),
                        trainee.getLastName()
                ))
                .toList();

        return new TrainerProfileResponse(
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.getSpecialization() != null ? trainer.getSpecialization().getId() : null,
                trainer.getSpecialization() != null ? trainer.getSpecialization().getTrainingTypeName() : null,
                trainer.getIsActive(),
                traineeSummaries
        );
    }

    public static TrainerDto toDto(Trainer trainer) {
        Long specializationId = trainer.getSpecialization() != null ? trainer.getSpecialization().getId() : null;
        String specializationName = trainer.getSpecialization() != null ? trainer.getSpecialization().getTrainingTypeName() : null;

        return new TrainerDto(
                trainer.getId(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.getUsername(),
                trainer.getPassword(),
                trainer.getIsActive(),
                specializationId,
                specializationName
        );
    }

    public static TrainingDto toDto(Training training) {
        return new TrainingDto(
                training.getId(),
                training.getTrainee().getUsername(),
                training.getTrainer().getUsername(),
                training.getTrainingName(),
                training.getTrainingType().getId(),
                training.getTrainingType().getTrainingTypeName(),
                training.getTrainingDate(),
                training.getTrainingDuration()
        );
    }
}

