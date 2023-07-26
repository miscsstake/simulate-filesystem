package com.eaglesoup.fs;

import com.eaglesoup.device.IDisk;

public interface IFileSystem {
    int WRITE = 1;
    int APPEND = 2;
    int READ = 0;

    /**
     * =======逻辑磁盘操作=======
     */
    void mount(IDisk disk);

    void umount();

    void format();

    /**
     * =======文件条目相关操作=======
     */
    boolean createDirectory(UnixFile file);

    int length(UnixFile file);

    boolean exist(UnixFile file);

    boolean isDir(UnixFile file);

    long lastModifiedTime(UnixFile file);

    UnixFile[] listPaths(String abstractPath);

    void delete(String abstractPath);

    /**
     * =======默认特性=======
     */
    String getDefaultRootPath();

    String getDefaultSeparator();

    /**
     * =======文件操作=======
     */
    UnixFileDescriptor open(String abstractPath);

    UnixFileDescriptor open(String abstractPath, int mode);

    UnixFileDescriptor open(UnixFile file);

    UnixFileDescriptor open(UnixFile file, int mode);

    int read(UnixFileDescriptor fd);

    void write(UnixFileDescriptor fd, int b);

    void flush(UnixFileDescriptor fd);

    void close(UnixFileDescriptor fd);
}
