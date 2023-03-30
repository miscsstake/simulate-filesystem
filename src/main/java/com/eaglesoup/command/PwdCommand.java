package com.eaglesoup.command;

import com.eaglesoup.service.DiskService;
import picocli.CommandLine;

@CommandLine.Command(name = "pwd", helpCommand = true, description = "创建目录")
public class PwdCommand implements Runnable {
    @Override
    public void run() {
        String currentPath = DiskService.getInstance().pwd();
        System.out.println(currentPath);
    }
}
