package com.eaglesoup.core.model;

import com.eaglesoup.util.SizeUtil;

import java.util.LinkedList;

/**
 * 每一个条目大小 2 bytes, 表示了对应 cluster 的存储状态
 * 1. 第一个条目用来存储保留区所在的簇
 * 2. 还需要条目用来存储fat本身的大小，计算公式为：
 * 2.1: 2个字节最大可以表示 2^16 = 65536个簇
 * 2.2: fat的大小 = (65536 * 2)
 * 2.3: fat占用的字节大小相当于： (65536 * 2) / (512 * 64) = 4个簇
 */
public class FAT16XStruct {
    LinkedList<Short> fatTable = new LinkedList<>();

    private static final FAT16XStruct instance = new FAT16XStruct();

    public static FAT16XStruct getInstance() {
        return instance;
    }

    private FAT16XStruct() {

    }

    public byte[] format() {
        //fat大小为2个字节，能存储的2^16 = 65536 个cluster;
        int size = 65536 * 2 * BootSectorStruct.getInstance().getFatCopiesCount();
        byte[] fatCopies = new byte[size];

        //第一个fat: 0xFFF8
        short tmp = (short) 0xFFF8;
        System.arraycopy(SizeUtil.longToByteArray(tmp, Short.BYTES), 0, fatCopies, 0, Short.BYTES);
        return fatCopies;
    }
}
