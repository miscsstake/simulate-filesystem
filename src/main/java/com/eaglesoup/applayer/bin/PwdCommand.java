package com.eaglesoup.applayer.bin;

import com.eaglesoup.applayer.bin.base.BaseCommand;
import picocli.CommandLine;

import java.io.*;

@CommandLine.Command(name = "pwd", helpCommand = true, description = "显示当前目录路径")
public class PwdCommand extends BaseCommand {

    public void call0(InputStream in, OutputStream out) {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
            bw.write(parent.curPath.get().getAbstractPath());
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
