package com.eaglesoup.util;

import com.eaglesoup.core.model.BootSectorStruct;
import com.eaglesoup.core.model.DirectoryEntityStruct;
import com.eaglesoup.core.model.FAT16XStruct;
import lombok.SneakyThrows;

import java.util.Calendar;

public class FATUtil {
    private final static BootSectorStruct bootSectorStruct = BootSectorStruct.getInstance();

    public static boolean isDir(byte attr) {
        return attr == 1 << 4;
    }

    public static byte[] initFatClusterStatus() {
        return new byte[]{
                (byte) 0xFF, (byte) 0xF8
        };
    }

    public static byte[] resetFatClusterStatus() {
        return new byte[]{
                (byte) 0x00, (byte) 0x00
        };
    }

    public static int rootDirectorySectorIndex() {
        return (bootSectorSize() + fatSize()) / bootSectorStruct.getPerSectorBytes();
    }

    /**
     * fat扇区启始位置
     */
    public static int fatSectorIndex() {
        return (bootSectorSize()) / bootSectorStruct.getPerSectorBytes();
    }

    public static int rootDirSectorIndex() {
        return (bootSectorSize() + fatSize()) / bootSectorStruct.getPerSectorBytes();
    }

    public static int rootDirectorySize() {
        return bootSectorStruct.getRootEntitiesCount() * DirectoryEntityStruct.ENTITY_SIZE;
    }

    public static int bootSectorSize() {
        return bootSectorStruct.getReservedSectors() * bootSectorStruct.getPerSectorBytes();
    }

    public static int fatSize() {
        int maxClusterCount = (int) Math.pow(FAT16XStruct.PER_FAT_SIZE, FAT16XStruct.PER_FAT_SIZE * 8);
        return maxClusterCount * FAT16XStruct.PER_FAT_SIZE * bootSectorStruct.getFatCopiesCount();
    }

    public static int clusterSize() {
        return bootSectorStruct.getPerClusterSectors() * bootSectorStruct.getPerSectorBytes();
    }

    @SneakyThrows
    public static DirectoryEntityStruct buildDirectoryEntityStruct(String filename, boolean isDir, int startingCluster, int fileSize) {
        //创建时间
        long millis = Calendar.getInstance().getTimeInMillis() / 1000;
        //上次访问日期
        long lastAccessTime = millis / 24 * 60 * 60;
        //文件后缀
        int extensionIndex = filename.lastIndexOf(".");
        String fileExtension = extensionIndex == -1 ? "" : filename.substring(extensionIndex + 1);
        //文件Attribute
        byte attribute = isDir ? 1 << 4 : (byte) 0;
        //构建 DirectoryEntityStruct
        DirectoryEntityStruct directoryEntityStruct = new DirectoryEntityStruct();

        byte[] filenameBytes = filename.getBytes();
        System.arraycopy(filenameBytes, 0, directoryEntityStruct.getFilename(), 0, filenameBytes.length);
        byte[] extensionBytes = fileExtension.getBytes();
        System.arraycopy(extensionBytes, 0, directoryEntityStruct.getFilenameExtension(), 0, extensionBytes.length);

        directoryEntityStruct.setAttribute(attribute);
        directoryEntityStruct.setReservedForWindowsNT((byte) 0);
        directoryEntityStruct.setCreationTimestamp((int) millis);
        directoryEntityStruct.setLastAccessDateStamp((short) lastAccessTime);
        directoryEntityStruct.setReservedForFAT32((short) 0);
        directoryEntityStruct.setLastWriteTimestamp((int) millis);
        directoryEntityStruct.setStartingCluster(SizeUtil.longToByteArray(startingCluster, Short.BYTES));
        directoryEntityStruct.setFileSize(isDir ? 0 : fileSize);

        return directoryEntityStruct;
    }
}
