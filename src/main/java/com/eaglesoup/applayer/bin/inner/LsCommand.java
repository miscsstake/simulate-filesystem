package com.eaglesoup.applayer.bin.inner;

import com.eaglesoup.applayer.bin.base.BaseCommand;
import com.eaglesoup.fs.UnixFile;
import picocli.CommandLine;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@CommandLine.Command(name = "ls", aliases = "ll", mixinStandardHelpOptions = true, description = "查询文件列表")
public class LsCommand extends BaseCommand {
    @CommandLine.Parameters(index = "0..1", paramLabel = "path")
    private List<String> paths;

    public void call0(InputStream in, OutputStream out) {
        if (paths == null || paths.isEmpty()) {
            printPath(parent.curPath.get(), out);
        } else if (paths.size() == 1) {
            printPath(new UnixFile(parent.curPath.get(), paths.get(0)), out);
        } else {
            for (String path : paths) {
                UnixFile file = new UnixFile(parent.curPath.get(), path);
                printPath(file, out);
            }
        }
    }

    private void printPath(UnixFile file, OutputStream out) {
        String format = "%11s %10s %s";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
            if (file.exist()) {
                for (UnixFile p : file.listFiles()) {
                    String filename = p.isDir() ? p.getName() + "/" : p.getName();
                    bw.write(String.format(format, p.length(), sdf.format(new Date(p.lastModified())), filename));
                    bw.newLine();
                }
            } else {
                throw new IllegalArgumentException(file.getAbstractPath() + "文件不存在");
            }
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
