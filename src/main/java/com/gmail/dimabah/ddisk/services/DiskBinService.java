package com.gmail.dimabah.ddisk.services;

import com.gmail.dimabah.ddisk.models.DiskBin;
import com.gmail.dimabah.ddisk.models.DiskUser;
import com.gmail.dimabah.ddisk.repositories.DiskBinRepository;
import org.springframework.stereotype.Service;

@Service
public class DiskBinService {
    private final DiskBinRepository binRepository;

    public DiskBinService(DiskBinRepository binRepository) {
        this.binRepository = binRepository;
    }
    public DiskBin createBin(DiskUser user){
        DiskBin bin = new DiskBin();
        binRepository.save(bin);
        return bin;
    }
}
