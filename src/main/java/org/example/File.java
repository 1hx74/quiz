package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

class File {
    String file_path = "src/main/resources/file2.json";
    public QiuzOptions[] data = new QiuzOptions[]{new QiuzOptions()};
    ObjectMapper mapper = new ObjectMapper();

    public void read() throws IOException {

        if ("src/main/resources/file2.json".equals(file_path)) {
            var input = getClass().getClassLoader().getResourceAsStream("file2.json");
            if (input == null) {
                System.out.println("Json file undefined");
                return;
            }
            data = mapper.readValue(input, QiuzOptions[].class);
        }

    }
}
