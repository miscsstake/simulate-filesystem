package com.eaglesoup.applayer.bin;

import com.eaglesoup.applayer.bin.base.BaseCommand;
import com.eaglesoup.fs.UnixFile;
import com.eaglesoup.os.MosOs;
import picocli.CommandLine;

import java.io.*;

@CommandLine.Command(name = "format", description = "格式化")
public class FormatCommand extends BaseCommand {
    public void call0(InputStream in, OutputStream out) {
        MosOs.fileSystem().format();
        parent.curPath.set(new UnixFile("/"));
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
            bw.write("disk format success");
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
