package org.example;

public class Main {
    public static void main(String[] args) {

        Memory memory = new Memory();
        memory.read();
        Quiz quiz = new Quiz();
        quiz.run(memory, true);

    }
}
