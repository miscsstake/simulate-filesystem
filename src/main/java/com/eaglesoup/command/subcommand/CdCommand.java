package com.eaglesoup.command.subcommand;

import com.eaglesoup.command.BaseCommand;
import com.eaglesoup.fs.UnixFile;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(name = "cd", description = "切换目录")
public class CdCommand extends BaseCommand {
    @CommandLine.Parameters(description = "目录路径")
    String path;

    @Override
    protected Integer call0() {
        UnixFile file = new UnixFile(parent.getCurr(), path);
        if (file.isDir()) {
            parent.setCurr(file);
        } else {
            throw new IllegalStateException("cd: no such file or directory: " + file.getAbstractPath());
        }
        return 0;
    }
}
