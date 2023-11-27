package com.eaglesoup.bin;

import com.eaglesoup.bin.base.ShellBaseCommand;
import com.eaglesoup.fs.UnixFile;
import picocli.CommandLine;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@CommandLine.Command(name = "ls", aliases = "ll", mixinStandardHelpOptions = true, description = "查询文件列表")
public class Ls extends ShellBaseCommand {
    public Ls(InputStream in, OutputStream out, UnixFile curPath) {
        super(in, out, curPath);
    }

    @CommandLine.Parameters(index = "0..1", paramLabel = "path")
    private List<String> paths;

    @Override
    public Integer call0() {
        if (paths == null || paths.isEmpty()) {
            printPath(this.curPath);
        } else if (paths.size() == 1) {
            printPath(new UnixFile(this.curPath, paths.get(0)));
        } else {
            for (String path : paths) {
                UnixFile file = new UnixFile(this.curPath, path);
                printPath(file);
            }
        }
        return 0;
    }

    private void printPath(UnixFile file) {
        String format = "%11s %10s %s";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(this.out))) {
            if (file.exist()) {
                for (UnixFile p : file.listFiles()) {
                    String filename = p.isDir() ? p.getName() + "/" : p.getName();
                    bw.write(String.format(format, p.length(), sdf.format(new Date(p.lastModified())), filename));
                    bw.newLine();
                }
            } else {
                throw new RuntimeException(file.getAbstractPath() + "文件不存在");
            }
            bw.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
