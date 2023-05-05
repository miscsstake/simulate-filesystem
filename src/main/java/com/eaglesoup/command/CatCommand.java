package com.eaglesoup.command;

import com.eaglesoup.service.FileApiService;
import com.eaglesoup.util.FileUtil;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "cat", helpCommand = true, description = "查看文件内容")
public class CatCommand extends AbsCommand implements Callable<String> {
    public CatCommand(String path) {
        super(path);
    }

    @CommandLine.Parameters(description = "文件名称")
    String filename;

    @Override
    public String call() {
        String fullFilename = FileUtil.fullFilename(this.getPath(), filename);
        byte[] result = new FileApiService(fullFilename).read();
        return new String(result);
    }
}
