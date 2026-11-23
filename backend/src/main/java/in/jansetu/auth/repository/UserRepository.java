package in.jansetu.auth.repository;

import in.jansetu.auth.entity.User;
import in.jansetu.security.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByMobile(String mobile);

    Optional<User> findByEmail(String email);  // emails are stored lower-cased

    boolean existsByMobile(String mobile);

    boolean existsByEmail(String email);

    boolean existsByRole(Role role);
}
