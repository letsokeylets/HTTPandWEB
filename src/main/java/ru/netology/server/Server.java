package ru.netology.server;

import ru.netology.server.handler.Handler;
import ru.netology.server.request.Request;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final ExecutorService executorService;
    //method -> (path -> handler)
    private Map<String, Map<String, Handler>> handlers;
    private static final List<String> VALID_PATHS = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    public Server(int poolSize) {
        this.executorService = Executors.newFixedThreadPool(poolSize);
        this.handlers = new ConcurrentHashMap<>();
    }

    //Метод для создания хендлера
    //method -> (GET, POST, ...), path -> (путь), handler -> (сама обработка)
    public void addHandler(String method, String path, Handler handler) {
        if (this.handlers.get(method) == null) {
            this.handlers.put(method, new ConcurrentHashMap<>());
        }
        this.handlers.get(method).put(path, handler);
    }

    public void listen(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                final var socket = serverSocket.accept();
                executorService.submit(() -> handleConnection(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleConnection(Socket socket) {
        try (
                socket;
                final var in = socket.getInputStream();
                final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            Request request = Request.fromInputStream(in);

            Map<String, Handler> handleMap = handlers.get(request.getMethod());
            //Выбрасываем 404 ошибку, если не найден хендлер по методу
            if (handleMap == null) {
                print404Error(out);
                return;
            }
            //Выбрасываем 404 ошибку, если не найден хендлер с таким путём к файлу
            Handler handler = handleMap.get(request.getPath());
            if (handler == null) {
                print404Error(out);
                return;
            }

            //Вызов обработчика, если найден нужный хендлер
            handler.handle(request, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Метод для вывода 404 ошибки
    private void print404Error(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

}
