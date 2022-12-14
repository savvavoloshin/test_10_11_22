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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.json.JSONArray;

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

        HttpContext context = server.createContext("/", new LoginHandler());
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

    static class LoginHandler implements HttpHandler {
        
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
        
        public static boolean passwordIsCorrect(String name, String password, Connection conn) {
            try {
                Statement statmt = conn.createStatement();
                ResultSet resSet;
                String password_hash = getHash(password);
                resSet = statmt.executeQuery(String.format("SELECT * FROM users WHERE name = '%s' AND password_hash = '%s'", name, password_hash));
                return (resSet.next()) ? true : false;
            } catch(SQLException e){
                throw new RuntimeException(e);
            }
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder builder = new StringBuilder();

            if("POST".equals(exchange.getRequestMethod())) {
                InputStream eis = exchange.getRequestBody();

                String text = new String(eis.readAllBytes(), StandardCharsets.UTF_8);

                JSONObject jo = new JSONObject(text);
                
                String name = jo.get("name").toString();
                String password = jo.get("password").toString();

                Connection conn = null;  
                try {  
                    // db parameters  
                    String url = "jdbc:sqlite:users_and_messages.db";  
                    // create a connection to the database  
                    conn = DriverManager.getConnection(url);
                    
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
        }
    }


    static class MessagesHandler implements HttpHandler {
        
        public static boolean tokenIsCorrect(String token, String name) {
            
            if(name.length() < 1)
                return false;

            Pattern p = Pattern.compile("Bearer_(.*)");
            Matcher m = p.matcher(token);
            String bearer_token = "";
            
            while (m.find()) {
                bearer_token = m.group(1);
            }
            
            String token_name = getNameInToken(bearer_token);
            
            return name.equals(token_name) ? true : false;
        }

        public static void saveMessageToDatabase(String message, Integer user_id, Connection conn) {
            try {
                Statement statmt = conn.createStatement();
                statmt.executeUpdate(String.format("INSERT INTO messages (MESSAGE, BELONGS_TO_USER) VALUES ('%s', %d)", message, user_id));
            } catch(SQLException e){
                System.out.println(e.getMessage());
                throw new RuntimeException(e);
            }
        }

        public static Integer obtainUserId(String name, Connection conn) {
            try {
                Statement statmt = conn.createStatement();
                ResultSet resSet;
                resSet = statmt.executeQuery(String.format("SELECT user_id FROM users WHERE name = '%s'", name));
                return (resSet.next()) ? resSet.getInt("user_id") : 0;
            } catch(SQLException e){
                System.out.println(e.getMessage());
                throw new RuntimeException(e);
            }
        }

        public static ArrayList<String> selectLastMessages(Connection conn, Integer limitValue) {
            try {
                ArrayList<String> messages = new ArrayList<String>();
                Statement statmt = conn.createStatement();
                ResultSet resSet;
                resSet = statmt.executeQuery(String.format("SELECT * FROM (SELECT * FROM messages ORDER BY message_id DESC LIMIT %d) ORDER BY message_id ASC;", limitValue));
                while (resSet.next()) {
                    messages.add(resSet.getString("message"));
                }
                return messages;
            } catch(SQLException e){
                System.out.println(e.getMessage());
                throw new RuntimeException(e);
            }
        }

        public static Integer isSpecialHistoryMessage(String message) {

            Pattern p = Pattern.compile("history (\\d+)");
            Matcher m = p.matcher(message);
            String historySteps = "";
            
            while (m.find()) {
                historySteps = m.group(1);
            }

            Integer steps = historySteps.length() > 0 ? Integer.parseInt(historySteps) : -1;
            
            return steps;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder builder = new StringBuilder();

            if("POST".equals(exchange.getRequestMethod())) {
                System.out.println(exchange.getRequestMethod());
                InputStream eis = exchange.getRequestBody();
                Headers headers = exchange.getRequestHeaders();
                
                String auth_header = headers.getFirst("Authorization");

                String text = new String(eis.readAllBytes(), StandardCharsets.UTF_8);

                JSONObject jo = new JSONObject(text);
                
                String name = jo.get("name").toString();
                String message = jo.get("message").toString();
                
                Connection conn = null;  
                try {  
                    // db parameters  
                    String url = "jdbc:sqlite:users_and_messages.db";  
                    // create a connection to the database  
                    conn = DriverManager.getConnection(url);
                    
                    System.out.println("Connection to SQLite has been established.");
                    
                    if(tokenIsCorrect(auth_header, name)) {

                        Integer user_id = obtainUserId(name, conn);
                        Integer specialHistoryMessage = isSpecialHistoryMessage(message);
                        List<String> lastMessages;
                        JSONArray ja_m = new JSONArray();

                        if( specialHistoryMessage >= 0) {
                            lastMessages = selectLastMessages(conn, specialHistoryMessage);
                            for( String aMessage : lastMessages) {
                                ja_m.put(aMessage);
                            }
                        } else if(user_id > 0) {
                            saveMessageToDatabase(message, user_id, conn);
                        }

                        builder.append(ja_m.toString());

                        byte[] bytes = builder.toString().getBytes();
                        exchange.sendResponseHeaders(200, bytes.length);

                        OutputStream os = exchange.getResponseBody();
                        os.write(bytes);
                        os.close();
                    } else {
                        builder.append("token isn't correct");

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