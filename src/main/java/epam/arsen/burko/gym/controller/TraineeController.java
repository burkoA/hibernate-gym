package epam.arsen.burko.gym.controller;

import epam.arsen.burko.gym.dto.RegistrationResponse;
import epam.arsen.burko.gym.dto.StatusUpdateRequest;
import epam.arsen.burko.gym.dto.TraineeDto;
import epam.arsen.burko.gym.dto.TraineeProfileResponse;
import epam.arsen.burko.gym.dto.TraineeRegistrationRequest;
import epam.arsen.burko.gym.dto.TraineeTrainingResponse;
import epam.arsen.burko.gym.dto.TraineeTrainerListUpdateRequest;
import epam.arsen.burko.gym.dto.TraineeUpdateRequest;
import epam.arsen.burko.gym.dto.TraineeUpdateResponse;
import epam.arsen.burko.gym.dto.TrainerSummaryDto;
import epam.arsen.burko.gym.service.TraineeService;
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

@Api(tags = "Trainees")
@RestController
@RequestMapping("/trainees")
@RequiredArgsConstructor
@Slf4j
public class TraineeController {

    private final TraineeService traineeService;
    private final TrainingService trainingService;

    @PostMapping("/register")
    @ApiOperation(value = "Register trainee")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 400, message = "Validation Failed")
    })
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse register(@Valid @RequestBody TraineeRegistrationRequest request) {
        log.info("Received registration request for: {} {}", request.firstName(), request.lastName());
        TraineeDto trainee = traineeService.createTrainee(
                request.firstName(),
                request.lastName(),
                request.dateOfBirth(),
                request.address()
        );
        return new RegistrationResponse(trainee.username(), trainee.password());
    }

    @GetMapping("/{username}")
    @ApiOperation(value = "Get trainee profile")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Trainee Not Found")
    })
    public TraineeProfileResponse getProfile(@PathVariable String username) {
        log.info("Fetching profile for trainee: {}", username);
        return traineeService.getProfile(username);
    }

    @GetMapping("/{username}/unassigned-trainers")
    @ApiOperation(value = "Get unassigned active trainers")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Trainee Not Found")
    })
    public List<TrainerSummaryDto> getUnassignedActiveTrainers(@PathVariable String username) {
        log.info("Fetching unassigned active trainers for trainee: {}", username);
        return traineeService.getUnassignedActiveTrainers(username);
    }

    @GetMapping("/{username}/trainings")
    @ApiOperation(value = "Get trainee trainings list")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Trainee Not Found")
    })
    public List<TraineeTrainingResponse> getTrainings(
            @PathVariable @ApiParam(required = true, value = "Username") String username,
            @RequestParam(required = false) @ApiParam(value = "Period from") LocalDate periodFrom,
            @RequestParam(required = false) @ApiParam(value = "Period to") LocalDate periodTo,
            @RequestParam(required = false) @ApiParam(value = "Trainer name") String trainerName,
            @RequestParam(required = false) @ApiParam(value = "Training type") String trainingType
    ) {
        log.info("Fetching trainings for trainee: {}", username);
        return trainingService.getTraineeTrainings(username, periodFrom, periodTo, trainerName, trainingType);
    }

    @PutMapping("/{username}/trainers")
    @ApiOperation(value = "Update trainee trainer list")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Validation Failed"),
            @ApiResponse(code = 404, message = "Trainee Not Found / Trainer Not Found")
    })
    public List<TrainerSummaryDto> updateTrainerList(
            @PathVariable String username,
            @Valid @RequestBody TraineeTrainerListUpdateRequest request
    ) {
        log.info("Updating trainer list for trainee: {}", username);
        return traineeService.updateTrainers(username, request);
    }

    @PutMapping("/{username}")
    @ApiOperation(value = "Update trainee profile")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Validation Failed"),
            @ApiResponse(code = 404, message = "Trainee Not Found")
    })
    public TraineeUpdateResponse updateProfile(
            @PathVariable String username,
            @Valid @RequestBody TraineeUpdateRequest request
    ) {
        log.info("Updating profile for trainee: {}", username);
        return traineeService.updateProfile(username, request);
    }

    @PatchMapping("/{username}/status")
    @ApiOperation(value = "Activate or deactivate trainee profile")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Validation Failed"),
            @ApiResponse(code = 404, message = "Trainee Not Found")
    })
    @ResponseStatus(HttpStatus.OK)
    public void updateStatus(
            @PathVariable String username,
            @Valid @RequestBody StatusUpdateRequest request
    ) {
        log.info("Updating active status for trainee: {}", username);
        traineeService.toggleStatus(username, request.isActive());
    }

    @DeleteMapping("/{username}")
    @ApiOperation(value = "Delete trainee profile")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Trainee Not Found")
    })
    @ResponseStatus(HttpStatus.OK)
    public void deleteProfile(@PathVariable String username) {
        log.info("Deleting profile for trainee: {}", username);
        traineeService.deleteTrainee(username);
    }
}

