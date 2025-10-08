package org.example;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;

class QuizTest {
    @Test
    public void testPrivateMethodGetIdxAnswer() throws Exception {
        String input = "A\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Quiz quiz = new Quiz();
        Method addMethod = Quiz.class.getDeclaredMethod("getIdxAnswer");
        addMethod.setAccessible(true);

        int index = (int) addMethod.invoke(quiz);
        assertEquals(0, index);
    }

    @Test
    public void testPrivateMethodQuizRun(){
        /*      подготовка данных       */
        Memory memory = new Memory();
        memory.data = new Data[3];

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

        memory.data[0] = q1;
        memory.data[1] = q2;
        memory.data[2] = q3;

        /*      перехват        */
        String answer = "B\nhelp\n1\nD";
        InputStream in = new ByteArrayInputStream(answer.getBytes());
        System.setIn(in);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        /*      выполнение          */
        Quiz quiz = new Quiz();
        quiz.run(memory,false);

        /*      восстановление      */
        System.setOut(originalOut);


        /*      проверки        */
        String output = outContent.toString();

        assertTrue(output.contains("Usage"),
                "Вывод не содержит справку (usage)");

        assertTrue(output.contains("Ваш счёт: 2 из 3"),
                "В выводе не найден счёт 2");
    }
}
