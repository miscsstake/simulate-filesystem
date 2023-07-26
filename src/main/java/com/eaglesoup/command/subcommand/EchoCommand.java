package com.eaglesoup.command.subcommand;

import com.eaglesoup.command.BaseCommand;
import picocli.CommandLine;

/**
 * echo > a.txt
 * echo aa bb > a.txt
 * echo aa bb > a.txt b c d
 */
@CommandLine.Command(name = "echo", description = "回显输入的内容")
public class EchoCommand extends BaseCommand {
    @CommandLine.Parameters(index = "0..*", paramLabel = "text")
    private String text;

    @Override
    public Integer call0() {
        /**
         * 1.判断最后一个字符是否是\n 如果是则不需要打印额外的换行符
         */
        if (text.endsWith("\n")) {
            print(text);
        } else {
            println(text);
        }
        return 0;
    }
}
