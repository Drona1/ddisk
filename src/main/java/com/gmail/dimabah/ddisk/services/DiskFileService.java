package com.gmail.dimabah.ddisk.services;

import com.gmail.dimabah.ddisk.models.*;
import com.gmail.dimabah.ddisk.models.enums.AccessRights;
import com.gmail.dimabah.ddisk.repositories.DiskFileRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiskFileService {
    DiskFileRepository fileRepository;
    DiskObjectService objectService;

    public DiskFileService(DiskFileRepository fileRepository, DiskObjectService objectService) {
        this.fileRepository = fileRepository;
        this.objectService = objectService;
    }

    @Transactional
    public DiskFile createFile(DiskUser user, String fileName, long size, DiskFolder parentFolder) {
        DiskObject object = objectService.createObj(fileName);
        DiskFile file = new DiskFile(object, formatSize(size));

        UserObjectPermission permission = new UserObjectPermission(AccessRights.MASTER);
        user.addPermission(permission);
        file.addPermission(permission);

        parentFolder.addFile(file);

        fileRepository.save(file);
        return file;
    }

    private String formatSize(long size) {
        if (size / 1024 / 1024 != 0) {
            double result = size / 1024.0 / 1024;
            return String.format("%.1f MB", result);
        } else if (size / 1024 != 0) {
            double result = size / 1024.0;
            return String.format("%.0f KB", result);
        }
        return String.format("%d B", size);
    }
//        @Transactional
//    public void remove(List<String> addressList) {
//        addressList.forEach((x) -> {
//            DiskFile file = fileRepository.findByAddress(x);
//            file.setLive(false);
//        });
//    }
}
