package com.gmail.dimabah.ddisk.models;

import com.gmail.dimabah.ddisk.dto.*;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class DiskBin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "bin")
    private List<DiskFolder> folderList = new ArrayList<>();

    @OneToMany(mappedBy = "bin")
    private List<DiskFile> fileList = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "user_id")
    private DiskUser user;

    public void addFolder(DiskFolder folder) {
        if (!folderList.contains(folder)) {
            folder.setLive(false);
            setLiveForInternalObjects(folder, false);
            folder.setBinnedDate(new Date());
            folderList.add(folder);
            folder.setBin(this);
        }
    }

    private void setLiveForInternalObjects(DiskFolder folder, boolean live) {
        folder.getFileList().forEach((e) -> e.setLive(live));
        folder.getFolderList().forEach((e) -> {
            e.setLive(live);
            setLiveForInternalObjects(e, live);
        });
    }

    public void addFile(DiskFile file) {
        if (!fileList.contains(file)) {
            file.setLive(false);
            file.setBinnedDate(new Date());
            fileList.add(file);
            file.setBin(this);
        }
    }

    public void removeFile(DiskFile file) {
        if (fileList.contains(file)) {
            file.setLive(true);
            file.setBinnedDate(null);
            fileList.remove(file);
            file.setBin(null);
        }
    }

    public void removeFolder(DiskFolder folder) {
        if (folderList.contains(folder)) {
            folder.setLive(true);
            setLiveForInternalObjects(folder, true);
            folder.setBinnedDate(null);
            folderList.remove(folder);
            folder.setBin(null);
        }
    }

    public DiskBinDTO toDTO() {
        DiskBinDTO result = new DiskBinDTO();
        List<DiskFolderBinnedDTO> folders = new ArrayList<>();
        List<DiskFileBinnedDTO> files = new ArrayList<>();

        for (var folder : folderList) {
            folders.add(folder.toFolderBinnedDTO());
        }
        result.setFolders(folders);
        for (var file : fileList) {
            files.add(file.toFileBinnedDTO());
        }
        result.setFiles(files);

        return result;
    }


}
