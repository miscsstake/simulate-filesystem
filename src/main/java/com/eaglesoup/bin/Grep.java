package com.eaglesoup.bin;

import com.eaglesoup.bin.base.ShellBaseCommand;
import com.eaglesoup.fs.UnixFile;
import picocli.CommandLine;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandLine.Command(name = "grep", description = "字符串匹配")
public class Grep extends ShellBaseCommand {

    public Grep(InputStream input, OutputStream output, UnixFile curPath) {
        super(input, output, curPath);
    }

    @CommandLine.Parameters(index = "0..*", paramLabel = "regex")
    private String regex;

    @Override
    public Integer call0() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(this.in));
             BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(this.out))) {

            String line;
            Pattern pattern = Pattern.compile(regex);
            while ((line = br.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    bw.write(line);
                }
            }
            bw.flush();
        } catch (IOException e) {
            return -1;
        }
        return 0;
    }
}
