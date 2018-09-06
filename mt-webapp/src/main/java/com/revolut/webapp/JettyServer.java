package com.revolut.webapp;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class JettyServer {
    private final Server server;

    JettyServer(URI baseUri, ResourceConfig configuration) {
        server = JettyHttpContainerFactory.createServer(baseUri, configuration, false);
    }

    public void start() throws Exception {
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
        server.join();
        server.destroy();
    }
}