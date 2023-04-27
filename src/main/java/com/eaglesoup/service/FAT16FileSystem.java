package com.eaglesoup.service;

import com.eaglesoup.dto.FileModeDTO;

/**
 * 具体的fat16文件系统层
 */
public class FAT16FileSystem implements IFileSystem {
    //设备层类：CoreFAT16XDiskService
    @Override
    public FileModeDTO open() {
        return null;
    }

    @Override
    public Object read() {
        return null;
    }

    @Override
    public Object write() {
        return null;
    }

    @Override
    public Object openDir() {
        return null;
    }

    @Override
    public Object listFiles() {
        return null;
    }

    @Override
    public void close() {

    }
}
