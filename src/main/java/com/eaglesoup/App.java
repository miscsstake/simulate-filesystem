package com.eaglesoup;

import com.eaglesoup.applayer.ShellCommandV2;
import com.eaglesoup.constans.ProjectPathConstant;
import com.eaglesoup.device.VirtualDisk;
import com.eaglesoup.os.MosOs;
import com.eaglesoup.applayer.SshServerFactory;
import com.eaglesoup.scp.ScpServerFactory;

public class App {
    public static void main(String[] args) {
        MosOs.fileSystem().mount(new VirtualDisk());
        try {
            ProjectPathConstant.getInstance().setProjectPath(System.getProperty("user.dir") + "/src/main/java/com/eaglesoup");

            ScpServerFactory scpServerFactory = new ScpServerFactory();
            scpServerFactory.run();

            SshServerFactory sshServerFactory = new SshServerFactory();
            sshServerFactory.run();

            new ShellCommandV2().execute();
        } finally {
            MosOs.fileSystem().umount();
        }
    }
}
