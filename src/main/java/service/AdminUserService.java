package service;

import dto.UserDto;
import exception.Login.ResourceNotFoundException;
import model.User.User;
import repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserService.class);
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService; // Để revoke tokens khi disable

    /**
     * ST-71: Lấy danh sách tất cả người dùng với phân trang.
     *
     * @param pageable Đối tượng Pageable cho phân trang và sắp xếp.
     * @return Page<UserDto> chứa danh sách người dùng.
     */
    public Page<UserDto> getAllUsers(Pageable pageable) {
        logger.info("Admin: Fetching all users with pagination: {}", pageable);
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(UserDto::fromEntity); // Convert User sang UserDto
    }

    /**
     * Lấy thông tin chi tiết của một người dùng theo ID (cho Admin).
     *
     * @param userId ID của người dùng.
     * @return UserDto chứa thông tin người dùng.
     * @throws ResourceNotFoundException nếu không tìm thấy người dùng.
     */
    public UserDto getUserByIdForAdmin(Long userId) {
        logger.info("Admin: Fetching user details for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Admin: User not found with ID: {}.", userId);
                    return new ResourceNotFoundException("User", "id", String.valueOf(userId));
                });
        return UserDto.fromEntity(user);
    }

    /**
     * ST-73: Kích hoạt tài khoản người dùng.
     *
     * @param userId ID của người dùng cần kích hoạt.
     * @return UserDto của người dùng đã được cập nhật.
     * @throws ResourceNotFoundException nếu không tìm thấy người dùng.
     * @throws IllegalStateException nếu tài khoản đã được kích hoạt từ trước.
     */
    @Transactional
    public UserDto enableUserAccount(Long userId) {
        logger.info("Admin: Enabling account for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Admin: User not found with ID: {} for enabling account.", userId);
                    return new ResourceNotFoundException("User", "id", String.valueOf(userId));
                });

        if (user.isEnabled()) {
            logger.info("Admin: User account ID: {} is already enabled. No action taken.", userId);
            throw new IllegalStateException("Tài khoản người dùng ID: " + userId + " đã được kích hoạt từ trước.");
        }

        user.setEnabled(true);
        // userRepository.save(user); // Không cần thiết nếu @Transactional và user là managed entity
        // nhưng để an toàn và rõ ràng, có thể giữ.
        // Vì phương thức này của Service có @Transactional, nên không bắt buộc.
        logger.info("Admin: User account ID: {} enabled successfully.", userId);
        return UserDto.fromEntity(user); // Trả về user đã được cập nhật (Hibernate sẽ flush thay đổi)
    }

    /**
     * ST-72: Vô hiệu hóa tài khoản người dùng.
     * Cũng sẽ revoke tất cả refresh token của người dùng đó.
     *
     * @param userId ID của người dùng cần vô hiệu hóa.
     * @return UserDto của người dùng đã được cập nhật.
     * @throws ResourceNotFoundException nếu không tìm thấy người dùng.
     * @throws IllegalStateException nếu tài khoản đã bị vô hiệu hóa từ trước.
     */
    @Transactional
    public UserDto disableUserAccount(Long userId) {
        logger.info("Admin: Disabling account for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Admin: User not found with ID: {} for disabling account.", userId);
                    return new ResourceNotFoundException("User", "id", String.valueOf(userId));
                });

        if (!user.isEnabled()) {
            logger.info("Admin: User account ID: {} is already disabled. No action taken.", userId);
            throw new IllegalStateException("Tài khoản người dùng ID: " + userId + " đã bị vô hiệu hóa từ trước.");
        }

        user.setEnabled(false);
        // userRepository.save(user); // Không cần thiết nếu @Transactional

        // Vô hiệu hóa tất cả refresh token của người dùng
        // Đảm bảo RefreshTokenService có phương thức này và nó hoạt động đúng
        refreshTokenService.revokeAllTokensForUser(userId);
        logger.info("Admin: All refresh tokens revoked for user ID: {} due to account disable.", userId);

        logger.info("Admin: User account ID: {} disabled successfully.", userId);
        return UserDto.fromEntity(user); // Trả về user đã được cập nhật
    }
}
