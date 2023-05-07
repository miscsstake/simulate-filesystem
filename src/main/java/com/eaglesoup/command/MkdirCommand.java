package com.eaglesoup.command;

import com.eaglesoup.service.FileApiService;
import com.eaglesoup.util.FileUtil;
import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "mkdir", helpCommand = true, description = "创建目录")
public class MkdirCommand extends AbsCommand implements Callable<String> {
    @CommandLine.Parameters(description = "文件名称，支持多个文件")
    List<String> dirNames;

    public MkdirCommand(String path) {
        super(path);
    }

    @Override
    public String call() {
        //path添加文件路径
        for (String dirname : dirNames) {
            String fullFilename = FileUtil.fullFilename(this.getPath(), dirname);
            new FileApiService(fullFilename).mkdir();
        }
        return "mkdir success!";
    }


}
