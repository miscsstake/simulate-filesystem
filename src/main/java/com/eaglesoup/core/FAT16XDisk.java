package com.eaglesoup.core;

import com.eaglesoup.util.LockUtil;

import java.io.*;

public class FAT16XDisk implements IDisk {
    private final RandomAccessFile disk;
    private static final FAT16XDisk instance = new FAT16XDisk("/tmp/a.txt");

    public FAT16XDisk(String path) {
        try {
            disk = new RandomAccessFile(path, "rw");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static FAT16XDisk getInstance() {
        return instance;
    }

    @Override
    public byte[] readSector(int sectorIdx) {
        return LockUtil.rLock(() -> {
            byte[] sectorData = new byte[sectorSize()];
            try {
                disk.seek((long) sectorIdx * sectorSize());
                disk.readFully(sectorData);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return sectorData;
        });
    }

    @Override
    public void writeSector(int sectorIdx, byte[] sectorData) {
        LockUtil.wLock(() -> {
            try {
                disk.seek((long) sectorIdx * sectorSize());
                disk.write(sectorData);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return "success!";
        });
    }
}
