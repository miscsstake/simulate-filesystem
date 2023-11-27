package com.eaglesoup.applayer.bin.base;

import com.eaglesoup.os.boot.UnixProcess;
import com.eaglesoup.fs.UnixFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

public abstract class ShellBaseCommand implements UnixProcess, Callable<Integer> {
    protected final InputStream in;
    protected final OutputStream out;
    protected UnixFile curPath;

    public ShellBaseCommand(InputStream in, OutputStream out, UnixFile curPath) {
        this.in = in;
        this.out = out;
        this.curPath = curPath;
    }

    @Override
    public InputStream getInputStream() {
        return this.in;
    }

    @Override
    public OutputStream getOutputStream() {
        return this.out;
    }

    @Override
    public OutputStream getErrorStream() {
        return null;
    }

    @Override
    public void exitCallback() {
        try {
            this.in.close();
            this.out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getLineReaderPrompt() {
        return null;
    }

    @Override
    public UnixFile getCurPath() {
        return this.curPath;
    }

    @Override
    public Integer call() {
        return execute();
    }

    public abstract Integer call0();


//    @CommandLine.Command(name = ">>")
//    public void append(@CommandLine.Parameters(paramLabel = "path") String path) {
//        redirect(path, IFileSystem.APPEND);
//    }

//    protected void redirect(String path, int mode) {
//        UnixFile file = new UnixFile(this.curPath, path);
//        if (file.exist()) {
//            if (file.isDir()) {
//                throw new IllegalStateException("zsh: is a directory: " + file.getAbstractPath());
//            }
//        }
//        new UnixFileOutputStream(MosOs.fileSystem().open(file.getAbstractPath(), mode));
////        out = new PrintStream(new UnixFileOutputStream(MosOs.fileSystem().open(file.getAbstractPath(), mode)));
//        execute();
//    }

    private Integer execute() {
        int exitCode = this.call0();
        this.exitCallback();
        return exitCode;
    }
}
