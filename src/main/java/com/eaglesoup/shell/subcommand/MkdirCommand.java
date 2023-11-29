package com.eaglesoup.shell.subcommand;

import com.eaglesoup.shell.BaseCommand;
import com.eaglesoup.fs.UnixFile;
import picocli.CommandLine;

@CommandLine.Command(name = "mkdir", helpCommand = true, description = "创建目录")
public class MkdirCommand extends BaseCommand {
    @CommandLine.Parameters(paramLabel = "path")
    private String path;

    @Override
    protected Integer call0() {
        UnixFile file = new UnixFile(parent.getCurr(), path);
        file.mkdir();
        return 0;
    }
}
