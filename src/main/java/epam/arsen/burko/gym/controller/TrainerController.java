package epam.arsen.burko.gym.controller;

import epam.arsen.burko.gym.dto.RegistrationResponse;
import epam.arsen.burko.gym.dto.StatusUpdateRequest;
import epam.arsen.burko.gym.dto.TrainerDto;
import epam.arsen.burko.gym.dto.TrainerProfileResponse;
import epam.arsen.burko.gym.dto.TrainerRegistrationRequest;
import epam.arsen.burko.gym.dto.TrainerTrainingResponse;
import epam.arsen.burko.gym.dto.TrainerUpdateRequest;
import epam.arsen.burko.gym.service.TrainerService;
import epam.arsen.burko.gym.service.TrainingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Api(tags = "Trainers")
@RestController
@RequestMapping("/trainers")
@RequiredArgsConstructor
@Slf4j
public class TrainerController {

    private final TrainerService trainerService;
    private final TrainingService trainingService;

    @PostMapping("/register")
    @ApiOperation(value = "Register trainer")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 400, message = "Validation Failed"),
            @ApiResponse(code = 404, message = "Specialization Not Found")
    })
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse register(@Valid @RequestBody TrainerRegistrationRequest request) {
        log.info("Received registration request for trainer: {} {}", request.firstName(), request.lastName());
        TrainerDto trainer = trainerService.createTrainer(
                request.firstName(),
                request.lastName(),
                request.specializationId()
        );
        return new RegistrationResponse(trainer.username(), trainer.password());
    }

    @GetMapping("/{username}")
    @ApiOperation(value = "Get trainer profile")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Trainer Not Found")
    })
    public TrainerProfileResponse getProfile(@PathVariable String username) {
        log.info("Fetching profile for trainer: {}", username);
        return trainerService.getProfile(username);
    }

    @GetMapping("/{username}/trainings")
    @ApiOperation(value = "Get trainer trainings list")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Trainer Not Found")
    })
    public List<TrainerTrainingResponse> getTrainings(
            @PathVariable @ApiParam(required = true, value = "Username") String username,
            @RequestParam(required = false) @ApiParam(value = "Period from") LocalDate periodFrom,
            @RequestParam(required = false) @ApiParam(value = "Period to") LocalDate periodTo,
            @RequestParam(required = false) @ApiParam(value = "Trainee name") String traineeName
    ) {
        log.info("Fetching trainings for trainer: {}", username);
        return trainingService.getTrainerTrainings(username, periodFrom, periodTo, traineeName);
    }

    @PutMapping("/{username}")
    @ApiOperation(value = "Update trainer profile")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Validation Failed"),
            @ApiResponse(code = 404, message = "Trainer Not Found / Specialization Not Found")
    })
    public TrainerProfileResponse updateProfile(
            @PathVariable String username,
            @Valid @RequestBody TrainerUpdateRequest request
    ) {
        log.info("Updating profile for trainer: {}", username);
        return trainerService.updateProfile(username, request);
    }

    @PatchMapping("/{username}/status")
    @ApiOperation(value = "Activate or deactivate trainer profile")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Validation Failed"),
            @ApiResponse(code = 404, message = "Trainer Not Found")
    })
    @ResponseStatus(HttpStatus.OK)
    public void updateStatus(
            @PathVariable String username,
            @Valid @RequestBody StatusUpdateRequest request
    ) {
        log.info("Updating active status for trainer: {}", username);
        trainerService.toggleStatus(username, request.isActive());
    }

    @DeleteMapping("/{username}")
    @ApiOperation(value = "Delete trainer profile")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Trainer Not Found")
    })
    @ResponseStatus(HttpStatus.OK)
    public void deleteProfile(@PathVariable String username) {
        log.info("Deleting profile for trainer: {}", username);
        trainerService.deleteTrainer(username);
    }
}

