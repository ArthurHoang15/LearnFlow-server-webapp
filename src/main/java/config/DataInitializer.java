package config; // Hoặc một package phù hợp

import model.User.ERole;
import model.User.Role;
import repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        // Bạn cũng có thể tạo tài khoản admin mẫu ở đây nếu cần
        // initializeAdminUser();
    }

    private void initializeRoles() {
        if (roleRepository.findByName(ERole.ROLE_USER).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_USER));
            System.out.println("ROLE_USER initialized.");
        }
        if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_ADMIN));
            System.out.println("ROLE_ADMIN initialized.");
        }
        // Thêm các role khác nếu cần
    }

    // private void initializeAdminUser() {
    //     // Logic tạo user admin nếu chưa có
    // }
}
