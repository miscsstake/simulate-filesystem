package com.eaglesoup.scp;

import org.apache.sshd.common.session.Session;
import org.apache.sshd.scp.common.helpers.DefaultScpFileOpener;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ScpTransferProgressMonitor extends DefaultScpFileOpener {

    @Override
    public Path resolveIncomingReceiveLocation(Session session, Path path, boolean recursive, boolean shouldBeDir, boolean preserve) throws IOException {
        return super.resolveIncomingReceiveLocation(session, Paths.get("/tmp/"), recursive, shouldBeDir, preserve);
    }
}
