package com.eaglesoup.fs;

import com.eaglesoup.os.MosOs;

import java.io.OutputStream;

public class UnixFileOutputStream extends OutputStream {

    private final UnixFileDescriptor fd;

    public UnixFileOutputStream(UnixFile file) {
        this(file, IFileSystem.WRITE);
    }

    /**
     * @param file
     * @param append {@link IFileSystem}
     */
    public UnixFileOutputStream(UnixFile file, int append) {
        this.fd = MosOs.fileSystem().open(file, append);
    }

    public UnixFileOutputStream(UnixFileDescriptor fd) {
        this.fd = fd;
    }

    @Override
    public void write(int b) {
        MosOs.fileSystem().write(fd, b);
    }

    @Override
    public void flush() {
        MosOs.fileSystem().flush(fd);
    }

    @Override
    public void close() {
        MosOs.fileSystem().close(fd);
    }
}
