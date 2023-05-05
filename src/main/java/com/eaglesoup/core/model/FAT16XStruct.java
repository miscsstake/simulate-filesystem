package com.eaglesoup.core.model;

import com.eaglesoup.util.FATUtil;
import com.eaglesoup.util.SizeUtil;

/**
 * 每一个条目大小 2 bytes, 表示了对应 cluster 的存储状态
 * 1. 第一个条目用来存储保留区所在的簇
 * 2. 还需要条目用来存储fat本身的大小，计算公式为：
 * 2.1: 2个字节最大可以表示 2^16 = 65536个簇
 * 2.2: fat的大小 = (65536 * 2)
 * 2.3: fat占用的字节大小相当于： (65536 * 2) / (512 * 64) = 4个簇
 */
public class FAT16XStruct {
    //每个条目大小为 2 字节
    public static final int PER_FAT_SIZE = 2;
    private static final FAT16XStruct instance = new FAT16XStruct();

    public static FAT16XStruct getInstance() {
        return instance;
    }

    private FAT16XStruct() {

    }

    public byte[] format() {
        BootSectorStruct bootSectorStruct = BootSectorStruct.getInstance();
        //fat大小为2个字节，能存储的2^16 = 65536 个cluster;
        int fatSize = FATUtil.fatSize();
        byte[] fatCopies = new byte[fatSize];

        int size = FATUtil.bootSectorSize() + FATUtil.fatSize() + FATUtil.rootDirectorySize();
        int count = size / (bootSectorStruct.getPerClusterSectors() * bootSectorStruct.getPerSectorBytes());
        //第一个fat: 0xFFF8, 第二个fat: 0xFFFF;
        System.arraycopy(SizeUtil.longToByteArray(0xFFF8, Short.BYTES), 0, fatCopies, 0, Short.BYTES);
        System.arraycopy(SizeUtil.longToByteArray(0xFFFF, Short.BYTES), 0, fatCopies, Short.BYTES, Short.BYTES);
        //从第三个开始
        for (int i = 2; i < count; i++) {
            System.arraycopy(SizeUtil.longToByteArray(0xFFF8, Short.BYTES), 0, fatCopies, i * Short.BYTES, Short.BYTES);
        }
        return fatCopies;
    }
}
