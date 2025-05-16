package dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class LessonUpdateRequest {

    @NotNull(message = "chapter_id không được để trống")
    private Integer chapterId;

    private String description;

    // Getters
    public Integer getChapterId() {
        return chapterId;
    }

    public String getDescription() {
        return description;
    }

    // Setters
    public void setChapterId(Integer chapterId) {
        this.chapterId = chapterId;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
