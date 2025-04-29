package controller;

import dto.AuthDto;
import service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthDto.LoginResponse> login(@Valid @RequestBody AuthDto.LoginRequest loginRequest) {
        AuthDto.LoginResponse loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthDto.LoginResponse> register(@Valid @RequestBody AuthDto.RegisterRequest registerRequest) {
        AuthDto.LoginResponse loginResponse = authService.register(registerRequest);
        return new ResponseEntity<>(loginResponse, HttpStatus.CREATED);
    }

    @GetMapping("/oauth2/redirect")
    public ResponseEntity<String> oauthRedirectHandler(@RequestParam String token) {
        // Endpoint để xử lý redirect từ OAuth2 login
        // Client sẽ lấy token từ URL và lưu vào local storage
        return ResponseEntity.ok("Authentication successful. Token: " + token);
    }
}
