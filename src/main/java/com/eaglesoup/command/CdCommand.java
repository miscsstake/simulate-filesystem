package com.eaglesoup.command;

import com.eaglesoup.exception.BusinessException;
import com.eaglesoup.service.CustomerDiskService;
import picocli.CommandLine;


@CommandLine.Command(name = "cd", description = "切换目录")
public class CdCommand implements Runnable {
    @CommandLine.Parameters
    String path;

    /**
     * 暂不支持 ..
     */
    public void run() {
        try {
            CustomerDiskService.getInstance().cd(path);
        } catch (BusinessException e) {
            System.out.println(e.getMessage());
        }
    }
}
