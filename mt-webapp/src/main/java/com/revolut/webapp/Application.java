package com.revolut.webapp;

import java.net.URI;

public class Application {

    public static void main(String[] args) throws Exception {
        JettyServer server = new JettyServer(
            new URI("http://localhost:8080"), new ApplicationConfiguration());
        server.start();
    }
}
