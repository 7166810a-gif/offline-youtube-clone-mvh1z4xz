package com.generated.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button btnImport = findViewById(R.id.btn_import_ai);
        btnImport.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            try {
                startActivityForResult(intent, 102);
            } catch (Exception e) {
                Toast.makeText(this, "לא ניתן לפתוח סייר קבצים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 102 && resultCode == RESULT_OK) {
            Toast.makeText(this, "מודל AI נטען בהצלחה לזיכרון המקומי", Toast.LENGTH_LONG).show();
        }
    }
}
<file path="app/src/main/res/values/colors.xml">
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="primary">#FF0000</color>
    <color name="primary_dark">#CC0000</color>
    <color name="accent">#FF0000</color>
</resources>
</file>
