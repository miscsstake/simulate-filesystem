package com.eaglesoup.enums;


public enum FileModeEnum {
    READ_MODE("r", "读取模式"),
    WRITE_MODE("w", "直接写模式"),
    APPEND_MODE("a", "追加写模式");

    FileModeEnum(String mode, String desc) {
        this.mode = mode;
        this.desc = desc;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    private String mode;
    private String desc;
}
