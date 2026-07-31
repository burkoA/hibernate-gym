package epam.arsen.burko.gym.controller;

import epam.arsen.burko.gym.dto.ChangeLoginRequest;
import epam.arsen.burko.gym.dto.LoginResponse;
import epam.arsen.burko.gym.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api(tags = "Authentication")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    @ApiOperation(value = "Login and get JWT token")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK - Returns JWT token"),
            @ApiResponse(code = 400, message = "Validation Failed"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "User Not Found"),
            @ApiResponse(code = 423, message = "User Locked - Too many failed attempts")
    })
    public ResponseEntity<LoginResponse> login(
            @RequestParam @ApiParam(required = true, value = "Username") @NotBlank(message = "Username is required") String username,
            @RequestParam @ApiParam(required = true, value = "Password") @NotBlank(message = "Password is required") String password
    ) {
        log.info("Login attempt for user: {}", username);
        LoginResponse response = authService.authenticate(username, password);
        log.info("Login successful for user: {}", username);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @ApiOperation(value = "Logout - invalidates current session")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "Unauthorized")
    })
    public ResponseEntity<Void> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            log.info("Logout request for user: {}", authentication.getName());
            SecurityContextHolder.clearContext();
            log.info("User logged out successfully: {}", authentication.getName());
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/login")
    @ApiOperation(value = "Change Login Password")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Validation Failed"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "User Not Found"),
            @ApiResponse(code = 423, message = "User Locked - Too many failed attempts")
    })
    public ResponseEntity<Void> changeLogin(@Valid @RequestBody ChangeLoginRequest request) {
        log.info("Password change request for user: {}", request.username());
        authService.changePassword(request.username(), request.oldPassword(), request.newPassword());
        log.info("Password changed successfully for user: {}", request.username());
        return ResponseEntity.ok().build();
    }
}

