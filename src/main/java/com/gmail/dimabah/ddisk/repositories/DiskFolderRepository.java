package com.gmail.dimabah.ddisk.repositories;

import com.gmail.dimabah.ddisk.models.DiskFolder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiskFolderRepository extends DiskObjectRepository {
    DiskFolder findByAddress(String address);




}
