package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

class Memory {
    String filePath = "src/main/resources/file.json";
    public Data[] data = new Data[]{new Data()};
    ObjectMapper mapper = new ObjectMapper();

    public void reConnect(String new_path) {
        filePath = new_path;
    }

    public void read() {
        try {
            if ("src/main/resources/file.json".equals(filePath)) {
                try (var input = getClass().getClassLoader().getResourceAsStream("file.json")) {
                    if (input == null) {
                        throw new IOException("file.json not found in resources");
                    }
                    data = mapper.readValue(input, Data[].class);
                }
            } else {
                data = mapper.readValue(new File(filePath), Data[].class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
