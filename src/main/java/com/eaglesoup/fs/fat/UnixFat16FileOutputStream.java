package com.eaglesoup.fs.fat;

import com.eaglesoup.device.IDisk;
import com.eaglesoup.fs.IFileSystem;
import com.eaglesoup.fs.UnixDirectory;
import com.eaglesoup.fs.fat.layout.DirectoryEntity;
import com.eaglesoup.fs.fat.layout.Fat16;

import java.io.OutputStream;

public class UnixFat16FileOutputStream extends OutputStream {
    private byte[] buffer;
    private IDisk disk;
    private Fat16 fat16;
    private UnixDirectory directory;
    private int clusterIdx;
    private int sectorIdx;
    private int pos;
    private int size;

    public UnixFat16FileOutputStream(IDisk disk, Fat16 fat16, UnixDirectory directory, int clusterIdx, int sectorIdx, int pos, int size) {
        this(disk, fat16, directory, clusterIdx, sectorIdx, pos, size, IFileSystem.WRITE);
    }

    public UnixFat16FileOutputStream(IDisk disk, Fat16 fat16, UnixDirectory directory, int clusterIdx, int sectorIdx, int pos, int size, int append) {
        this.directory = directory;
        this.disk = disk;
        this.fat16 = fat16;
        this.clusterIdx = clusterIdx;
        this.sectorIdx = sectorIdx;
        this.pos = pos;
        this.size = size;
        buffer = this.disk.readSector(clusterIdx * Layout.SECTORS_PER_CLUSTER + sectorIdx);
    }

    @Override
    public void write(int b) {

        if (pos >= buffer.length) {
            //数据写入
            flush();
            //写下一个Sector
            sectorIdx = nextSectorIdx();
            pos = 0;
        }

        buffer[pos++] = (byte) (b & 0xFF);
        size++;
    }

    private int nextSectorIdx() {
        buffer = new byte[Layout.SECTOR_SIZE];
        if (sectorIdx == (Layout.SECTORS_PER_CLUSTER - 1)) {
            clusterIdx = fat16.findNextFreeCluster(clusterIdx);
            return 0;
        }
        return sectorIdx + 1;
    }

    @Override
    public void flush() {
        this.disk.writeSector(clusterIdx * Layout.SECTORS_PER_CLUSTER + sectorIdx, buffer);
        DirectoryEntity original = directory.getOriginal();
        original.setFileSize(size);
        original.setLastWriteTimeStamp((int) (System.currentTimeMillis() / 1000));
        fat16.updateDirectory(directory);
    }

    @Override
    public void close() {
        flush();
    }
}
