package com.eaglesoup;

import com.eaglesoup.applayer.ShellCommandV2;
import com.eaglesoup.device.VirtualDisk;
import com.eaglesoup.os.MosOs;
import com.eaglesoup.scp.SshScpFactory;

public class App {
    public static void main(String[] args) {
        MosOs.fileSystem().mount(new VirtualDisk());
        try {
            SshScpFactory sshScpFactory = new SshScpFactory();
            sshScpFactory.run();

            new ShellCommandV2().execute();
        } finally {
            MosOs.fileSystem().umount();
        }
    }
}
