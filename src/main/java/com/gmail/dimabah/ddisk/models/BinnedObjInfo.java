//package com.gmail.dimabah.ddisk.models;
//
//import jakarta.persistence.*;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.util.Date;
//
//@Entity
//@Data
//@NoArgsConstructor
//public class BinnedObjInfo {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Temporal(value = TemporalType.TIMESTAMP)
//    @Column(nullable = false)
//    private Date binnedDate;
//
//    @OneToOne
//    @JoinColumn(name = "object_id")
//    private DiskObject object;
//
//    @ManyToOne
//    @JoinColumn(name = "bin_id")
//    private DiskBin bin;
//
//    public BinnedObjInfo(DiskObject object) {
//        this.object = object;
//        this.binnedDate = new Date();
//    }
//
//    @Override
//    public String toString() {
//        return "BinnedObjInfo{" +
//                "id=" + id +
//                ", binnedDate=" + binnedDate +
//                ", objectId=" + object.getId() +
//                ", binId=" + bin.getId() +
//                '}';
//    }
//}
