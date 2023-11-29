package com.eaglesoup.applayer.bin;

import com.eaglesoup.applayer.bin.base.BaseCommand;
import com.eaglesoup.applayer.bin.base.ShellBaseCommand2;
import com.eaglesoup.fs.UnixFile;
import com.eaglesoup.fs.UnixFileInputStream;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@CommandLine.Command(name = "cat", description = "切换目录")
public class CatCommand extends BaseCommand {
    @CommandLine.Parameters(description = "文件名称")
    String path;

    public void call0(InputStream in, OutputStream out) {
        UnixFile file = new UnixFile(parent.curPath.get(), path);
        try (UnixFileInputStream input = new UnixFileInputStream(file)) {
            int v;
            int lastv = 0;
            while ((v = input.read()) != -1) {
                lastv = v;
                out.write(v);
            }
            if (lastv != '\n') {
                out.write('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
