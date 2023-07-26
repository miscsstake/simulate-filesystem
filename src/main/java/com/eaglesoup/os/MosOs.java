package com.eaglesoup.os;

import com.eaglesoup.fs.IFileSystem;
import com.eaglesoup.fs.UnixFileSystem;

public class MosOs {
    public static IFileSystem fileSystem() {
        return UnixFileSystem.getDefaultFileSystem();
    }
}
