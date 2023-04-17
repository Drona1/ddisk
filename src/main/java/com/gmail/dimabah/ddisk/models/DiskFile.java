package com.gmail.dimabah.ddisk.models;

import com.gmail.dimabah.ddisk.dto.*;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.*;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DiskFile extends DiskObject {

    private String size;

    @ManyToOne
    @JoinColumn(name = "parent_folder_id")
    private DiskFolder parentFolder;

    @ManyToOne
    @JoinColumn(name = "bin_id")
    private DiskBin bin;

    public DiskFile(DiskObject diskObject, String size) {
        super(diskObject);
        this.size = size;
    }

    public DiskFileDTO toDTO() {
        DiskFileDTO result = new DiskFileDTO();
        result.setName(getName());
        result.setOwner(getPermissions().get(0).getUser().getEmail());
        result.setAddress(getAddress());
        result.setCreateDate(getCreateDate());
        result.setSize(size);

        result.setOpenToAll(getOpenToAll() != null ? getOpenToAll().toString() : null);

        Map<String, String> map = new HashMap<>();
        List<UserObjectPermission> permissions = getPermissions();
        for (int i = 1; i < permissions.size(); i++) {
            var permission = permissions.get(i);
            map.put(permission.getUser().getEmail(), permission.getAccessRights().toString());
        }
        result.setPermissions(map);

        return result;
    }

    public DiskFileBinnedDTO toFileBinnedDTO() {
        DiskFileBinnedDTO result = new DiskFileBinnedDTO();
        result.setName(getName());
        result.setAddress(getAddress());
        result.setOwner(getPermissions().get(0).getUser().getEmail());
        result.setBinnedDate(getBinnedDate());
        result.setParentFolderName(parentFolder.getName());
        result.setSize(size);

        return result;
    }

    public OneFileDTO oneFileDTO() {
        OneFileDTO result = new OneFileDTO();
        List<DiskFileDTO> files = new ArrayList<>();
        files.add(this.toDTO());
        result.setFiles(files);
        return result;

    }

    @Override
    public String toString() {
        return "DiskFile{" +
                "size=" + size +
                ", parentFolder=" + (parentFolder == null ? null : parentFolder.getId().toString()) +
                '}';
    }
}
