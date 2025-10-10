package org.example;

import java.util.Arrays;
import java.util.Scanner;

public class Quiz {
    /*      private!     */
    private final Scanner sc = new Scanner(System.in);
    private final String[] alphabet = {"A","B","C","D","E","F","G","H","I","J","K","L"};

    private void printQuestion(Memory memory, int index, boolean choose) {
        if (choose) {
            System.out.println(memory.getData()[index].getQuestion());
        }
        else{
            System.out.println("Вопрос №" + (index + 1) + ") " + memory.getData()[index].getQuestion());
        }
        for (int j = 0; j < memory.getData()[index].getOptions().length; j++) {
            System.out.println(alphabet[j] + ") " + memory.getData()[index].getOptions()[j]);
        }
        if (choose) {
            System.out.print("\nВаш ответ: ");
        }
        else {
            System.out.print("\nВаш выбор: ");
        }
    }

    int getIdxAnswer() {
        String answer = sc.next().toUpperCase();
        if (answer.equalsIgnoreCase("help")) {
            return -1;
        }
        try {
            int temp = Integer.parseInt(answer);
            if (temp >= 0 && temp <= alphabet.length) {
                return temp - 1;
            }
        } catch (NumberFormatException e) {
            // pass
        }

        return Arrays.asList(alphabet).indexOf(answer);
    }


    private int prodAnswer(Memory memory, int index, int answer, boolean choose) {
        if (answer == -1) {
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
            System.out.println(help_message);
            printQuestion(memory, index, choose);
            return prodAnswer(memory, index, getIdxAnswer(), choose);
        }
        if (choose) {
            memory.reConnect("/" + memory.getData()[index].getOptions()[answer] + ".json");
            memory.read();
            Quiz quiz = new Quiz();
            quiz.run(memory);
            System.exit(0);
        }
        else  {
            if (memory.getData()[index].validAnswer(answer)) {
                System.out.println("Верно!");
                return 1;
            } else {
                System.out.println("К сожалению это не правильный ответ, правильным был вариант " +
                        alphabet[memory.getData()[index].getAnswer()] + ") " +
                        memory.getData()[index].getOptions()[memory.getData()[index].getAnswer()]);
            }
        }
        return 0;
    }



    /*      public!     */
    public void run(Memory memory) {
        run(memory, false);
    }

    public void run(Memory memory, boolean choose) {
        int score = 0;

        for (int i = 0; i < memory.getData().length; i++) {
            printQuestion(memory, i, choose);
            score += prodAnswer(memory, i, getIdxAnswer(), choose);

            System.out.print("\n");     // it's for GUI format
        }

        if (!choose) {                  // return score
            System.out.println("Ваш счёт: " + score + " из " + memory.getData().length);
        }
    }
}
