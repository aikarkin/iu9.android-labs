package android.iu9.bmstu.ru.lab4;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

public class EmailActivity extends AppCompatActivity {
    private static final String TAG = "EmailActivity";

    public static final String EMAIL_SUBJECT_KEY = "email_subject";
    public static final String EMAIL_TO_KEY = "email_to";
    public static final String EMAIL_CONTENT_KEY = "email_content";

    public static final int RESULT_SAVE = 0;
    public static final int RESULT_CANCEL = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);

        final Intent currentIntent = getIntent();

        Button btnCancel = findViewById(R.id.button_email_cancel),
                btnSave = findViewById(R.id.button_email_save);


        btnCancel.setOnClickListener((btn) -> {
            EmailActivity.this.setResult(RESULT_CANCEL);
            EmailActivity.this.finish();
        });

        btnSave.setOnClickListener((btn) -> {
            String subject = getTextViewContent(R.id.edit_subject),
                    to = getTextViewContent(R.id.edit_to),
                    content = getTextViewContent(R.id.matv_email_content);

            Log.i(TAG, "subject: " + subject);
            Log.i(TAG, "to: " + to);
            Log.i(TAG, "content: " + content);

            currentIntent.putExtra(EMAIL_TO_KEY, to);
            currentIntent.putExtra(EMAIL_SUBJECT_KEY, subject);
            currentIntent.putExtra(EMAIL_CONTENT_KEY, content);

            EmailActivity.this.setResult(RESULT_SAVE, currentIntent);
            EmailActivity.this.finish();
        });

    }

    private String getTextViewContent(int id) {
        TextView tv = findViewById(id);
        if(tv != null) {
            CharSequence text = tv.getText();

            return (text != null && text.length() > 0) ? text.toString() : null;
        }

        return null;
    }
}
