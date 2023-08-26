package org.example.network;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;

public class ApiHandler implements HttpHandler {
    HttpServer server;
    ApiActionHelper apiActionHelper;


    public ApiHandler(HttpServer server) {
        this.server = server;
        apiActionHelper = ApiActionHelper.getInstance();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestURI().getPath().equals("/io/shutdown")){
            sendResponse(exchange,200,"Server shutting down");
            apiActionHelper.performAction("shutdown");
        }

        else if ("POST".equals(exchange.getRequestMethod())) {

            String response = "Task completed successfully!";

            //read request body
            InputStream is = exchange.getRequestBody();
            BufferedReader buff = new BufferedReader(new InputStreamReader((is)));
            System.out.print("Request body : ");
            while (buff.ready()) System.out.print((char) buff.read());
            System.out.println('\n');

            // Send response back to the web UI
            sendResponse(exchange, 200, response);
        } else
            // Handle other HTTP methods or unsupported paths
            sendResponse(exchange, 405, "Unsupported request");

    }

    public void sendResponse(HttpExchange exchange, int code, String response) throws IOException {
        exchange.sendResponseHeaders(code, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
