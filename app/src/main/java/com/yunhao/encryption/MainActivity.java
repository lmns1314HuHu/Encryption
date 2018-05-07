package com.yunhao.encryption;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private boolean firstReadFile = false;
    private String rightDirectory = "";

    private ListView queue;
    private List<Map<String, Object>> queueList;
    private SimpleAdapter queueAdapter;

    private ListView sideFile;
    private List<Map<String, Object>> sideList;
    private SimpleAdapter sideAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initQueue();
        initFileList();
        setDrawerLock();
        btnClickBundle();
    }

    private void initQueue(){
        queue = (ListView)findViewById(R.id.queue);
        queueList = new ArrayList<Map<String, Object>>();
        queueAdapter = new SimpleAdapter(
                this,
                queueList,
                R.layout.items,
                new String[]{"img", "name", "path"},
                new int[]{R.id.image, R.id.title, R.id.path});
        queue.setAdapter(queueAdapter);

        queue.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                queueList.remove(position);
                queueAdapter.notifyDataSetChanged();
            }
        });
    }

    private void initFileList(){
        sideFile = (ListView) findViewById(R.id.list_view);
        sideList = new ArrayList<Map<String, Object>>();
        sideAdapter = new SimpleAdapter(
                MainActivity.this,
                sideList,
                R.layout.items,
                new String[] {"img", "name", "img2", "path"},
                new int[] {R.id.image, R.id.title, R.id.imageright, R.id.path}
        );
        sideFile.setAdapter(sideAdapter);

        sideFile.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String,Object> map =  (Map<String, Object>) parent.getItemAtPosition(position);

                TextView textView = (TextView)findViewById(R.id.root);
                String origin = textView.getText().toString();
                File file = new File("/storage/emulated/0" + origin + "/" + map.get("name").toString());
                if(file.isDirectory()){
                    readRootFile("/storage/emulated/0" + origin + "/" + map.get("name"));
                    textView.setText(origin + "/" + map.get("name"));
                    rightDirectory = "";
                }
                else{
                    addToQueue("/storage/emulated/0" + origin + "/" + map.get("name"));
                }
            }
        });
    }

    private void setDrawerLock(){
        DrawerLayout drawerLayout = (DrawerLayout)findViewById(R.id.drawer);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    private void btnClickBundle(){
        ImageButton btn1 = (ImageButton)findViewById(R.id.encrypte);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                encryption_btn();
            }
        });

        ImageButton btn2 = (ImageButton)findViewById(R.id.decrypte);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decryption_btn();
            }
        });

        ImageButton add = (ImageButton)findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFile();
            }
        });

        ImageButton remove = (ImageButton)findViewById(R.id.remove);
        remove.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                queueList.clear();
                queueAdapter.notifyDataSetChanged();
            }
        });

        ImageButton selectall = (ImageButton)findViewById(R.id.selectall);
        selectall.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                for(int i = 0; i < sideAdapter.getCount(); i++){
                    LinearLayout father = (LinearLayout)sideFile.getAdapter().getView(i, null, null);
                    TextView path = (TextView)father.findViewById(R.id.path);
                    String fullname = path.getText().toString();
                    File file = new File(fullname);
                    if(!file.isDirectory()){
                        addToQueue(fullname);
                    }
                }
            }
        });

        ImageButton home = (ImageButton)findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readRootFile("/storage/emulated/0");
                TextView textView = (TextView)findViewById(R.id.root);
                textView.setText("");
                rightDirectory = "";
            }
        });

        ImageButton refresh = (ImageButton)findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView = (TextView)findViewById(R.id.root);
                readRootFile("/storage/emulated/0" + textView.getText());
            }
        });

        ImageButton right = (ImageButton)findViewById(R.id.right);
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView = (TextView)findViewById(R.id.root);
                String origin = textView.getText().toString();
                if(rightDirectory.equals("")){
                    readRootFile("/storage/emulated/0" + origin);
                }
                else if(rightDirectory.lastIndexOf("/") == 0){
                    textView.setText(origin + rightDirectory);
                    readRootFile("/storage/emulated/0" + origin + rightDirectory);
                    rightDirectory = "";
                }
                else{
                    String tmp = rightDirectory.substring(0, rightDirectory.indexOf("/", 1));
                    textView.setText(origin + tmp);
                    readRootFile("/storage/emulated/0" + origin + tmp);
                    rightDirectory = rightDirectory.substring(rightDirectory.indexOf("/", 1));
                }
            }
        });

        ImageButton left = (ImageButton)findViewById(R.id.left);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView = (TextView)findViewById(R.id.root);
                String origin = textView.getText().toString();
                if(origin.equals("")){
                }
                else{
                    readRootFile("/storage/emulated/0" + origin.substring(0, origin.lastIndexOf('/')));
                    textView.setText(origin.substring(0, origin.lastIndexOf('/')));
                    rightDirectory = origin.substring(origin.lastIndexOf('/')) + rightDirectory;
                }
            }
        });
    }

    private void loadFile(){
        DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer);
        drawer.openDrawer(Gravity.LEFT);
        if(!firstReadFile) {
            firstReadFile = true;
            readRootFile("/storage/emulated/0");
        }
    }

    private void readRootFile(String root){
        File path = new File(root);
        if(path.exists()) {
            File[] files = path.listFiles();
            String[] filenames = new String[files.length];
            String[] foldernames = new String[files.length];
            int[] img = {
                    R.drawable.folder,
                    R.drawable.file,
                    R.drawable.right
            };

            int filecnt = 0;
            int foldercnt = 0;
            for (File file : files) {
                String filename = file.getName();
                if(file.isDirectory()){
                    String totpath = file.getAbsolutePath();
                    filename = totpath.substring(totpath.lastIndexOf('/') + 1);
                    foldernames[foldercnt] = filename;
                    foldercnt++;
                }
                else{
                    filenames[filecnt] = filename;
                    filecnt++;
                }
            }
            Arrays.sort(filenames, 0, filecnt);
            Arrays.sort(foldernames, 0, foldercnt);

            int cnt = foldercnt;
            for (int i = 0; i < filecnt; i++){
                foldernames[cnt] = filenames[i];
                cnt++;
            }

            sideList.clear();
            cnt = 0;
            for(String str : foldernames){
                Map<String, Object> item = new HashMap<String, Object>();
                if(cnt < foldercnt){
                    item.put("img", img[0]);
                    item.put("img2", img[2]);
                }
                else{
                    item.put("img", img[1]);
                }
                item.put("name", foldernames[cnt]);
                item.put("path", root + "/" + foldernames[cnt]);
                sideList.add(item);
                cnt++;
            }

            sideAdapter.notifyDataSetChanged();
        }
    }

    private void addToQueue(String path){
        ListView queue = (ListView)findViewById(R.id.queue);
        Map<String, Object> item = new HashMap<String, Object>();
        String shortname = path.substring(path.lastIndexOf("/") + 1);
        if(hasEncrypted(path)){
            item.put("img", R.drawable.safe);
        }
        else{
            item.put("img", R.drawable.danger);
        }
        item.put("name", shortname);
        item.put("path", path);
        if(!queueList.contains(item)){
            queueList.add(item);
            queueAdapter.notifyDataSetChanged();
        }
    }

    private void encryption_btn(){
        AlertDialog.Builder bb = new AlertDialog.Builder(this);
        bb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for(int idx = 0; idx < queueAdapter.getCount(); idx++){
                    LinearLayout father = (LinearLayout)queue.getAdapter().getView(idx, null, null);
                    TextView path = (TextView)father.findViewById(R.id.path);
                    String fullname = path.getText().toString();
                    File file = new File(fullname);
                    if(!file.isDirectory() && !hasEncrypted(fullname)){
                        XorFile(fullname);
                        renameToNewFile(fullname, encrypte_name(fullname));

                        queueList.remove(idx);
                        idx--;
                    }
                }
                queueAdapter.notifyDataSetChanged();

                ImageButton refresh = (ImageButton)findViewById(R.id.refresh);
                refresh.callOnClick();
            }
        });
        bb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        bb.setMessage("Encrypte these file?");
        bb.setTitle("Warnning");
        bb.show();

    }

    private void decryption_btn(){
        AlertDialog.Builder bb = new AlertDialog.Builder(this);
        bb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for(int idx = 0; idx < queueAdapter.getCount(); idx++){
                    LinearLayout father = (LinearLayout)queue.getAdapter().getView(idx, null, null);
                    TextView path = (TextView)father.findViewById(R.id.path);
                    String fullname = path.getText().toString();
                    File file = new File(fullname);
                    if(!file.isDirectory() && hasEncrypted(fullname)){
                        String tgtName = decrypte_name(fullname);
                        renameToNewFile(fullname, tgtName);
                        XorFile(tgtName);

                        queueList.remove(idx);
                        idx--;
                    }
                }
                queueAdapter.notifyDataSetChanged();

                ImageButton refresh = (ImageButton)findViewById(R.id.refresh);
                refresh.callOnClick();
            }
        });
        bb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        bb.setMessage("Decrypte these file?");
        bb.setTitle("Warnning");
        bb.show();

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

    private boolean hasEncrypted(String path) {
        String srcName = path.substring(path.lastIndexOf('/') + 1);
        if(srcName.contains("_")) {
            String suffix = srcName.substring(srcName.lastIndexOf('_') + 1);
            if(suffix.length() > 6 && suffix.startsWith("hy") && suffix.endsWith("snml"))
                return true;
        }
        return false;
    }

    private String encrypte_name(String path) {
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

    private String decrypte_name(String path) {
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

    private void renameToNewFile(String src, String dest) {
        File srcDir = new File(src);
        srcDir.renameTo(new File(dest));
    }

    private void XorFile(String path){
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
