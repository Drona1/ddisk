package com.gmail.dimabah.ddisk.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OneFileDTO {
    private String address = "file";
    private String port;
    private String domain;
    List<String> folders = new ArrayList<>();
    List<DiskFileDTO> files = new ArrayList<>();
}
