package org.example;

import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String help_message = """
    Usage\s
    for a classic qiuz:
    \tjava -jar ./out/artifacts/first_jar/first.jar
    or usage for your quiz
    \tjava -jar ./out/artifacts/first_jar/first.jar /your/path/to/question\s
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

        File read = new File();
        try {
            read.read();
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        System.out.println(read.data[0].question);

        for(int j =0;j<read.data.length;j++) {
            for (int i = 0; i < read.data[j].option.length; i++) {
                System.out.println(read.data[j].option[i]);

            }
            Scanner in = new Scanner(System.in);
            String answer;
            System.out.print("Введите букву для выбора:");
            answer = in.nextLine();
            switch(answer){
                case "a":
                    System.out.println("Вы выбрали тему:Столицы");
                    System.out.println("Викторина началась!");
                    memory.reConnect(stolici);

                    break;
                case "b":
                    System.out.println("Вы выбрали тему:Страны");
                    System.out.println("Викторина началась!");
                    memory.reConnect(countries);
                    break;
                case "c":
                    System.out.println("Вы выбрали тему:Космос");
                    System.out.println("Викторина началась!");
                    memory.reConnect(space);
                    break;
                case "d":
                    System.out.println("Вы выбрали тему:Разное");
                    System.out.println("Викторина началась!");
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
