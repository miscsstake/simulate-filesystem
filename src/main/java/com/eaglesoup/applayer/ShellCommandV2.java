package com.eaglesoup.applayer;

import com.eaglesoup.boot.UnixCommandExecutor;
import com.eaglesoup.boot.UnixProcess;
import com.eaglesoup.os.MosOs;
import org.apache.sshd.server.ExitCallback;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class ShellCommandV2 implements UnixProcess {
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
    public ExitCallback getExitCallback() {
        return null;
    }

    @Override
    public String getLineReaderPrompt() {
        Scanner scanner = new Scanner(getInputStream());
        return scanner.nextLine();
    }

    @Override
    public String getCurPath() {
        return MosOs.fileSystem().getDefaultRootPath();
    }
}
