package com.example.edusummarize.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.InputStream;
import java.util.List;

/**
 * DOCX Text Extraction Utility using Apache POI
 */
public class DocxUtil {

    private static final String TAG = "DocxUtil";

    public static String extractTextFromDocx(Context context, Uri docxUri) {
        StringBuilder extractedText = new StringBuilder();

        try (InputStream inputStream = context.getContentResolver().openInputStream(docxUri)) {
            if (inputStream != null) {
                XWPFDocument document = new XWPFDocument(inputStream);
                List<XWPFParagraph> paragraphs = document.getParagraphs();

                for (XWPFParagraph paragraph : paragraphs) {
                    String text = paragraph.getText();
                    if (text != null && !text.trim().isEmpty()) {
                        extractedText.append(text).append("\n");
                    }
                }

                document.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting text from DOCX", e);
        }

        return extractedText.toString().trim();
    }
}