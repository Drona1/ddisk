package com.gmail.dimabah.ddisk.dto;

import lombok.Data;

import java.util.Date;

@Data
public class DiskFolderBinnedDTO extends DiskObjectBinnedDTO {
    private String parentFolderName;
}
