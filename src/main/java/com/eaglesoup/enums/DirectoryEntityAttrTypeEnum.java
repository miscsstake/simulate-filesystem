package com.eaglesoup.enums;

import lombok.Getter;

@Getter
public enum DirectoryEntityAttrTypeEnum {
    /**
     * 文件
     */
    FILE((byte) 0x0),

    /**
     * 目录
     */
    DIR((byte) 0x10),

    /**
     * 长文件
     */
    LFN((byte) 0xF);

    private final byte code;

    DirectoryEntityAttrTypeEnum(byte code) {
        this.code = code;
    }

    public static DirectoryEntityAttrTypeEnum valueOf(byte attribute) {
        for (DirectoryEntityAttrTypeEnum entityAttrTypeEnum : DirectoryEntityAttrTypeEnum.values()) {
            if (entityAttrTypeEnum.getCode() == attribute) {
                return entityAttrTypeEnum;
            }
        }
        return null;
    }
}
