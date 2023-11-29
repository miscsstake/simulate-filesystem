package com.eaglesoup;

import com.eaglesoup.scp.ScpServerFactory;
import com.eaglesoup.shell.ShellCommand;
import com.eaglesoup.device.VirtualDisk;
import com.eaglesoup.os.MosOs;
import com.eaglesoup.ssh.SshServerFactory;

public class App {
    public static void main(String[] args) {
        MosOs.fileSystem().mount(new VirtualDisk());
        try {
            ScpServerFactory scpServerFactory = new ScpServerFactory();
            scpServerFactory.run();

            //ssh
            SshServerFactory sshServerFactory = new SshServerFactory();
            sshServerFactory.run();

            //shell
            ShellCommand shell = new ShellCommand(MosOs.fileSystem().getDefaultRootPath());
            shell.run();
        } finally {
            MosOs.fileSystem().umount();
        }
    }
}
