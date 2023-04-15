package com.gmail.dimabah.ddisk.models;


import com.gmail.dimabah.ddisk.models.enums.AccessRights;
import com.gmail.dimabah.ddisk.models.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class DiskUser {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    private String pass;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @OneToMany(mappedBy = "user")
    private List<UserObjectPermission> permissions = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "main_folder_id")
    private DiskFolder mainFolder;

    @OneToOne
    @JoinColumn(name = "bin_id")
    private DiskBin bin;
    public DiskUser(String email, String pass, UserRole role) {
        this.email = email;
        this.pass = pass;
        this.role = role;
    }
    public void addPermission(UserObjectPermission userObjectPermission) {
        if ( !permissions.contains(userObjectPermission)) {
            permissions.add(userObjectPermission);
            userObjectPermission.setUser(this);
        }
    }
}
