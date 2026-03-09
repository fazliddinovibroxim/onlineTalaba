package com.example.onlinetalaba.component;

import com.example.onlinetalaba.entity.Role;
import com.example.onlinetalaba.enums.AppPermissions;
import com.example.onlinetalaba.enums.AppRoleName;
import com.example.onlinetalaba.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Set;
@Component
@RequiredArgsConstructor
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Value("${spring.sql.init.mode}")
    private String mode;

    @Override
    public void run(String... args) {
        if ("always".equals(mode)) {
            createRole(AppRoleName.SUPER_ADMIN, Set.of(
                    AppPermissions.ADD,
                    AppPermissions.EDIT,
                    AppPermissions.DELETE,
                    AppPermissions.LIST,
                    AppPermissions.GET,
                    AppPermissions.DELETE_ALL
            ));

            createRole(AppRoleName.ADMIN, Set.of(
                    AppPermissions.ADD,
                    AppPermissions.EDIT,
                    AppPermissions.DELETE,
                    AppPermissions.LIST,
                    AppPermissions.GET
            ));

            createRole(AppRoleName.TEACHER, Set.of(
                    AppPermissions.ADD,
                    AppPermissions.EDIT,
                    AppPermissions.DELETE,
                    AppPermissions.LIST,
                    AppPermissions.GET
            ));

            createRole(AppRoleName.STUDENT, Set.of(
                    AppPermissions.ADD,
                    AppPermissions.EDIT,
                    AppPermissions.LIST,
                    AppPermissions.GET
            ));

            createRole(AppRoleName.USER, Set.of(
                    AppPermissions.ADD,
                    AppPermissions.LIST,
                    AppPermissions.GET
            ));
        }
    }
    private void createRole(AppRoleName roleName, Set<AppPermissions> permissions) {
        roleRepository.findByAppRoleName(roleName)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setAppRoleName(roleName);
                    role.setAppPermissions(permissions);
                    return roleRepository.save(role);
                });
    }
}
