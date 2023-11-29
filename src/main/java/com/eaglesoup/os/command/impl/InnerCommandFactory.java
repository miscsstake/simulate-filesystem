package com.eaglesoup.os.command.impl;

import com.eaglesoup.applayer.bin.base.ShellBaseCommand2;
import com.eaglesoup.fs.UnixFile;
import com.eaglesoup.os.command.ICommandExec;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

public class InnerCommandFactory implements ICommandExec {
    @Override
    public int exec(InputStream in, OutputStream out, AtomicReference<UnixFile> curr, String[] args) throws IOException {
        if (args.length == 0) {
            return 0;
        }
        String commandName = args[0].toLowerCase();
        CommandLine commandLine = new CommandLine(new ShellBaseCommand2(in, out, curr, args));
        if (commandLine.getSubcommands().containsKey(commandName)) {
            //内置命令
            commandLine.execute(args);
        } else {
            //外部命令
            UnixFile unixFile;
            if (commandName.startsWith("/")) {
                unixFile = new UnixFile(commandName);
            } else {
                unixFile = new UnixFile(curr.get(), commandName);
            }
            if (unixFile.exist()) {
                commandLine = new CommandLine(new ShellBaseCommand2(in, out, curr, args));
                String[] newArgs = new String[args.length + 1];
                newArgs[0] = "default";
                System.arraycopy(args, 0, newArgs, 1, args.length);
                commandLine.execute(newArgs);
            } else {
                out.write("命令不存在!!!".getBytes());
                out.flush();
            }
        }
        return 0;
    }
}
