package controller;

import dto.UserDto;
import service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault; // Để đặt giá trị mặc định cho Pageable
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users") // Base path cho các API quản lý user của admin
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Yêu cầu quyền ADMIN cho tất cả API trong controller này
public class AdminUserController {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);
    private final AdminUserService adminUserService;

    /**
     * ST-71: [BE] Build API GET List of Users
     * API để admin lấy danh sách người dùng (có phân trang).
     * Ví dụ: /api/admin/users?page=0&size=10&sort=username,asc
     *
     * @param pageable Đối tượng Pageable (tự động được Spring inject từ request params).
     *                 @PageableDefault để đặt giá trị mặc định nếu client không gửi.
     * @return ResponseEntity chứa Page<UserDto>.
     */
    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        logger.info("Admin request to get all users. Page: {}, Size: {}, Sort: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<UserDto> users = adminUserService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * API để admin xem chi tiết một người dùng theo ID.
     *
     * @param userId ID của người dùng.
     * @return ResponseEntity chứa UserDto.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserByIdForAdmin(@PathVariable Long userId) {
        logger.info("Admin request to get user details for user ID: {}", userId);
        UserDto userDto = adminUserService.getUserByIdForAdmin(userId);
        return ResponseEntity.ok(userDto);
    }

    /**
     * ST-73: [BE] Build API for User's Account Enable
     * API để admin kích hoạt tài khoản người dùng.
     *
     * @param userId ID của người dùng cần kích hoạt.
     * @return ResponseEntity chứa UserDto của người dùng đã được cập nhật.
     */
    @PutMapping("/{userId}/enable")
    public ResponseEntity<UserDto> enableUserAccount(@PathVariable Long userId) {
        logger.info("Admin request to enable account for user ID: {}", userId);
        UserDto updatedUser = adminUserService.enableUserAccount(userId);
        logger.info("Account enabled successfully for user ID: {}", userId);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * ST-72: [BE] Build API for User's Account Disable
     * API để admin vô hiệu hóa tài khoản người dùng.
     *
     * @param userId ID của người dùng cần vô hiệu hóa.
     * @return ResponseEntity chứa UserDto của người dùng đã được cập nhật.
     */
    @PutMapping("/{userId}/disable")
    public ResponseEntity<UserDto> disableUserAccount(@PathVariable Long userId) {
        logger.info("Admin request to disable account for user ID: {}", userId);
        UserDto updatedUser = adminUserService.disableUserAccount(userId);
        logger.info("Account disabled successfully for user ID: {}", userId);
        return ResponseEntity.ok(updatedUser);
    }
}
