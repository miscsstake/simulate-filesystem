package com.eaglesoup.service;

/**
 * 抽象文件系统层
 */
public interface IFileSystem {
    /*** 文件操作 ****/
    Object open();

    Object read();

    Object write();

    /*** 目录操作 ****/
    Object openDir();

    Object listFiles();

    void close();


}
