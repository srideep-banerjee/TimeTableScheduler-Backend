package org.example.network;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.Random;

public class LocalServer {
    private final String homeHtml = "Dashboard.html";
    private HttpServer server;
    private int port;

    public LocalServer() {
        Random random = new Random();
        while (true) {
            try {
                this.port = random.nextInt(5000, 65535);
                this.server = HttpServer.create(new InetSocketAddress(port), 0);
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        server.createContext("/io", new ApiHandler(server));

        //create context from web files
        addFileContexts(server);

        //create default context
        server.createContext("/", exchange -> servePageNotFoundHtml(exchange));

        //start server
        server.start();
        System.out.println("Server started on port "+this.port);

    }

    public int getPort() {
        return this.port;
    }

    public void addFileContexts(HttpServer server) {
        for (String path : FileIterator.getPathList()) {
            server.createContext(path, exchange -> {
                if(!new File("web/" +path).exists()){
                    servePageNotFoundHtml(exchange);
                    return;
                }
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

    public String getDefaultURL(){
        return "http://localhost:"+port+"/"+homeHtml;
    }

    public void servePageNotFoundHtml(HttpExchange exchange){
        try {
            InputStream is=this.getClass().getClassLoader().getResourceAsStream("NotFound.html");
            byte[] bytes=is.readAllBytes();
            is.close();
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void stop() {
        if (server != null) server.stop(0);
    }
}
