package com.eaglesoup.bin;

import com.eaglesoup.bin.base.ShellBaseCommand;
import com.eaglesoup.fs.UnixFile;
import com.eaglesoup.fs.UnixFileInputStream;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@CommandLine.Command(name = "cat", mixinStandardHelpOptions = true, description = "查看文件内容")
public class Cat extends ShellBaseCommand {

    public Cat(InputStream in, OutputStream out, UnixFile curPath) {
        super(in, out, curPath);
    }

    @CommandLine.Parameters(description = "文件名称")
    String path;

    @Override
    public Integer call0() {
        UnixFile file = new UnixFile(this.curPath, path);
        try (UnixFileInputStream input = new UnixFileInputStream(file)) {
            int v;
            int lastv = 0;
            while ((v = input.read()) != -1) {
                lastv = v;
                this.out.write(v);
            }
            if (lastv != '\n') {
                this.out.write('\n');
            }
        } catch (IOException e) {
            return -1;
        }
        return 0;
    }
}
