import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;

public class ExampleTest1 {
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

    public static final String secret = "fj32Jfv02Mq33g0f8ioDkw";
    public static String createToken(String email)
    {
        try {
            return JWT.create()
                    .withIssuer("auth0")
                    .withClaim("email", email)
                    .sign(Algorithm.HMAC256(secret));
        } catch (JWTCreationException exception){
            throw new RuntimeException("You need to enable Algorithm.HMAC256");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public static String getEmailInToken(String token)
    {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer("auth0")
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("email").asString();
        } catch (JWTDecodeException exception){
            return null;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
    public static void main(String[] args) throws Exception {
        System.out.println("123");

        final MessageDigest md = MessageDigest.getInstance("SHA-256");
        final byte[] hashbytes = md.digest("181483331264".getBytes(StandardCharsets.UTF_8));
        // String sha3Hex = bytesToHex(hashbytes);
        System.out.println(new String(hashbytes, StandardCharsets.UTF_8));
        System.out.println(toHex(hashbytes));
        
        System.out.println(createToken("123"));
        System.out.println(createToken("456"));
        System.out.println(getEmailInToken(createToken("456")));
        
    }
}