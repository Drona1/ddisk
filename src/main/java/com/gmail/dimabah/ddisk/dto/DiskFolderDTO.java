package com.gmail.dimabah.ddisk.dto;

import com.gmail.dimabah.ddisk.models.DiskObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DiskFolderDTO{
    private String address;
    private List<DiskObjectDTO> folders = new ArrayList<>();
    private List<DiskFileDTO> files = new ArrayList<>();

}
