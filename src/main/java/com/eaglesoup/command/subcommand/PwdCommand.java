package com.eaglesoup.command.subcommand;

import com.eaglesoup.command.BaseCommand;
import picocli.CommandLine;

@CommandLine.Command(name = "pwd", helpCommand = true, description = "显示当前目录路径")
public class PwdCommand extends BaseCommand {

    @Override
    protected Integer call0() {
        println(parent.getCurr().getAbstractPath());
        return 0;
    }
}
