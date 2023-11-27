package com.eaglesoup.boot;

import com.eaglesoup.fs.UnixFile;

import java.io.InputStream;
import java.io.OutputStream;

public interface UnixProcess {
    InputStream getInputStream();

    OutputStream getOutputStream();

    OutputStream getErrorStream();

    void exitCallback();

    String getLineReaderPrompt();

    UnixFile getCurPath();
}
