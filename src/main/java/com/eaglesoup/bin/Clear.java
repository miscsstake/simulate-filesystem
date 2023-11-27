package com.eaglesoup.bin;

import com.eaglesoup.bin.base.ShellBaseCommand;
import com.eaglesoup.fs.UnixFile;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@CommandLine.Command(name = "clear", description = "清屏")
public class Clear extends ShellBaseCommand {
    public Clear(InputStream in, OutputStream out, UnixFile curPath) {
        super(in, out, curPath);
    }

    @Override
    public Integer call0() {
        try {
            this.out.write("\033[H\033[2J".getBytes());
        } catch (IOException e) {
            return -1;
        }
        return 0;
    }
}
