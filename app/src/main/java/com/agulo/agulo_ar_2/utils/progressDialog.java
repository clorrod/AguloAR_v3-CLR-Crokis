package com.agulo.agulo_ar_2.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.agulo.agulo_ar_2.R;

public class progressDialog extends Dialog {

    public progressDialog(@NonNull Context mContext){

        super(mContext);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER_HORIZONTAL;

        View v = LayoutInflater.from(mContext).inflate(R.layout.progress_design,null);

        setContentView(v);
        getWindow().setAttributes(params);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        setCancelable(false);
        setTitle(null);
        setCanceledOnTouchOutside(false);


    }

}