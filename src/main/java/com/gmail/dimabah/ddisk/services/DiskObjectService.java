package com.gmail.dimabah.ddisk.services;

import com.gmail.dimabah.ddisk.models.*;
import com.gmail.dimabah.ddisk.models.enums.AccessRights;
import com.gmail.dimabah.ddisk.repositories.DiskObjectRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class DiskObjectService {
    DiskObjectRepository objectRepository;

    public DiskObjectService(DiskObjectRepository diskObjectRepository) {
        this.objectRepository = diskObjectRepository;
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
        String result = checkForAddressCollisionAndCreateNewAddress(stringBuilder.toString());
        return result;
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
                DiskBin bin = user.getBin();
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
            }
        });
    }

    @Transactional
    public void restore(List<String> addressList, DiskUser user) {
        addressList.forEach((x) -> {
            DiskObject object = objectRepository.findDiskObjectByAddress(x);
            if (checkUserPermission(object, user, AccessRights.MASTER)) {
                DiskBin bin = user.getBin();
                if (object instanceof DiskFile) {
                    bin.removeFile((DiskFile) object);
                } else if (object instanceof DiskFolder) {
                    bin.removeFolder((DiskFolder) object);
                }
            }
        });
    }

    public boolean rename(String address, String newName, DiskUser user) {
        DiskObject object = objectRepository.findDiskObjectByAddress(address);

        if (checkUserPermission(object, user, AccessRights.EDITOR)) return false;

        object.setName(newName);
        objectRepository.save(object);

        return true;
    }

    public List<File> getFileListByAddress(List<String> addressList, DiskUser user) {
        List<File> files = new ArrayList<>();
        addressList.forEach((x) -> {
            DiskObject object = objectRepository.findDiskObjectByAddress(x);
                addInternalObjects(object, files, "*", user);
        });
        return files;
    }


    private void addInternalObjects(DiskObject object, List<File> files, String path, DiskUser user) {
        if (!object.getLive()) return;
        if (!checkUserPermission(object, user, AccessRights.VIEWER)) return;

        if (object instanceof DiskFile) {
            File file = new File("D:/upload_dir/" +
                    object.getPermissions().get(0).getUser().getEmail() + "/" +
                    object.getAddress() + "/" + object.getName());
            if ("*".equals(path)) {
                files.add(0, file);
            } else {
                files.add(file);
            }
        } else {
            path = path + "/" + object.getName();
            files.add(new File(path));
            for (var file : ((DiskFolder) object).getFileList()) {
                addInternalObjects(file, files, path, user);
            }
            for (var folder : ((DiskFolder) object).getFolderList()) {
                addInternalObjects(folder, files, path, user);
            }
        }

    }

    private boolean checkUserPermission(DiskObject object, DiskUser user, AccessRights rights) {
        if (object.getOpenToAll()) {
            return true;
        }
        for (var permission : object.getPermissions()) {
            if (permission.getUser().equals(user) &&
                    permission.getAccessRights().getValue() >= rights.getValue()) {
                return true;
            }
        }
        return false;
    }
}
