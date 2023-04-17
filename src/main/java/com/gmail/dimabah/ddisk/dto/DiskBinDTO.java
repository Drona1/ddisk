package com.gmail.dimabah.ddisk.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DiskBinDTO {
    private List<DiskFolderBinnedDTO> folders = new ArrayList<>();
    private List<DiskFileBinnedDTO> files = new ArrayList<>();
}
