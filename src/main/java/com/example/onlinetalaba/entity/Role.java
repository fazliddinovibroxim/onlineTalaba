package com.example.onlinetalaba.entity;

import com.example.onlinetalaba.enums.AppPermissions;
import com.example.onlinetalaba.enums.AppRoleName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(
        name = "role",
        uniqueConstraints = @UniqueConstraint(columnNames = "app_role_name")
)
public class Role extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppRoleName appRoleName;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<AppPermissions> appPermissions;

}
