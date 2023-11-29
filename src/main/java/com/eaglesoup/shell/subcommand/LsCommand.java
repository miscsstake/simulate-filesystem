package com.eaglesoup.shell.subcommand;

import com.eaglesoup.shell.BaseCommand;
import com.eaglesoup.fs.UnixFile;
import picocli.CommandLine;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@CommandLine.Command(name = "ls", aliases = {"ll"})
public class LsCommand extends BaseCommand {
    @CommandLine.Parameters(index = "0..1", paramLabel = "path")
    private List<String> paths;

    @Override
    protected Integer call0() {
        if (paths == null || paths.isEmpty()) {
            printPath(parent.getCurr());
        } else if (paths.size() == 1) {
            printPath(new UnixFile(parent.getCurr(), paths.get(0)));
        } else {
            for (String path : paths) {
                UnixFile file = new UnixFile(parent.getCurr(), path);
                println(file.getAbstractPath() + ":");
                printPath(file);
            }
        }
        return 0;
    }

    private void printPath(UnixFile file) {
        String format = "%11s %10s %s";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        if (file.exist()) {
            for (UnixFile p : file.listFiles()) {
                String filename = p.isDir() ? p.getName() + "/" : p.getName();
                println(String.format(format, p.length(), sdf.format(new Date(p.lastModified())), filename));
            }
        } else {
            println(file.getAbstractPath() + "文件不存在");
        }
    }
}
