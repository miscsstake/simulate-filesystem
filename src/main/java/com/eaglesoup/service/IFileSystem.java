package com.eaglesoup.service;

import com.eaglesoup.core.model.DirectoryEntityStruct;
import com.eaglesoup.dto.FileModeDTO;
import com.eaglesoup.enums.FileModeEnum;

import java.util.List;

/**
 * 抽象文件系统层
 */
public interface IFileSystem {
    /*** 文件操作 ****/
    FileModeDTO open(String fileName, FileModeEnum fileModeEnum);

    void changeFileMode(FileModeEnum fileModeEnum);

    boolean exist();

    byte[] read();

    boolean write(byte[] bytes);

    /*** 目录操作 ****/
    boolean mkdir();

    List<DirectoryEntityStruct> listFiles();

    void close();

    void format();

    boolean rm();
}
