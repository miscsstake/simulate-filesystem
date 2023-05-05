package com.eaglesoup.command;

import com.eaglesoup.service.FileApiService;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "format", helpCommand = true, description = "创建目录")
public class FormatCommand extends AbsCommand implements Callable<String> {
    public FormatCommand(String path) {
        super(path);
    }

    @Override
    public String call() {
        new FileApiService("/").format();
        return "format success!";
    }


}
