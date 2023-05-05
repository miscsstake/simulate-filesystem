package com.eaglesoup.command;

import com.eaglesoup.core.model.DirectoryEntityStruct;
import com.eaglesoup.service.FileApiService;
import com.eaglesoup.util.FileUtil;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "ll")
public class LsCommand extends AbsCommand implements Callable<String> {
    @CommandLine.Parameters(description = "文件名称", defaultValue = "")
    String filepath;

    public LsCommand(String path) {
        super(path);
    }

    @Override
    public String call() {
        String fullFilename = FileUtil.fullFilename(this.getPath(), filepath);
        List<DirectoryEntityStruct> entityStructList = new FileApiService(fullFilename).listFiles();
        List<String> response = new ArrayList<>();
        for (DirectoryEntityStruct struct : entityStructList) {
            long createTime = (long) struct.getCreationTimestamp() * 1000;
            String humanCreateTime = new SimpleDateFormat("yyyy/MM/dd").format(createTime);
            String filename = new String(struct.getFilename()).trim();
            response.add(String.format("%s %s %s\r", struct.getFileSize(), humanCreateTime, filename));
        }
        return StringUtils.join(response, "\n");
    }
}
