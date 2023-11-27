package com.eaglesoup.bin;

import com.eaglesoup.bin.base.ShellBaseCommand;
import com.eaglesoup.fs.UnixFile;
import picocli.CommandLine;

import java.io.InputStream;
import java.io.OutputStream;

@CommandLine.Command(name = "rm", helpCommand = true, description = "删除文件")
public class Rm extends ShellBaseCommand {

    @CommandLine.Parameters(paramLabel = "path")
    private String path;

    public Rm(InputStream in, OutputStream out, UnixFile curPath) {
        super(in, out, curPath);
    }

    @Override
    public Integer call0() {
        UnixFile file = new UnixFile(this.curPath, path);
        file.delete();
        return 0;
    }
}
