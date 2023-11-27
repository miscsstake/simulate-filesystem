package com.eaglesoup.bin;

import com.eaglesoup.bin.base.ShellBaseCommand;
import com.eaglesoup.fs.UnixFile;
import com.eaglesoup.fs.UnixFileOutputStream;
import picocli.CommandLine;

import java.io.InputStream;
import java.io.OutputStream;

@CommandLine.Command(name = "touch", mixinStandardHelpOptions = true, description = "创建文件")
public class Touch extends ShellBaseCommand {
    public Touch(InputStream in, OutputStream out, UnixFile curPath) {
        super(in, out, curPath);
    }

    @CommandLine.Parameters(paramLabel = "文件路径")
    private String path;

    @Override
    public Integer call0() {
        UnixFile file = new UnixFile(this.curPath, path);
        if (file.exist()) {
            throw new IllegalStateException(file.getAbstractPath() + " is already exist");
        }
        UnixFileOutputStream fileOutputStream = new UnixFileOutputStream(file);
        fileOutputStream.close();
        return 0;
    }
}
