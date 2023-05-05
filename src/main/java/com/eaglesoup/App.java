package com.eaglesoup;

import com.eaglesoup.command.ShellCommand;
import com.eaglesoup.ssh.SSHServer;

public class App {
    public static void main(String[] args) throws Exception {
        //启动sshd
        SSHServer sshServer = new SSHServer(2033);
        sshServer.startServer();

        //启动非ssh模式
        new ShellCommand(System.in, System.out).start("aa", () -> System.out.close());
    }
}
