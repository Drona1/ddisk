package com.gmail.dimabah.ddisk.services;

import com.gmail.dimabah.ddisk.models.DiskBin;
import com.gmail.dimabah.ddisk.repositories.DiskBinRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class DiskBinService {
    private final DiskBinRepository binRepository;

    public DiskBinService(DiskBinRepository binRepository) {
        this.binRepository = binRepository;
    }
    @Transactional
    public DiskBin createBin() {
        DiskBin bin = new DiskBin();
        binRepository.save(bin);
        return bin;
    }
}
