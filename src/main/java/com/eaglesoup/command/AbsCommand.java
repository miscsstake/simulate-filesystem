package com.eaglesoup.command;

import picocli.CommandLine;

public class AbsCommand {
    @CommandLine.Option(names = {">", ">>"})
    public String outputFile = "";

    private String path;

    public AbsCommand(String path) {
        this.path = path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }

    public String getOutputFile() {
        return this.outputFile;
    }
}
