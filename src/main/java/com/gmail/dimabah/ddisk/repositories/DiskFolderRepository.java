package com.gmail.dimabah.ddisk.repositories;

import com.gmail.dimabah.ddisk.models.DiskFolder;

public interface DiskFolderRepository extends DiskObjectRepository {
    DiskFolder findByAddress(String address);
}
