package com.eaglesoup.shell.subcommand;

import com.eaglesoup.shell.BaseCommand;
import com.eaglesoup.fs.UnixFile;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(name = "rm", helpCommand = true, description = "删除文件")
public class RmCommand extends BaseCommand {
    @CommandLine.Parameters(paramLabel = "path")
    private String path;

    @Override
    protected Integer call0() throws IOException {
        UnixFile file = new UnixFile(parent.getCurr(), path);
        file.delete();
        return 0;
    }
}
