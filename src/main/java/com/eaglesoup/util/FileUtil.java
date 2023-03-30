package com.eaglesoup.util;

import com.eaglesoup.core.model.BootSectorStruct;

import java.io.File;

public class FileUtil {
    //设置项目路径信息
    private static String baseFile = "";

    //当前目录：初始化为根目录
    private static String currentDir = "/";

    private FileUtil() {

    }

    public static void setBaseFile(String filename) {
        baseFile = filename;
    }

    public static String getBaseFile() {
        return baseFile;
    }

    public static void setCurrentPath(String path) {
        currentDir = path;
    }

    public static String getCurrentPath() {
        return currentDir;
    }


    public static int bootSectorSize() {
        return 1;
    }

    public static int fatSectorSize() {
        int size = 65536 * 2 * BootSectorStruct.getInstance().getFatCopiesCount();
        return size / 512;
    }

    public static int rootDirSectorSize() {
        return 63;
    }

    public static int getClusterCount() {
        return (int) new File(getBaseFile()).length() / 64 / 512;
    }

    /**
     * 0xFFEF怎么用两个字节表示来着
     *
     * @param fat
     * @return
     */
    public static boolean isNextCluster(byte[] fat) {
        int clusterIndex = (int) SizeUtil.byteArrayToLong(fat, 4);
        int start = 0x0003;
        int end = 0xFFEF;
        return start <= clusterIndex && clusterIndex <= end;
    }

    /**
     * 判断是否是文件
     *
     * @param attr
     * @return
     */
    public static boolean isDir(byte attr) {
        return attr == 1 << 4;
    }
}
