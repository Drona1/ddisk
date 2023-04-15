package com.gmail.dimabah.ddisk.repositories;

import com.gmail.dimabah.ddisk.models.DiskFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiskFileRepository extends DiskObjectRepository {
    DiskFile findByAddress(String address);

}
