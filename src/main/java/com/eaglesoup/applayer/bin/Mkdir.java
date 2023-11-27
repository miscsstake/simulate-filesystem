package com.eaglesoup.applayer.bin;

import com.eaglesoup.applayer.bin.base.ShellBaseCommand;
import com.eaglesoup.fs.UnixFile;
import picocli.CommandLine;

import java.io.InputStream;
import java.io.OutputStream;

@CommandLine.Command(name = "mkdir", mixinStandardHelpOptions = true, description = "创建目录")
public class Mkdir extends ShellBaseCommand {

    public Mkdir(InputStream in, OutputStream out, UnixFile curPath) {
        super(in, out, curPath);
    }

    @CommandLine.Parameters(paramLabel = "目录名称")
    private String path;

    @Override
    public Integer call0() {
        UnixFile file = new UnixFile(this.curPath, path);
        file.mkdir();
        return 0;
    }
}
