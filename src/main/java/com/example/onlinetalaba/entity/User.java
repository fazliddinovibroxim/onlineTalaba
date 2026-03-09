package com.example.onlinetalaba.entity;


import com.example.onlinetalaba.enums.AuthProvider;
import com.example.onlinetalaba.enums.UserGender;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "users")
public class User extends BaseEntity implements UserDetails {

    private String fullName;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserGender gender;

    @Column()
    private String emailCode;
    /////
    private String phoneNumber;
    private String address;
    @Column
    private String resetToken;

    @Column
    private LocalDate tokenExpiryDate;

    @ManyToOne(fetch = FetchType.EAGER)
    private Role roles;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AuthProvider provider = AuthProvider.LOCAL;

    @Builder.Default
    private Boolean isDeleted = false;

    @Builder.Default
    private boolean isAccountNonExpired = true;

    @Builder.Default
    private boolean isAccountNonLocked = true;

    @Builder.Default
    private boolean isCredentialsNonExpired = true;

    @Builder.Default
    private boolean isEnabled = false;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // ROLE
        if (roles != null) {
            authorities.add(new SimpleGrantedAuthority(
                    "ROLE_" + roles.getAppRoleName().name()
//                    roles.getAppRoleName().name()
            ));
        }

        // PERMISSIONS
        if (roles != null && roles.getAppPermissions() != null) {
            authorities.addAll(
                    roles.getAppPermissions().stream()
                            .map(p -> new SimpleGrantedAuthority(p.name()))
                            .toList()
            );
        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return isAccountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isAccountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isCredentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}
