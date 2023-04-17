package com.gmail.dimabah.ddisk.models;

import com.gmail.dimabah.ddisk.dto.*;
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


    @OneToMany(mappedBy = "parentFolder", cascade = CascadeType.ALL)
    private List<DiskFolder> folderList = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "parent_folder_id")
    private DiskFolder parentFolder;

    @OneToMany(mappedBy = "parentFolder", cascade = CascadeType.ALL)
    private List<DiskFile> fileList = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "bin_id")
    private DiskBin bin;

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

    public DiskFolderDTO toFolderDTO(boolean live) {
        DiskFolderDTO result = new DiskFolderDTO();
        List<DiskObjectDTO> folders = new ArrayList<>();
        List<DiskFileDTO> files = new ArrayList<>();

        for (var folder : folderList) {
            if (folder.getLive() == live) {
                folders.add(folder.toDTO());
            }
        }
        result.setFolders(folders);
        for (var file : fileList) {
            if (file.getLive() == live) {
                files.add(file.toDTO());
            }
        }
        result.setFiles(files);

        result.setAddress(this.getAddress());

        return result;
    }

    public DiskFolderBinnedDTO toFolderBinnedDTO() {
        DiskFolderBinnedDTO result = new DiskFolderBinnedDTO();
        result.setName(getName());
        result.setAddress(getAddress());
        result.setOwner(getPermissions().get(0).getUser().getEmail());
        result.setBinnedDate(getBinnedDate());
        result.setParentFolderName(parentFolder.getName());
        return result;
    }


    @Override
    public String toString() {
        return "DiskFolder{" +
                "folderList=" + folderList +
                ", parentFolder=" + (parentFolder == null ? null : parentFolder.getId().toString()) +
                ", fileList=" + fileList +
                '}';
    }
}
