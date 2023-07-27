package com.eaglesoup;

import com.eaglesoup.command.ShellCommand;
import com.eaglesoup.device.VirtualDisk;
import com.eaglesoup.os.MosOs;
import com.eaglesoup.ssh.SSHServer;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;


public class App {
    public static void main(String[] args) throws Exception {
//        JSch jsch = new JSch();
//        Session session = jsch.getSession(username, host, 22);
//        Channel channel = session.openChannel("sftp");

        MosOs.fileSystem().mount(new VirtualDisk());
        try {
            ShellCommand shell = new ShellCommand(MosOs.fileSystem().getDefaultRootPath());
            shell.run();
        } finally {
            MosOs.fileSystem().umount();
        }
        //启动sshd
//        SSHServer sshServer = new SSHServer(2033);
//        sshServer.startServer();

        //启动非ssh模式
//        new ShellCommand2(System.in, System.out).start("aa", () -> System.out.close());
    }
}
