package com.gmail.dimabah.ddisk.services;

import com.gmail.dimabah.ddisk.models.UserObjectPermission;
import com.gmail.dimabah.ddisk.repositories.UserObjectPermissionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UserObjectPermissionService {
    private final UserObjectPermissionRepository permissionRepository;

    public UserObjectPermissionService(UserObjectPermissionRepository userObjectPermissionRepository) {
        this.permissionRepository = userObjectPermissionRepository;
    }

    @Transactional
    public void delete(UserObjectPermission permission) {
        permissionRepository.delete(permission);
    }
}
