package com.gmail.dimabah.ddisk.services;

import com.gmail.dimabah.ddisk.models.UserObjectPermission;
import com.gmail.dimabah.ddisk.models.enums.AccessRights;
import com.gmail.dimabah.ddisk.repositories.UserObjectPermissionRepository;
import org.springframework.stereotype.Service;

@Service
public class UserObjectPermissionService {
    UserObjectPermissionRepository permissionRepository;

    public UserObjectPermissionService(UserObjectPermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public UserObjectPermission addPermission(AccessRights permission){
        UserObjectPermission userObjectPermission = new UserObjectPermission(permission);
        permissionRepository.save(userObjectPermission);

        return userObjectPermission;
    }
}
