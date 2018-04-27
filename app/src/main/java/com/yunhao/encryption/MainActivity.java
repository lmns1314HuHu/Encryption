package com.yunhao.encryption;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.RandomAccessFile;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    protected void verifyStoragePermissions(Activity activity) {
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

    protected void identify(){
        EditText editText1 = (EditText)findViewById(R.id.editText);
        String passwd = editText1.getText().toString();

        if(passwd.equals("0624")){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
            setContentView(R.layout.activity_main);

            Button btn1 = (Button)findViewById(R.id.button1);
            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    encryption_btn();
                }
            });

            Button btn2 = (Button)findViewById(R.id.button2);
            btn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    decryption_btn();
                }
            });
        }
        else{
            Toast.makeText(getApplicationContext(), "Wrong", 1).show();
        }
    }

    protected void encryption_btn(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,0);
    }

    protected void decryption_btn(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,1);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            final String path = getRealFilePath(getApplicationContext(), uri);
            File targetFile = new File(path);
            if (targetFile.isFile()){
                if (requestCode == 0) {
                    if(hasEncrypted(path)){
                        Toast.makeText(getApplicationContext(), "This file has already been encrypted", 1).show();
                        return;
                    }

                    AlertDialog.Builder bb = new AlertDialog.Builder(this);
                    bb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            XorFile(path);
                            renameToNewFile(path, encrypte_name(path));
                            Toast.makeText(getApplicationContext(), "Done", 1).show();
                        }
                    });
                    bb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    bb.setMessage("Encrpte file :\n" + path.substring(path.lastIndexOf('/') + 1) + " ?");
                    bb.setTitle("Warnning");
                    bb.show();

                }
                else{
                    if(!hasEncrypted(path)) {
                        Toast.makeText(getApplicationContext(), "This file has not been encrypted yet", 1).show();
                        return;
                    }

                    AlertDialog.Builder bb = new AlertDialog.Builder(this);
                    bb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String tgtName = decrypte_name(path);
                            renameToNewFile(path, tgtName);
                            XorFile(tgtName);
                            Toast.makeText(getApplicationContext(), "Done", 1).show();
                        }
                    });
                    bb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    bb.setMessage("Decrpte file :\n" + path.substring(path.lastIndexOf('/') + 1) + " ?");
                    bb.setTitle("Warnning");
                    bb.show();

                }
            }
            else{
                Toast.makeText(getApplicationContext(), "Not a file", 1).show();
            }
        }
    }

    protected String getRealFilePath(Context context, Uri uri) {
        if(uri == null)
            return null;
        String scheme = uri.getScheme();
        String data = null;
        if(scheme == null)
            data = uri.getPath();
        else if(ContentResolver.SCHEME_FILE.equals(scheme)){
            data = uri.getPath();
        }
        else if(ContentResolver.SCHEME_CONTENT.equals(scheme)){
            Cursor cursor = context.getContentResolver().query(
                    uri,
                    new String[] {MediaStore.Images.ImageColumns.DATA },
                    null,
                    null,
                    null
            );
            if(cursor != null){
                if (cursor.moveToFirst()){
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if(index > -1){
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    protected boolean hasEncrypted(String path) {
        String srcName = path.substring(path.lastIndexOf('/') + 1);
        if(srcName.contains("_")) {
            String suffix = srcName.substring(srcName.lastIndexOf('_') + 1);
            if(suffix.length() > 6 && suffix.startsWith("hy") && suffix.endsWith("snml"))
                return true;
        }
        return false;
    }

    protected String encrypte_name(String path) {
        String newName = path.substring(path.lastIndexOf('/') + 1);
        String tp = "";
        if(newName.contains(".")){
            tp = newName.substring(newName.lastIndexOf('.') + 1);
            if(tp.equals(""))
                tp = "=";
            newName = newName.substring(0, newName.lastIndexOf('.')) + "_hy" + tp + "snml";
        }
        else{
            newName += "_hy+snml";
        }
        return path.substring(0, path.lastIndexOf('/') + 1) + newName;
    }

    protected String decrypte_name(String path) {
        String newName = path.substring(path.lastIndexOf('/') + 1);
        String tp = newName.substring(newName.lastIndexOf('_') + 1);
        tp = tp.substring(2, tp.length() - 4);

        newName = newName.substring(0, newName.lastIndexOf('_'));
        if(tp.equals("+")) {
            return path.substring(0, path.lastIndexOf('/') + 1) + newName;
        }
        else if(tp.equals("=")) {
            newName += ".";
        }
        else {
            newName += "." + tp;
        }
        return path.substring(0, path.lastIndexOf('/') + 1) + newName;
    }

    protected void renameToNewFile(String src, String dest) {
        File srcDir = new File(src);
        srcDir.renameTo(new File(dest));
    }

    protected void XorFile(String path){
        try {
            File tgtFile = new File(path);
            int cnt = 20 + (path.length() % 30);

            RandomAccessFile raf = new RandomAccessFile(tgtFile, "r");
            byte[] buff = new byte[64];
            raf.read(buff, 0, cnt);
            raf.close();

            for (int i = 0; i < cnt; i++) {
                buff[i] ^= 0xFF;
            }

            raf = new RandomAccessFile(tgtFile, "rw");
            raf.write(buff, 0, cnt);
            raf.close();
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(), "Exception", 1).show();
        }
    }
}
