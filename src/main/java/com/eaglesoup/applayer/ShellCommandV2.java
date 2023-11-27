package com.eaglesoup.applayer;

import com.eaglesoup.boot.UnixCommandExecutor;
import com.eaglesoup.boot.UnixProcess;
import com.eaglesoup.fs.UnixFile;
import com.eaglesoup.os.MosOs;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class ShellCommandV2 implements UnixProcess {
    Scanner scanner = new Scanner(getInputStream());

    public void execute() {
        new UnixCommandExecutor(this).fire();
    }

    @Override
    public InputStream getInputStream() {
        return System.in;
    }

    @Override
    public OutputStream getOutputStream() {
        return System.out;
    }

    @Override
    public OutputStream getErrorStream() {
        return System.err;
    }

    @Override
    public void exitCallback() {
        if (scanner != null) {
            scanner.close();
        }
    }

    @Override
    public String getLineReaderPrompt() {
        return scanner.nextLine();
    }

    @Override
    public UnixFile getCurPath() {
        return new UnixFile(MosOs.fileSystem().getDefaultRootPath());
    }
}
