package com.gmail.dimabah.ddisk.services;

import com.gmail.dimabah.ddisk.models.DiskFolder;
import com.gmail.dimabah.ddisk.models.DiskObject;
import com.gmail.dimabah.ddisk.models.DiskUser;
import com.gmail.dimabah.ddisk.models.UserObjectPermission;
import com.gmail.dimabah.ddisk.models.enums.AccessRights;
import com.gmail.dimabah.ddisk.repositories.DiskObjectRepository;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class DiskObjectService {
    DiskObjectRepository diskObjectRepository;

    public DiskObjectService(DiskObjectRepository diskObjectRepository) {
        this.diskObjectRepository = diskObjectRepository;
    }
    public DiskObject createObj (DiskUser user, String nameObj){
        DiskObject object = new DiskObject(user, nameObj);
        String address = generateAddress();
        UserObjectPermission permission = new UserObjectPermission(AccessRights.MASTER);

        object.setAddress(address);
        permission.setUser(user);
        object.addPermission(permission);

        return object;
    }


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
        } while (diskObjectRepository.existsByAddress(newAddress));

        return newAddress;
    }

}
