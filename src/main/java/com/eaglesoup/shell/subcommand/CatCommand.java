package com.eaglesoup.shell.subcommand;

import com.eaglesoup.shell.BaseCommand;
import com.eaglesoup.fs.UnixFile;
import com.eaglesoup.fs.UnixFileInputStream;
import picocli.CommandLine;

@CommandLine.Command(name = "cat", helpCommand = true, description = "查看文件内容")
public class CatCommand extends BaseCommand {
    @CommandLine.Parameters(description = "文件名称")
    String path;

    @Override
    protected Integer call0() {
        UnixFile file = new UnixFile(parent.getCurr(), path);
        UnixFileInputStream input = new UnixFileInputStream(file);
        int v;
        int lastv = 0;
        while ((v = input.read()) != -1) {
            lastv = v;
            print((char) v);
        }
        if (lastv != '\n') {
            print('\n');
        }
        return 0;
    }
}
