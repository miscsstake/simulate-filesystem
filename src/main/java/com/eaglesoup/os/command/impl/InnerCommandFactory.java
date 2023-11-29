package com.eaglesoup.os.command.impl;

import com.eaglesoup.constans.ProjectPathConstant;
import com.eaglesoup.fs.UnixFile;
import com.eaglesoup.os.boot.UnixProcess;
import com.eaglesoup.os.command.ICommandExec;
import com.eaglesoup.util.LoadSourceClassUtil;
import picocli.CommandLine;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class InnerCommandFactory implements ICommandExec {
    @Override
    public int exec(InputStream in, OutputStream out, AtomicReference<UnixFile> curr, String[] args) {
        if (args.length == 0) {
            return 0;
        }
        String classesDir = ProjectPathConstant.getInstance().getInnerBinPath();
        String clzName = args[0].trim();
        try {
            Class<?> clz = LoadSourceClassUtil.loadClass(classesDir, clzName);
            Constructor<?> constructor = clz.getConstructor(InputStream.class, OutputStream.class, UnixFile.class);
            Object instance = constructor.newInstance(in, out, curr.get());
            //String[] args 移除第一个参数
            args = Arrays.copyOfRange(args, 1, args.length);
            new CommandLine(instance).execute(args);
            if (instance instanceof UnixProcess) {
                UnixProcess commandProcess = (UnixProcess) instance;
                curr.set(commandProcess.getCurPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
