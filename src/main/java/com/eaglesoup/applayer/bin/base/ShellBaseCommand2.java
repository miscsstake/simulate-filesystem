package com.eaglesoup.applayer.bin.base;


import com.eaglesoup.applayer.bin.*;
import com.eaglesoup.fs.UnixFile;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

@CommandLine.Command(name = "bb", subcommands = {
        CatCommand.class,
        CdCommand.class,
        ClearCommand.class,
        EchoCommand.class,
        FormatCommand.class,
        GrepCommand.class,
        LsCommand.class,
        MkdirCommand.class,
        PwdCommand.class,
        RmCommand.class,
        TouchCommand.class
})
public class ShellBaseCommand2 implements Callable<Integer> {
    public final InputStream in;
    public final OutputStream out;
    public AtomicReference<UnixFile> curPath;
    public String[] args;

    public ShellBaseCommand2(InputStream in, OutputStream out, AtomicReference<UnixFile> curPath, String[] args) {
        this.in = in;
        this.out = out;
        this.curPath = curPath;
        this.args = args;
    }

    @Override
    public Integer call() {
        return 0;
    }

    public void afterCommand() {
        try {
            this.out.flush();
            if (this.out instanceof PipedOutputStream) {
                this.out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
