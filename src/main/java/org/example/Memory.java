package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class Memory {
    private String filePath = "/choose.json";
    private final ObjectMapper mapper = new ObjectMapper();
    private Data[] data = new Data[0];

    public Data[] getData() {
        return data;
    }

    public void setData(Data[] data) {
        this.data = data;
    }

    public void reConnect(String new_path) {
        filePath = new_path;
    }

    public void read() {
        try {
            data = mapper.readValue(getClass().getResourceAsStream(filePath), Data[].class);
        } catch (IOException e) {
            System.err.println("[ERROR]: " + e.getMessage());
        }
    }
}
