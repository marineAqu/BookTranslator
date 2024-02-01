package com.acquaintsoft.vision;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Trans_By_Voice extends AppCompatActivity {

    Intent intent;
    SpeechRecognizer mRecognizer;
    ImageView request_mic;
    TextView trans_textview;
    TextView plain_textview;
    final int PERMISSION = 1;
    Bundle temp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trans_by_voice);


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        if (Build.VERSION.SDK_INT >= 23) {
            // 퍼미션 체크
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO}, PERMISSION);
        }
        plain_textview = (TextView) findViewById(R.id.plain_textview);
        trans_textview = (TextView) findViewById(R.id.trans_textview);

        request_mic = (ImageView) findViewById(R.id.request_mic);

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "eng-ENG");

//        sttBtn.setOnClickListener(view -> {
//            Toast.makeText(getApplicationContext(), "버튼 클릭 함수 테스트", Toast.LENGTH_LONG).show();
//            sttBtn.setColorFilter(Color.RED);
//            mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
//            mRecognizer.setRecognitionListener(listener);
//            mRecognizer.startListening(intent);
//        });
//
//    }
        request_mic.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                request_mic.setColorFilter(Color.RED);
                mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
                mRecognizer.setRecognitionListener(listener);
                mRecognizer.startListening(intent);
                return true;
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                request_mic.setColorFilter(Color.BLACK);

                return true;
            }
            return false;
        });
    }
    private RecognitionListener listener = new RecognitionListener() {

        @Override
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getApplicationContext(),"음성인식을 시작합니다.",Toast.LENGTH_LONG).show();
        }

        @Override
        public void onBeginningOfSpeech() {}

        @Override
        public void onRmsChanged(float rmsdB) {}

        @Override
        public void onBufferReceived(byte[] buffer) {}

        @Override
        public void onEndOfSpeech() {}

        @Override
        public void onError(int error) {
            String message;

            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 에러";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션 없음";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "찾을 수 없음";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RECOGNIZER가 바쁨";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버가 이상함";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간초과";
                    break;
                default:
                    message = "알 수 없는 오류임";
                    break;
            }

            Toast.makeText(getApplicationContext(), "에러가 발생하였습니다. : " + message,Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResults(Bundle results) {
            StringBuilder Allstring = new StringBuilder();
            // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어줍니다.
            ArrayList<String> matches =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for(int i = 0; i < matches.size() ; i++){
                plain_textview.setText(matches.get(i));
                Allstring.append(matches.get(i));
            }
            translateText(Allstring.toString());
        }

        @Override
        public void onPartialResults(Bundle partialResults) {}

        @Override
        public void onEvent(int eventType, Bundle params) {}
    };
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
    private final MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private final WeakReference<Trans_By_Voice> mActivity;

        public MyHandler(Trans_By_Voice activity) {
            mActivity = new WeakReference<Trans_By_Voice>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle bun = msg.getData();
            String res = bun.getString("translatedText");
            Trans_By_Voice activity = mActivity.get();
            activity.handleMessage(msg);



        }
    }

    private void handleMessage(Message msg) {
        Bundle bun = msg.getData();
        String res = bun.getString("translatedText");
        res = res.replace(". ", ". \n").replace("! ", "! \n").replace("? ", "? \n");
        trans_textview.setText(res);

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
}