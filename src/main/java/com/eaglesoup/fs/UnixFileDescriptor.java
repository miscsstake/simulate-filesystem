package com.eaglesoup.fs;

import lombok.Data;

@Data
public class UnixFileDescriptor {
    private final Integer fd;

    public UnixFileDescriptor(Integer fd) {
        this.fd = fd;
    }

}
