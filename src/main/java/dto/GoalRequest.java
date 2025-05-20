package dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class GoalRequest {
    @NotBlank(message = "goalType không được để trống")
    private String goalType;

    @Min(value = 0, message = "target phải lớn hơn hoặc bằng 0")
    private int target;

    // Getters và Setters
    public String getGoalType() {
        return goalType;
    }

    public void setGoalType(String goalType) {
        this.goalType = goalType;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }
}
