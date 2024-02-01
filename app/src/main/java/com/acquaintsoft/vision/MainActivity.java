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


import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;


/**
 * Main activity demonstrating how to pass extra parameters to an activity that
 * recognizes text.
 */
public class MainActivity extends AppCompatActivity {

    // Use a compound button so either checkbox or switch widgets work.
    private Button picture_book_trans;
    private Button voice_trans;
    private Button document_trans;
    private  Button e_book;
    private CompoundButton autoFocus;
    private CompoundButton useFlash;
    private static final int RC_OCR_CAPTURE = 9003;
    private static final String TAG = "MainActivity";
    private int check=0;
    public static final TranslatorOptions options =
            new TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                    .setTargetLanguage(TranslateLanguage.KOREAN)
                    .build();
    public static final Translator translator =
            Translation.getClient(options);
    public static final DownloadConditions conditions = new DownloadConditions.Builder()
            .build();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        Log.d("Language Translation", "Model is found ");
                    }
                }).addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Language Translation", "Model not found ");
                    }});

        document_trans = (Button)findViewById(R.id.document_trans);
        document_trans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, DocumentActivity.class));
            }
        });

        picture_book_trans = (Button)findViewById(R.id.picture_book_trans);
        picture_book_trans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, OcrCaptureActivity.class);
                intent.putExtra(OcrCaptureActivity.AutoFocus, autoFocus.isChecked());
                intent.putExtra(OcrCaptureActivity.UseFlash, useFlash.isChecked());

                startActivityForResult(intent, RC_OCR_CAPTURE);
            }
        });



        voice_trans = (Button)findViewById(R.id.voice_trans);
        voice_trans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Trans_By_Voice.class));
            }
        });
        e_book = findViewById(R.id.e_book);
        e_book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Ebook.class));
            }
        });
        autoFocus = (CompoundButton) findViewById(R.id.auto_focus);
        useFlash = (CompoundButton) findViewById(R.id.use_flash);
    }


}
