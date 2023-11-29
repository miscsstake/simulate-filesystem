package com.eaglesoup.applayer.bin.base;

import com.eaglesoup.fs.IFileSystem;
import com.eaglesoup.fs.UnixFile;
import com.eaglesoup.fs.UnixFileOutputStream;
import com.eaglesoup.os.MosOs;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

public class BaseCommand implements Callable<Integer> {
    @CommandLine.ParentCommand
    protected ShellBaseCommand2 parent;

    @Override
    public Integer call() {
        this.call0(parent.in, parent.out);
        parent.afterCommand();
        return 0;
    }

    public void call0(InputStream inputStream, OutputStream outputStream) {

    }

    @CommandLine.Command(name = ">")
    public void write(@CommandLine.Parameters(paramLabel = "path") String path) {
        redirect(path, IFileSystem.WRITE);
    }

    @CommandLine.Command(name = ">>")
    public void append(@CommandLine.Parameters(paramLabel = "path") String path) {
        redirect(path, IFileSystem.APPEND);
    }

    private void redirect(String path, int mode) {
        UnixFile file = new UnixFile(parent.curPath.get(), path);
        if (file.exist()) {
            if (file.isDir()) {
                throw new IllegalStateException("zsh: is a directory: " + file.getAbstractPath());
            }
        }
        OutputStream outputStream = new UnixFileOutputStream(MosOs.fileSystem().open(file.getAbstractPath(), mode));
        this.call0(parent.in, outputStream);
        try {
            outputStream.flush();
            parent.afterCommand();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
