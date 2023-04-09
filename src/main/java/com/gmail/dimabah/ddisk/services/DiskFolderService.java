package com.gmail.dimabah.ddisk.services;

import com.gmail.dimabah.ddisk.models.DiskFolder;
import com.gmail.dimabah.ddisk.models.DiskObject;
import com.gmail.dimabah.ddisk.models.DiskUser;
import com.gmail.dimabah.ddisk.models.UserObjectPermission;
import com.gmail.dimabah.ddisk.models.enums.AccessRights;
import com.gmail.dimabah.ddisk.repositories.DiskFolderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class DiskFolderService{
    private final DiskFolderRepository diskFolderRepository;
    private final DiskObjectService diskObjectService;

    public DiskFolderService(DiskFolderRepository diskFolderRepository, DiskObjectService diskObjectService) {
        this.diskFolderRepository = diskFolderRepository;
        this.diskObjectService = diskObjectService;
    }


    public DiskFolder createFolder(DiskUser user, String folderName) {
//        DiskObject object = diskObjectService.createObj(user, folderName);
//        DiskFolder folder = new DiskFolder(object);
        DiskFolder folder = new DiskFolder(user,folderName);
        String address = diskObjectService.generateAddress();
        folder.setAddress(address);

        UserObjectPermission permission = new UserObjectPermission(AccessRights.MASTER);
        permission.setUser(user);
        folder.addPermission(permission);

        diskFolderRepository.save(folder);
        return folder;
    }
    public DiskFolder findByAddress(String address){
        return diskFolderRepository.findByAddress(address);
    }

    @Transactional
    public void addFolder(DiskFolder currentFolder, DiskFolder newFolder) {
        currentFolder.addFolder(newFolder);
        diskFolderRepository.save(currentFolder);
    }

    @Transactional
    public void updateFolder(DiskFolder folder){
        diskFolderRepository.save(folder);
    }
}
