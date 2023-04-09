package com.gmail.dimabah.ddisk.repositories;

import com.gmail.dimabah.ddisk.models.DiskFolder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiskFolderRepository extends JpaRepository<DiskFolder,Long> {
    DiskFolder findByAddress(String address);
    boolean existsByAddress (String address);



}
