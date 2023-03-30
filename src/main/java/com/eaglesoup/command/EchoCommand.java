package com.eaglesoup.command;

import com.eaglesoup.exception.BusinessException;
import com.eaglesoup.service.DiskService;
import picocli.CommandLine;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * echo > a.txt
 * echo aa bb > a.txt
 * echo aa bb > a.txt b c d
 */
@CommandLine.Command(name = "echo", description = "回显输入的内容")
public class EchoCommand implements Runnable {
    @CommandLine.Parameters(description = "写入的内容不能为空")
    List<String> content;

    public void run() {
        if (!content.contains(">") && !content.contains(">>")) {
            StringBuffer str1 = new StringBuffer();
            for (String s : content) {
                str1.append(s + " ");
            }
            System.out.println(str1.toString());
            return;
        }

        boolean isAppend = content.contains(">>");
        String fileName = getFileName(isAppend);
        byte[] fileContent = buildContent(isAppend);
        //写入文件内容
        try {
            DiskService.getInstance().echo(fileContent, fileName, isAppend);
        } catch (BusinessException e) {
            System.out.println(e.getMessage());
        }
    }

    private String getFileName(boolean isAppend) {
        String fileName = "";
        int fileOperateIndex;
        String fileOperateString = isAppend ? ">>" : ">";
        fileOperateIndex = content.lastIndexOf(fileOperateString);
        if (fileOperateIndex == content.size() - 1) {
            return fileName;
        }
        return content.get(fileOperateIndex + 1);
    }

    private byte[] buildContent(boolean isAppend) {
        int fileOperateIndex;
        String fileOperateString = isAppend ? ">>" : ">";
        fileOperateIndex = content.lastIndexOf(fileOperateString);

        //换行符
        if (0 == fileOperateIndex) {
            return new byte[]{0x0A};
        }

        StringBuilder fileContentBuilder = new StringBuilder();
        for (int i = 0; i < fileOperateIndex; i++) {
            if (i == 0) {
                fileContentBuilder.append(content.get(i));
            } else {
                fileContentBuilder.append(" ").append(content.get(i));
            }
        }
        //echo aabb > a.txt 1122
        for (int i = fileOperateIndex + 2; i < content.size(); i++) {
            fileContentBuilder.append(" ").append(content.get(i));
        }
        return fileContentBuilder.toString().getBytes(StandardCharsets.UTF_8);
    }
}
