package com.eaglesoup.fs.fat;

import com.eaglesoup.fs.UnixDirectory;
import lombok.Data;

@Data
public class UnixFat16FileDescriptor {
    private final Integer fd;
    private final UnixDirectory directory;
    private UnixFat16FileInputStream inputStream;
    private UnixFat16FileOutputStream outputStream;

    public UnixFat16FileDescriptor(Integer fd, UnixDirectory directory, UnixFat16FileInputStream inputStream, UnixFat16FileOutputStream outputStream) {
        this.fd = fd;
        this.directory = directory;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }
}
