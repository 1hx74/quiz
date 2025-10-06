package org.example;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

class QuizTest {

public
    @Test
    void testAdd() {
        assertEquals(1, 1);
    }

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
    //Method memory = Memory.class.getDeclaredMethod("Memory");

    }
}
