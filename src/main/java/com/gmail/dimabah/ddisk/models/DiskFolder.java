package com.gmail.dimabah.ddisk.models;

import com.gmail.dimabah.ddisk.dto.DiskFileDTO;
import com.gmail.dimabah.ddisk.dto.DiskFolderDTO;
import com.gmail.dimabah.ddisk.dto.DiskObjectDTO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DiskFolder extends DiskObject {

//    @OneToOne
//    @JoinColumn(name = "prev_folder_id")
//    private DiskFolder prevFolder;

    @OneToMany(mappedBy = "parentFolder")
    private List<DiskFolder> folderList = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "parent_folder_id")
    private DiskFolder parentFolder;

    @OneToMany(mappedBy = "parentFolder")
    private List<DiskFile> fileList = new ArrayList<>();

    public DiskFolder(DiskUser user, String folderName) {
        super(user, folderName);
    }

    public DiskFolder(DiskObject diskObject) {
        super(diskObject);
    }

    public void addFolder(DiskFolder folder) {
        if (!folderList.contains(folder)) {
            folderList.add(folder);
            folder.setParentFolder(this);
        }
    }

    public void addFile(DiskFile file) {
        if (!fileList.contains(file)) {
            fileList.add(file);
            file.setParentFolder(this);
        }
    }

    public DiskFolderDTO toFolderDTO() {
        DiskFolderDTO result = new DiskFolderDTO();
        List<DiskObjectDTO> folders = new ArrayList<>();
        List<DiskFileDTO> files = new ArrayList<>();

        for (var folder : folderList) {
            folders.add(folder.toDTO());
        }
        result.setFolders(folders);
        for (var file : fileList) {
            files.add(file.toDTO());
        }
        result.setFiles(files);

        result.setAddress(this.getAddress());

        return result;
    }


}
