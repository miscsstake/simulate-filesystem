package com.eaglesoup.os.command.impl;

import com.eaglesoup.fs.UnixFile;
import com.eaglesoup.fs.UnixFileInputStream;
import com.eaglesoup.os.command.ICommandExec;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

public class ExternalCommandFactory implements ICommandExec {

    @Override
    public int exec(InputStream in, OutputStream out, AtomicReference<UnixFile> curr, String[] args) {
        String scriptContent = readFile(curr.get());
        Binding groovyBinding = new Binding();
        groovyBinding.setVariable("args", args);
        groovyBinding.setVariable("out", out);
        groovyBinding.setVariable("in", in);

        GroovyShell groovyShell = new GroovyShell(groovyBinding);
//        String scriptContent = "out.write(\"Hello World!\" + args[0] + \"\\n\"); out.write(\"end\");";
        Script script = groovyShell.parse(scriptContent);
        script.run();
        return 0;
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
