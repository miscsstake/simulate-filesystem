package com.eaglesoup.core;

import com.eaglesoup.util.LockUtil;

import java.io.*;

public class FAT16XDisk2 implements IDisk2 {
    private final RandomAccessFile disk;
    private static final FAT16XDisk2 instance = new FAT16XDisk2("/tmp/a.txt");

    public FAT16XDisk2(String path) {
        try {
            disk = new RandomAccessFile(path, "rw");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static FAT16XDisk2 getInstance() {
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
