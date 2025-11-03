package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Token {
    String token = "";

    public String get() {
        this.load();
        return token;
    }

    private void load() {
        String fileName = "/bot_token.txt";

        try (InputStream is = getClass().getResourceAsStream(fileName)) {
            assert is != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                token = reader.readLine();
                System.out.println("token loaded");
            }
        } catch (IOException e) {
            System.err.println("[ERROR]: " + e.getMessage());
        }
    }
}
