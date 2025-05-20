package controller;

import dto.GoalRequest;
import dto.ProgressResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.GoalService;

@RestController
@RequestMapping("/api/learning")
public class GoalController {
    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping("/goals")
    public ResponseEntity<String> setGoal(@Valid @RequestBody GoalRequest goalRequest,
                                          @RequestParam Integer userId) {
        String message = goalService.setGoal(goalRequest, userId);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/goals/progress")
    public ResponseEntity<ProgressResponse> getProgress(@RequestParam Integer userId) {
        ProgressResponse progress = goalService.getProgress(userId);
        return ResponseEntity.ok(progress);
    }
}
