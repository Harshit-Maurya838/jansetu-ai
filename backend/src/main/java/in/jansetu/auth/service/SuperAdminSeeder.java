package in.jansetu.auth.service;

import in.jansetu.auth.entity.User;
import in.jansetu.auth.entity.UserStatus;
import in.jansetu.auth.repository.UserRepository;
import in.jansetu.security.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the first SUPER_ADMIN from SUPERADMIN_* env vars when none exists. Credentials are never hard-coded.
 * Outside the dev profile, a missing or weak SUPERADMIN_PASSWORD (or missing SUPERADMIN_MOBILE) fails startup.
 */
@Slf4j
@Component
public class SuperAdminSeeder implements ApplicationRunner {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;
    private final String name;
    private final String mobile;
    private final String email;
    private final String password;

    public SuperAdminSeeder(UserRepository users, PasswordEncoder passwordEncoder, Environment env,
                            @Value("${jansetu.super-admin.name:Super Admin}") String name,
                            @Value("${jansetu.super-admin.mobile:}") String mobile,
                            @Value("${jansetu.super-admin.email:}") String email,
                            @Value("${jansetu.super-admin.password:}") String password) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.env = env;
        this.name = name;
        this.mobile = mobile;
        this.email = email;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.existsByRole(Role.SUPER_ADMIN)) return;
        String problem = mobile.isBlank() || !mobile.matches("^(\\+91)?[6-9]\\d{9}$") ? "SUPERADMIN_MOBILE is missing or invalid"
                : password.isBlank() ? "SUPERADMIN_PASSWORD is not set"
                : !PasswordPolicy.isValid(password) ? "SUPERADMIN_PASSWORD does not meet the password policy (min 8 chars, letters and digits)"
                : null;
        if (problem != null) {
            if (env.matchesProfiles("dev")) {
                log.warn("No SUPER_ADMIN seeded: {}", problem);
                return;
            }
            throw new IllegalStateException("Cannot seed the first SUPER_ADMIN: " + problem);
        }
        var u = new User();
        u.setFullName(name);
        u.setMobile(Identifiers.mobile(mobile));
        u.setEmail(email.isBlank() ? null : Identifiers.email(email));
        u.setRole(Role.SUPER_ADMIN);
        u.setStatus(UserStatus.ACTIVE);
        u.setPreferredLanguage("en");
        u.setPasswordHash(passwordEncoder.encode(password));
        users.save(u);
        log.info("Seeded SUPER_ADMIN user {}", u.getId());
    }
}
