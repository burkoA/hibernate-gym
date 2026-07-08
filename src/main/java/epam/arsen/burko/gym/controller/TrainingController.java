package epam.arsen.burko.gym.controller;

import epam.arsen.burko.gym.dto.TrainingCreateRequest;
import epam.arsen.burko.gym.service.TrainingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Trainings")
@RestController
@RequestMapping("/trainings")
@RequiredArgsConstructor
@Slf4j
public class TrainingController {

    private final TrainingService trainingService;

    @PostMapping
    @ApiOperation(value = "Add training")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Validation Failed"),
            @ApiResponse(code = 404, message = "Trainee Not Found / Trainer Not Found / Specialization Not Found")
    })
    public ResponseEntity<Void> addTraining(@Valid @RequestBody TrainingCreateRequest request) {
        log.info("Received training creation request for trainee: {} and trainer: {}",
                request.traineeUsername(), request.trainerUsername());
        trainingService.addTraining(request);
        return ResponseEntity.ok().build();
    }
}

