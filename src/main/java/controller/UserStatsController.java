package controller;

import dto.UserStatsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import service.UserStatsService;

@RestController
@RequestMapping("/api/admin/users")
public class UserStatsController {

    private final UserStatsService userStatsService;

    public UserStatsController(UserStatsService userStatsService) {
        this.userStatsService = userStatsService;
    }

    @GetMapping("/stats")
    public ResponseEntity<UserStatsResponse> getUserStats(@RequestParam("dateRange") String dateRange) {
        UserStatsResponse response = userStatsService.getUserStats(dateRange);
        return ResponseEntity.ok(response);
    }
}
