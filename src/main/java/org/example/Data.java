package org.example;

public class Data {
    private final String question;
    private final String[] options;
    private final int answer;

    Data(String question, String[] options, int answer) {
        this.question = question;
        this.options = options;
        this.answer = answer;
    }


    public String getQuestion() {
        return question;
    }

    public String[] getOptions() {
        return options;
    }

    public int getAnswer() {
        return answer;
    }

    public boolean validAnswer(int userAnswer) {
        return userAnswer == answer;
    }
}
