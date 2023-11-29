package com.eaglesoup.os.command.impl;

import com.eaglesoup.applayer.bin.base.ShellBaseCommand2;
import com.eaglesoup.fs.UnixFile;
import com.eaglesoup.os.command.ICommandExec;
import picocli.CommandLine;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

public class InnerCommandFactory implements ICommandExec {
    @Override
    public int exec(InputStream in, OutputStream out, AtomicReference<UnixFile> curr, String[] args) {
        if (args.length == 0) {
            return 0;
        }
        CommandLine commandLine = new CommandLine(new ShellBaseCommand2(in, out, curr, args));
        commandLine.execute(args);
        return 0;
    }
}
