package com.eaglesoup.util;

import com.eaglesoup.enums.DirectoryEntityAttrTypeEnum;

public class EntityUtil {
    public static boolean isDir(byte val) {
        return DirectoryEntityAttrTypeEnum.DIR == DirectoryEntityAttrTypeEnum.valueOf(val);
    }
}
