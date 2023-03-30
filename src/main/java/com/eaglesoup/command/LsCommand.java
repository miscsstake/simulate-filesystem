package com.eaglesoup.command;

import com.eaglesoup.core.model.DirectoryEntityStruct;
import com.eaglesoup.service.DiskService;
import com.eaglesoup.util.FileUtil;
import picocli.CommandLine;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@CommandLine.Command
public class LsCommand implements Runnable {
    @CommandLine.Option(names = "-l", description = "展示文件详细信息")
    private boolean isFileDetail;

    @CommandLine.Parameters(description = "要查看的目录", defaultValue = "")
    String path;

    @Override
    public void run() {
        System.out.println("输出当前目录文件列表：");
        List<DirectoryEntityStruct> entityStructList = DiskService.getInstance().ls(path);
        if (isFileDetail) {
            outputFileDetail(entityStructList);
        } else {
            outputFileName(entityStructList);
        }
    }

    private void outputFileName(List<DirectoryEntityStruct> entityStructList) {
        StringBuilder builder = new StringBuilder();
        for (DirectoryEntityStruct entityStruct : entityStructList) {
            String fileName = new String(entityStruct.getFilename()).trim();
            if (!FileUtil.isDir(entityStruct.getAttribute())) {
                String extension = new String(entityStruct.getFilenameExtension()).trim();
                if (!"".equals(extension)) {
                    fileName += "." + extension;
                }
            }
            builder.append(fileName).append(" ");
        }
        if (builder.length() > 0) System.out.println(builder);
    }

    private void outputFileDetail(List<DirectoryEntityStruct> entityStructList) {
        List<String> list = new ArrayList<>();
        for (DirectoryEntityStruct entityStruct : entityStructList) {
            String fileName = new String(entityStruct.getFilename()).trim();
            if (!FileUtil.isDir(entityStruct.getAttribute())) {
                String extension = new String(entityStruct.getFilenameExtension()).trim();
                if (!"".equals(extension)) {
                    fileName += "." + extension;
                }
            }
            long createTime = (long) entityStruct.getCreationTimestamp() * 1000;
            String humanCreateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(createTime);
            String fileAttr = String.format("%s %s %s", entityStruct.getFileSize(), humanCreateTime, fileName);
            list.add(fileAttr);
        }
        for (String fileAttr : list) {
            System.out.println(fileAttr);
        }
    }
}
