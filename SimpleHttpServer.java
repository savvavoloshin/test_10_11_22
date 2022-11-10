// http://www.javenue.info/post/java-http-server

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpPrincipal;
import com.sun.net.httpserver.HttpServer;

import netscape.javascript.JSObject;

import org.json.JSONObject;

import java.sql.Connection;  
import java.sql.DriverManager;  
import java.sql.SQLException;  

public class SimpleHttpServer {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(8765), 0);

        HttpContext context = server.createContext("/", new EchoHandler());
        context.setAuthenticator(new Auth());

        server.setExecutor(null);
        server.start();
    }

    static class EchoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder builder = new StringBuilder();

            if("POST".equals(exchange.getRequestMethod())) {
                System.out.println(exchange.getRequestMethod());
                InputStream eis = exchange.getRequestBody();

                String text = new String(eis.readAllBytes(), StandardCharsets.UTF_8);

                JSONObject jo = new JSONObject(text);
                
                String name = jo.get("name").toString();
                String password = jo.get("password").toString();

                System.out.println(jo.toString());
                System.out.println(password);

                Connection conn = null;  
                try {  
                    // db parameters  
                    String url = "jdbc:sqlite:users_and_messages.db";  
                    // create a connection to the database  
                    conn = DriverManager.getConnection(url);  
                    
                    System.out.println("Connection to SQLite has been established.");  
                    
                } catch (SQLException e) {  
                    System.out.println(e.getMessage());  
                } finally {  
                    try {  
                        if (conn != null) {  
                            conn.close();  
                        }  
                    } catch (SQLException ex) {  
                        System.out.println(ex.getMessage());  
                    }  
                } 

            }

            builder.append("<h1>URI: ").append(exchange.getRequestURI()).append("</h1>");

            Headers headers = exchange.getRequestHeaders();
            for (String header : headers.keySet()) {
                builder.append("<p>").append(header).append("=")
                        .append(headers.getFirst(header)).append("</p>");
            }

            byte[] bytes = builder.toString().getBytes();
            exchange.sendResponseHeaders(200, bytes.length);

            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    static class Auth extends Authenticator {
        @Override
        public Result authenticate(HttpExchange httpExchange) {
            if ("/forbidden".equals(httpExchange.getRequestURI().toString()))
                return new Failure(403);
            else
                return new Success(new HttpPrincipal("c0nst", "realm"));
        }
    }
}