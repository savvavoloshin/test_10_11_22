// http://www.javenue.info/post/java-http-server
import java.io.UnsupportedEncodingException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpPrincipal;
import com.sun.net.httpserver.HttpServer;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;

public class SimpleHttpServer {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(8765), 0);

        HttpContext context = server.createContext("/", new EchoHandler());
        HttpContext messagesContext = server.createContext("/messages", new MessagesHandler());
        context.setAuthenticator(new Auth());
        messagesContext.setAuthenticator(new Auth());

        server.setExecutor(null);
        server.start();
    }

    public static final String secret = "fj32Jfv02Mq33g0f8ioDkw";
    public static String createJWTToken(String name)
    {
        try {
            return JWT.create()
                    .withIssuer("auth0")
                    .withClaim("name", name)
                    .sign(Algorithm.HMAC256(secret));
        } catch (JWTCreationException exception){
            throw new RuntimeException("You need to enable Algorithm.HMAC256");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public static String getNameInToken(String token)
    {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer("auth0")
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("name").asString();
        } catch (JWTDecodeException exception){
            return null;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    static class EchoHandler implements HttpHandler {
        
        public static String toHex(byte[] bytes) {
            StringBuilder hash = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                String hex = Integer.toHexString(0xFF & bytes[i]);
                if (hex.length() == 1) {
                    hash.append('0');
                }
                hash.append(hex);
            }
            return hash.toString();
        }

        public static String getHash(String s) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hashbytes = md.digest(s.getBytes(StandardCharsets.UTF_8));
                return toHex(hashbytes);            
            } catch (NoSuchAlgorithmException e) {
                System.out.println(e.getMessage());
                throw new RuntimeException(e);
            }
        }
        
        boolean passwordIsCorrect(String name, String password, Connection conn) {
            try {
                Statement statmt = conn.createStatement();
                ResultSet resSet;
                String password_hash = getHash(password);
                resSet = statmt.executeQuery(String.format("SELECT * FROM users WHERE name = '%s' AND password_hash = '%s'", name, password_hash));
                return (resSet.next()) ? true : false;
            } catch(SQLException e){
                System.out.println(e.getMessage());
                throw new RuntimeException(e);
            }
        }

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
                
                System.out.println(getHash(password));

                Connection conn = null;  
                try {  
                    // db parameters  
                    String url = "jdbc:sqlite:users_and_messages.db";  
                    // create a connection to the database  
                    conn = DriverManager.getConnection(url);
                    
                    System.out.println("Connection to SQLite has been established.");

                    if(passwordIsCorrect(name, password, conn)) {

                        JSONObject jo_out = new JSONObject();
                        jo_out.put("token", createJWTToken(name));
                        builder.append(jo_out.toString());

                        byte[] bytes = builder.toString().getBytes();
                        exchange.sendResponseHeaders(200, bytes.length);

                        OutputStream os = exchange.getResponseBody();
                        os.write(bytes);
                        os.close();
                    } else {
                        builder.append("password isn't correct");

                        byte[] bytes = builder.toString().getBytes();
                        exchange.sendResponseHeaders(200, bytes.length);

                        OutputStream os = exchange.getResponseBody();
                        os.write(bytes);
                        os.close();

                    }
                    
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

            // builder.append("<h1>URI: ").append(exchange.getRequestURI()).append("</h1>");

            // Headers headers = exchange.getRequestHeaders();
            // for (String header : headers.keySet()) {
            //     builder.append("<p>").append(header).append("=")
            //             .append(headers.getFirst(header)).append("</p>");
            // }

            // byte[] bytes = builder.toString().getBytes();
            // exchange.sendResponseHeaders(200, bytes.length);

            // OutputStream os = exchange.getResponseBody();
            // os.write(bytes);
            // os.close();
        }
    }


    static class MessagesHandler implements HttpHandler {
        
        public static String toHex(byte[] bytes) {
            StringBuilder hash = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                String hex = Integer.toHexString(0xFF & bytes[i]);
                if (hex.length() == 1) {
                    hash.append('0');
                }
                hash.append(hex);
            }
            return hash.toString();
        }

        public static String getHash(String s) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hashbytes = md.digest(s.getBytes(StandardCharsets.UTF_8));
                return toHex(hashbytes);            
            } catch (NoSuchAlgorithmException e) {
                System.out.println(e.getMessage());
                throw new RuntimeException(e);
            }
        }
        
        boolean passwordIsCorrect(String name, String password, Connection conn) {
            try {
                Statement statmt = conn.createStatement();
                ResultSet resSet;
                String password_hash = getHash(password);
                resSet = statmt.executeQuery(String.format("SELECT * FROM users WHERE name = '%s' AND password_hash = '%s'", name, password_hash));
                return (resSet.next()) ? true : false;
            } catch(SQLException e){
                System.out.println(e.getMessage());
                throw new RuntimeException(e);
            }

        }

        public static boolean tokenIsCorrect(String token) {
            return true;
        }

        public static void saveMessageToDatabase(String message, Integer user_id, Connection conn) {
            try {
                Statement statmt = conn.createStatement();
                statmt.executeQuery(String.format("INSERT INTO messages (MESSAGE, BELONGS_TO_USER) VALUES ('%s', %d)", message, user_id));
            } catch(SQLException e){
                System.out.println(e.getMessage());
                throw new RuntimeException(e);
            }
        }

        public static Integer obtainUserId(String bearerToken, Connection conn) {
            return 0;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder builder = new StringBuilder();

            if("POST".equals(exchange.getRequestMethod())) {
                System.out.println(exchange.getRequestMethod());
                InputStream eis = exchange.getRequestBody();
                Headers headers = exchange.getRequestHeaders();
                
                System.out.println(headers.getFirst("Authorization"));
                String auth_header = headers.getFirst("Authorization");
                
                Pattern p = Pattern.compile("Bearer_(.*)");
                Matcher m = p.matcher("Bearer_Ym9zY236Ym9zY28=");
                System.out.println(auth_header);
                while (m.find()) {
                    System.out.println(m.group(0));
                    System.out.println(m.group(1));
                }

                String text = new String(eis.readAllBytes(), StandardCharsets.UTF_8);

                JSONObject jo = new JSONObject(text);
                
                String name = jo.get("name").toString();
                String message = jo.get("message").toString();
                
                System.out.println(name);
                System.out.println(message);

                String bearerToken = "12QW";
                
                Connection conn = null;  
                try {  
                    // db parameters  
                    String url = "jdbc:sqlite:users_and_messages.db";  
                    // create a connection to the database  
                    conn = DriverManager.getConnection(url);
                    
                    System.out.println("Connection to SQLite has been established.");

                    
                    if(tokenIsCorrect(bearerToken)) {

                        Integer user_id = obtainUserId(bearerToken, conn);

                        saveMessageToDatabase(message, user_id, conn);
                        builder.append("message probably saved");

                        byte[] bytes = builder.toString().getBytes();
                        exchange.sendResponseHeaders(200, bytes.length);

                        OutputStream os = exchange.getResponseBody();
                        os.write(bytes);
                        os.close();
                    } else {
                        builder.append("password isn't correct");

                        byte[] bytes = builder.toString().getBytes();
                        exchange.sendResponseHeaders(200, bytes.length);

                        OutputStream os = exchange.getResponseBody();
                        os.write(bytes);
                        os.close();

                    }
                    
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