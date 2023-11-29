package com.eaglesoup.fs;

import com.eaglesoup.os.MosOs;

import java.io.InputStream;

public class UnixFileInputStream extends InputStream {

    private UnixFileDescriptor fd;

    public UnixFileInputStream(UnixFile file) {
        this.fd = MosOs.fileSystem().open(file);
    }

    public UnixFileInputStream(UnixFileDescriptor fd) {
        this.fd = fd;
    }

    @Override
    public int read() {
        return MosOs.fileSystem().read(fd);
    }
}
