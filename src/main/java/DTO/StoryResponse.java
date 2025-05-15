package DTO;

public class StoryResponse {
    private String title;
    private String coverImage;

    public StoryResponse(String title, String coverImage) {
        this.title = title;
        this.coverImage = coverImage;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }
}
