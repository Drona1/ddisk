package com.gmail.dimabah.ddisk.repositories;

import com.gmail.dimabah.ddisk.models.DiskBin;
import com.gmail.dimabah.ddisk.models.DiskObject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiskBinRepository  extends JpaRepository<DiskBin,Long> {

}
