package com.example.edusummarize.ui.components;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.example.edusummarize.R;

/**
 * Custom Loading Dialog with modern design
 * Better than default ProgressDialog
 */
public class LoadingDialog {
    private Dialog dialog;
    private TextView tvMessage;

    public LoadingDialog(Context context) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
        dialog.setContentView(view);

        tvMessage = view.findViewById(R.id.tv_loading_message);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
    }

    public void show(String message) {
        tvMessage.setText(message);
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public boolean isShowing() {
        return dialog.isShowing();
    }
}

