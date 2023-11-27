package com.eaglesoup.bin;

import com.eaglesoup.bin.base.ShellBaseCommand;
import com.eaglesoup.fs.UnixFile;
import picocli.CommandLine;

import java.io.InputStream;
import java.io.OutputStream;

@CommandLine.Command(name = "cd", description = "切换目录")
public class Cd extends ShellBaseCommand {
    public Cd(InputStream in, OutputStream out, UnixFile curPath) {
        super(in, out, curPath);
    }

    @CommandLine.Parameters(description = "输入目录的路径")
    String path;


    @Override
    public Integer call0() {
        UnixFile file = new UnixFile(this.curPath, path);
        if (file.isDir()) {
            this.curPath = file;
        } else {
            throw new IllegalStateException("cd: no such file or directory: " + file.getAbstractPath());
        }
        return 0;
    }
}
