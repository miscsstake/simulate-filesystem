package com.eaglesoup;

import com.eaglesoup.applayer.ShellCommandV2;
import com.eaglesoup.device.VirtualDisk;
import com.eaglesoup.os.MosOs;
import com.eaglesoup.ssh.SshServerFactory;

public class PipedInputStreamDemo {

    public static void main(String[] args) {
        MosOs.fileSystem().mount(new VirtualDisk());
        try {
            SshServerFactory sshServerFactory = new SshServerFactory();
            sshServerFactory.run();

            new ShellCommandV2().execute();
        } finally {
            MosOs.fileSystem().umount();
        }
    }
}
