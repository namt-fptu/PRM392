package com.example.edusummarize.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * PDF Text Extraction Utility using PdfRenderer and OCR
 */
public class PdfUtil {

    private static final String TAG = "PdfUtil";

    public static String extractTextFromPdf(Context context, Uri pdfUri) {
        StringBuilder extractedText = new StringBuilder();

        try {
            // Copy PDF to temp file
            File tempFile = File.createTempFile("temp_pdf", ".pdf", context.getCacheDir());
            try (InputStream inputStream = context.getContentResolver().openInputStream(pdfUri);
                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {

                if (inputStream != null) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
            }

            // Render PDF pages as images and OCR
            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(
                    tempFile, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);

            int pageCount = pdfRenderer.getPageCount();

            // Limit to first 10 pages for performance
            int pagesToProcess = Math.min(pageCount, 10);

            for (int i = 0; i < pagesToProcess; i++) {
                PdfRenderer.Page page = pdfRenderer.openPage(i);

                // Render page to bitmap
                Bitmap bitmap = Bitmap.createBitmap(
                        page.getWidth() * 2,
                        page.getHeight() * 2,
                        Bitmap.Config.ARGB_8888
                );

                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                // Extract text using OCR
                String pageText = OcrUtil.extractTextFromBitmap(bitmap);
                extractedText.append(pageText).append("\n\n");

                page.close();
                bitmap.recycle();
            }

            pdfRenderer.close();
            fileDescriptor.close();
            tempFile.delete();

        } catch (Exception e) {
            Log.e(TAG, "Error extracting text from PDF", e);
        }

        return extractedText.toString().trim();
    }
}