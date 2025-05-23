package dto;

public class ProgressResponse {
    private int timeSpent;
    private int lessonCount;

    // Constructor
    public ProgressResponse(int timeSpent, int lessonCount) {
        this.timeSpent = timeSpent;
        this.lessonCount = lessonCount;
    }

    // Getters và Setters
    public int getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(int timeSpent) {
        this.timeSpent = timeSpent;
    }

    public int getLessonCount() {
        return lessonCount;
    }

    public void setLessonCount(int lessonCount) {
        this.lessonCount = lessonCount;
    }
}
