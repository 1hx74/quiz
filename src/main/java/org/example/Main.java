package org.example;

public class Main {
    public static void main(String[] args) {
        Memory memory = new Memory();
        memory.read();
        Quiz quiz = new Quiz();
        quiz.run(memory, true);

        //Users users = new Users();

        //Token token = new Token();

        //Bot bot = new Bot(token.get());
        //bot.start();

    }
}