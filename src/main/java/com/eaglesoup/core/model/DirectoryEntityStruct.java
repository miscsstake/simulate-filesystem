package com.eaglesoup.core.model;

import com.eaglesoup.util.SizeUtil;
import lombok.Data;

import java.util.Arrays;

@Data
public class DirectoryEntityStruct {
    private byte[] filename = new byte[8];

    private byte[] filenameExtension = new byte[3];

    private byte attribute;

    private byte reservedForWindowsNT = 0;

    private byte creation = 0;

    //创建时间的秒级时间戳
    private int creationTimestamp;

    //上次访问日期，天级时间戳
    private short lastAccessDateStamp;

    private short reservedForFAT32 = 0;

    //最后写时间，秒级时间戳
    private int lastWriteTimestamp;

    //指向该文件/文件夹起始 cluster。如果是文件，cluster 里保存的是这个文件的第一部分数据；如果是文件夹，cluster 里保存改文件的子条目项
    private byte[] startingCluster = new byte[2];

    //如果是文件，表示文件字节数，最大 2 的 32 次方。如果不是文件，此值为 0
    private int fileSize;

    public DirectoryEntityStruct() {
    }

    //字节大小：32个字节
    public static final int ENTITY_SIZE = 32;

    /**
     * 格式化：预留63个sector字节大小的磁盘空间
     */
    public static byte[] format() {
        int count = BootSectorStruct.getInstance().getRootEntitiesCount();
        return new byte[count * ENTITY_SIZE];
    }

    public byte[] toByteArray() {
        byte[] buffer = new byte[ENTITY_SIZE];
        System.arraycopy(this.getFilename(), 0, buffer, 0x00, 8);
        System.arraycopy(this.getFilenameExtension(), 0, buffer, 0x08, 3);
        buffer[0x0B] = this.getAttribute();
        buffer[0x0C] = this.getReservedForWindowsNT();
        buffer[0x0D] = this.getCreation();
        System.arraycopy(SizeUtil.longToByteArray(this.getCreationTimestamp(), 4), 0, buffer, 0x0E, 4);
        System.arraycopy(SizeUtil.longToByteArray(this.getLastAccessDateStamp(), 2), 0, buffer, 0x12, 2);
        System.arraycopy(SizeUtil.longToByteArray(this.getReservedForFAT32(), 2), 0, buffer, 0x14, 2);
        System.arraycopy(SizeUtil.longToByteArray(this.getLastWriteTimestamp(), 4), 0, buffer, 0x16, 4);
        System.arraycopy(this.getStartingCluster(), 0, buffer, 0x1A, 2);
        System.arraycopy(SizeUtil.longToByteArray(this.getFileSize(), 4), 0, buffer, 0x1C, 4);
        return buffer;
    }

    public static DirectoryEntityStruct byteToObj(byte[] byteArray) {
        DirectoryEntityStruct entityStruct = new DirectoryEntityStruct();
        entityStruct.setFilename(Arrays.copyOfRange(byteArray, 0x00, 0x08));
        entityStruct.setFilenameExtension(Arrays.copyOfRange(byteArray, 0x08, 0x0B));
        entityStruct.setAttribute(byteArray[0x0B]);
        entityStruct.setReservedForWindowsNT(byteArray[0x0C]);
        entityStruct.setCreation(byteArray[0x0D]);
        entityStruct.setCreationTimestamp((int) SizeUtil.byteArrayToLong(Arrays.copyOfRange(byteArray, 0x0E, 0x12), 4));
        entityStruct.setLastAccessDateStamp((short) SizeUtil.byteArrayToLong(Arrays.copyOfRange(byteArray, 0x12, 0x14), 2));
        entityStruct.setReservedForFAT32((short) SizeUtil.byteArrayToLong(Arrays.copyOfRange(byteArray, 0x14, 0x16), 2));
        entityStruct.setLastWriteTimestamp((int) SizeUtil.byteArrayToLong(Arrays.copyOfRange(byteArray, 0x16, 0x1A), 4));
        entityStruct.setStartingCluster(Arrays.copyOfRange(byteArray, 0x1A, 0x1C));
        entityStruct.setFileSize((int) SizeUtil.byteArrayToLong(Arrays.copyOfRange(byteArray, 0x1C, 0x20), 4));

        return entityStruct;
    }
}
