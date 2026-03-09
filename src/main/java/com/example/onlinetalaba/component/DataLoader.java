package com.example.onlinetalaba.component;

import com.example.onlinetalaba.entity.Role;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.enums.AppPermissions;
import com.example.onlinetalaba.enums.AppRoleName;
import com.example.onlinetalaba.repository.RoleRepository;
import com.example.onlinetalaba.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataLoader implements ApplicationRunner {

//    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Value("${spring.sql.init.mode}")
    String mode;

    @Override
    public void run(ApplicationArguments args) {
        if ("always".equals(mode)) {

            // SUPER_ADMIN role
            Role saveAdmin = roleRepository.findByAppRoleName(AppRoleName.SUPER_ADMIN)
                    .orElseGet(() -> {
                        Role admin = new Role();
                        admin.setAppRoleName(AppRoleName.SUPER_ADMIN);
                        admin.setAppPermissions(Set.of(AppPermissions.values()));
                        return roleRepository.save(admin);
                    });

            // admin user
            User existingAdmin = userRepository.findByEmail("admin@gmail.com");
            if (existingAdmin == null) {
                User authUserAdmin = new User();
                authUserAdmin.setEnabled(true);
                authUserAdmin.setEmail("admin@gmail.com");
                authUserAdmin.setUsername("admin123");
                authUserAdmin.setRoles(saveAdmin);
                authUserAdmin.setPassword(passwordEncoder.encode("admin321"));
                authUserAdmin.setFullName("Ibrohim Fazliddinov");
                authUserAdmin.setPhoneNumber("998932003316");
                userRepository.save(authUserAdmin);
            }

            // USER role
            roleRepository.findByAppRoleName(AppRoleName.USER)
                    .orElseGet(() -> {
                        Role userRole = new Role();
                        userRole.setAppRoleName(AppRoleName.USER);
                        return roleRepository.save(userRole);
                    });
        }
    }
}
