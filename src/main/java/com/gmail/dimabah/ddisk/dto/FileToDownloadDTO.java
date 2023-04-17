package com.gmail.dimabah.ddisk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;

@Data
@AllArgsConstructor
public class FileToDownloadDTO {
    private File file;
    private String newFileName;
}
