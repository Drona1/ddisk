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
    private final DiskFolderRepository folderRepository;
    private final DiskObjectService objectService;

    public DiskFolderService(DiskFolderRepository diskFolderRepository, DiskObjectService diskObjectService) {
        this.folderRepository = diskFolderRepository;
        this.objectService = diskObjectService;
    }

    @Transactional
    public DiskFolder createFolder(DiskUser user, String folderName, DiskFolder parentFolder) {
        DiskObject object = objectService.createObj(folderName);
        DiskFolder folder = new DiskFolder(object);

//        DiskFolder folder = new DiskFolder(user,folderName);
//        String address = diskObjectService.generateAddress();
//        folder.setAddress(address);
//        DiskFolder folder = (DiskFolder) diskObjectService.createObj(user,folderName);

        UserObjectPermission permission = new UserObjectPermission(AccessRights.MASTER);
        user.addPermission(permission);
        folder.addPermission(permission);
        if (parentFolder!= null){
            parentFolder.addFolder(folder);
        }
        folderRepository.save(folder);
        return folder;
    }
    @Transactional
    public DiskFolder findByAddress(String address){
        return folderRepository.findByAddress(address);
    }

    @Transactional
    public void addFolder(DiskFolder currentFolder, DiskFolder newFolder) {
        currentFolder.addFolder(newFolder);
        folderRepository.save(currentFolder);
    }

    @Transactional
    public void updateFolder(DiskFolder folder){
        folderRepository.save(folder);
    }
}
