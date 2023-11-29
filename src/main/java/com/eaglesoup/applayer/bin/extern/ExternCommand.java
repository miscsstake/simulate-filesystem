package com.eaglesoup.applayer.bin.extern;

import com.eaglesoup.applayer.bin.base.BaseCommand;
import com.eaglesoup.fs.UnixFile;
import com.eaglesoup.fs.UnixFileInputStream;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import picocli.CommandLine;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


@CommandLine.Command(name = "default", description = "外部默认命令")
public class ExternCommand extends BaseCommand {
    @CommandLine.Parameters(description = "自定义命令参数")
    private List<String> args;

    public void call0(InputStream in, OutputStream out) {
        String fileName = args.get(0);
        args.remove(0);
        String scriptContent = readFile(new UnixFile(parent.curPath.get(), fileName));
        Binding groovyBinding = new Binding();
        groovyBinding.setVariable("args", args);
        groovyBinding.setVariable("out", out);
        groovyBinding.setVariable("input", in);

        GroovyShell groovyShell = new GroovyShell(groovyBinding);
        Script script = groovyShell.parse(scriptContent);
        script.run();
    }

    private String readFile(UnixFile file) {
        StringBuilder fileContent = new StringBuilder();
        try (UnixFileInputStream input = new UnixFileInputStream(file)) {
            int v;
            while ((v = input.read()) != -1) {
                fileContent.append((char) v);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileContent.toString();
    }
}
