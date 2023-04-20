package com.gmail.dimabah.ddisk.models;

import com.gmail.dimabah.ddisk.dto.DiskObjectDTO;
import com.gmail.dimabah.ddisk.models.enums.AccessRights;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Data
@NoArgsConstructor
public class DiskObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false)
    private String address;

    private Boolean live = true;

    private AccessRights openToAll;

    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createDate;

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date binnedDate;

    @OneToMany(mappedBy = "diskObject", cascade = CascadeType.ALL)
    private List<UserObjectPermission> permissions = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "shared_object",
            joinColumns = @JoinColumn(name = "object_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<DiskUser> sharedToUsers;

    public DiskObject(String objName) {
        this.name = objName;
        originalName = objName;
        createDate = new Date();
    }

    public DiskObject(DiskObject diskObject) {
        this.name = diskObject.name;
        this.originalName = diskObject.originalName;
        this.address = diskObject.address;
        this.createDate = diskObject.createDate;
    }

    public void addPermission(UserObjectPermission userObjectPermission) {
        if (!permissions.contains(userObjectPermission)) {
            permissions.add(userObjectPermission);
            userObjectPermission.setDiskObject(this);
        }
    }

    public void addUserToShared(DiskUser user) {
        if (!sharedToUsers.contains(user) &&
                !permissions.get(0).getUser().equals(user)) {
            sharedToUsers.add(user);
            user.getSharedObjects().add(this);
        }
    }

    public DiskObjectDTO toDTO() {
        DiskObjectDTO result = new DiskObjectDTO();
        result.setName(name);
        result.setOwner(permissions.get(0).getUser().getEmail());
        result.setAddress(address);
        result.setCreateDate(createDate);
        result.setOpenToAll(openToAll != null ? openToAll.toString() : null);

        Map<String, String> map = new HashMap<>();
        for (int i = 1; i < permissions.size(); i++) {
            var permission = permissions.get(i);
            map.put(permission.getUser().getEmail(), permission.getAccessRights().toString());
        }
        result.setPermissions(map);

        return result;
    }

    @Override
    public String toString() {
        return "DiskObject{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", live=" + live +
                ", openToAll=" + openToAll +
                ", createDate=" + createDate +
                ", permissions=" + permissions +
                '}';
    }
}
