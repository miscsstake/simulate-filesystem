package com.eaglesoup.util;

public class FATUtil {
    public static boolean isFileEnd(byte[] i) {
        short flag = (short) SizeUtil.byteArrayToLong(i, Short.BYTES);
        return flag == (short) 0xFFF8;
    }

    public static boolean isBigFile(byte[] i) {
        int flag = (int) SizeUtil.byteArrayToLong(i, Short.BYTES);
        return 0x0003 <= flag && flag <= 0xFFEF;
    }

    public static boolean isEmptyFile(byte[] i) {
        short flag = (short) SizeUtil.byteArrayToLong(i, Short.BYTES);
        return flag == 0x0000;
    }
}
