package com.gmail.dimabah.ddisk.models;

import com.gmail.dimabah.ddisk.dto.DiskFileDTO;
import com.gmail.dimabah.ddisk.dto.DiskObjectDTO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Random;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DiskFile extends DiskObject{

    private Long size;

    @ManyToOne
    @JoinColumn(name = "parent_folder_id")
    private DiskFolder parentFolder;

    public DiskFile(DiskUser user, String fileName, Long size) {
        super(user, fileName);
        this.size = size;
    }
    public DiskFile(DiskObject diskObject){
        super(diskObject);
    }

    public DiskFileDTO toDTO(){
        DiskFileDTO result = new DiskFileDTO();
        result.setName(getName());
        result.setOwner(getPermissions().get(0).getUser().getEmail());
        result.setAddress(getAddress());
        result.setCreateDate(getCreateDate());
        result.setSize(size);
        return result;
    }
}
