package org.example;

import java.util.Arrays;
import java.util.Scanner;

public class Quiz {
    public void runtime (Memory memory){
        String[] text = {"Вопрос №", "\nВаш ответ: "};
        boolean[] args = {true};
        run(memory, text, args);
    }

    public void choose (Memory memory){
        String[] text = {"", "\nВаш выбор:" };
        boolean[] args = {false};
        run(memory, text, args);
    }

    private void run(Memory memory, String[] text, boolean[] args) {
        Scanner sc = new Scanner(System.in);
        String[] alphabet = {"A","B","C","D","E","F","G","H","I","J","K","L"};
        int score = 0;

        for (int i = 0; i < memory.data.length; i++) {
            if(args[0]) {
                System.out.println(text[0] + (i + 1) + ") " + memory.data[i].getQuestion());
            }
            else{
                System.out.println(memory.data[i].getQuestion());
            }
            for (int j = 0; j < memory.data[i].getOptions().length; j++) {
                System.out.println(alphabet[j] + ") " + memory.data[i].getOptions()[j]);
            }

            System.out.print(text[1]);

            String ans = sc.next().toUpperCase();
            int index_ans = Arrays.asList(alphabet).indexOf(ans);
            if (args[0]) {
                if (memory.data[i].validAnswer(index_ans)) {
                    System.out.println("Верно!");
                    score++;
                } else {
                    System.out.println("К сожалению это не правильный ответ, правильным был вариант " +
                            alphabet[memory.data[i].getAnswer()] + ") " +
                            memory.data[i].getOptions()[memory.data[i].getAnswer()]);
                }
            }
            else {
                memory.reConnect("/" + memory.data[i].getOptions()[index_ans] + ".json");
                memory.read();
                Quiz quiz = new Quiz();
                quiz.runtime(memory);
            }

            System.out.print("\n");
        }
        if (args[0]) {
            System.out.println("Ваш счёт: " + score + " из " + memory.data.length);
        }
    }
}
