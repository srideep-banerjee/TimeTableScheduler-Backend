package org.example.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.example.DefaultConfig;
import org.example.algorithms.Generator;
import org.example.dao.SubjectDao;
import org.example.dao.TeacherDao;
import org.example.files.SavesHandler;
import org.example.interfaces.OnResultListener;
import org.example.pojo.ScheduleSolution;
import org.example.pojo.ScheduleStructure;
import org.example.pojo.Subject;
import org.example.pojo.Teacher;

import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class ApiHandler implements HttpHandler {
    HttpServer server;
    ObjectMapper objectMapper;
    Generator generator;


    public ApiHandler(HttpServer server) {
        this.server = server;
        objectMapper = new ObjectMapper();
        generator = new Generator(null);
    }

    @Override
    public void handle(HttpExchange exchange) {
        //try{
        String path = exchange.getRequestURI().getPath();
        List<String> apiTokenHeader = exchange.getRequestHeaders().get("Api-Token");
        if (DefaultConfig.REQUIRE_TOKEN && (apiTokenHeader == null || apiTokenHeader.isEmpty() || !apiTokenHeader.get(0).equals(TokenManager.token))) {
            sendInvalidTokenResponse(exchange);
            return;
        }
        String requestMethod = exchange.getRequestMethod();
        String querys = exchange.getRequestURI().getQuery();
        System.out.println(requestMethod + " " + path + (querys != null ? "?" + querys : ""));
        if (requestMethod.equals("OPTIONS")) {
            sendPreflightResponse(exchange);
            return;
        }

        if (path.equals("/io/heartbeat")) {
            sendTextResponse(exchange, 200, "Ok");

        } else if (path.equals("/io/teachers")) {
            switch (requestMethod) {
                case "GET" -> {
                    try {
                        String response = objectMapper.writeValueAsString(TeacherDao.getInstance());
                        sendJsonResponse(exchange, 200, response);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
                case "PUT" -> {
                    JsonNode arr;
                    try {
                        arr = objectMapper.readTree(exchange.getRequestBody());
                    } catch (IOException e) {
                        sendTextResponse(exchange, 400, "Invalid data format");
                        return;
                    }
                    for (Iterator<String> it = arr.fieldNames(); it.hasNext(); ) {
                        String name = it.next();
                        if (Pattern.compile("[$&+,:;=\\\\?@#|/'<>.^*()%!-]").matcher(name).find()) {
                            sendTextResponse(exchange, 400, "Name must not contain special character");
                            return;
                        } else if (name.length() == 0) {
                            sendTextResponse(exchange, 400, "Name can't be empty");
                            return;
                        } else if (name.length() > 50) {
                            sendTextResponse(exchange, 400, "Name can't be longer than 50 characters");
                            return;
                        }
                        JsonNode teacherJson = arr.get(name);
                        try {
                            Teacher teacher = objectMapper.reader().readValue(teacherJson, Teacher.class);
                            TeacherDao.getInstance().put(name, teacher);
                            sendTextResponse(exchange, 200, "Teachers updated");
                        } catch (IOException e) {
                            sendTextResponse(exchange, 400, "Invalid data format");
                        }
                    }
                }
                case "DELETE" -> {
                    generator.stop();
                    ScheduleSolution.getInstance().removeAllTeachers();
                    TeacherDao.getInstance().clear();
                    sendTextResponse(exchange, 200, "Request accepted");
                }
                default -> sendInvalidOperationResponse(exchange);
            }
        } else if (path.equals("/io/teachers/names")) {
            if (requestMethod.equals("GET")) {

                try {
                    String response = new ObjectMapper().writeValueAsString(TeacherDao.getInstance().keySet());
                    sendJsonResponse(exchange, 200, response);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            } else sendInvalidOperationResponse(exchange);
        } else if (path.startsWith("/io/teachers/") && (path.length() > "/io/teachers/".length())) {
            String name = path.substring(path.lastIndexOf("/") + 1).toUpperCase();

            switch (requestMethod) {
                case "GET" -> {
                    if (!TeacherDao.getInstance().containsKey(name)) {
                        sendTextResponse(exchange, 404, "Teacher not found");
                        return;
                    }
                    try {
                        String response = objectMapper.writeValueAsString(TeacherDao.getInstance().get(name));
                        sendJsonResponse(exchange, 200, response);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
                case "PUT" -> {
                    if (Pattern.compile("[$&+,:;=\\\\?@#|/'<>.^*()%!-]").matcher(name).find()) {
                        sendTextResponse(exchange, 400, "name must not contain special character");
                        return;
                    } else if (name.length() == 0) {
                        sendTextResponse(exchange, 400, "name can't be empty");
                        return;
                    } else if (name.length() > 50) {
                        sendTextResponse(exchange, 400, "name can't be longer than 50 characters");
                        return;
                    }
                    try {
                        TeacherDao.getInstance().put(name, objectMapper.readValue(exchange.getRequestBody(), Teacher.class));
                        sendTextResponse(exchange, 200, "Request accepted");
                    } catch (IOException e) {
                        sendTextResponse(exchange, 400, "Invalid data format");
                    }
                }
                case "DELETE" -> {
                    if (!TeacherDao.getInstance().containsKey(name)) {
                        sendTextResponse(exchange, 404, "Teacher not found");
                        return;
                    }
                    generator.stop();
                    ScheduleSolution.getInstance().removeTeacherByName(name);
                    TeacherDao.getInstance().remove(name);
                    sendTextResponse(exchange, 200, "Request accepted");
                }
                default -> sendInvalidOperationResponse(exchange);
            }
        } else if (path.equals("/io/subjects")) {
            switch (requestMethod) {
                case "GET" -> {
                    try {
                        String response = objectMapper.writeValueAsString(SubjectDao.getInstance());
                        sendJsonResponse(exchange, 200, response);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
                case "PUT" -> {
                    JsonNode arr;
                    try {
                        arr = objectMapper.readTree(exchange.getRequestBody());
                    } catch (IOException e) {
                        sendTextResponse(exchange, 400, "Invalid data format");
                        return;
                    }
                    for (Iterator<String> it = arr.fieldNames(); it.hasNext(); ) {
                        String code = it.next();
                        if (code.length() == 0) {
                            sendTextResponse(exchange, 400, "Subject code can't be empty");
                            return;
                        } else if (code.length() > 20) {
                            sendTextResponse(exchange, 400, "Subject code can't be longer than 20 characters");
                            return;
                        }
                        JsonNode subJson = arr.get(code);
                        try {
                            Subject subject = objectMapper.reader().readValue(subJson, Subject.class);
                            SubjectDao.getInstance().put(code, subject);
                            sendTextResponse(exchange, 200, "Subjects updated");
                        } catch (IOException e) {
                            sendTextResponse(exchange, 400, "Invalid data format");
                        }
                    }
                }
                case "DELETE" -> {
                    generator.stop();
                    ScheduleSolution.getInstance().resetData();
                    sendTextResponse(exchange, 200, "Request accepted");
                }
                default -> sendInvalidOperationResponse(exchange);
            }
        } else if (path.equals("/io/subjects/codes")) {
            if (requestMethod.equals("GET")) {
                String response;
                try {
                    response = new ObjectMapper().writeValueAsString(SubjectDao.getInstance().keySet());
                    sendJsonResponse(exchange, 200, response);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            } else sendInvalidOperationResponse(exchange);
        } else if (path.startsWith("/io/subjects/") && (path.length() > "/io/subjects/".length())) {
            String code = path.substring(path.lastIndexOf("/") + 1).toUpperCase();

            switch (requestMethod) {
                case "GET" -> {
                    if (!SubjectDao.getInstance().containsKey(code)) {
                        sendTextResponse(exchange, 404, "Subject not found");
                        return;
                    }
                    try {
                        String response = objectMapper.writeValueAsString(SubjectDao.getInstance().get(code));
                        sendJsonResponse(exchange, 200, response);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
                case "PUT" -> {
                    if (code.length() == 0) {
                        sendTextResponse(exchange, 400, "Subject code can't be empty");
                        return;
                    } else if (code.length() > 20) {
                        sendTextResponse(exchange, 400, "Subject code can't be longer than 20 characters");
                        return;
                    }
                    try {
                        SubjectDao.getInstance().put(code, objectMapper.readValue(exchange.getRequestBody(), Subject.class));
                        sendTextResponse(exchange, 200, "Request accepted");
                    } catch (IOException e) {
                        sendTextResponse(exchange, 400, "Invalid data format");
                    }
                }
                case "DELETE" -> {
                    if (!SubjectDao.getInstance().containsKey(code)) {
                        sendTextResponse(exchange, 404, "Subject not found");
                        return;
                    }
                    generator.stop();
                    ScheduleSolution.getInstance().removeSubjectByCode(code);
                    SubjectDao.getInstance().remove(code);
                    sendTextResponse(exchange, 200, "Request accepted");
                }
                default -> sendInvalidOperationResponse(exchange);
            }
        } else if (path.equals("/io/schedule")) {
            boolean generateNew = false;
            int sem = -1;
            int sec = -1;
            if (querys != null) {
                querys = querys.toLowerCase();
                for (String query : querys.split("&")) {
                    String[] entry = query.split("=");
                    try {
                        switch (entry[0]) {
                            case "generatenew" -> generateNew = entry[1].equals("true");
                            case "sem" -> sem = Integer.parseInt(entry[1]);
                            case "sec" -> sec = Integer.parseInt(entry[1]);
                            default -> {
                                sendTextResponse(exchange, 400, "Invalid query parameters");
                                return;
                            }
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
            switch (requestMethod) {
                case "GET" -> {
                    if (generateNew) {
                        generator.stop();
                        generator = new Generator(new OnResultListener() {
                            @Override
                            public void onResult() {
                                try {
                                    String response = objectMapper.writeValueAsString(ScheduleSolution.getInstance().getData());
                                    sendJsonResponse(exchange, 200, response);
                                } catch (JsonProcessingException e) {
                                    e.printStackTrace();
                                }
                                System.gc();
                            }

                            @Override
                            public void onError(String msg) {
                                sendTextResponse(exchange, 500, msg);
                                System.gc();
                            }
                        });
                        generator.generate();
                    } else {
                        if (ScheduleSolution.getInstance().isEmpty()) {
                            sendTextResponse(exchange, 404, "Schedule is empty");
                            return;
                        }
                        if (sem != -1 && sec != -1) {
                            String response = "null";
                            try {
                                response = objectMapper.writeValueAsString(ScheduleSolution.getInstance().getData(sem, sec));
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }
                            if (response.equals("null")) sendTextResponse(exchange, 400, "Semester or section invalid");
                            else sendJsonResponse(exchange, 200, response);
                        } else if (sem != -1) {
                            String response = "null";
                            try {
                                response = objectMapper.writeValueAsString(ScheduleSolution.getInstance().getData(sem));
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }
                            if (response.equals("null")) sendTextResponse(exchange, 400, "Semester or section invalid");
                            else sendJsonResponse(exchange, 200, response);
                        } else {
                            try {
                                String response = objectMapper.writeValueAsString(ScheduleSolution.getInstance().getData());
                                sendJsonResponse(exchange, 200, response);
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                case "PUT" -> {
                    try {
                        List<List<List<String>>> data = objectMapper.readValue(exchange.getRequestBody(), new TypeReference<>() {
                        });
                        if (!ScheduleSolution.getInstance().setData(sem, sec, data)) {
                            sendTextResponse(exchange, 400, "Invalid data format");
                            return;
                        }
                        sendTextResponse(exchange, 200, "Request accepted");
                    } catch (Exception e) {
                        sendTextResponse(exchange, 400, "Invalid data format");
                    }
                }
                default -> sendInvalidOperationResponse(exchange);
            }
        } else if (path.startsWith("/io/schedule/teacher/") && path.length() > 21) {
            String name = path.substring(path.lastIndexOf("/") + 1).toUpperCase();
            if (requestMethod.equals("GET")) {
                if (!TeacherDao.getInstance().containsKey(name)) {
                    sendTextResponse(exchange, 404, "Teacher not found");
                    return;
                }
                if (ScheduleSolution.getInstance().isEmpty()) {
                    sendTextResponse(exchange, 404, "Schedule is empty");
                    return;
                }
                try {
                    String response = objectMapper.writeValueAsString(ScheduleSolution.getInstance().getTeacherScheduleByName(name));
                    sendTextResponse(exchange, 200, response);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            } else sendInvalidOperationResponse(exchange);
        } else if (path.startsWith("/io/schedule/structure")) {
            if (requestMethod.equals("GET")) {
                try {
                    String response = objectMapper.writeValueAsString(ScheduleStructure.getInstance());
                    sendJsonResponse(exchange, 200, response);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            } else if (requestMethod.equals("PUT")) {
                try {
                    objectMapper.readerForUpdating(ScheduleStructure.getInstance()).readValue(exchange.getRequestBody());
                    sendTextResponse(exchange, 200, "Request accepted");
                    ScheduleSolution.getInstance().updateStructure();
                } catch (Exception e) {
                    e.printStackTrace();
                    sendTextResponse(exchange, 400, "Invalid data format");
                }
            } else sendInvalidOperationResponse(exchange);
        } else if (path.equals("/io/saves/newEmpty")) {
            if (querys == null) {
                sendTextResponse(exchange, 400, "No name provided to create");
                return;
            }
            String res = SavesHandler.newEmptySave(querys.substring(5).toUpperCase());
            if (res == null) sendTextResponse(exchange, 200, "Request accepted");
            else sendTextResponse(exchange, 400, res);
        } else if (path.equals("/io/saves/load")) {
            if (querys == null) {
                sendTextResponse(exchange, 400, "No name provided to load");
                return;
            }
            String res = SavesHandler.load(querys.substring(5).toUpperCase());
            if (res == null) sendTextResponse(exchange, 200, "Request accepted");
            else sendTextResponse(exchange, 400, res);
        } else if (path.equals("/io/saves/save")) {
            if (querys == null) {
                sendTextResponse(exchange, 400, "No name provided to save");
                return;
            }
            String res = SavesHandler.save(querys.substring(5).toUpperCase());
            if (res == null) sendTextResponse(exchange, 200, "Request accepted");
            else sendTextResponse(exchange, 400, res);
        } else if (path.equals("/io/saves/delete")) {
            if (querys == null) {
                sendTextResponse(exchange, 400, "No name provided to delete");
                return;
            }
            String res = SavesHandler.delete(querys.substring(5).toUpperCase());
            if (res == null) sendTextResponse(exchange, 200, "Request accepted");
            else sendTextResponse(exchange, 400, res);
        } else if (path.equals("/io/saves/currentName")) {
            String res = SavesHandler.getCurrentSave();
            if (res == null) sendTextResponse(exchange, 400, "null");
            else sendTextResponse(exchange, 200, res);
        } else if (path.equals("/io/saves/list")) {
            try {
                String response = objectMapper.writeValueAsString(SavesHandler.getSaveList());
                sendJsonResponse(exchange, 200, response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (path.equals("/io/saves/isSaved")) {
            try {
                sendTextResponse(exchange, 200, SavesHandler.isSaved()+"");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else
            // Handle other HTTP methods or unsupported paths
            sendTextResponse(exchange, 405, "Unsupported request");

        //}catch(Exception e){
        //    e.printStackTrace();
        //}

    }

    public void sendInvalidOperationResponse(HttpExchange exchange) {
        sendTextResponse(exchange, 405, "Method not allowed");
    }

    public void sendTextResponse(HttpExchange exchange, int code, String response) {
        try {
            Headers headers = exchange.getResponseHeaders();
            int port = server.getAddress().getPort();
            int clientPort = 3000;
            headers.set("Content-Type", "text/plain");
            if (Arrays.asList(3000,port).contains(clientPort)) {
                headers.set("Access-Control-Allow-Origin", "http://localhost:" + clientPort);
            }
            exchange.sendResponseHeaders(code, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendJsonResponse(HttpExchange exchange, int code, String response) {
        try {
            Headers headers = exchange.getResponseHeaders();
            int port = server.getAddress().getPort();
            int clientPort = 3000;
            headers.set("Content-Type", "application/json");
            if (Arrays.asList(3000,port).contains(clientPort)) {
                headers.set("Access-Control-Allow-Origin", "http://localhost:" + clientPort);
            }
            exchange.sendResponseHeaders(code, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendPreflightResponse(HttpExchange exchange) {
        try {
            Headers headers = exchange.getResponseHeaders();
            int port = server.getAddress().getPort();
            int clientPort = 3000;
            headers.set("Content-Type", "text/plain");
            if (Arrays.asList(3000,port).contains(clientPort)) {
                headers.set("Access-Control-Allow-Origin", "http://localhost:" + clientPort);
                headers.set("Access-Control-Allow-Methods","GET,PUT,POST,DELETE");
                headers.set("Access-Control-Allow-Headers","Content-Type");
            }
            exchange.sendResponseHeaders(200, 0);
            OutputStream os = exchange.getResponseBody();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendInvalidTokenResponse(HttpExchange exchange) {
        sendTextResponse(exchange, 400, "Invalid token");
    }
}
