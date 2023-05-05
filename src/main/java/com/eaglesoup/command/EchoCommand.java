package com.eaglesoup.command;

import picocli.CommandLine;

import java.util.concurrent.Callable;

/**
 * echo > a.txt
 * echo aa bb > a.txt
 * echo aa bb > a.txt b c d
 */
@CommandLine.Command(name = "echo", description = "回显输入的内容")
public class EchoCommand extends AbsCommand implements Callable<String> {
    @CommandLine.Parameters(description = "写入的内容不能为空")
    String content;

    public EchoCommand(String path) {
        super(path);
    }

    @Override
    public String call() {
        return content;
    }
}
