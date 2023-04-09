package com.gmail.dimabah.ddisk.services;

import com.gmail.dimabah.ddisk.models.DiskFolder;
import com.gmail.dimabah.ddisk.models.DiskUser;
import com.gmail.dimabah.ddisk.models.UserObjectPermission;
import com.gmail.dimabah.ddisk.models.enums.AccessRights;
import com.gmail.dimabah.ddisk.models.enums.UserRole;
import com.gmail.dimabah.ddisk.repositories.DiskUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class DiskUserService {
    private final DiskUserRepository userRepository;
    private final DiskFolderService diskFolderService;

    public DiskUserService(DiskUserRepository userRepository, DiskFolderService diskFolderService) {
        this.userRepository = userRepository;
        this.diskFolderService = diskFolderService;
    }

    public DiskUser findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public boolean addUser(String email, String pass, UserRole role) {
        if (userRepository.existsByEmail(email)) {
            return false;
        }
        DiskUser user = new DiskUser(email, pass, role);
        DiskFolder folder = diskFolderService.createFolder(user, email);
//        UserObjectPermission permission = new UserObjectPermission(AccessRights.MASTER);
//
//        folder.addPermission(permission);
        user.setMainFolder(folder);
//        user.addPermission(permission);
        userRepository.save(user);

        return true;
    }



}
