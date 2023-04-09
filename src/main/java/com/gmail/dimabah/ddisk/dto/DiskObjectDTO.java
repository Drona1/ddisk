package com.gmail.dimabah.ddisk.dto;

import lombok.Data;

import java.util.Date;

@Data
public class DiskObjectDTO {
    private String name;
    private String address;
    private String owner;
    private Date createDate;
}
