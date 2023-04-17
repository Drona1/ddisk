package com.gmail.dimabah.ddisk.repositories;

import com.gmail.dimabah.ddisk.models.DiskUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiskUserRepository extends JpaRepository<DiskUser, Long> {
    DiskUser findByEmail(String email);

    boolean existsByEmail(String email);
}
