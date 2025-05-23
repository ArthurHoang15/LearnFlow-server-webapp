package Service;

import dto.UserSettingsRequest;

public interface UserSettingsService {
    void updateUserSettings(Long userId, UserSettingsRequest request);
}
