package com.gmail.dimabah.ddisk.services;

import com.gmail.dimabah.ddisk.dto.BlobToDownloadDTO;
import com.gmail.dimabah.ddisk.dto.FileToDownloadDTO;
import com.gmail.dimabah.ddisk.models.*;
import com.gmail.dimabah.ddisk.models.enums.AccessRights;
import com.gmail.dimabah.ddisk.repositories.DiskObjectRepository;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


import java.io.File;
import java.util.*;

@Service
public class DiskObjectService {
    private final DiskObjectRepository objectRepository;
    private final UserObjectPermissionService permissionService;


    public DiskObjectService(DiskObjectRepository diskObjectRepository, UserObjectPermissionService permissionService) {
        this.objectRepository = diskObjectRepository;
        this.permissionService = permissionService;
    }

    public DiskObject createObj(String nameObj) {
        DiskObject object = new DiskObject(nameObj);
        String address = generateAddress();
        object.setAddress(address);
        return object;
    }

    @Transactional
    public String generateAddress() {
        int length = 32;
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_";
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            char randomChar = characters.charAt(index);
            stringBuilder.append(randomChar);
        }

        return checkForAddressCollisionAndCreateNewAddress(stringBuilder.toString());
    }

    @Transactional
    public void updateObj(DiskObject object) {
        objectRepository.save(object);
    }

    private String checkForAddressCollisionAndCreateNewAddress(String address) {
        int counter = 0;
        String newAddress;

        do {
            counter++;
            newAddress = counter + address;
        } while (objectRepository.existsByAddress(newAddress));

        return newAddress;
    }

    @Transactional
    public void remove(List<String> addressList, DiskUser user) {
        addressList.forEach((x) -> {
            DiskObject object = objectRepository.findDiskObjectByAddress(x);
            if (checkUserPermission(object, user, AccessRights.EDITOR)) {
                DiskBin bin = object.getPermissions().get(0).getUser().getBin();
                if (object instanceof DiskFile) {
                    bin.addFile((DiskFile) object);
                } else if (object instanceof DiskFolder) {
                    bin.addFolder((DiskFolder) object);
                }
            }
        });
    }

    @Transactional
    public void delete(List<String> addressList, DiskUser user) {
        addressList.forEach((x) -> {
            DiskObject object = objectRepository.findDiskObjectByAddress(x);
            if (checkUserPermission(object, user, AccessRights.EDITOR)) {
                objectRepository.delete(object);
                deleteObjectFromRemoteDrive("ddisk/" + user.getEmail() +
                        "/" + object.getAddress() + "/" + object.getOriginalName());
            }
        });
    }

    private static void deleteObjectFromRemoteDrive(String objectName) {
        String projectId = "ddisk-diploma";
        String bucket = "ddisk-storage";
        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
        Blob blob = storage.get(bucket, objectName);
        if (blob == null) {
            System.out.println("The object " + objectName + " wasn't found in " + bucket);
            return;
        }

        Storage.BlobSourceOption precondition =
                Storage.BlobSourceOption.generationMatch(blob.getGeneration());

        storage.delete(bucket, objectName, precondition);
        System.out.println("Object " + objectName + " was deleted from " + bucket);
    }

    @Transactional
    public void restore(List<String> addressList, DiskUser user) {
        addressList.forEach((x) -> {
            DiskObject object = objectRepository.findDiskObjectByAddress(x);
            if (checkUserPermission(object, user, AccessRights.EDITOR)) {
                DiskBin bin = user.getBin();
                if (object instanceof DiskFile) {
                    bin.removeFile((DiskFile) object);
                } else if (object instanceof DiskFolder) {
                    bin.removeFolder((DiskFolder) object);
                }
            }
        });
    }

    @Transactional
    public boolean rename(String address, String newName, DiskUser user) {
        DiskObject object = objectRepository.findDiskObjectByAddress(address);

        if (!checkUserPermission(object, user, AccessRights.EDITOR)) return false;

        object.setName(newName);
        objectRepository.save(object);

        return true;
    }

    @Transactional
    public boolean share(Map<DiskUser, AccessRights> map,
                         String globalAccessRight, String currentObj, DiskUser user) {

        DiskObject object = objectRepository.findDiskObjectByAddress(currentObj);
        boolean result;

        if (object == null) return false;
        if (!checkUserPermission(object, user, AccessRights.EDITOR)) {
            return false;
        }

        result = checkForGlobalAccess(object, globalAccessRight, user);
        if (map == null) {
            map = new HashMap<>();
        }
        if (checkForChange(map, object)) {
            result = true;
        }
        if (checkForNewUser(map, object)) {
            result = true;
        }
        if (result) {
            objectRepository.save(object);
        }

        return result;
    }

    private boolean checkForGlobalAccess(DiskObject object, String globalAccessRight, DiskUser user) {
        if (!checkUserPermission(object, user, AccessRights.MASTER)) {
            return false;
        }
        if (globalAccessRight != null) {
            try {
                AccessRights globalRights = AccessRights.valueOf(globalAccessRight.toUpperCase());
                return changeShareInInternalObj(object, globalRights, null, true);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        } else {
            return changeShareInInternalObj(object, null, null, true);
        }
        return false;
    }

    private boolean checkForChange(Map<DiskUser, AccessRights> map, DiskObject object) {
        List<UserObjectPermission> permissions = object.getPermissions();
        List<Integer> indexPermToRemove = new LinkedList<>();
        boolean result = false;
        for (int i = 1; i < permissions.size(); i++) {
            var permission = permissions.get(i);
            var tempUser = permission.getUser();

            if (map.containsKey(tempUser)) {
                AccessRights currentRight = map.get(tempUser);
                if (changeShareInInternalObj(object, currentRight, tempUser, false)) {
                    result = true;
                }
                continue;
            }
            indexPermToRemove.add(0, i);
            object.getSharedToUsers().remove(tempUser);
            tempUser.getSharedObjects().remove(object);
        }

        indexPermToRemove.forEach((i) -> {
            changeShareInInternalObj(object, null, permissions.get(i).getUser(), false);
        });

        return result || indexPermToRemove.size() > 0;
    }

    private boolean checkForNewUser(Map<DiskUser, AccessRights> map, DiskObject object) {
        List<UserObjectPermission> permissions = object.getPermissions();
        if (map.size() + 1 == permissions.size()) {
            return false;
        }
        map.forEach((key, value) -> {
            for (int i = 0; i < permissions.size(); i++) {
                var permission = permissions.get(i);
                if (permission.getUser().equals(key)) {
                    break;
                }
                if (i == permissions.size() - 1) {
                    object.addUserToShared(key);
                    changeShareInInternalObj(object, value, key, false);
                }
            }
        });
        return true;
    }

    private boolean changeShareInInternalObj(DiskObject object, AccessRights rights, DiskUser user, boolean global) {
        if (global) {
            if (!changeGlobalRights(object, rights)) return false;
        } else {
            if (!changeAccessRights(object, rights, user)) return false;
        }

        if (object instanceof DiskFolder) {
            for (var file : ((DiskFolder) object).getFileList()) {
                if (global) {
                    changeGlobalRights(file, rights);
                } else {
                    changeAccessRights(file, rights, user);
                }
            }
            for (var folder : ((DiskFolder) object).getFolderList()) {
                changeShareInInternalObj(folder, rights, user, global);
            }
        }
        return true;
    }

    private boolean changeAccessRights(DiskObject object, AccessRights rights, DiskUser user) {
        UserObjectPermission permissionToDelete = null;
        for (var permission : object.getPermissions()) {
            if (permission.getUser().equals(user)) {
                if (rights == null) {
                    permissionToDelete = permission;
                    break;
                }
                if (permission.getAccessRights() != rights) {
                    permission.setAccessRights(rights);
                    return true;
                }
                return false;
            }
        }
        if (permissionToDelete != null) {
            permissionToDelete.getUser().getPermissions().remove(permissionToDelete);
            object.getPermissions().remove(permissionToDelete);
            permissionService.delete(permissionToDelete);
            return true;
        }
        if (rights != null) {
            UserObjectPermission uop = new UserObjectPermission(rights);
            user.addPermission(uop);
            object.addPermission(uop);
            return true;
        }
        return false;
    }

    private boolean changeGlobalRights(DiskObject object, AccessRights rights) {
        if (object.getOpenToAll() != rights) {
            object.setOpenToAll(rights);
            return true;
        }
        return false;
    }

    // Not used because there is no access to the server's local disk
    @Transactional
    public List<FileToDownloadDTO> getFileListByAddress(List<String> addressList, DiskUser user) {
        List<FileToDownloadDTO> files = new ArrayList<>();
        addressList.forEach((x) -> {
            DiskObject object = objectRepository.findDiskObjectByAddress(x);
            addInternalObjects(object, files, "*", user);
        });
        return files;
    }

    @Transactional
    public List<BlobToDownloadDTO> getBlobListByAddress(List<String> addressList, DiskUser user) {
        String projectId = "ddisk-diploma";
        List<BlobToDownloadDTO> blobs = new ArrayList<>();
        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();

        addressList.forEach((x) -> {
            DiskObject object = objectRepository.findDiskObjectByAddress(x);
            addInternalBlobs(object, blobs, "*", user, storage);
        });

        return blobs;
    }

    // Not used because there is no access to the server's local disk
    private void addInternalObjects(DiskObject object, List<FileToDownloadDTO> files, String path, DiskUser user) {
        if (!object.getLive()) return;
        if (!checkUserPermission(object, user, AccessRights.VIEWER)) return;

        if (object instanceof DiskFile) {
            FileToDownloadDTO file = new FileToDownloadDTO(new File("D:/upload_dir/" +
                    object.getPermissions().get(0).getUser().getEmail() + "/" +
                    object.getAddress() + "/" + object.getOriginalName()), object.getName());

            if ("*".equals(path)) {
                files.add(0, file);
            } else {
                files.add(file);
            }
        } else {
            path = path + "/" + object.getName();
            files.add(new FileToDownloadDTO(new File(path), ""));
            for (var file : ((DiskFolder) object).getFileList()) {
                addInternalObjects(file, files, path, user);
            }
            for (var folder : ((DiskFolder) object).getFolderList()) {
                addInternalObjects(folder, files, path, user);
            }
        }

    }

    private void addInternalBlobs(DiskObject object, List<BlobToDownloadDTO> blobs, String path, DiskUser user, Storage storage) {
        if (!object.getLive()) return;
        if (!checkUserPermission(object, user, AccessRights.VIEWER)) return;

        if (object instanceof DiskFile) {
            String objectName = "ddisk/" + object.getPermissions().get(0).getUser().getEmail() + "/" +
                    object.getAddress() + "/" + object.getOriginalName();
            Blob blob = storage.get(BlobId.of("ddisk-storage", objectName));

            BlobToDownloadDTO file = new BlobToDownloadDTO(blob, objectName, object.getName());

            if ("*".equals(path)) {
                blobs.add(0, file);
            } else {
                blobs.add(file);
            }
        } else {
            path = path + "/" + object.getName();
            blobs.add(new BlobToDownloadDTO(null, path, ""));
            for (var file : ((DiskFolder) object).getFileList()) {
                addInternalBlobs(file, blobs, path, user, storage);
            }
            for (var folder : ((DiskFolder) object).getFolderList()) {
                addInternalBlobs(folder, blobs, path, user, storage);
            }
        }

    }

    private boolean checkUserPermission(DiskObject object, DiskUser user, AccessRights rights) {
        if (object.getOpenToAll() != null && object.getOpenToAll().getValue() >= rights.getValue()) {
            return true;
        }
        if (user == null) return false;

        for (var permission : object.getPermissions()) {
            if (permission.getUser().equals(user) &&
                    permission.getAccessRights().getValue() >= rights.getValue()) {
                return true;
            }
        }
        return false;
    }


}
