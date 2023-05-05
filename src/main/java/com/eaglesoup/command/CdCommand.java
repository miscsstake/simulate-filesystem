package com.eaglesoup.command;

import com.eaglesoup.exception.BusinessException;
import com.eaglesoup.service.FileApiService;
import com.eaglesoup.util.FileUtil;
import lombok.SneakyThrows;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "cd", description = "切换目录")
public class CdCommand extends AbsCommand implements Callable<String> {
    @CommandLine.Parameters(description = "切换目录路径")
    String filepath;

    public CdCommand(String path) {
        super(path);
    }

    @SneakyThrows
    @Override
    public String call() {
        String fullFilename = FileUtil.fullFilename(this.getPath(), filepath);
        boolean exits = new FileApiService(fullFilename).exists();
        if (exits) {
            return fullFilename;
        } else {
            throw new BusinessException("目录不存在");
        }
    }
}
