package com.eaglesoup.command;

import com.eaglesoup.service.FileApiService;
import com.eaglesoup.util.FileUtil;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "rm", helpCommand = true, description = "删除文件")
public class RmCommand extends AbsCommand implements Callable<String> {
    @CommandLine.Parameters(description = "文件名称，支持多个文件")
    String filename;

    public RmCommand(String path) {
        super(path);
    }

    @Override
    public String call() {
        String fullFilename = FileUtil.fullFilename(this.getPath(), filename);
        new FileApiService(fullFilename).delete();
        return "rm success!";
    }
}
