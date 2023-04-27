package com.eaglesoup.command;


import com.eaglesoup.service.CustomerDiskService;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "mkdir", helpCommand = true, description = "创建目录")
public class MkdirCommand implements Runnable {
    @CommandLine.Parameters(description = "文件名称，支持多个文件")
    List<String> dirNames;

    public void run() {
        for (String dirname : dirNames) {
            CustomerDiskService.getInstance().mkdir(dirname);
        }
    }
}
