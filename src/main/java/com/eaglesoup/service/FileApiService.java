package com.eaglesoup.service;

import com.eaglesoup.core.model.DirectoryEntityStruct;
import com.eaglesoup.enums.FileModeEnum;
import com.eaglesoup.fs.IFileSystem2;

import java.util.List;

/**
 * 文件api层
 */
public class FileApiService {
    /**
     * 抽象文件系统层
     */
    private static final IFileSystem2 fileSystem = new FAT16FileSystem();

    public FileApiService(String filename) {
        fileSystem.open(filename, null);
    }

    public boolean exists() {
        fileSystem.changeFileMode(FileModeEnum.READ_MODE);
        return fileSystem.exist();
    }

    public void format() {
        fileSystem.changeFileMode(FileModeEnum.WRITE_MODE);
        fileSystem.format();
    }

    public void mkdir() {
        fileSystem.changeFileMode(FileModeEnum.WRITE_MODE);
        fileSystem.mkdir();
    }

    public void createNewFile() {
        fileSystem.changeFileMode(FileModeEnum.APPEND_MODE);
        fileSystem.write(new byte[0]);
    }

    public List<DirectoryEntityStruct> listFiles() {
        fileSystem.changeFileMode(FileModeEnum.READ_MODE);
        return fileSystem.listFiles();
    }

    public void writeAppend(byte[] buffer) {
        fileSystem.changeFileMode(FileModeEnum.APPEND_MODE);
        fileSystem.write(buffer);
    }

    public void write(byte[] buffer) {
        fileSystem.changeFileMode(FileModeEnum.WRITE_MODE);
        fileSystem.write(buffer);
    }

    public byte[] read() {
        fileSystem.changeFileMode(FileModeEnum.READ_MODE);
        return fileSystem.read();
    }

    public void delete() {
        fileSystem.changeFileMode(FileModeEnum.WRITE_MODE);
        fileSystem.rm();
    }
}
