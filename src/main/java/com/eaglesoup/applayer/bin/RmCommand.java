package com.eaglesoup.applayer.bin;

import com.eaglesoup.applayer.bin.base.BaseCommand;
import com.eaglesoup.fs.UnixFile;
import picocli.CommandLine;

import java.io.InputStream;
import java.io.OutputStream;

@CommandLine.Command(name = "rm", helpCommand = true, description = "删除文件")
public class RmCommand extends BaseCommand {

    @CommandLine.Parameters(paramLabel = "path")
    private String path;

    public void call0(InputStream in, OutputStream out) {
        UnixFile file = new UnixFile(parent.curPath.get(), path);
        file.delete();
    }
}
