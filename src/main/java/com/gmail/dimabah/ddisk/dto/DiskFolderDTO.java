package com.gmail.dimabah.ddisk.dto;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DiskFolderDTO {
    private String domain;
//    private String port;
    private String address;
    private List<DiskObjectDTO> folders = new ArrayList<>();
    private List<DiskFileDTO> files = new ArrayList<>();

}
