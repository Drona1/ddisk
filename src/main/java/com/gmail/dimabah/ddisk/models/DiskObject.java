package com.gmail.dimabah.ddisk.models;

import com.gmail.dimabah.ddisk.dto.DiskObjectDTO;
import com.gmail.dimabah.ddisk.models.enums.AccessRights;
import jakarta.annotation.Nullable;
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
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    private Boolean live = true;
    private Boolean openToAll = false;

    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createDate;

    @OneToMany(mappedBy = "diskObject", cascade = CascadeType.ALL)
    private List<UserObjectPermission> permissions = new ArrayList<>();


    public DiskObject(DiskUser user, String objName) {
        this.name = objName;
        createDate = new Date();
//        address = generateAddress();
//
//        UserObjectPermission permission = new UserObjectPermission(AccessRights.MASTER);
//        addPermission(permission);
//        user.addPermission(permission);
    }
    public DiskObject (DiskObject diskObject){
        this.name = diskObject.name;
        this.address = diskObject.address;
        this.live = diskObject.live;
        this.openToAll = diskObject.openToAll;
        this.createDate = diskObject.createDate;
        this.permissions = diskObject.permissions;
    }

    public void addPermission(UserObjectPermission userObjectPermission) {
        if ( !permissions.contains(userObjectPermission)) {
            permissions.add(userObjectPermission);
            userObjectPermission.setDiskObject(this);
        }
    }
    public DiskObjectDTO toDTO(){
        DiskObjectDTO result = new DiskObjectDTO();
        result.setName(name);
        result.setOwner(permissions.get(0).getUser().getEmail());
        result.setAddress(address);
        result.setCreateDate(createDate);
        return result;
    }
}
