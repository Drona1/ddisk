package com.gmail.dimabah.ddisk.dto;

import com.google.cloud.storage.Blob;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class BlobToDownloadDTO {
    private Blob blob;
    private String filePath;
    private String newFileName;
}
