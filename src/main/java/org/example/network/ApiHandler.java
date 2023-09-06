package org.example.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.example.algorithms.Generator;
import org.example.dao.SubjectDao;
import org.example.dao.TeacherDao;
import org.example.interfaces.OnResultListener;
import org.example.pojo.ScheduleSolution;
import org.example.pojo.Subject;
import org.example.pojo.Teacher;

import java.io.*;
import java.util.Iterator;
import java.util.regex.Pattern;

public class ApiHandler implements HttpHandler {
    HttpServer server;
    ApiActionHelper apiActionHelper;
    ObjectMapper objectMapper;
    Generator generator;
    static int ii;


    public ApiHandler(HttpServer server) {
        this.server = server;
        apiActionHelper = ApiActionHelper.getInstance();
        objectMapper=new ObjectMapper();
        generator=new Generator(null);
    }

    @Override
    public void handle(HttpExchange exchange) {
        try{
        apiActionHelper.performAction("heart beat received");
        String path=exchange.getRequestURI().getPath();
        String requestMethod= exchange.getRequestMethod();

        if (path.equals("/io/heartbeat")) {
            sendTextResponse(exchange, 200, "Ok");

        }
        else if (path.equals("/io/teachers")) {
            if (requestMethod.equals("GET")) {
                String response;
                try {
                    response = objectMapper.writeValueAsString(TeacherDao.getInstance());
                    sendJsonResponse(exchange, 200, response);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }

            else if (requestMethod.equals("PUT")) {
                JsonNode arr;
                try {
                    arr = objectMapper.readTree(exchange.getRequestBody());
                } catch (IOException e) {
                    sendTextResponse(exchange,400,"Invalid data format");
                    return;
                }
                for (Iterator<String> it = arr.fieldNames(); it.hasNext(); ) {
                    String name = it.next();
                    if(Pattern.compile("[$&+,:;=\\\\?@#|/'<>.^*()%!-]").matcher(name).find()) {
                        sendTextResponse(exchange, 400, "Name must not contain special character");
                        return;
                    }
                    else if(name.length()==0) {
                        sendTextResponse(exchange, 400, "Name can't be empty");
                        return;
                    }
                    else if(name.length()>50) {
                        sendTextResponse(exchange, 400, "Name can't be longer than 50 characters");
                        return;
                    }
                }
                try {
                    objectMapper.readerForUpdating(TeacherDao.getInstance()).readValue(arr);
                    sendTextResponse(exchange,200,"Teachers updated");
                } catch (IOException e) {
                    sendTextResponse(exchange,400,"Invalid data format");
                }
            }

            else if (requestMethod.equals("DELETE")) {
                generator.stop();
                ScheduleSolution.getInstance().removeAllTeachers();
                TeacherDao.getInstance().clear();
                sendTextResponse(exchange,200,"Request accepted");
            }

            else sendInvalidOperationResponse(exchange);
        }
        else if(path.equals("/io/teachers/names")){
            if(requestMethod.equals("GET")) {

                try {
                    String response = new ObjectMapper().writeValueAsString(TeacherDao.getInstance().keySet());
                    sendJsonResponse(exchange,200,response);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
            else sendInvalidOperationResponse(exchange);
        }
        else if (path.startsWith("/io/teachers/") && (path.length()>"/io/teachers/".length())) {
            String name=path.substring(path.lastIndexOf("/")+1).toUpperCase();

            if(requestMethod.equals("GET")){
                if(!TeacherDao.getInstance().containsKey(name)){
                    sendTextResponse(exchange,404,"Teacher not found");
                    return;
                }
                try {
                    String response=objectMapper.writeValueAsString(TeacherDao.getInstance().get(name));
                    sendJsonResponse(exchange,200,response);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
            else if(requestMethod.equals("PUT")){
                if(Pattern.compile("[$&+,:;=\\\\?@#|/'<>.^*()%!-]").matcher(name).find()) {
                    sendTextResponse(exchange, 400, "name must not contain special character");
                    return;
                }
                else if(name.length()==0) {
                    sendTextResponse(exchange, 400, "name can't be empty");
                    return;
                }
                else if(name.length()>50) {
                    sendTextResponse(exchange, 400, "name can't be longer than 50 characters");
                    return;
                }
                try {
                    TeacherDao.getInstance().put(name,objectMapper.readValue(exchange.getRequestBody(),Teacher.class));
                    sendTextResponse(exchange,200,"Request accepted");
                } catch (IOException e) {
                    sendTextResponse(exchange,400,"Invalid data format");
                }
            }
            else if(requestMethod.equals("DELETE")){
                if(!TeacherDao.getInstance().containsKey(name)){
                    sendTextResponse(exchange,404,"Teacher not found");
                    return;
                }
                generator.stop();
                ScheduleSolution.getInstance().removeTeacherByName(name);
                TeacherDao.getInstance().remove(name);
                sendTextResponse(exchange,200,"Request accepted");
            }
            else sendInvalidOperationResponse(exchange);
        }
        else if(path.equals("/io/subjects")) {
            if(requestMethod.equals("GET")){
                try {
                    String response=objectMapper.writeValueAsString(SubjectDao.getInstance());
                    sendJsonResponse(exchange,200,response);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
            else if (requestMethod.equals("PUT")) {
                JsonNode arr;
                try {
                    arr = objectMapper.readTree(exchange.getRequestBody());
                } catch (IOException e) {
                    sendTextResponse(exchange,400,"Invalid data format");
                    return;
                }
                for (Iterator<String> it = arr.fieldNames(); it.hasNext(); ) {
                    String code = it.next();
                    if(code.length()==0) {
                        sendTextResponse(exchange, 400, "Subject code can't be empty");
                        return;
                    }
                    else if(code.length()>20) {
                        sendTextResponse(exchange, 400, "Subject code can't be longer than 20 characters");
                        return;
                    }
                }
                try {
                    objectMapper.readerForUpdating(SubjectDao.getInstance()).readValue(arr);
                    sendTextResponse(exchange,200,"Subjects updated");
                } catch (IOException e) {
                    sendTextResponse(exchange,400,"Invalid data format");
                }
            }
            else if (requestMethod.equals("DELETE")) {
                generator.stop();
                ScheduleSolution.getInstance().resetData();
                sendTextResponse(exchange,200,"Request accepted");
            }
        }
        else if(path.equals("/io/subjects/codes")){
            if(requestMethod.equals("GET")) {
                String response= null;
                try {
                    response = new ObjectMapper().writeValueAsString(SubjectDao.getInstance().keySet());
                    sendJsonResponse(exchange,200,response);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
            else sendInvalidOperationResponse(exchange);
        }
        else if(path.startsWith("/io/subjects/") && (path.length()>"/io/subjects/".length())){
            String code=path.substring(path.lastIndexOf("/"+1)).toUpperCase();

            if(requestMethod.equals("GET")){
                if(!SubjectDao.getInstance().containsKey(code)){
                    sendTextResponse(exchange,404,"Subject not found");
                    return;
                }
                try {
                    String response=objectMapper.writeValueAsString(SubjectDao.getInstance().get(code));
                    sendJsonResponse(exchange,200,response);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
            else if(requestMethod.equals("PUT")){
                if(code.length()==0) {
                    sendTextResponse(exchange, 400, "Subject code can't be empty");
                    return;
                }
                else if(code.length()>20) {
                    sendTextResponse(exchange, 400, "Subject code can't be longer than 20 characters");
                    return;
                }
                try {
                    SubjectDao.getInstance().put(code,objectMapper.readValue(exchange.getRequestBody(), Subject.class));
                    sendTextResponse(exchange,200,"Request accepted");
                } catch (IOException e) {
                    sendTextResponse(exchange,400,"Invalid data format");
                }
            }
            else if(requestMethod.equals("DELETE")){
                if(!SubjectDao.getInstance().containsKey(code)){
                    sendTextResponse(exchange,404,"Subject not found");
                    return;
                }
                generator.stop();
                ScheduleSolution.getInstance().removeSubjectByCode(code);
                sendTextResponse(exchange,200,"Request accepted");
            }
            else sendInvalidOperationResponse(exchange);
        }
        else if(path.equals("/io/schedule")){
            String query=exchange.getRequestURI().getQuery().toLowerCase();
            boolean generateNew=query.contains("generatenew=true");
            if(requestMethod.equals("GET")){
                if(generateNew){
                    generator.stop();
                    generator=new Generator(new OnResultListener() {
                        @Override
                        public void onResult() {

                            try {
                                String response = new ObjectMapper().writeValueAsString(ScheduleSolution.getInstance().getData());
                                sendJsonResponse(exchange,200,response);
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(String msg) {
                            sendTextResponse(exchange,500,msg);
                        }
                    });
                    generator.generate();
                }
                else{
                    try {
                        String response = new ObjectMapper().writeValueAsString(ScheduleSolution.getInstance().getData());
                        sendTextResponse(exchange,200,response);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
            }
            else if(requestMethod.equals("PUT")){

            }
        }
        else
            // Handle other HTTP methods or unsupported paths
            sendTextResponse(exchange, 405, "Unsupported request");

        System.out.println(path + "again");
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public void sendInvalidOperationResponse(HttpExchange exchange) {
        sendTextResponse(exchange,405,"Method not allowed");
    }

    public void sendTextResponse(HttpExchange exchange, int code, String response) {
        try {
            exchange.getResponseHeaders().set("Content-Type","text/plain");
            exchange.sendResponseHeaders(code, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendJsonResponse(HttpExchange exchange, int code, String response){
        try {
            exchange.getResponseHeaders().set("Content-Type","application/json");
            exchange.sendResponseHeaders(code, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
