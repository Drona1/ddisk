package com.gmail.dimabah.ddisk.components;

import com.gmail.dimabah.ddisk.models.DiskObject;
import com.gmail.dimabah.ddisk.models.UserObjectPermission;
import com.gmail.dimabah.ddisk.models.enums.AccessRights;
import com.gmail.dimabah.ddisk.repositories.DiskObjectRepository;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {
    private final DiskObjectRepository diskObjectRepository;

    public CustomPermissionEvaluator(DiskObjectRepository diskObjectRepository) {
        this.diskObjectRepository = diskObjectRepository;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (targetDomainObject instanceof String && permission instanceof String) {
            String address = (String) targetDomainObject;
            AccessRights accessRight = AccessRights.valueOf((String) permission);
            DiskObject folder = diskObjectRepository.findDiskObjectByAddress(address);
            if (folder != null) {
                if (folder.getOpenToAll() != null && folder.getOpenToAll().getValue() >= accessRight.getValue()) {
                    return true;
                }
                for (UserObjectPermission p : folder.getPermissions()) {
                    if (p.getUser().getEmail().equals(authentication.getName()) &&
                            p.getAccessRights().getValue() >= accessRight.getValue()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return false;
    }
}
