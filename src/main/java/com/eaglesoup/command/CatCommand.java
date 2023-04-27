package com.eaglesoup.command;

import com.eaglesoup.service.CustomerDiskService;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "cat", helpCommand = true, description = "创建目录")
public class CatCommand implements Runnable {
    @CommandLine.Parameters(description = "文件名称，支持多个文件")
    List<String> filenameList;

    @Override
    public void run() {
        for (String fileName : filenameList) {
            try {
                byte[] result = CustomerDiskService.getInstance().cat(fileName);
                System.out.println(new String(result).trim());
            } catch (Exception exception) {
                System.out.println(exception.getMessage());
            }
        }
    }
}
