package com.gmail.dimabah.ddisk.repositories;

import com.gmail.dimabah.ddisk.models.DiskFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiskFileRepository extends JpaRepository<DiskFile,Long> {
    boolean existsByAddress (String address);
}
