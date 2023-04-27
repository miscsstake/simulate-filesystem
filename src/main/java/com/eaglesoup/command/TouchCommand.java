package com.eaglesoup.command;

import com.eaglesoup.service.CustomerDiskService;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "touch", helpCommand = true, description = "创建文件")
public class TouchCommand implements Runnable {
    @CommandLine.Parameters(description = "文件名称，支持多个文件")
    List<String> filenameList;

    @Override
    public void run() {
        for (String filename : filenameList) {
            CustomerDiskService.getInstance().touch(filename);
        }
    }
}
