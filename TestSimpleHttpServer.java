import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
// you don't need to register classname this after Java 7 I guess.
// Class.forName("org.h2.Driver");
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestSimpleHttpServer {

    @Test
    public void createJWTToken() {
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJhdXRoMCIsIm5hbWUiOiJzYXZ2YS52b2xvc2hpbiJ9.B1OTtby9PvGz0EPfV9QtRXWFm81oJUxB-PdjVDA4IVw";
        assertEquals(token,SimpleHttpServer.createJWTToken("savva.voloshin"));
        assertNotEquals(token,SimpleHttpServer.createJWTToken("savva.voloshin.1"));  
    }  
    
    @Test
    public void passwordIsCorrect() {
        
        boolean passwordIsCorrect = false;
        try (
            Connection conn = DriverManager.getConnection("jdbc:h2:~/db;INIT=RUNSCRIPT FROM 'classpath:create_sample_data.sql'");
            ) {
                passwordIsCorrect = SimpleHttpServer.LoginHandler.passwordIsCorrect("savva.voloshin", "123456", conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
        assertTrue(passwordIsCorrect);
    }
    @Test
    public void saveMessageToDatabase() {
        
        Integer messagesCount = 0;
        try (
            Connection conn = DriverManager.getConnection("jdbc:h2:~/db;INIT=RUNSCRIPT FROM 'classpath:create_sample_data.sql'");
            ) {
                SimpleHttpServer.MessagesHandler.saveMessageToDatabase("a test message", 1, conn);
                SimpleHttpServer.MessagesHandler.saveMessageToDatabase("a test message 2", 1, conn);
                SimpleHttpServer.MessagesHandler.saveMessageToDatabase("a test message 3", 1, conn);

                Statement stmt = conn.createStatement();
                ResultSet resultSet = stmt.executeQuery("SELECt count(*) FROM messages");
                
                if (resultSet.next()) {
                    messagesCount = resultSet.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
        assertTrue(messagesCount == 15);
    }
    @Test
    public void obtainUserId() {
        
        Integer id = 0;
        try (
            Connection conn = DriverManager.getConnection("jdbc:h2:~/db;INIT=RUNSCRIPT FROM 'classpath:create_sample_data.sql'");
            ) {
                id = SimpleHttpServer.MessagesHandler.obtainUserId("savva.voloshin.2", conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
        assertTrue(id == 2);
    }
    @Test
    public void selectLastMessages() {
        
        ArrayList<String> messages = new ArrayList<String>();
        try (
            Connection conn = DriverManager.getConnection("jdbc:h2:~/db;INIT=RUNSCRIPT FROM 'classpath:create_sample_data.sql'");
            ) {
                messages = SimpleHttpServer.MessagesHandler.selectLastMessages(conn, 11);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
        assertTrue(messages.size() == 11);
    }

    @Test
    public void getHash() {
        assertEquals("8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92", SimpleHttpServer.LoginHandler.getHash("123456"));  
        assertNotEquals("8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92", SimpleHttpServer.LoginHandler.getHash("1234567"));  
    }
    @Test
    public void tokenIsCorrect() {
        String token = "Bearer_eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJhdXRoMCIsIm5hbWUiOiJzYXZ2YS52b2xvc2hpbiJ9.B1OTtby9PvGz0EPfV9QtRXWFm81oJUxB-PdjVDA4IVw";
        String name = "savva.voloshin";
        assertEquals(true, SimpleHttpServer.MessagesHandler.tokenIsCorrect(token, name));  
        assertEquals(false, SimpleHttpServer.MessagesHandler.tokenIsCorrect(token, "name"));  
    }  
    @Test
    public void isSpecialHistoryMessage() {
        assertEquals(true, SimpleHttpServer.MessagesHandler.isSpecialHistoryMessage("history 12") == 12);  
        assertEquals(true, SimpleHttpServer.MessagesHandler.isSpecialHistoryMessage("history -3") == -1);  
    }  
}