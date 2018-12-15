package android.iu9.bmstu.ru.rkapp;

import org.junit.Test;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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
    public void testTimeValue() {
        long time = 1539216000L * 1000;
        Date dt = new Date(time);
        //LocalDate date = new LocalDate(time);
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        assertEquals("11.10.2018", df.format(dt));
    }
}