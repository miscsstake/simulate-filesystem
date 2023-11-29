package com.eaglesoup.applayer.bin;

import com.eaglesoup.applayer.bin.base.ShellBaseCommand;
import com.eaglesoup.fs.UnixFile;
import picocli.CommandLine;

import java.io.*;

@CommandLine.Command(name = "pwd", helpCommand = true, description = "显示当前目录路径")
public class Pwd extends ShellBaseCommand {
    public Pwd(InputStream in, OutputStream out, UnixFile curPath) {
        super(in, out, curPath);
    }

    @Override
    public Integer call0() {
        try  {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(this.out));
            bw.write(this.curPath.getAbstractPath());
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            return -1;
        }
        return 0;
    }
}
