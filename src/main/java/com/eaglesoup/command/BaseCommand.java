package com.eaglesoup.command;

import com.eaglesoup.fs.IFileSystem;
import com.eaglesoup.fs.UnixFile;
import com.eaglesoup.fs.UnixFileOutputStream;
import com.eaglesoup.os.MosOs;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.Callable;

public abstract class BaseCommand implements Callable<Integer> {
    protected InputStream in;
    protected OutputStream out;
    protected OutputStream err;

    @CommandLine.ParentCommand
    protected ShellCommand parent;

    protected void print(char msg) {
        try {
            out.write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void print(String msg) {
        try {
            out.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void println(String msg) {
        print(msg + "\n");
    }

    protected abstract Integer call0() throws IOException;

    @CommandLine.Command(name = ">")
    public void write(@CommandLine.Parameters(paramLabel = "path") String path) throws IOException {
        redirect(path, IFileSystem.WRITE);
    }

    @CommandLine.Command(name = ">>")
    public void append(@CommandLine.Parameters(paramLabel = "path") String path) throws IOException {
        redirect(path, IFileSystem.APPEND);
    }

    private void redirect(String path, int mode) throws IOException {
        UnixFile file = new UnixFile(parent.curr, path);
        if (file.exist()) {
            if (file.isDir()) {
                throw new IllegalStateException("zsh: is a directory: " + file.getAbstractPath());
            }
        }
        out = new PrintStream(new UnixFileOutputStream(MosOs.fileSystem().open(file.getAbstractPath(), mode)));
        this.call0();
        out.flush();
    }

    @Override
    public Integer call() throws Exception {
        out = parent.out;
        Integer result = call0();
        out.flush();
        return result;
    }
}
