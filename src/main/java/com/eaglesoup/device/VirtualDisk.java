package com.eaglesoup.device;

import com.eaglesoup.fs.fat.Layout;
import lombok.SneakyThrows;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class VirtualDisk implements IDisk {
    private final RandomAccessFile disk;
    private final long fileSize = (long) sectorSize() * sectorCount();

    public VirtualDisk() {
        try {
            disk = new RandomAccessFile("/tmp/a.txt", "rwd");
            disk.setLength(fileSize);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] readSector(int sectorIdx) {
        byte[] sectorData = new byte[sectorSize()];
        try {
            disk.seek((long) sectorIdx * sectorSize());
            disk.read(sectorData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sectorData;
    }

    @Override
    public synchronized void writeSector(int sectorIdx, byte[] sectorData) {
        try {
            disk.seek((long) sectorIdx * sectorSize());
            disk.write(sectorData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void format() {
        try {
            disk.setLength(fileSize);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            disk.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
