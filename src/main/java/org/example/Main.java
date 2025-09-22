package org.example;

import java.util.Arrays;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

class Data {
    public String question;
    public String[] options;
    public int answer;

    public String ConvertToString(String type) {
        return switch (type) {
            case "c" -> "Data {\n" +
                    "   question=" + question + ",\n" +
                    "   options=" + Arrays.toString(options) + ",\n" +
                    "   answer=" + answer +
                    "\n}";
            case "q" -> "Вопрос: " + question + ",\n" + Arrays.toString(options);
            case "a" -> "" + answer;
            default -> "None";
        };
    }
}

class Memory {
    String file_path = "src/main/resources/file.json";
    public Data[] data = new Data[]{new Data()};
    ObjectMapper mapper = new ObjectMapper();

    public void reConnect(String new_path) {
        file_path = new_path;
    }

    public void read() {
        try {
            if ("src/main/resources/file.json".equals(file_path)) {
                try (var input = getClass().getClassLoader().getResourceAsStream("file.json")) {
                    if (input == null) {
                        throw new IOException("file.json not found in resources");
                    }
                    data = mapper.readValue(input, Data[].class);
                }
            } else {
                data = mapper.readValue(new File(file_path), Data[].class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write() {
        try {
            mapper.writeValue(new File(file_path), data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class Main {
    public static void main(String[] args) {
        String help_message = """
    Usage 
    for a classic qiuz:
    \tjava -jar ./out/artifacts/first_jar/first.jar
    or usage for your quiz
    \tjava -jar ./out/artifacts/first_jar/first.jar /your/path/to/question 
    You can see this message if you use -h or --help
    To see imput file format check us repository:
    https://github.com/1hx74/quiz
 
    """;


        if (args.length > 1) {
            System.out.println("too many arguments");
            return;
        }

        Memory memory = new Memory();
        Quiz quiz = new Quiz();

        if (args.length == 1) {
            if (Objects.equals(args[0], "-h") || Objects.equals(args[0], "--help")) {
                System.out.println(help_message);
                return;
            }
            else {
                memory.reConnect(args[0]);
            }
        }

        memory.read();
        quiz.runtime(memory);

    }
}
