package com.gmail.dimabah.ddisk.dto;

import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class DiskObjectDTO {
    private String name;
    private String address;
    private String owner;
    private Date createDate;
    private String openToAll;
    Map<String, String> permissions;
}
