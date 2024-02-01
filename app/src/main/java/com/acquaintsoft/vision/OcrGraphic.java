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


import static com.acquaintsoft.vision.MainActivity.conditions;
import static com.acquaintsoft.vision.MainActivity.translator;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import androidx.annotation.NonNull;

import com.acquaintsoft.vision.camera.GraphicOverlay;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import java.util.List;

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
public class OcrGraphic extends GraphicOverlay.Graphic {

    private int mId;
    private static final int TEXT_COLOR = Color.BLACK;
    public static String res=null;

    private static Paint sRectPaint;
    private static Paint sTextPaint;
    private final TextBlock mText;


    OcrGraphic(GraphicOverlay overlay, TextBlock text) {
        super(overlay);

        mText = text;

        if (sRectPaint == null) {
            sRectPaint = new Paint();
            sRectPaint.setColor(Color.WHITE);
            sRectPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            sRectPaint.setStrokeWidth(10.0f);
        }

        if (sTextPaint == null) {
            sTextPaint = new Paint();
            sTextPaint.setColor(TEXT_COLOR);
            sTextPaint.setTextSize(35f);
        }
        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public TextBlock getTextBlock() {
        return mText;
    }

    /**
     * Checks whether a point is within the bounding box of this graphic.
     * The provided point should be relative to this graphic's containing overlay.
     * @param x An x parameter in the relative context of the canvas.
     * @param y A y parameter in the relative context of the canvas.
     * @return True if the provided point is contained within this graphic's bounding box.
     */

    public boolean contains(float x, float y) {
        TextBlock text = mText;
        if (text == null) {
            return false;
        }
        RectF rect = new RectF(text.getBoundingBox());
        rect.left = translateX(rect.left);
        rect.top = translateY(rect.top);
        rect.right = translateX(rect.right);
        rect.bottom = translateY(rect.bottom);
        return (rect.left < x && rect.right > x && rect.top < y && rect.bottom > y);
    }

    /**
     * Draws the text block annotations for position, size, and raw value on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        TextBlock text = mText;
        if (text == null) {
            return;
        }
        /*try {
            Thread.sleep(15);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        // Draws the bounding box around the TextBlock.
        RectF rect = new RectF(text.getBoundingBox());
        rect.left = translateX(rect.left);
        rect.top = translateY(rect.top);
        rect.right = translateX(rect.right);
        rect.bottom = translateY(rect.bottom);


        // Break the text into multiple lines and draw each one according to its own bounding box.
        List<? extends Text> textComponents = text.getComponents();
        for (Text currentText : textComponents) {

            float left = translateX(currentText.getBoundingBox().left);
            float right = translateX(currentText.getBoundingBox().right);
            float top = translateY(currentText.getBoundingBox().top);
            float bottom = translateY(currentText.getBoundingBox().bottom);
            sTextPaint.setTextSize((bottom - top) * 0.8f);
            if(currentText!=null&&currentText.toString()!=res) {
                translator.translate(currentText.getValue())
                        .addOnSuccessListener(
                                new OnSuccessListener<String>() {
                                    @Override
                                    public void onSuccess(@NonNull String s) {
                                        // Translation successful.
                                        Log.d("Language Translation", "Translation: SUCCESS!!");
                                        res=s;
                                        canvas.drawRect(rect, sRectPaint);
                                        canvas.drawText(s, left, bottom, sTextPaint);
                                        /*try {
                                            Thread.sleep(15);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }*/
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("Language Translation", "Translation: FAIL!!");
                                        /*canvas.drawRect(rect, sRectPaint);
                                        canvas.drawText(currentText.getValue(), left, bottom, sTextPaint);*/
                                    }
                                });
            }else{
                Log.d("Language Translation", "CurrentText is NULL!!");
                /*canvas.drawRect(rect, sRectPaint);
                canvas.drawText(currentText.getValue(), left, bottom, sTextPaint);*/
            }
        }
    }
}