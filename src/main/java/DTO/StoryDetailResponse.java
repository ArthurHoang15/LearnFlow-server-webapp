package DTO;

public class StoryDetailResponse {
    private String contentUrl;
    private String audioUrl;

    public StoryDetailResponse(String contentUrl, String audioUrl) {
        this.contentUrl = contentUrl;
        this.audioUrl = audioUrl;
    }

    // Getters and Setters
    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }
}
