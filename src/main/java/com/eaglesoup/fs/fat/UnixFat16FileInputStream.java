package com.eaglesoup.fs.fat;

import com.eaglesoup.device.IDisk;
import com.eaglesoup.fs.fat.layout.Fat16;

import java.io.InputStream;

public class UnixFat16FileInputStream extends InputStream {
    private byte[] buffer;
    private final IDisk disk;
    private final Fat16 fat16;
    private int clusterIdx;
    private int sectorIdx;
    private int pos;
    private final int size;
    private int count;

    public UnixFat16FileInputStream(IDisk disk, Fat16 fat16, int clusterIdx, int sectorIdx, int pos, int size) {
        this.disk = disk;
        this.fat16 = fat16;
        this.clusterIdx = clusterIdx;
        this.sectorIdx = sectorIdx;
        this.pos = pos;
        this.size = size;
        this.buffer = new byte[0];
    }

    @Override
    public void close() {

    }

    @Override
    public int read() {
        if (count >= size) {
            return -1;
        }

        if (pos >= buffer.length) {
            this.buffer = this.disk.readSector(clusterIdx * Layout.SECTORS_PER_CLUSTER + sectorIdx);
            sectorIdx = nextSectorIdx();
            pos = 0;
        }
        count++;
        return buffer[pos++] & 0xff;
    }

    private int nextSectorIdx() {
        if (sectorIdx == (Layout.SECTORS_PER_CLUSTER - 1)) {
            clusterIdx = fat16.findNextCluster(clusterIdx);
            return 0;
        }
        return sectorIdx + 1;
    }
}
