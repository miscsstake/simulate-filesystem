package com.eaglesoup.scp;

import com.eaglesoup.fs.UnixFile;
import com.eaglesoup.fs.UnixFileOutputStream;
import com.eaglesoup.os.MosOs;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.scp.common.ScpTransferEventListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Scanner;
import java.util.Set;

public class ScpCustomizeTransferEventListener implements ScpTransferEventListener {
    @Override
    public void endFileEvent(Session session, FileOperation op, Path file, long length, Set<PosixFilePermission> perms, Throwable thrown) throws IOException {
        writeFs(file);
    }

    private void writeFs(Path path) {
        String tmpFilePath = path.toUri().getPath();
        String filePath = tmpFilePath.substring("/tmp".length());

        File file = new File(filePath);
        UnixFile unixFile = new UnixFile(filePath);
        if (unixFile.exist()) {
            unixFile.delete();
        }


        mkdir(file.getParentFile());


        UnixFileOutputStream out = new UnixFileOutputStream(MosOs.fileSystem().open(file.getAbsolutePath()));

        //读取file数据流
        try (FileInputStream fis = new FileInputStream(tmpFilePath)) {


            int content;
            while ((content = fis.read()) != -1) {
                // 将字节转换为字符
                out.write((char) content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.close();
    }

    private void mkdir(File file) {
        if ("/".equals(file.getPath())) {
            return;
        }
        mkdir(file.getParentFile());
        UnixFile unixFile = new UnixFile(file.getPath());
        if (!unixFile.exist()) {
            unixFile.mkdir();
        }
    }
}
