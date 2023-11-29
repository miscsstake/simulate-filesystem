package com.eaglesoup.applayer.bin;

import com.eaglesoup.applayer.bin.base.BaseCommand;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@CommandLine.Command(name = "clear", description = "清屏")
public class ClearCommand extends BaseCommand {

    public void call0(InputStream in, OutputStream out) {
        try {
            out.write("\033[H\033[2J".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
