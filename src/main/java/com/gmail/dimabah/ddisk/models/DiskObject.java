package com.gmail.dimabah.ddisk.models;

import com.gmail.dimabah.ddisk.dto.DiskObjectBinnedDTO;
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

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date binnedDate;

    @OneToMany(mappedBy = "diskObject", cascade = CascadeType.ALL)
    private List<UserObjectPermission> permissions = new ArrayList<>();


    public DiskObject(String objName) {
        this.name = objName;
        createDate = new Date();
    }
    public DiskObject (DiskObject diskObject){
        this.name = diskObject.name;
        this.address = diskObject.address;
//        this.live = diskObject.live;
//        this.openToAll = diskObject.openToAll;
        this.createDate = diskObject.createDate;
//        this.permissions = diskObject.permissions;
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
    public DiskObjectBinnedDTO toBinnedDTO(){
        DiskObjectBinnedDTO result = new DiskObjectBinnedDTO();
        result.setName(name);
        result.setOwner(permissions.get(0).getUser().getEmail());
        result.setAddress(address);
        result.setBinnedDate(binnedDate);
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
