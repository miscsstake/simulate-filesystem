package com.eaglesoup.command.subcommand;

import com.eaglesoup.command.BaseCommand;
import com.eaglesoup.fs.UnixFile;
import com.eaglesoup.fs.UnixFileOutputStream;
import picocli.CommandLine;

@CommandLine.Command(name = "touch", helpCommand = true, description = "创建文件")
public class TouchCommand extends BaseCommand {
    @CommandLine.Parameters(paramLabel = "path")
    private String path;

    @Override
    protected Integer call0() {
        UnixFile file = new UnixFile(parent.getCurr(), path);
        if (file.exist()) {
            throw new IllegalStateException(file.getAbstractPath() + " is already exist");
        }
        UnixFileOutputStream out = new UnixFileOutputStream(file);
        out.close();
        return 0;
    }

}
