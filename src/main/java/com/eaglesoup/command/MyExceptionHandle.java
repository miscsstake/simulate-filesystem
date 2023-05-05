package com.eaglesoup.command;

import picocli.CommandLine;

public class MyExceptionHandle implements CommandLine.IExecutionExceptionHandler {
    public Exception exception;

    @Override
    public int handleExecutionException(Exception ex, CommandLine commandLine, CommandLine.ParseResult parseResult) {
        this.exception = ex;
        return commandLine.getCommandSpec().exitCodeOnExecutionException();
    }
}
