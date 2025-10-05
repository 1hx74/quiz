package org.example;

import java.util.Arrays;
import java.util.Scanner;

public class Quiz {
    /*      private!     */
    Scanner sc = new Scanner(System.in);
    String[] alphabet = {"A","B","C","D","E","F","G","H","I","J","K","L"};

    private void printQuestion(Memory memory, int index, boolean choose) {
        if (choose) {
            System.out.println(memory.data[index].getQuestion());
        }
        else{
            System.out.println("Вопрос №" + (index + 1) + ") " + memory.data[index].getQuestion());
        }
        for (int j = 0; j < memory.data[index].getOptions().length; j++) {
            System.out.println(alphabet[j] + ") " + memory.data[index].getOptions()[j]);
        }
        if (choose) {
            System.out.print("\nВаш ответ: ");
        }
        else {
            System.out.print("\nВаш выбор:");
        }
    }

    private int getIdxAnswer() {
        String answer = sc.next().toUpperCase();
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
        if (choose) {
            memory.reConnect("/" + memory.data[index].getOptions()[answer] + ".json");
            memory.read();
            Quiz quiz = new Quiz();
            quiz.run(memory);
            System.exit(0);
        }
        else  {
            if (memory.data[index].validAnswer(answer)) {
                System.out.println("Верно!");
                return 1;
            } else {
                System.out.println("К сожалению это не правильный ответ, правильным был вариант " +
                        alphabet[memory.data[index].getAnswer()] + ") " +
                        memory.data[index].getOptions()[memory.data[index].getAnswer()]);
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

        for (int i = 0; i < memory.data.length; i++) {
            printQuestion(memory, i, choose);
            score += prodAnswer(memory, i, getIdxAnswer(), choose);

            System.out.print("\n");     // it's for GUI format
        }

        if (!choose) {                  // return score
            System.out.println("Ваш счёт: " + score + " из " + memory.data.length);
        }
    }
}
