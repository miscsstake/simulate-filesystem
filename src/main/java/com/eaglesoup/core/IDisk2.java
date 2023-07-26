package com.eaglesoup.core;

import java.io.IOException;

public interface IDisk2 {
    /**
     * 读取一个指定扇区的数据。
     *
     * @param sectorIdx 扇区索引，起始索引为0，终止索引为 {@code sectorCount()-1}
     * @return 扇区数据，返回的字节数组长度必须等于{@code sectorSize()}
     */
    byte[] readSector(int sectorIdx) throws IOException;

    /**
     * 写一个指定扇区。
     *
     * @param sectorIdx  扇区索引，起始索引为0，终止索引为 {@code sectorCount()-1}
     * @param sectorData 待写入的数据. 长度必须等于{@code sectorSize()}
     */
    void writeSector(int sectorIdx, byte[] sectorData) throws IOException;

    /**
     * 磁盘每个扇区的大小，固定为 512 字节
     *
     * @return int
     */
    default int sectorSize() {
        return 512;
    }

    /**
     * 磁盘扇区数量，固定为 2G/512
     */
    default long sectorCount() {
        return diskSize() / 512;
    }

    /**
     * 2G大小的磁盘空间
     *
     * @return int
     */
    default long diskSize() {
        return 2L * 1024 * 1024 * 1024;
    }
}
