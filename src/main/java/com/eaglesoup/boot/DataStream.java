package com.eaglesoup.boot;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

class DataStream {
    public InputStream is;
    public OutputStream os;

    public DataStream() {
        this.is = new PipedInputStream();
        this.os = new PipedOutputStream();
    }

    public DataStream(PipedInputStream is, PipedOutputStream os) {
        this.is = is;
        this.os = os;
    }
}

