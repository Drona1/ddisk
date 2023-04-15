package com.gmail.dimabah.ddisk.services;

import com.gmail.dimabah.ddisk.models.DiskBin;
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
    private final DiskBinService binService;

    public DiskUserService(DiskUserRepository userRepository, DiskFolderService diskFolderService, DiskBinService binService) {
        this.userRepository = userRepository;
        this.diskFolderService = diskFolderService;
        this.binService = binService;
    }

    @Transactional
    public DiskUser findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public boolean addUser(String email, String pass, UserRole role) {
        if (userRepository.existsByEmail(email)) {
            return false;
        }
        DiskUser user = new DiskUser(email, pass, role);
        DiskFolder folder = diskFolderService.createFolder(user, email,null);
        DiskBin bin = binService.createBin(user);

        user.setMainFolder(folder);
        user.setBin(bin);
//        bin.setUser(user);

        userRepository.save(user);

        return true;
    }



}
