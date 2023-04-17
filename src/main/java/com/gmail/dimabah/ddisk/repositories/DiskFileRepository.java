package com.gmail.dimabah.ddisk.repositories;

import com.gmail.dimabah.ddisk.models.DiskFile;

public interface DiskFileRepository extends DiskObjectRepository {
    DiskFile findByAddress(String address);
}
