package com.eaglesoup.command;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "pwd", helpCommand = true, description = "显示当前目录路径")
public class PwdCommand extends AbsCommand implements Callable<String> {
    public PwdCommand(String path) {
        super(path);
    }

    @Override
    public String call() {
        return getPath();
    }
}
