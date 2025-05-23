package repository;

import model.User.ERole;
import model.User.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> { // ID của Role là Integer
    Optional<Role> findByName(ERole name);
}
