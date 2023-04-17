package com.gmail.dimabah.ddisk.models.enums;

public enum AccessRights {
    MASTER(5),
    EDITOR(3),
    VIEWER(1);
    private final int value;

    AccessRights(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
