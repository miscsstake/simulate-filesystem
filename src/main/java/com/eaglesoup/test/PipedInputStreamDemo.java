package com.eaglesoup.test;

import com.eaglesoup.boot.UnixCommandExecutor;
import com.eaglesoup.device.VirtualDisk;
import com.eaglesoup.os.MosOs;

public class PipedInputStreamDemo {

    public static void main(String[] args) {
        MosOs.fileSystem().mount(new VirtualDisk());
        try {
            String rootPath = MosOs.fileSystem().getDefaultRootPath();
            UnixCommandExecutor shell = new UnixCommandExecutor(rootPath, System.in, System.out, System.err);
            shell.fire();
        } finally {
            MosOs.fileSystem().umount();
        }
    }
}
