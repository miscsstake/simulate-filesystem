package com.eaglesoup;

import com.eaglesoup.applayer.ShellCommandV2;
import com.eaglesoup.device.VirtualDisk;
import com.eaglesoup.os.MosOs;
import com.eaglesoup.applayer.SshServerFactory;

public class App {
    public static void main(String[] args) {
        MosOs.fileSystem().mount(new VirtualDisk());
        try {
//            ScpServerFactory scpServerFactory = new ScpServerFactory();
//            scpServerFactory.run();

            SshServerFactory sshServerFactory = new SshServerFactory();
            sshServerFactory.run();

            new ShellCommandV2().execute();
        } finally {
            MosOs.fileSystem().umount();
        }
    }
}
