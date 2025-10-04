package org.example;

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

        Memory memory = new Memory();
        memory.read();
        Quiz quiz = new Quiz();
        quiz.choose(memory);

    }
}
