package org.pytorch.demo.log;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;

import androidx.core.app.ActivityCompat;

import org.pytorch.demo.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ModelsCollector {
    private static final String DOWNLOAD_DEFAULT = Environment.getExternalStoragePublicDirectory("Download") +"/";
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


    public static List<String> getModels(Context context){
        List<String> results = new ArrayList<String>();

        File[] files = new File(getPathToModels(context)).listFiles();

        //DATASET LIST
        List<String> datasetsName = new ArrayList<String>();
        datasetsName.add("cifar10");
        datasetsName.add("cifar100");
        datasetsName.add("imagenet");

        for (File file : files) {
            if (file.isFile() && (file.getName().substring((file.getName().length())-3)).equals(".pt")) {
                String modelSelected = file.getName().substring(0,file.getName().indexOf('.'));
                if (modelSelected.contains("_")){
                    if (datasetsName.contains(modelSelected.substring(0, modelSelected.indexOf('_'))))
                        results.add(file.getName());
                }
            }
        }
        java.util.Collections.sort(results);
        results.add("Download models..");
        results.add(0,"Choose a model");
        return results;
    }

    public static void writeStringToFile(Context context, String target){
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput("path_to_models.txt", 0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fos.write(target.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readStringFromFile(Context context) {
        FileInputStream fis = null;
        try {
            fis = context.openFileInput("path_to_models.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        StringBuilder sb = new StringBuilder();
        String text = "";
        try {
            text = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }

    public static String getPathToModels(Context context){
        String filename = "path_to_models.txt";

        File file = context.getFileStreamPath(filename);
        if (!file.exists()) {
            writeStringToFile(context, DOWNLOAD_DEFAULT);
        }
        String result = readStringFromFile(context);
        return result;

    }
}
