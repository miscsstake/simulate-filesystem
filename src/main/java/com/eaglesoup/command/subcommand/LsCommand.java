package com.eaglesoup.command.subcommand;

import com.eaglesoup.command.BaseCommand;
import com.eaglesoup.fs.UnixFile;
import picocli.CommandLine;

import java.io.IOException;
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
        String format = "%11s %10s %-20s";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        if (file.exist()) {
            for (UnixFile p : file.listFiles()) {
                println(String.format(format, p.length(), sdf.format(new Date(p.lastModified())), p.getName()));
            }
        } else {
            println(file.getAbstractPath() + "文件不存在");
        }
    }
}
