package ru.bmstu.iu9.lab9;

import android.util.SparseArray;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testSparseArray() {
        SparseArray<String> arr = new SparseArray<>();

        arr.append(1, "asdfasdf");
        arr.append(4, "asdf");

        System.out.println(arr.get(1));
        System.out.println(arr.get(2));
        System.out.println(arr.get(4));
    }
}