package org.example.network;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLConnection;
import java.util.Random;

public class LocalServer {
    private final String homeHtml = "index.html";
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

        //create default context
        server.createContext("/", this::handleDefaultRequest);

        //start server
        server.start();
        System.out.println("Server started on port " + this.port);

    }

    public void handleDefaultRequest(HttpExchange exchange) {
        try{
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";

            File file = new File("web/" + path);
            if (path.contains("..") || !file.exists() || file.isDirectory() || file.getName().startsWith(".")) {
                path = "/index.html";
            }

            try {
                FileInputStream fis = new FileInputStream("web/" + path);
                byte[] bytes = fis.readAllBytes();
                fis.close();

                String contentType;
                if (path.endsWith(".map")) {
                    contentType = "application/json";
                } else if(path.endsWith(".ico")) {
                    contentType = "image/x-icon";
                } else {
                    contentType = URLConnection.guessContentTypeFromName(new File(path).getName());
                }
                if (contentType == null) {
                    contentType = "plain/text";
                }

                Headers headers = exchange.getResponseHeaders();
                headers.set("Content-Type", contentType);
                headers.set("Access-Control-Allow-Origin", "http://localhost:3000 http://localhost:" + port);
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            } catch (IOException e) {
                System.out.println(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getDefaultURL() {
        return "http://localhost:" + port;
    }

    public void servePageNotFoundHtml(HttpExchange exchange) {
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("NotFound.html");
            byte[] bytes = is.readAllBytes();
            is.close();
            Headers headers = exchange.getResponseHeaders();
            headers.set("Content-Type", "text/html");
            headers.set("Access-Control-Allow-Origin", "http://localhost:3000 http://localhost:"+port);
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
