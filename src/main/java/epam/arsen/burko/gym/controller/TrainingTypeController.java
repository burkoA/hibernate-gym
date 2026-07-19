package epam.arsen.burko.gym.controller;

import epam.arsen.burko.gym.dto.TrainingTypeResponse;
import epam.arsen.burko.gym.service.TrainingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "Training Types")
@RestController
@RequestMapping("/training-types")
@RequiredArgsConstructor
@Slf4j
public class TrainingTypeController {

    private final TrainingService trainingService;

    @GetMapping
    @ApiOperation(value = "Get training types")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK")
    })
    public List<TrainingTypeResponse> getTrainingTypes() {
        log.info("Fetching training types");
        return trainingService.getTrainingTypes();
    }
}

