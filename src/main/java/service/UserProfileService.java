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
        userProfile.setFullname(userProfileDTO.getFullname());
        userProfile.setDateOfBirth(userProfileDTO.getDateOfBirth());
        userProfile.setEmail(userProfileDTO.getEmail());
        userProfile.setPhoneNumber(userProfileDTO.getPhoneNumber());
        userProfile.setAddress(userProfileDTO.getAddress());
        userProfile.setPicture(userProfileDTO.getPicture());
        userProfile.setUpdatedAt(LocalDateTime.now());

        UserProfile updatedUser = userProfileRepository.save(userProfile);

        // Chuyển đổi entity thành DTO để trả về
        UserProfileDTO responseDTO = new UserProfileDTO();
        responseDTO.setFullname(updatedUser.getFullname());
        responseDTO.setDateOfBirth(updatedUser.getDateOfBirth());
        responseDTO.setEmail(updatedUser.getEmail());
        responseDTO.setPhoneNumber(updatedUser.getPhoneNumber());
        responseDTO.setAddress(updatedUser.getAddress());
        responseDTO.setPicture(updatedUser.getPicture());

        return responseDTO;
    }
}
