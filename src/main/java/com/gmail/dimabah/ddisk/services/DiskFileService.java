package com.gmail.dimabah.ddisk.services;

import com.gmail.dimabah.ddisk.models.DiskFile;
import com.gmail.dimabah.ddisk.models.DiskFolder;
import com.gmail.dimabah.ddisk.models.DiskUser;
import com.gmail.dimabah.ddisk.models.UserObjectPermission;
import com.gmail.dimabah.ddisk.models.enums.AccessRights;
import com.gmail.dimabah.ddisk.repositories.DiskFileRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class DiskFileService {
    DiskFileRepository fileRepository;
    DiskObjectService diskObjectService;

    public DiskFileService(DiskFileRepository fileRepository, DiskObjectService objectService) {
        this.fileRepository = fileRepository;
        this.diskObjectService = objectService;
    }
    @Transactional
    public DiskFile createFile(DiskUser user, String fileName, long size){
        DiskFile file = new DiskFile(user, fileName, size);
        String address = diskObjectService.generateAddress();

        file.setAddress(address);

        UserObjectPermission permission = new UserObjectPermission(AccessRights.MASTER);
        permission.setUser(user);
        file.addPermission(permission);


        fileRepository.save(file);
        return file;
    }
}
