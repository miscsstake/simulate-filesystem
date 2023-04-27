package com.eaglesoup.command;

import com.eaglesoup.service.CustomerDiskService;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "format", helpCommand = true, description = "创建目录")
public class FormatCommand implements Callable<String> {
    @Override
    public String call() {
        return CustomerDiskService.getInstance().format();
    }
}
