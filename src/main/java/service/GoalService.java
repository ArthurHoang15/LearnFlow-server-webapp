package service;

import dto.GoalRequest;
import dto.ProgressResponse;
import entity.DailyProgress;
import entity.Goal;
import exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import repository.DailyProgressRepository;
import repository.GoalRepository;

@Service
public class GoalService {
    private final GoalRepository goalRepository;
    private final DailyProgressRepository dailyProgressRepository;

    public GoalService(GoalRepository goalRepository, DailyProgressRepository dailyProgressRepository) {
        this.goalRepository = goalRepository;
        this.dailyProgressRepository = dailyProgressRepository;
    }

    public String setGoal(GoalRequest goalRequest, Integer userId) {
        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setGoalType(goalRequest.getGoalType());
        goal.setTarget(goalRequest.getTarget());

        goalRepository.save(goal);
        return "Mục tiêu đã được đặt thành công!";
    }

    public ProgressResponse getProgress(Integer userId) {
        DailyProgress progress = dailyProgressRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tiến độ cho userId: " + userId));
        return new ProgressResponse(progress.getTimeSpent(), progress.getLessonCount());
    }
}
