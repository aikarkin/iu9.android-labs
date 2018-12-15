package android.iu9.bmstu.ru.lab4;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int PICK_CONTACT_REQUEST = 1;
    private static final int ENTER_EMAIL_REQUEST = 2;

    private static final double LATITUDE = 55.7649979;
    private static final double LONGITUDE = 37.68406129999994;
    private static final String YOUTUBE_LINK = "https://www.youtube.com/watch?v=4AG-h-zoY4g";

    private static String selectedContactNumber = null;

    private static void startUriIntent(Activity activity, Uri uri, int errorMessageId) {
        Intent uriOpen = new Intent(Intent.ACTION_VIEW, uri);
        if(uriOpen.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(uriOpen);
        } else {
            Log.e(TAG, activity.getResources().getString(errorMessageId));
            TextView errTxt = activity.findViewById(R.id.errorText);
            if(errTxt != null) {
                errTxt.setText(errorMessageId);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button openMapBtn = findViewById(R.id.open_map),
                pickContactBtn = findViewById(R.id.pick_contact),
                openBrowserBtn = findViewById(R.id.open_browser),
                sendEmail = findViewById(R.id.send_email);

        final TextView errText = findViewById(R.id.errorText);
        final Context ctx = MainActivity.this;

        openMapBtn.setOnClickListener((View btn) -> {
            Uri mapUri = Uri.parse(
                    String.format(Locale.ENGLISH, "geo:%f,%f", LATITUDE, LONGITUDE)
            );
            Log.i(TAG, "Point uri: " + mapUri.toString());
            startUriIntent(this, mapUri, R.string.msg_map_open_failed);
        });

        pickContactBtn.setOnClickListener((View btn) -> {
            Intent pickContact = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
            pickContact.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
            if(pickContact.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(pickContact, PICK_CONTACT_REQUEST);
            } else {
                errText.setText(R.string.msg_pickcontact_fail);
            }
        });

        openBrowserBtn.setOnClickListener((View btn) -> {
            startUriIntent(this, Uri.parse(YOUTUBE_LINK), R.string.msg_browseropen_failed);
        });

        sendEmail.setOnClickListener((View btn) -> {
            Intent emailIntent = new Intent(MainActivity.this, EmailActivity.class);
            startActivityForResult(emailIntent, ENTER_EMAIL_REQUEST);

//            Intent emailIntent = new Intent(Intent.ACTION_SEND);
//            emailIntent.setType("text/plain");
//            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {DEST_EMAIL});
//            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Android Development - Email sent intent");
//            emailIntent.putExtra(Intent.EXTRA_TEXT, "Test message from my app.");
//            startActivity(emailIntent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_CONTACT_REQUEST) {
            TextView contactOut = findViewById(R.id.selected_contact);

            if(resultCode == RESULT_OK) {
                Uri contactUri = data.getData();
                Log.i(TAG, "Contact uri: " + (contactUri != null ? contactUri.toString() : "[empty]"));

                if(contactUri != null) {
                    String[] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
                    try (Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null)) {
                        if(cursor != null) {
                            cursor.moveToFirst();
                            int numberIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER),
                                    nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

                            String name = cursor.getString(nameIdx),
                                    number = cursor.getString(numberIdx);

                            contactOut.setText(
                                    String.format("Selected contact: %s / %s", name, number)
                            );

                            selectedContactNumber = String.format("%s (%s)", number, name);
                        } else {
                            Log.e(TAG, "Failed to create contact cursor");
                            ((TextView)findViewById(R.id.errorText)).setText(R.string.msg_open_contact_failed);
                        }
                    }
                }

            }
            else if(resultCode == RESULT_CANCELED) {
                contactOut.setText(R.string.msg_no_contact);
            }
        } else if(requestCode == ENTER_EMAIL_REQUEST && resultCode == EmailActivity.RESULT_SAVE) { // when email entered...
            String to = data.hasExtra(EmailActivity.EMAIL_TO_KEY) ? data.getStringExtra(EmailActivity.EMAIL_TO_KEY) : null,
                subject = data.hasExtra(EmailActivity.EMAIL_SUBJECT_KEY) ? data.getStringExtra(EmailActivity.EMAIL_SUBJECT_KEY) : null,
                content = data.hasExtra(EmailActivity.EMAIL_CONTENT_KEY) ? data.getStringExtra(EmailActivity.EMAIL_CONTENT_KEY) : null;

            Log.i(TAG, "subject: " + subject);
            Log.i(TAG, "to: " + to);
            Log.i(TAG, "content: " + content);

            if(selectedContactNumber != null) {
                content += "\n\n\nTelephone number: " + selectedContactNumber;
            }

            sendEmail(to, subject, content);
        }
    }

    private void sendEmail(String to, String subject, String body) {
        Intent emailSend = new Intent(Intent.ACTION_SEND);
        emailSend.setType("text/plain");

        if(to != null)
            emailSend.putExtra(Intent.EXTRA_EMAIL, new String[] {to});
        if(subject != null)
            emailSend.putExtra(Intent.EXTRA_SUBJECT, subject);
        if(body != null)
            emailSend.putExtra(Intent.EXTRA_TEXT, body);

        startActivity(emailSend);
    }
}
