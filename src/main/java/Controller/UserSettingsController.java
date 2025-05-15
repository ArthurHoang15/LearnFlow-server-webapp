package Controller;

import Service.UserSettingsService;
import dto.UserSettingsRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/settings")
public class UserSettingsController {

    @Autowired
    private UserSettingsService service;

    @PutMapping
    public ResponseEntity<String> updateSettings(
            @RequestHeader("User-Id") Long userId,
            @Valid @RequestBody UserSettingsRequest request) {
        service.updateUserSettings(userId, request);
        return ResponseEntity.ok("Cập nhật cài đặt thành công");
    }
}
