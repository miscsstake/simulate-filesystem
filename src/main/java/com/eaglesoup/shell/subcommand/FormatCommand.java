package com.eaglesoup.shell.subcommand;

import com.eaglesoup.shell.BaseCommand;
import com.eaglesoup.fs.UnixFile;
import com.eaglesoup.os.MosOs;
import picocli.CommandLine;

import java.io.IOException;


@CommandLine.Command(name = "format", description = "格式化")
public class FormatCommand extends BaseCommand {

    @Override
    protected Integer call0() throws IOException {
        MosOs.fileSystem().format();
        parent.setCurr(new UnixFile("/"));
        println("disk format success!!!");
        return 0;
    }
}
