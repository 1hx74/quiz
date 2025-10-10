package org.example;

import org.junit.jupiter.api.Test;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;

class QuizTest {
    @Test
    public void testMethodGetIdxAnswer() {
        String input = "A\nB\nhelp\n3\n";
        int[] expected = {0, 1, -1, 2};

        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        Quiz quiz = new Quiz();

        for (int j : expected) {
            int index = quiz.getIdxAnswer();
            assertEquals(j, index);
        }
    }

    @Test
    public void testPrivateMethodQuizRun(){
        /*      data preparation      */
        Memory memory = new Memory();
        Data[] data = new Data[3];

        Data q1 = new Data();
        q1.setQuestion("question1");
        q1.setOptions(new String[] { "answer1", "answer2", "answer3", "answer4" });
        q1.setAnswer(2);

        Data q2 = new Data();
        q2.setQuestion("question2");
        q2.setOptions(new String[] { "answer1", "answer2", "answer3", "answer4" });
        q2.setAnswer(0);

        Data q3 = new Data();
        q3.setQuestion("question3");
        q3.setOptions(new String[] { "answer1", "answer2", "answer3", "answer4" });
        q3.setAnswer(3);

        data[0] = q1;
        data[1] = q2;
        data[2] = q3;

        memory.setData(data);

        /*      input interception      */
        String answer = "B\nhelp\n1\nD";
        InputStream in = new ByteArrayInputStream(answer.getBytes());
        System.setIn(in);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        /*      execution     */
        Quiz quiz = new Quiz();
        quiz.run(memory,false);

        /*      restoration      */
        System.setOut(originalOut);


        /*      assertions      */
        String output = outContent.toString();

        assertTrue(output.contains("Usage"),
                "Output does not contain help message (usage)");

        assertTrue(output.contains("Ваш счёт: 2 из 3"),
                "Output does not contain score 2");
    }
}
