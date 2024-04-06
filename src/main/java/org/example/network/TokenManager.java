package org.example.network;

import java.util.Random;

public class TokenManager {
    public static String token = "";

    private static String includedCharacters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ{}[]|+-*/_.<>{}[]()~'\"\\!@#$%^&?,";

    public static void generateNewRandomToken() {
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for(int  i = 0; i < 40; i++) {
            sb.append(includedCharacters.charAt(random.nextInt(includedCharacters.length())));
        }
        token = sb.toString();
    }
}
