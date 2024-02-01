package com.acquaintsoft.vision;


import static android.os.Environment.getExternalStoragePublicDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Ebook extends Activity {

    EditText editTextTextFileName;
    TextView editTextContent;

    TextView current_page;
    Button next_btn;
    Button pre_btn;
    Button buttonRead;

    public int page = 1;
    public int totalpage = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ebook);

        current_page = findViewById(R.id.currentPage);

        next_btn = (Button) findViewById(R.id.next_btn);
        next_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (page < totalpage) {
                    page++;
                }
                String path = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/Ebook/" + editTextTextFileName.getText().toString()+"Trans";
                String SaveFilePath = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/Ebook/" + editTextTextFileName.getText().toString() +"Trans"+ "/" + page + ".txt";
                File text = new File(SaveFilePath);
                String textFileName = page + ".txt"; //번역본 책 은 Trans추가
                BufferedReader br = null;

                try {
                    br = BufferedReaderFactory.create(path, textFileName);

                    String temp;
                    StringBuilder contentGetter = new StringBuilder();

                    temp = br.readLine();
                    if (null != temp)
                        contentGetter.append(temp);
                    for (; temp != null; temp = br.readLine()) {
                        contentGetter.append('\n' + temp);
                    }

                    editTextContent.setText(contentGetter.toString());
                    current_page.setText(page+"/"+totalpage);
                }

                // 파일이 존재하지 않는 경우에 대한 예외 처리
                catch (FileNotFoundException e) {
                    String exceptionMessage = textFileName + " 파일 또는 경로가 존재하지 않습니다.";
                    e.printStackTrace();
                }

                // 입출력 관련된 예외 처리
                catch (IOException e) {
                    String exceptionMessage = "파일을 읽는 도중에 오류가 발생했습니다.";
                    e.printStackTrace();
                }

                // 기타 예외 처리
                catch (Exception e) {
                    e.printStackTrace();
                }

                // 파일을 닫을 때에도 try ... catch 문 작성을 해야 합니다.
                try {
                    if (null != br)
                        br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        });
        pre_btn = (Button) findViewById(R.id.pre_btn);
        pre_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (page != 1) {
                    page--;
                }
                String path = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/Ebook/" + editTextTextFileName.getText().toString()+"Trans";
                String SaveFilePath = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/Ebook/" + editTextTextFileName.getText().toString() +"Trans"+ "/" + page + ".txt";
                File text = new File(SaveFilePath);
                String textFileName = page + ".txt"; //번역본 책 은 Trans추가
                BufferedReader br = null;

                try {
                    br = BufferedReaderFactory.create(path, textFileName);

                    String temp;
                    StringBuilder contentGetter = new StringBuilder();

                    temp = br.readLine();
                    if (null != temp)
                        contentGetter.append(temp);
                    for (; temp != null; temp = br.readLine()) {
                        contentGetter.append('\n' + temp);
                    }
                    current_page.setText(page+"/"+totalpage);
                    editTextContent.setText(contentGetter.toString());
                }

                // 파일이 존재하지 않는 경우에 대한 예외 처리
                catch (FileNotFoundException e) {
                    String exceptionMessage = textFileName + " 파일 또는 경로가 존재하지 않습니다.";
                    e.printStackTrace();
                }

                // 입출력 관련된 예외 처리
                catch (IOException e) {
                    String exceptionMessage = "파일을 읽는 도중에 오류가 발생했습니다.";
                    e.printStackTrace();
                }

                // 기타 예외 처리
                catch (Exception e) {
                    e.printStackTrace();
                }

                // 파일을 닫을 때에도 try ... catch 문 작성을 해야 합니다.
                try {
                    if (null != br)
                        br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        });


        editTextTextFileName = (EditText) findViewById(R.id.editTextTextFileName);
        editTextContent = (TextView) findViewById(R.id.editTextContent);
        buttonRead = (Button) findViewById(R.id.buttonRead);
        buttonRead.setOnClickListener(new ButtonReadClickListener(this));

    }

    class ButtonReadClickListener implements OnClickListener {
        Context context;

        public ButtonReadClickListener(Context context) {
            this.context = context;
        }

        public void onClick(View v) {

            String path = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/Ebook/" + editTextTextFileName.getText().toString() +"Trans";
            String SaveFilePath = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/Ebook/" + editTextTextFileName.getText().toString() +"Trans"+ "/" + page + ".txt";
            File text = new File(SaveFilePath);

            String textFileName = page + ".txt"; //번역본 책 은 Trans추가
            BufferedReader br = null;
            while (text.exists()) {
                page++;
                SaveFilePath = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/Ebook/" + editTextTextFileName.getText().toString() +"Trans"+ "/" + page + ".txt";
                text = new File(SaveFilePath);
            }
            totalpage = page - 1;
            page = 1;
            try {
                br = BufferedReaderFactory.create(path, textFileName);

                String temp;
                StringBuilder contentGetter = new StringBuilder();

                temp = br.readLine();
                if (null != temp)
                    contentGetter.append(temp);
                for (; temp != null; temp = br.readLine()) {
                    contentGetter.append('\n' + temp);
                }
                current_page.setText(page+"/"+totalpage);
                editTextContent.setText(contentGetter.toString());
                Toast.makeText(this.context, "성공적으로 읽었습니다.", Toast.LENGTH_SHORT).show();

            }

            // 파일이 존재하지 않는 경우에 대한 예외 처리
            catch (FileNotFoundException e) {
                String exceptionMessage =  " 파일 또는 경로가 존재하지 않습니다.";
                Toast.makeText(this.context, exceptionMessage, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            // 입출력 관련된 예외 처리
            catch (IOException e) {
                String exceptionMessage = "파일을 읽는 도중에 오류가 발생했습니다.";
                Toast.makeText(this.context, exceptionMessage, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            // 기타 예외 처리
            catch (Exception e) {
                Toast.makeText(this.context, "알 수 없는 오류입니다.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            // 파일을 닫을 때에도 try ... catch 문 작성을 해야 합니다.
            try {
                if (null != br)
                    br.close();
            } catch (IOException e) {
                Toast.makeText(this.context,  "파일을 닫을 수 없습니다.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
}
