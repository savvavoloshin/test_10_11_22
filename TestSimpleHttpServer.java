import static org.junit.Assert.*;
import org.junit.Test;  
  
public class TestSimpleHttpServer {
    @Test
    public void createJWTToken() {
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJhdXRoMCIsIm5hbWUiOiJzYXZ2YS52b2xvc2hpbiJ9.B1OTtby9PvGz0EPfV9QtRXWFm81oJUxB-PdjVDA4IVw";
        assertEquals(token,SimpleHttpServer.createJWTToken("savva.voloshin"));  
        assertNotEquals(token,SimpleHttpServer.createJWTToken("savva.voloshin.1"));  
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