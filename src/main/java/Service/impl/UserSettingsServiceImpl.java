package Service.impl;
import Service.UserSettingsService;
import dto.UserSettingsRequest;
import entity.UserSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.UserSettingsRepository;

@Service
public class UserSettingsServiceImpl implements UserSettingsService {

    @Autowired
    private UserSettingsRepository repository;

    @Override
    public void updateUserSettings(Long userId, UserSettingsRequest request) {
        UserSettings settings = repository.findById(userId)
                .orElse(new UserSettings());

        settings.setUserId(userId);
        settings.setSoundEnabled(request.getSoundEnabled());
        settings.setNotificationsEnabled(request.getNotificationsEnabled());

        repository.save(settings);
    }
}
