package com.acquaintsoft.vision;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.util.Log;


public class BufferedReaderFactory {

    private static String TAG = "BufferedReaderFactory";

    public static BufferedReader create(String path, String textFileName) throws IOException {
        // 경로(path)가 존재하는지 확인하고, 그것이 없으면 FileNotFoundException 예외를 던집니다.
        File filePath = new File(path);
        if (!filePath.exists()) {
            Log.e(TAG, "경로 " + path + "가 존재하지 않습니다.");
            throw new FileNotFoundException();
        }


        BufferedReader br = new BufferedReader(new FileReader(path + File.separator + textFileName));
        return br;
    }
}