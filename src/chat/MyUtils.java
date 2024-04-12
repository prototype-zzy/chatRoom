package chat;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Properties;

public class MyUtils {

    private static final Key AESKey;

    static {
        Properties properties;
        try {
            properties = new Properties();
            properties.load(Files.newInputStream(Paths.get("config.properties")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        AESKey = new SecretKeySpec(properties.getProperty("seed").getBytes(), "AES");

    }


    public static String encrypt(String input) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, AESKey);
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return Base64.getEncoder().encodeToString(cipherText);
    }

}
