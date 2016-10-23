import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alanchern on 10/23/16.
 * Custom file-exporter.
 */

public class FileExporter {

    private static final String TAG = "FileExporter";
    private static final String REGEX = "[\\\\/:\\*?\"<>|]";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    public static void export(@NonNull Context context, @NonNull List<String> inputText, int maxNameLength, @NonNull String directoryName) {
        if (inputText.size() == 0) {
            Toast.makeText(context, "Input empty", Toast.LENGTH_SHORT).show();
        } else if (maxNameLength < 1 || directoryName.isEmpty()) {
            throw (new IllegalArgumentException("maxNameLength must be equal to or greater than 1, and directoryName must be non-empty"));
        } else {
            EditText inputEditText = new EditText(context);
            setupEditText(context, inputEditText, maxNameLength);

            showDialog(context, inputEditText, inputText, directoryName);
        }
    }

    private static void setupEditText(final Context context, EditText inputEditText, int maxNameLength) {
        // set default name as current date and time
        inputEditText.setText(DateFormat.format("yyyy_MM_dd_HH_mm_ss", new Date()).toString());
        inputEditText.setSelectAllOnFocus(true);
        inputEditText.setInputType(
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        InputFilter[] filter = new InputFilter[2];
        filter[0] = new InputFilter.LengthFilter(maxNameLength);

        // use regex to check for invalid filename characters
        filter[1] = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart,
                                       int dend) {
                Matcher charMatcher = PATTERN.matcher(source);
                if (charMatcher.find()) {
                    Toast.makeText(context, "Invalid character", Toast.LENGTH_LONG).show();
                    return "";
                } else {
                    return null;
                }
            }
        };
        inputEditText.setFilters(filter);
    }

    private static void showDialog(final Context context, final EditText inputEditText, final List<String> inputText, final String directoryName) {
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setTitle("Enter filename");
        dialog.setView(inputEditText);
        dialog.setCancelable(true);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        break;
                }
            }
        };

        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Save", listener);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", listener);
        dialog.show();

        Button saveButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        saveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String filename = inputEditText.getText().toString();
                Matcher filenameMatcher = PATTERN.matcher(filename);

                if ((filename.length() == 0) || (filenameMatcher.find())) {
                    // if no filename entered or contains invalid characters
                    Toast.makeText(context, "Please enter a valid filename", Toast.LENGTH_SHORT).show();
                } else {
                    File fileDir = new File(Environment.getExternalStorageDirectory() + File.separator + directoryName);
                    fileDir.mkdirs();

                    filename = filename + ".txt";
                    File file = new File(fileDir, filename);

                    if (file.exists()) {
                        Toast.makeText(context, "File already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));

                        for (int i = 0; i < inputText.size(); i++) {
                            bufferedWriter.write(inputText.get(i) + "\n");
                        }
                        bufferedWriter.close();

                        Toast.makeText(context, "File successfully saved as: " + " \"" + filename + "\"", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } catch (IOException e) {
                        Log.e(TAG, "File export error: " + e.toString());

                        Toast.makeText(context, "File export error", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
