package com.eaglesoup.applayer.bin.inner;

import com.eaglesoup.applayer.bin.base.BaseCommand;
import picocli.CommandLine;

import java.io.*;

@CommandLine.Command(name = "echo", mixinStandardHelpOptions = true, description = "模拟linux的echo命令")
public class EchoCommand extends BaseCommand {
    @CommandLine.Parameters(index = "0..*", description = "输出的内容")
    private String message;

    @Override
    public void call0(InputStream in, OutputStream out) {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
            bw.write(this.message);
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
