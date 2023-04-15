package com.gmail.dimabah.ddisk.models;

import com.gmail.dimabah.ddisk.models.enums.AccessRights;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class UserObjectPermission {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private DiskUser user;

    @ManyToOne
    @JoinColumn(name = "object_id")
    private DiskObject diskObject;

    @Enumerated(EnumType.STRING)
    private AccessRights accessRights;

    public UserObjectPermission(AccessRights permission) {
        this.accessRights = permission;
    }

    @Override
    public String toString() {
        return "UserObjectPermission{" +
                "id=" + id +
                ", user=" + user.getId() +
                ", diskObject=" + diskObject.getId() +
                ", permission=" + accessRights +
                '}';
    }
}
