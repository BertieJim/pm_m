package com.example.bertie.pm_m;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import util.IpAutoRemove;
import views.SignaturePad;

/**
 * Created by Bertie on 17/3/14.
 */

public class Register extends Activity {
    private SignaturePad sign_pad;
    private Button btn_clear;
    private Button btn_confirm;
    //设置 多次按确定，有猜测嫌疑，应锁定 取消机会
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 加上这句设置为全屏 不加则只隐藏title
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.register);
        sign_pad = (SignaturePad)findViewById(R.id.signature_pad);
        fullScreenDisplay();
        sign_pad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
                // do nothing here.
            }

            @Override
            public void onSigned() {
                btn_confirm.setEnabled(true);
                btn_clear.setEnabled(true);
            }

            @Override
            public void onClear() {
                btn_confirm.setEnabled(false);
                btn_clear.setEnabled(false);
            }
        });
        btn_clear = (Button) findViewById(R.id.btn_clear);
        btn_confirm = (Button) findViewById(R.id.btn_regiconfirm);
        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sign_pad.clear();
            }
        });

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                SparseArray<SignaturePad.MotionEventRecorder> records = sign_pad.getMotionEventRecord();
                sign_pad.clearMotionEventRecord();
                fullScreenDisplay();

                Toast.makeText(Register.this,
                        "ddddd",
                        Toast.LENGTH_SHORT).show();
            }
        });



    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void fullScreenDisplay () {
        if(sign_pad != null) {
            ;
            // full screen setting, make our sign UI fullscreen.
            sign_pad.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

}
