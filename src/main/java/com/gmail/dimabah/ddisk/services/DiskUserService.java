package com.gmail.dimabah.ddisk.services;


import com.gmail.dimabah.ddisk.models.*;
import com.gmail.dimabah.ddisk.models.enums.AccessRights;
import com.gmail.dimabah.ddisk.models.enums.UserRole;
import com.gmail.dimabah.ddisk.repositories.DiskUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.Map;

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
        DiskFolder folder = diskFolderService.createFolder(user, email, null);
        DiskBin bin = binService.createBin();

        user.setMainFolder(folder);
        user.setBin(bin);

        userRepository.save(user);

        return true;
    }

    public Map<DiskUser, AccessRights> convertToMap(String[] userEmails, String[] accessRights) {
        Map<DiskUser, AccessRights> map = new HashMap<>();

        for (int i = 0; i < userEmails.length; i++) {
            DiskUser user = findByEmail(userEmails[i]);
            if (user == null) continue;
            try {
                if (accessRights[i] != null) {
                    AccessRights tempRight = AccessRights.valueOf(accessRights[i].toUpperCase());
                    map.put(user, tempRight);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        return map;
    }


}
