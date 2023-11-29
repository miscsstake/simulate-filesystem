package com.eaglesoup.fs.fat;

import lombok.Data;

@Data
public class Layout {
    /**
     * ======= Attribute Byte ========
     */
    //Directory Entry类型：文件
    public static final byte FILE_MARK = 0x0;
    //Directory Entry类型：目录
    public static final byte DIR_MARK = 0x10;
    //Directory Entry类型：长文件
    public static final byte LFN_MARK = 0xF;

    /**
     * ======= LFN Entry: Ordinal Field ========
     */
    //Directory Entry类型：长文件最后一个
    public static final byte LFN_LAST_NUMBER = 0x40;
    public static final int LFN_FILE_LENGTH = 30;
    public static final int LONG_FILE_NAME_LENGTH = 8;

    /**
     * ======= 保留区 ========
     */
    public static final int SECTOR_SIZE = 512;
    public static final int SECTORS_PER_CLUSTER = 64;
    public static final int RESERVED_SECTORS = 1;
    public static final int NUMBER_OF_FAT_COPIES = 2;
    public static final int NUMBER_OF_POSSIBLE_ROOT_ENTRIES = 63;
    public static final int NUMBER_OF_ROOT_ENTRIES_COUNT = 1008;
    public static final int SECTORS_PER_FAT = 256;
    public static final int FAT_ENTRY_SIZE = 2;
    public static final int FAT_ENTRIES_COUNT = SECTORS_PER_FAT * SECTOR_SIZE / FAT_ENTRY_SIZE; //65536个条目
    public static final int DIRECTORY_ENTRY_SIZE = 32;
    public static final int NUMBER_OF_CLUSTER_ENTRIES_COUNT = SECTORS_PER_CLUSTER * SECTOR_SIZE / DIRECTORY_ENTRY_SIZE;
    /**
     * =======REGION OFFSET========
     */
    public static final int VOLUME_START = 0;
    public static final int RESERVED_REGION_START = VOLUME_START;
    public static final int FAT_REGION_START = RESERVED_REGION_START + RESERVED_SECTORS;
    public static final int ROOT_DIRECTORY_REGION_START = FAT_REGION_START + (NUMBER_OF_FAT_COPIES * SECTORS_PER_FAT); //根目录513: 1(保留区)+2*256(fat区)
    public static final int DATA_REGION_START = ROOT_DIRECTORY_REGION_START + NUMBER_OF_POSSIBLE_ROOT_ENTRIES; //1(保留区)+2*256(fat区) + 63(root) = 576
    public static final int FAT_FREE_START = DATA_REGION_START / SECTORS_PER_CLUSTER; //从第9个cluster开始
    /**
     * =======REGION SECTOR SIZE========
     */
    public static final int RESERVED_REGION_SIZE = RESERVED_SECTORS; //保留区：共1个sector
    public static final int FAT_REGION_SIZE = NUMBER_OF_FAT_COPIES * SECTORS_PER_FAT; //fat区:共2*256=512个sector
    public static final int ROOT_DIRECTORY_REGION_SIZE = NUMBER_OF_POSSIBLE_ROOT_ENTRIES; //根目录区：共63个sector
    public static final int NUMBER_OF_LIST_CLUSTER_SIZE = 9; //（RESERVED_REGION_SIZE+FAT_REGION_SIZE+ROOT_DIRECTORY_REGION_SIZE）/ 64
    public static final int DATA_REGION_SIZE = (FAT_ENTRIES_COUNT - NUMBER_OF_LIST_CLUSTER_SIZE) * Layout.SECTORS_PER_CLUSTER;
}
