package org.example;

import java.util.Arrays;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;

class Qiuzoptions {
    public String question;
    public String[] option;
    public Qiuzoptions(){

    }
}
class file {
    String file_path = "src/main/resources/file2.json";
    public Qiuzoptions[] data = new Qiuzoptions[]{new Qiuzoptions()};
    ObjectMapper mapper = new ObjectMapper();

    public void read() throws IOException {

        if ("src/main/resources/file2.json".equals(file_path)) {
            var input = getClass().getClassLoader().getResourceAsStream("file2.json");
            if (input == null) {
                System.out.printf("Json file undefined");
                return;
            }
            data = mapper.readValue(input, Qiuzoptions[].class);
        }

    }
}
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
 \s
    \s""";
        //Пути к файлам/
        String raznoe = "src/main/resources/raznoe.json";
        String stolici= "src/main/resources/stolici.json";
        String countries = "src/main/resources/countries.json";
        String space = "src/main/resources/space.json";
        Memory memory = new Memory();
        Quiz quiz = new Quiz();

        file read = new file();
        try {
            read.read();
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        System.out.printf(read.data[0].question);
        System.out.printf("\n");
        for(int j =0;j<read.data.length;j++) {
            for (int i = 0; i < read.data[j].option.length; i++) {
                System.out.printf(read.data[j].option[i]);
                System.out.printf("\n");
            }
            Scanner in = new Scanner(System.in);
            String answer;
            System.out.printf("Введите букву для выбора:");
            answer = in.nextLine();
            switch(answer){
                case "a":
                    System.out.printf("Вы выбрали тему:Столицы\n");
                    System.out.printf("Викторина началась!\n");
                    memory.reConnect(stolici);

                    break;
                case "b":
                    System.out.printf("Вы выбрали тему:Страны\n");
                    System.out.printf("Викторина началась!\n");
                    memory.reConnect(countries);
                    break;
                case "c":
                    System.out.printf("Вы выбрали тему:Космос\n");
                    System.out.printf("Викторина началась!\n");
                    memory.reConnect(space);
                    break;
                case "d":
                    System.out.printf("Вы выбрали тему:Разное\n");
                    System.out.printf("Викторина началась!\n");
                    memory.reConnect(raznoe);
                    break;
            }
        }
        if (args.length > 1) {
            System.out.println("too many arguments");
            return;
        }


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
