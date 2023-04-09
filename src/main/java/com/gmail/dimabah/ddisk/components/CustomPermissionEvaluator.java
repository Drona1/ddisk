package com.gmail.dimabah.ddisk.components;

import com.gmail.dimabah.ddisk.models.DiskFolder;
import com.gmail.dimabah.ddisk.models.UserObjectPermission;
import com.gmail.dimabah.ddisk.repositories.DiskFolderRepository;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {
    private final DiskFolderRepository diskFolderRepository;

    public CustomPermissionEvaluator(DiskFolderRepository diskFolderRepository) {
        this.diskFolderRepository = diskFolderRepository;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (targetDomainObject instanceof String && permission instanceof String) {
            String address = (String) targetDomainObject;
//            String accessRight = (String) permission;
            DiskFolder folder = diskFolderRepository.findByAddress(address);
            if (folder != null) {
                if (folder.getOpenToAll()){
                    return true;
                }
                for (UserObjectPermission p : folder.getPermissions()) {
                    if (p.getUser().getEmail().equals(authentication.getName())){
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
