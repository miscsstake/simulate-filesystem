package com.eaglesoup.os.command;

import com.eaglesoup.fs.UnixFile;
import com.eaglesoup.os.command.impl.InnerCommandFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

public class CommandResolveFactory {

    public int execute(InputStream in, OutputStream out, AtomicReference<UnixFile> curr, String[] args) {
        if (args.length == 0) {
            return 0;
        }
        try {
            return new InnerCommandFactory().exec(in, out, curr, args);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
