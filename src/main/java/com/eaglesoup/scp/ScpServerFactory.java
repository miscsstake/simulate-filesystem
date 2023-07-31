package com.eaglesoup.scp;

import com.eaglesoup.ssh.SshShellCommand;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.scp.ScpModuleProperties;
import org.apache.sshd.scp.client.ScpClientCreator;
import org.apache.sshd.scp.common.ScpTransferEventListener;
import org.apache.sshd.scp.server.ScpCommandFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.AcceptAllPasswordAuthenticator;
import org.apache.sshd.server.command.CommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;

import java.io.File;
import java.io.IOException;

public class ScpServerFactory {
    public void run() {
        SshServer sshd = SshServer.setUpDefaultServer();
        sshd.setPort(20222);

        CommandFactory commandFactory = new ScpCommandFactory.Builder()
                .withFileOpener(new ScpTransferProgressMonitor())
                .addEventListener(new ScpCustomizeTransferEventListener())
                .build();

        sshd.setCommandFactory(commandFactory);
        sshd.setPasswordAuthenticator(AcceptAllPasswordAuthenticator.INSTANCE);
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File("key.ser").toPath()));
        try {
            sshd.start();
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
}
