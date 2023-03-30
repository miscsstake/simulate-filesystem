package com.eaglesoup.command;

import picocli.CommandLine;

@CommandLine.Command(name = "help", helpCommand = true, description = "使用指南")
public class HelpCommand implements Runnable {
    public void run() {
        System.out.println("==================================================");
        String msg = "格式化:       <format>\n" +
                "显示所有文件和目录:  <ls>\n" +
                "创建目录:          <mkdir>\n" +
                "创建文件:          <touch>\n" +
                "切换目录:          <cd>\n" +
                "查看当前路径:       <pwd>\n" +
                "回显内容:          <echo>\n" +
                "显示文件内容:       <cat>\n";
        System.out.println(msg);
        System.out.println("==================================================");
    }
}
