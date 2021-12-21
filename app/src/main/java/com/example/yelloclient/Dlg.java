package com.example.yelloclient;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

public class Dlg extends Dialog implements View.OnClickListener{

    private int mResId;
    private String mText;
    public DialogInterface mDialogInterface;

    public Dlg(Context context, String msg, int resId) {
        super(context);
        mText = msg;
        mResId = resId;
    }

    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        setContentView(mResId);
        View v = findViewById(R.id.btn_yes);
        if (v != null) {
            v.setOnClickListener(this);
        }
        v = findViewById(R.id.btn_no);
        if (v != null) {
            v.setOnClickListener(this);
        }
        ((TextView) findViewById(R.id.msg)).setText(mText);
    }

    public void setDialogInterface(DialogInterface d) {
        mDialogInterface = d;
    }

    public static Dlg alertDialog(Context context, int title, int message) {
        String strTitle = "";
        if (title > 0) {
            strTitle = context.getString(title);
        }
        Dlg dlg = new Dlg(context, context.getString(message), R.layout.custom_dialog);
        dlg.show();
        return dlg;
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setMessage(context.getString(message))
//                .setTitle(strTitle);
//        builder.setCancelable(false);
//        builder.setNeutralButton(R.string.OK, null);
//        AlertDialog dialog = builder.create();
//        dialog.show();
//        return dialog;
    }

    public static Dlg alertError(Context context, String message) {
        Dlg dlg = new Dlg(context, message, R.layout.custom_dialog_ok);
        dlg.show();
        return dlg;
    }

    public static Dlg alertError(Context context, int message) {
        return alertOK(context, context.getString(message));
    }

    public static Dlg alertOK(Context context, String message) {
        Dlg dlg = new Dlg(context, message, R.layout.custom_dialog_ok);
        dlg.show();
        return dlg;
    }

    public static Dlg alertOK(Context context, int message) {
        return alertOK(context, context.getString(message));
    }

    public static Dlg alertDialog(Context context, int title, String message) {
        String strTitle = "";
        if (title > 0) {
            strTitle = context.getString(title);
        }
        Dlg dlg = new Dlg(context, message, R.layout.custom_dialog);
        dlg.show();
        return dlg;

//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setMessage(message)
//                .setTitle(strTitle);
//        builder.setCancelable(false);
//        builder.setNeutralButton(R.string.OK, null);
//        AlertDialog dialog = builder.create();
//        dialog.show();
//        return dialog;
    }

    public static Dlg alertDialog(Context context, int title, String message, DialogInterface okClick) {
        AlertDialog.Builder ab = new AlertDialog.Builder(context);
        ab.setMessage(message);
        if (title > 0) {
            ab.setTitle(context.getString(title));
        }
        Dlg dlg = new Dlg(context, message, R.layout.custom_dialog);
        dlg.setDialogInterface(okClick);
        dlg.show();
        return dlg;
//        ab.setCancelable(false);
//        ab.setPositiveButton(R.string.YES, okClick);
//        ab.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            }
//        });
//        ab.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//
//            }
//        });
//        AlertDialog dlg = ab.create();
//        dlg.show();
//        return dlg;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yes:
                if (mDialogInterface != null) {
                    mDialogInterface.dismiss();
                    dismiss();
                } else {
                    dismiss();
                }
                break;
            case R.id.btn_no:
                if (mDialogInterface != null) {
                    mDialogInterface.cancel();
                    dismiss();
                } else {
                    dismiss();
                }
                break;
        }
    }
}
