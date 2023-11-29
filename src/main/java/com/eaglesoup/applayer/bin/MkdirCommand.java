package com.eaglesoup.applayer.bin;

import com.eaglesoup.applayer.bin.base.BaseCommand;
import com.eaglesoup.fs.UnixFile;
import picocli.CommandLine;

import java.io.InputStream;
import java.io.OutputStream;


@CommandLine.Command(name = "mkdir", mixinStandardHelpOptions = true, description = "创建目录")
public class MkdirCommand extends BaseCommand {
    @CommandLine.Parameters(paramLabel = "目录名称")
    private String path;

    public void call0(InputStream in, OutputStream out) {
        UnixFile file = new UnixFile(parent.curPath.get(), path);
        file.mkdir();
    }
}
