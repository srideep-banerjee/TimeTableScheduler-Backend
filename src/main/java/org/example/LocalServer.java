package org.example;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;

public class LocalServer {
    String homeHtml = "Test.html";
    public boolean running = true;

    public LocalServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
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
                } catch (Exception e) {
                    System.out.println(e);
                }
            });

            server.createContext("/io", new APIHandler(server));

            //create context from web files
            addFile(server);
            //start server
            server.start();
            System.out.println("Server started on port 8080");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addFile(HttpServer server) {
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

    static class APIHandler implements HttpHandler {
        HttpServer server;
        public APIHandler(HttpServer server){
            this.server=server;
        }
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if(exchange.getRequestURI().getPath().equals("/io/shutdown")){
                exchange.sendResponseHeaders(200, 0);
                exchange.getResponseBody().close();
                server.stop(0);
                System.exit(0);
            }
            if ("POST".equals(exchange.getRequestMethod())) {

                String response = "Task completed successfully!";

                //read request body
                InputStream is = exchange.getRequestBody();
                BufferedReader buff = new BufferedReader(new InputStreamReader((is)));
                System.out.print("Request body : ");
                while (buff.ready()) System.out.print((char) buff.read());
                System.out.println('\n');

                // Send response back to the web UI
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                // Handle other HTTP methods or unsupported paths
                String response = "Unsupported request";
                exchange.sendResponseHeaders(405, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }
}
