package com.eaglesoup.enums;

public enum CreateFileSectorTypeEnum {
    AVAILABLE_SECTOR(0, "创建文件或目录"),

    DISK_FULL(-1, "空间已满"),

    FILE_NAME_EXISTS(-2, "文件已存在");

    private int code;
    private String msg;

    CreateFileSectorTypeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
