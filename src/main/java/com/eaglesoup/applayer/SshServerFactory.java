package com.eaglesoup.applayer;

import com.eaglesoup.applayer.SshCommandV2;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.AcceptAllPasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;

import java.io.File;
import java.io.IOException;

public class SshServerFactory implements Runnable {
    public void run() {
        SshServer sshd = SshServer.setUpDefaultServer();
        sshd.setPort(20333);
        //ssh
        sshd.setShellFactory(channelSession -> new SshCommandV2());
        sshd.setPasswordAuthenticator(AcceptAllPasswordAuthenticator.INSTANCE);
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File("key.ser").toPath()));
        try {
            sshd.start();
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
}
