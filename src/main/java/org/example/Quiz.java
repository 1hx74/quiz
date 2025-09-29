package org.example;

import java.util.Arrays;
import java.util.Scanner;

public class Quiz {
    public void runtime(Memory memory) {
        Scanner sc = new Scanner(System.in);
        String[] alphabet = {"A","B","C","D","E","F","G","H","I","J","K","L"};
        int score = 0;

        for (int i = 0; i < memory.data.length; i++) {

            System.out.println("Вопрос №" + (i + 1) + ") " + memory.data[i].getQuestion());

            for (int j = 0; j < memory.data[i].getOptions().length; j++) {
                System.out.println(alphabet[j] + ") " + memory.data[i].getOptions()[j]);
            }

            System.out.print("\nВаш ответ: ");

            String ans = sc.next().toUpperCase();
            int index_ans = Arrays.asList(alphabet).indexOf(ans);

            if (memory.data[i].validAnswer(index_ans)) {
                System.out.println("Верно!");
                score++;
            } else {
                System.out.println("К сожалению это не правильный ответ, правильным был вариант " +
                        alphabet[memory.data[i].getAnswer()] + ") " +
                        memory.data[i].getOptions()[memory.data[i].getAnswer()]);
            }

            System.out.print("\n");
        }

        System.out.println("Ваш счёт: " + score + " из " + memory.data.length);
    }
}
