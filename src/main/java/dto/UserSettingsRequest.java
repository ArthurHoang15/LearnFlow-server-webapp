package dto;

import jakarta.validation.constraints.NotNull;

public class UserSettingsRequest {
    @NotNull(message = "soundEnabled không được để trống")
    private Boolean soundEnabled;

    @NotNull(message = "notificationsEnabled không được để trống")
    private Boolean notificationsEnabled;

    // Getters và Setters
    public Boolean getSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(Boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public Boolean getNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(Boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
}
