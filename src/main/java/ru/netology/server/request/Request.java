package ru.netology.server.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> header;
    private final InputStream body;

    private Request(String method, String path, Map<String, String> header, InputStream body) {
        this.method = method;
        this.path = path;
        this.header = header;
        this.body = body;
    }

    /**
     * Метод для разбора запроса (Создание объекта Request)
     * @param inputStream
     * @return Request
     * @throws IOException
     */
    public static Request fromInputStream(InputStream inputStream) throws IOException {
        final var in = new BufferedReader(new InputStreamReader(inputStream));

        final var requestLine = in.readLine();
        final var parts = requestLine.split(" ");

        //Бросаем ексепшн, если пришёл некорректный запрос
        if (parts.length != 3) {
            throw new IOException("Invalid request!");
        }

        String method = parts[0];
        String path = parts[1];

        Map<String, String> headers = new HashMap<>();
        String line;
        while (!(line = in.readLine()).isEmpty()) {
            int i = line.indexOf(":");
            String name = line.substring(0, i);
            String value = line.substring(i + 2);
            headers.put(name, value);
        }

        //Создаём объект типа Реквест
        return new Request(method, path, headers, inputStream);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public InputStream getBody() {
        return body;
    }

}
