package service;

import DTO.UserProfileDTO;
import entity.UserProfile;
import exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import respository.UserProfileRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserProfileService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    public UserProfileDTO updateProfile(Long userId, UserProfileDTO userProfileDTO) {
        Optional<UserProfile> optionalUser = userProfileRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        UserProfile userProfile = optionalUser.get();
        userProfile.setFirstName(userProfileDTO.getFirstName());
        userProfile.setLastName(userProfileDTO.getLastName());
        userProfile.setDateOfBirth(userProfileDTO.getDateOfBirth());
        userProfile.setGender(userProfileDTO.getGender());
        userProfile.setPicture(userProfileDTO.getPicture());
        userProfile.setPublic(userProfileDTO.isPublic());
        userProfile.setUpdatedAt(LocalDateTime.now());

        UserProfile updatedUser = userProfileRepository.save(userProfile);

        // Chuyển đổi entity thành DTO để trả về
        UserProfileDTO responseDTO = new UserProfileDTO();
        responseDTO.setFirstName(updatedUser.getFirstName());
        responseDTO.setLastName(updatedUser.getLastName());
        responseDTO.setDateOfBirth(updatedUser.getDateOfBirth());
        responseDTO.setGender(updatedUser.getGender());
        responseDTO.setPicture(updatedUser.getPicture());
        responseDTO.setPublic(updatedUser.isPublic());

        return responseDTO;
    }
}
