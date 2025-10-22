package com.example.edusummarize.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.edusummarize.R;
import com.example.edusummarize.firebase.FirebaseRepository;
import com.example.edusummarize.model.Summary;
import com.example.edusummarize.network.ApiClient;
import com.example.edusummarize.network.SummarizeRequest;
import com.example.edusummarize.network.SummarizeResponse;
import com.example.edusummarize.network.SummarizerService;
import com.example.edusummarize.utils.DocxUtil;
import com.example.edusummarize.utils.OcrUtil;
import com.example.edusummarize.utils.PdfUtil;
import com.example.edusummarize.utils.TtsUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SummarizeActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 101;
    private static final int REQUEST_PDF = 102;
    private static final int REQUEST_DOCX = 103;
    private static final int PERMISSION_CAMERA = 200;
    private static final int PERMISSION_STORAGE = 201;

    private Button btnChooseSource;
    private CardView cardOriginalText, cardSummary;
    private TextView tvOriginalText, tvSummaryText, tvNoOriginalText, tvNoSummary;
    private EditText etDocTitle;
    private Button btnSummarize, btnCreateAudio;
    private ProgressBar progressBar;

    private Uri photoUri;
    private String extractedText = "";
    private String summaryText = "";
    private FirebaseRepository firebaseRepository;
    private TtsUtil ttsUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summarize);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tóm tắt tài liệu");
        }

        firebaseRepository = new FirebaseRepository();
        ttsUtil = new TtsUtil(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnChooseSource = findViewById(R.id.btn_choose_source);
        cardOriginalText = findViewById(R.id.card_original_text);
        cardSummary = findViewById(R.id.card_summary);
        tvOriginalText = findViewById(R.id.tv_original_text);
        tvSummaryText = findViewById(R.id.tv_summary_text);
        tvNoOriginalText = findViewById(R.id.tv_no_original_text);
        tvNoSummary = findViewById(R.id.tv_no_summary);
        etDocTitle = findViewById(R.id.et_doc_title);
        btnSummarize = findViewById(R.id.btn_summarize);
        btnCreateAudio = findViewById(R.id.btn_create_audio);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupListeners() {
        btnChooseSource.setOnClickListener(v -> showSourceSelectionDialog());
        btnSummarize.setOnClickListener(v -> summarizeText());
        btnCreateAudio.setOnClickListener(v -> createAudioAndSave());
    }

    private void showSourceSelectionDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_source, null);

        view.findViewById(R.id.btn_camera).setOnClickListener(v -> {
            dialog.dismiss();
            checkCameraPermission();
        });

        view.findViewById(R.id.btn_gallery).setOnClickListener(v -> {
            dialog.dismiss();
            checkStoragePermission(REQUEST_GALLERY);
        });

        view.findViewById(R.id.btn_pdf).setOnClickListener(v -> {
            dialog.dismiss();
            checkStoragePermission(REQUEST_PDF);
        });

        view.findViewById(R.id.btn_docx).setOnClickListener(v -> {
            dialog.dismiss();
            checkStoragePermission(REQUEST_DOCX);
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
        } else {
            openCamera();
        }
    }

    private void checkStoragePermission(int requestCode) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ doesn't need storage permission for opening files
            openFilePicker(requestCode);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_STORAGE);
        } else {
            openFilePicker(requestCode);
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, REQUEST_CAMERA);
            }
        }
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir("Pictures");
        try {
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void openFilePicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        if (requestCode == REQUEST_PDF) {
            intent.setType("application/pdf");
        } else if (requestCode == REQUEST_DOCX) {
            intent.setType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        } else {
            intent.setType("image/*");
        }
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            progressBar.setVisibility(View.VISIBLE);

            if (requestCode == REQUEST_CAMERA) {
                processImage(photoUri);
            } else if (requestCode == REQUEST_GALLERY && data != null) {
                processImage(data.getData());
            } else if (requestCode == REQUEST_PDF && data != null) {
                processPdf(data.getData());
            } else if (requestCode == REQUEST_DOCX && data != null) {
                processDocx(data.getData());
            }
        }
    }

    private void processImage(Uri imageUri) {
        new Thread(() -> {
            String text = OcrUtil.extractTextFromImage(this, imageUri);
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(text)) {
                    displayExtractedText(text);
                } else {
                    Toast.makeText(this, "Không thể trích xuất text từ ảnh",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void processPdf(Uri pdfUri) {
        new Thread(() -> {
            String text = PdfUtil.extractTextFromPdf(this, pdfUri);
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(text)) {
                    displayExtractedText(text);
                } else {
                    Toast.makeText(this, "Không thể trích xuất text từ PDF",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void processDocx(Uri docxUri) {
        new Thread(() -> {
            String text = DocxUtil.extractTextFromDocx(this, docxUri);
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(text)) {
                    displayExtractedText(text);
                } else {
                    Toast.makeText(this, "Không thể trích xuất text từ DOCX",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void displayExtractedText(String text) {
        extractedText = text;
        tvOriginalText.setText(text);
        tvOriginalText.setVisibility(View.VISIBLE);
        tvNoOriginalText.setVisibility(View.GONE);
        btnSummarize.setEnabled(true);
    }

    private void summarizeText() {
        if (TextUtils.isEmpty(extractedText)) {
            Toast.makeText(this, "Chưa có văn bản để tóm tắt", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSummarize.setEnabled(false);

        SummarizerService service = ApiClient.getSummarizerService();
        SummarizeRequest request = new SummarizeRequest(extractedText, 500);

        service.summarize(request).enqueue(new Callback<SummarizeResponse>() {
            @Override
            public void onResponse(@NonNull Call<SummarizeResponse> call,
                                   @NonNull Response<SummarizeResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnSummarize.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    summaryText = response.body().getSummary();
                    displaySummary(summaryText);
                } else {
                    Toast.makeText(SummarizeActivity.this,
                            "Lỗi khi tóm tắt: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SummarizeResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSummarize.setEnabled(true);
                Toast.makeText(SummarizeActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displaySummary(String summary) {
        tvSummaryText.setText(summary);
        tvSummaryText.setVisibility(View.VISIBLE);
        tvNoSummary.setVisibility(View.GONE);
        btnCreateAudio.setEnabled(true);
    }

    private void createAudioAndSave() {
        String title = etDocTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            etDocTitle.setError("Vui lòng nhập tên tài liệu");
            return;
        }

        if (TextUtils.isEmpty(summaryText)) {
            Toast.makeText(this, "Chưa có bản tóm tắt", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnCreateAudio.setEnabled(false);

        ttsUtil.convertTextToSpeech(summaryText, new TtsUtil.TtsCallback() {
            @Override
            public void onSuccess(File audioFile) {
                uploadAudioAndSaveSummary(title, audioFile);
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnCreateAudio.setEnabled(true);
                    Toast.makeText(SummarizeActivity.this,
                            "Lỗi tạo audio: " + error,
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void uploadAudioAndSaveSummary(String title, File audioFile) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firebaseRepository.uploadAudio(audioFile, new FirebaseRepository.UploadCallback() {
            @Override
            public void onSuccess(String audioUrl) {
                Summary summary = new Summary(
                        null,
                        userId,
                        title,
                        extractedText,
                        summaryText,
                        audioUrl,
                        Timestamp.now()
                );

                firebaseRepository.saveSummary(summary, new FirebaseRepository.SaveCallback() {
                    @Override
                    public void onSuccess(String documentId) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            btnCreateAudio.setEnabled(true);

                            new MaterialAlertDialogBuilder(SummarizeActivity.this)
                                    .setTitle("Thành công!")
                                    .setMessage("Tóm tắt và audio đã được lưu vào thư viện")
                                    .setPositiveButton("Xem thư viện", (dialog, which) -> {
                                        Intent intent = new Intent(SummarizeActivity.this, LibraryActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .setNegativeButton("Ở lại", null)
                                    .show();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            btnCreateAudio.setEnabled(true);
                            Toast.makeText(SummarizeActivity.this,
                                    "Lỗi lưu dữ liệu: " + error,
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnCreateAudio.setEnabled(true);
                    Toast.makeText(SummarizeActivity.this,
                            "Lỗi upload audio: " + error,
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == PERMISSION_CAMERA) {
                openCamera();
            } else if (requestCode == PERMISSION_STORAGE) {
                // Will be handled by next action
            }
        } else {
            Toast.makeText(this, "Cần cấp quyền để sử dụng chức năng này",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ttsUtil != null) {
            ttsUtil.shutdown();
        }
    }
}