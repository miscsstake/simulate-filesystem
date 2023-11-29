package com.eaglesoup.os.command;

import com.eaglesoup.fs.UnixFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

public interface ICommandExec {
    public int exec(InputStream in, OutputStream out, AtomicReference<UnixFile> curr, String[] args) throws IOException;
}
