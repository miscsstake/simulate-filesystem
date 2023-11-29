package com.eaglesoup.applayer.bin.inner;

import com.eaglesoup.applayer.bin.base.BaseCommand;
import com.eaglesoup.fs.UnixFile;
import com.eaglesoup.fs.UnixFileOutputStream;
import picocli.CommandLine;

import java.io.InputStream;
import java.io.OutputStream;

@CommandLine.Command(name = "touch", mixinStandardHelpOptions = true, description = "创建文件")
public class TouchCommand extends BaseCommand {
    @CommandLine.Parameters(paramLabel = "文件路径")
    private String path;

    public void call0(InputStream in, OutputStream out) {
        UnixFile file = new UnixFile(parent.curPath.get(), path);
        if (file.exist()) {
            throw new IllegalStateException(file.getAbstractPath() + " is already exist");
        }
        UnixFileOutputStream fileOutputStream = new UnixFileOutputStream(file);
        fileOutputStream.close();
    }
}
