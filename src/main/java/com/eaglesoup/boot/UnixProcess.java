package com.eaglesoup.boot;

import org.apache.sshd.server.ExitCallback;

import java.io.InputStream;
import java.io.OutputStream;

public interface UnixProcess {
    InputStream getInputStream();

    OutputStream getOutputStream();

    OutputStream getErrorStream();

    ExitCallback getExitCallback();

    String getLineReaderPrompt();

    String getCurPath();
}
