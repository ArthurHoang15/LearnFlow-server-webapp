package dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LessonCreateRequest {

    @NotNull(message = "chapter_id không được để trống")
    private Integer chapterId;

    private String description;

    // Getter và Setter
    public Integer getChapterId() {
        return chapterId;
    }

    public void setChapterId(Integer chapterId) {
        this.chapterId = chapterId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
