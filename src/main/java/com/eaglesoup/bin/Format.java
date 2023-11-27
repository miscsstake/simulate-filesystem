package com.eaglesoup.bin;

import com.eaglesoup.bin.base.ShellBaseCommand;
import com.eaglesoup.fs.UnixFile;
import com.eaglesoup.os.MosOs;
import picocli.CommandLine;

import java.io.*;

@CommandLine.Command(name = "format", description = "格式化")
public class Format extends ShellBaseCommand {
    public Format(InputStream in, OutputStream out, UnixFile curPath) {
        super(in, out, curPath);
    }


    @Override
    public Integer call0() {
        MosOs.fileSystem().format();
        this.curPath = new UnixFile("/");
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(this.out))) {
            bw.write("disk format success");
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            return -1;
        }
        return 0;
    }
}
