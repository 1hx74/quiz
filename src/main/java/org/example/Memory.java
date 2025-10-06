package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class Memory {
    String filePath = "/choose.json";
    public Data[] data = new Data[0];
    ObjectMapper mapper = new ObjectMapper();

    public void reConnect(String new_path) {
        filePath = new_path;
    }

    public void read() {
        try {
            data = mapper.readValue(getClass().getResourceAsStream(filePath), Data[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
