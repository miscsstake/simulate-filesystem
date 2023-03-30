package com.eaglesoup.command;

import com.eaglesoup.service.DiskService;
import picocli.CommandLine;

@CommandLine.Command(name = "format", helpCommand = true, description = "创建目录")
public class FormatCommand implements Runnable {
    @Override
    public void run() {
        DiskService.getInstance().format();
    }
}
