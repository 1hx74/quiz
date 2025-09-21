package org.example;

import java.util.Arrays;
import java.util.Scanner;

public class Quiz {
    private void clearTerminal(int lines) {
        for (int i = 0; i < lines; i++) {
            System.out.print("\033[F");
            System.out.print("\033[2K");
        }
    }

    public void runtime(Memory memory) {
        Scanner sc = new Scanner(System.in);
        String[] alphabet = {"A","B","C","D","E","F","G","H","I","J","K","L"};
        // int prevLines = 0;
        int score = 0;

        for (int i = 0; i < memory.data.length; i++) {

            //if (i > 0) {
            //    clearTerminal(prevLines);
            //}

            //int linesPrinted = 0;
            System.out.println("Вопрос №" + (i + 1) + ") " + memory.data[i].question);
            //linesPrinted++;

            for (int j = 0; j < memory.data[i].options.length; j++) {
                System.out.println(alphabet[j] + ") " + memory.data[i].options[j]);
                //linesPrinted++;
            }

            System.out.print("\nВаш ответ: ");
            //linesPrinted += 2;

            String ans = sc.next().toUpperCase();
            int index_ans = Arrays.asList(alphabet).indexOf(ans);

            if (index_ans == memory.data[i].answer) {
                System.out.println("Верно!");
                //linesPrinted++;
                score++;
            } else {
                System.out.println("К сожалению это не правильный ответ, правильным был вариант " +
                        alphabet[memory.data[i].answer] + ") " +
                        memory.data[i].options[memory.data[i].answer]);
                //linesPrinted++;
            }

            System.out.print("\n");
            //linesPrinted++;

            //prevLines = linesPrinted;
        }

        System.out.println("Ваш счёт: " + score + " из " + memory.data.length);
    }
}
