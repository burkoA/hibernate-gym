package epam.arsen.burko.gym.service;

import epam.arsen.burko.gym.entity.Trainee;
import epam.arsen.burko.gym.entity.Trainer;
import epam.arsen.burko.gym.repository.TraineeRepository;
import epam.arsen.burko.gym.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TraineeService {
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final IdentityGenerationService identityService;
    private final AuthService auth;

    @Transactional
    public Trainee createTrainee(String firstName, String lastName, Date dateOfBirth, String address) {
        Trainee trainee = new Trainee();
        trainee.setFirstName(firstName);
        trainee.setLastName(lastName);
        trainee.setUsername(identityService.generateUsername(firstName, lastName));
        trainee.setPassword(identityService.generatePassword());
        trainee.setIsActive(true);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);

        return traineeRepository.save(trainee);
    }

    @Transactional
    public void updateTrainers(String username, String password, Set<Long> trainerIds) {
        auth.validate(username, password);
        Trainee trainee = traineeRepository.findByUsername(username).orElseThrow();
        trainee.setTrainers(new HashSet<>(trainerRepository.findAllById(trainerIds)));
    }

    public Trainee get(String username, String password) {
        auth.validate(username, password);
        return traineeRepository.findByUsername(username).orElseThrow();
    }

    @Transactional
    public Trainee updateProfile(String username, String password, Trainee updated) {
        auth.validate(username, password);

        Trainee trainee = traineeRepository.findByUsername(username).orElseThrow();

        trainee.setFirstName(updated.getFirstName());
        trainee.setLastName(updated.getLastName());
        trainee.setDateOfBirth(updated.getDateOfBirth());
        trainee.setAddress(updated.getAddress());

        return trainee;
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        auth.validate(username, oldPassword);
        Trainee trainee = traineeRepository.findByUsername(username).orElseThrow();
        trainee.setPassword(newPassword);
    }

    @Transactional
    public void toggleStatus(String username, String password, boolean isActive) {
        auth.validate(username, password);

        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Trainee not found"));
        trainee.setIsActive(isActive);

        traineeRepository.save(trainee);
    }

    @Transactional
    public void deleteTrainee(String username, String password) {
        auth.validate(username, password);
        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Trainee not found"));
        traineeRepository.delete(trainee);
    }

    @Transactional
    public List<Trainer> getUnassigned(String username, String password) {
        auth.validate(username, password);

        return trainerRepository.findTrainersNotAssignedToTrainee(username);
    }
}
