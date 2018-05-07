package com.yunhao.encryption;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class IdentifyActivity extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify);

        verifyStoragePermissions(this);

        Button btn = (Button)findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                identify();
            }
        });
    }

    private void identify(){
        EditText editText1 = (EditText)findViewById(R.id.editText);
        String passwd = editText1.getText().toString();

        if(passwd.equals("0624")){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
            jumpToMain();
        }
        else{
            Toast.makeText(getApplicationContext(), "Wrong", 1).show();
        }
    }

    private void jumpToMain(){
        Intent intent = new Intent();
        intent.setClass(IdentifyActivity.this, MainActivity.class);
        IdentifyActivity.this.startActivity(intent);
        finish();
    }

    private boolean checkFingerModule() {
        try {
            FingerprintManager fingerManager = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);
            return fingerManager.isHardwareDetected();
        } catch (Exception e) {
            return false;
        }
    }
}
