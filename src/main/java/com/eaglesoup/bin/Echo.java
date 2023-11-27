package com.eaglesoup.bin;

import com.eaglesoup.bin.base.ShellBaseCommand;
import com.eaglesoup.fs.IFileSystem;
import com.eaglesoup.fs.UnixFile;
import com.eaglesoup.fs.UnixFileOutputStream;
import com.eaglesoup.os.MosOs;
import picocli.CommandLine;

import java.io.*;

@CommandLine.Command(name = "echo", mixinStandardHelpOptions = true, description = "模拟linux的echo命令")
public class Echo extends ShellBaseCommand {

    public Echo(InputStream input, OutputStream output, UnixFile curPath) {
        super(input, output, curPath);
    }

    @CommandLine.Parameters(index = "0..*", description = "输出的内容")
    private String message;

    @CommandLine.Command(name = ">")
    public void write(@CommandLine.Parameters(paramLabel = "path") String path) {
        redirect(path, IFileSystem.WRITE);
    }

    @CommandLine.Command(name = ">>")
    public void append(@CommandLine.Parameters(paramLabel = "path") String path) throws IOException {
        redirect(path, IFileSystem.APPEND);
    }

    private void redirect(String path, int mode) {
        UnixFile file = new UnixFile(this.curPath, path);
        if (file.exist()) {
            if (file.isDir()) {
                throw new IllegalStateException("zsh: is a directory: " + file.getAbstractPath());
            }
        }
        OutputStream outputStream = new UnixFileOutputStream(MosOs.fileSystem().open(file.getAbstractPath(), mode));
        execute(outputStream);
    }

    @Override
    public Integer call0() {
        return execute(this.out);
    }

    public Integer execute(OutputStream out) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out))) {
            bw.write(this.message);
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            return -1;
        }
        return 0;
    }
}
