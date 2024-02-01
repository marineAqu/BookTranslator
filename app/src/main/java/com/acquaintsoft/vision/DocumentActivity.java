/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.acquaintsoft.vision;

import static android.os.Environment.getExternalStoragePublicDirectory;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity for the multi-tracker app.  This app detects text and displays the value with the
 * rear facing camera. During detection overlay graphics are drawn to indicate the position,
 * size, and contents of each TextBlock.
 */
public class DocumentActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "MainActivity";
    private Mat mRgba;
    private Mat mGray;
    private CameraBridgeViewBase mOpenCvCameraView;
    private ImageView take_picture_button;
    private ArrayList<String> list;
    private TextRecognizer textRecognizer;
    private String Camera_or_RecognizieText = "camera";
    private Bitmap bitmap = null;
    public String Filename;
    public int page_i;
    public int page_it;
    private TextView testView;
    public String res;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface
                        .SUCCESS: {
                    Log.i(TAG, "OpenCv Is loaded");
                    mOpenCvCameraView.enableView();
                }
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public DocumentActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        setContentView(R.layout.activity_camera);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.frame_Surface);
        mOpenCvCameraView.setCvCameraViewListener(this);

        take_picture_button = (ImageView) findViewById(R.id.take_picture_button);


        int MY_PERMISSIONS_REQUEST_CAMERA = 0;
        if (ContextCompat.checkSelfPermission(DocumentActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(DocumentActivity.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }


        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Create File");
        final EditText InputFilename = new EditText(this);
        alert.setView(InputFilename);
        alert.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Filename = InputFilename.getText().toString();
            }
        });
        alert.show();


        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();


        take_picture_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mOpenCvCameraView.setVisibility(View.VISIBLE);

                    if (Camera_or_RecognizieText == "camera") {
                        take_picture_button.setColorFilter(Color.DKGRAY);
                        Mat a = mRgba.t();
                        Core.flip(a, mRgba, 1);
                        a.release();
                        bitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(mRgba, bitmap);

                        mOpenCvCameraView.disableView();
                        Camera_or_RecognizieText = "recognizeText";

                        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                        SparseArray<TextBlock> items = textRecognizer.detect(frame);
                        StringBuilder string = new StringBuilder();
                        for (int i = 0; i < items.size(); i++) {
                            TextBlock myItem = items.valueAt(i);
                            string.append(myItem.getValue());
                            string.append("\n");
                        }
                        translateText(string.toString());
                       // add_out_prin(Filename, string.toString());

                    } else {
                        take_picture_button.setColorFilter(Color.WHITE);
                        mOpenCvCameraView.enableView();
                        Camera_or_RecognizieText = "camera";
                    }

                    return true;
                }
                return false;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "Opencv initialization is done");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            //if not loaded
            Log.d(TAG, "Opencv is not loaded. try again");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onDestroy(Bundle savedInstanceState) {
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        return mRgba;
    }

    private final MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private final WeakReference<DocumentActivity> mActivity;

        public MyHandler(DocumentActivity activity) {
            mActivity = new WeakReference<DocumentActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle bun = msg.getData();
            String res = bun.getString("translatedText");
            DocumentActivity activity = mActivity.get();
            activity.handleMessage(msg);

        }
    }

    private void handleMessage(Message msg) {
        Bundle bun = msg.getData();
        res = bun.getString("translatedText");
        res = res.replace(". ", ". \n").replace("! ", "! \n").replace("? ", "? \n");
        add_out_tran(Filename + "Trans", res);
    }

    public void translateText(String text) {//번역 메소드
        new Thread() {
            public void run() {
                String resOfmid = midProcess(text);

                Bundle bun = new Bundle();
                bun.putString("translatedText", resOfmid);
                Message msg = mHandler.obtainMessage();
                msg.setData(bun);
                mHandler.sendMessage(msg);

            }
        }.start();

    }

    private String midProcess(String text) {
        String clientId = "j02JEVTlNo4Onr8k6jBR";//애플리케이션 클라이언트 아이디값";
        String clientSecret = "O3bZoYOIQu";//애플리케이션 클라이언트 시크릿값";
        String foratest = "Please Connect NetWork";
        String inputVal = text;
        inputVal = inputVal.replace("\n", " ");
        String apiURL = "https://openapi.naver.com/v1/papago/n2mt";

        String result = "aboutRES";
        String data = "source=en&target=ko&text=" + inputVal.replaceAll("\\+", "%20").replaceAll("%0A", "%5Cn");

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("X-Naver-Client-Id", clientId);
        requestHeaders.put("X-Naver-Client-Secret", clientSecret);

        try {
            result = post(apiURL, requestHeaders, data);
            return new JSONObject(result).getJSONObject("message").getJSONObject("result").getString("translatedText");

        } catch (JSONException e) {
            e.printStackTrace();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return foratest;
        }
    }

    public static String post(String url, Map<String, String> headers, String data) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod("POST");
        for (Map.Entry<String, String> header : headers.entrySet()) {
            con.setRequestProperty(header.getKey(), header.getValue());
        }
        con.setDoOutput(true);
        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.writeBytes(data);
            wr.flush();
        }
        if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return readBody(con.getInputStream());
        } else {
            return readBody(con.getErrorStream());
        }
    }


    private static String readBody(InputStream body) throws IOException {
        BufferedReader lineReader = new BufferedReader(new InputStreamReader(body));
        StringBuilder responseBody = new StringBuilder();
        String line;
        while ((line = lineReader.readLine()) != null) {
            responseBody.append(line);
        }
        return responseBody.toString();
    }


    private void add_out_prin(String fileName, String msg) { //외부 저장소
        String SaveFolderPath = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/Ebook/" + fileName;
        int page = 1;
        String SaveFilePath = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/Ebook/" + fileName + "/" + page + ".txt";
        File text = new File(SaveFilePath);
        File dir = new File(SaveFolderPath);
        String page_s = String.valueOf(page_i);
        if (!dir.exists()) {
            dir.mkdir();
        } else {
            while (text.exists()) {
                page++;
                SaveFilePath = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/Ebook/" + fileName + "/" + page + ".txt";
                text = new File(SaveFilePath);
            }
        }
        if (fileName != null) {
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/Ebook/" + fileName + "/" + page + ".txt", true));
                bw.write(msg);
                bw.write("\n");
//                bw.write("\t\t\t\t(" + page_s + ")");

//                SharedPreferences sharedPref = getSharedPreferences(fileName, MODE_PRIVATE);
//                SharedPreferences.Editor editor = sharedPref.edit();
//
//                page_i++;
//                editor.putInt(fileName, page_i);
//                editor.apply();

                bw.write("\n\n");
                bw.close();
                Toast.makeText(this, "저장완료", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void add_out_tran(String fileName, String msg) { //외부 저장소
        String SaveFolderPath = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/Ebook/" + fileName ;
        int page = 1;
        String SaveFilePath = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/Ebook/" + fileName + "/" + page + ".txt";
        File text = new File(SaveFilePath);
        File dir = new File(SaveFolderPath);
        String page_s = String.valueOf(page_i);
        if (!dir.exists()) {
            dir.mkdir();
        } else {
            while (text.exists()) {
                page++;
                SaveFilePath = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/Ebook/" + fileName + "/" + page + ".txt";
                text = new File(SaveFilePath);
            }
        }
        if (fileName != null) {
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/Ebook/" + fileName + "/" + page + ".txt", true));
                bw.write(msg);
                bw.write("\n");

                bw.write("\n\n");
                bw.close();
                Toast.makeText(this, "저장완료", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}