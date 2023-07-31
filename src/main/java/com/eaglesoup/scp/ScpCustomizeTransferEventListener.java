package com.eaglesoup.scp;

import com.eaglesoup.fs.UnixFile;
import com.eaglesoup.fs.UnixFileOutputStream;
import com.eaglesoup.os.MosOs;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.scp.common.ScpTransferEventListener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Objects;
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
        if (!"/".equals(file.getParent())) {
            UnixFile unixFile = new UnixFile(file.getParent());
            if (!unixFile.exist()) {
                unixFile.mkdir();
            }
        }
        UnixFileOutputStream out = new UnixFileOutputStream(MosOs.fileSystem().open(file.getAbsolutePath()));
        try {
            Scanner scanner = new Scanner(path);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                out.write(line.getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        out.close();
    }
}
