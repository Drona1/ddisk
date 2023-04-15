package com.gmail.dimabah.ddisk.repositories;

import com.gmail.dimabah.ddisk.models.DiskFile;
import com.gmail.dimabah.ddisk.models.DiskObject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiskObjectRepository extends JpaRepository<DiskObject,Long> {
    boolean existsByAddress (String address);
    DiskObject findDiskObjectByAddress(String address);
}
