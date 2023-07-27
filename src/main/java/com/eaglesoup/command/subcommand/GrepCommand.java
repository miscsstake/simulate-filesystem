package com.eaglesoup.command.subcommand;

import com.eaglesoup.command.BaseCommand;
import com.sun.istack.internal.NotNull;
import picocli.CommandLine;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandLine.Command(name = "grep")
public class GrepCommand extends BaseCommand {
    @CommandLine.Parameters(index = "0..*", paramLabel = "regex")
    private String regex;

    @Override
    protected Integer call0() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        String line;
        Pattern pattern = Pattern.compile(regex);
        while ((line = bufferedReader.readLine()) != null) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                println(line);
            }
        }
        bufferedReader.close();
        return 0;
    }
}
