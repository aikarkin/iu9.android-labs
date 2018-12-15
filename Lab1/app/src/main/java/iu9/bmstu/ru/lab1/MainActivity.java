package iu9.bmstu.ru.lab1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextClock;
import android.widget.TextView;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        

        Button btn = findViewById(R.id.button);

        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText editText = findViewById(R.id.editText);
                TextView txt = findViewById(R.id.txt_1);
                txt.setText(editText.getText());
            }
        });

//        ScrollView scroll = findViewById(R.id.scroll_1);

//        TextView txt1 = new TextView(getApplicationContext());
//        txt1.setText("2");

//        ListView listView = findViewById(R.id.listView2);
//        String[] names = new String[] {"a", "b", "c"};
//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.activity_main, R.id.listView2, names);
//        listView.setAdapter(arrayAdapter);

//        scroll.addView(txt1);
    }
}
