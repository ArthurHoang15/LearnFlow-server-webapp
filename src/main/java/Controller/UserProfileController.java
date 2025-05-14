package Controller;

import DTO.UserProfileDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.UserProfileService;

@RestController
@RequestMapping("/api/user")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDTO> updateProfile(@RequestBody UserProfileDTO userProfileDTO) {
        // Giả sử userId được lấy từ token hoặc session (hardcode tạm thời)
        Long userId = 1L; // Thay bằng logic lấy userId thực tế (ví dụ: từ SecurityContext)
        UserProfileDTO updatedProfile = userProfileService.updateProfile(userId, userProfileDTO);
        return ResponseEntity.ok(updatedProfile);
    }
}
