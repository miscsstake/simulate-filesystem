package com.eaglesoup.os.command;

import com.eaglesoup.fs.UnixFile;
import com.eaglesoup.os.command.impl.ExternalCommandFactory;
import com.eaglesoup.os.command.impl.InnerCommandFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

public class CommandResolveFactory {

    //    public int execute(InputStream in, OutputStream out, UnixFile curr, String[] args) {
    public int execute(InputStream in, OutputStream out, AtomicReference<UnixFile> curr, String[] args) {
        if (args.length == 0) {
            return 0;
        }
        String commandName = args[0];
        UnixFile unixFile;
        if (commandName.startsWith("/")) {
            unixFile = new UnixFile(commandName);
        } else {
            unixFile = new UnixFile(curr.get(), commandName);
        }
        if (unixFile.exist()) {
            AtomicReference<UnixFile> fileRef = new AtomicReference<>(unixFile);
            return new ExternalCommandFactory().exec(in, out, fileRef, args);
        } else {
            //内置命令
            return new InnerCommandFactory().exec(in, out, curr, args);
        }
    }
}
