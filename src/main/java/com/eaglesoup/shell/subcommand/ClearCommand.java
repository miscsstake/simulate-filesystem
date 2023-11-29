package com.eaglesoup.shell.subcommand;

import com.eaglesoup.shell.BaseCommand;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(name = "clear", description = "清屏")
public class ClearCommand extends BaseCommand {

    @Override
    protected Integer call0() throws IOException {
        print("\033[H\033[2J");
        return 0;
    }
}
