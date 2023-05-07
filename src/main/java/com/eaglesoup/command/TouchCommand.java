package com.eaglesoup.command;

import com.eaglesoup.service.FileApiService;
import com.eaglesoup.util.FileUtil;
import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "touch", helpCommand = true, description = "创建文件")
public class TouchCommand extends AbsCommand implements Callable<String> {
    @CommandLine.Parameters(description = "文件名称，支持多个文件")
    List<String> filenameList;

    public TouchCommand(String path) {
        super(path);
    }

    @Override
    public String call() {
        for (String filename : filenameList) {
            String fullFilename = FileUtil.fullFilename(this.getPath(), filename);
            new FileApiService(fullFilename).createNewFile();
        }
        return "touch success!";
    }
}
