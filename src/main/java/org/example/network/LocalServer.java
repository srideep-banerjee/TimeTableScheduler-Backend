package org.example.network;

import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;

public class LocalServer {
    private String homeHtml = "Test.html";
    private HttpServer server;

    public LocalServer(int port) {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", exchange -> {
                try {
                    FileInputStream fis = new FileInputStream("web/" + homeHtml);
                    byte[] bytes = fis.readAllBytes();
                    fis.close();
                    exchange.getResponseHeaders().set("Content-Type", "text/html");
                    exchange.sendResponseHeaders(200, bytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(bytes);
                    os.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            });

            server.createContext("/io", new ApiHandler(server));

            //create context from web files
            addFileContexts(server);
            //start server
            server.start();
            System.out.println("Server started on port 5999");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addFileContexts(HttpServer server) {
        for (String path : FileIterator.getPathList()) {
            server.createContext(path, exchange -> {
                try {
                    FileInputStream fis = new FileInputStream("web/" + path);
                    byte[] bytes = fis.readAllBytes();
                    fis.close();
                    String contentType = "text/plain";
                    if (path.toLowerCase().endsWith(".html")) contentType = "text/html";
                    else if (path.toLowerCase().endsWith(".css")) contentType = "text/css";
                    else if (path.toLowerCase().endsWith(".js")) contentType = "text/javascript";
                    exchange.getResponseHeaders().set("Content-Type", contentType);
                    exchange.sendResponseHeaders(200, bytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(bytes);
                    os.close();
                } catch (Exception e) {
                    System.out.println(e);
                }
            });
        }
    }

    public void stop() {
        if (server != null) server.stop(0);
    }
}
