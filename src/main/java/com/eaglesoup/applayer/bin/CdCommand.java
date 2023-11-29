package com.eaglesoup.applayer.bin;

import com.eaglesoup.applayer.bin.base.BaseCommand;
import com.eaglesoup.fs.UnixFile;
import picocli.CommandLine;

import java.io.InputStream;
import java.io.OutputStream;

@CommandLine.Command(name = "cd", description = "切换目录")
public class CdCommand extends BaseCommand {
    @CommandLine.Parameters(description = "输入目录的路径")
    String path;

    public void call0(InputStream in, OutputStream out) {
        UnixFile file = new UnixFile(parent.curPath.get(), path);
        if (file.isDir()) {
            parent.curPath.set(file);
        } else {
            throw new IllegalStateException("cd: no such file or directory: " + file.getAbstractPath());
        }
    }
}
