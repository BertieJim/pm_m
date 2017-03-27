package com.example.bertie.pm_m;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import pm_m.SignaturesOuterClass;
import service.SendMessage;
import views.SignaturePad;

/**
 * Created by Bertie on 17/3/14.
 */

/**
 * 重复发送消息队列问题
 * 多次点击button clickable问题
 *
 *
 **/
public class Unlock extends Activity {
    private SignaturePad sign_pad;
    private Button btn_clear;
    private Button btn_confirm;
    public static MyHandler handler;
    private SendMessage mService = null;
    public boolean iftokenright = false;
    private boolean mIsBound = false;
    public int trytimes = 5;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mService = ((SendMessage.LocalBinder) service).getService();


        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mService = null;

        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
        sign_pad.clearMotionEventRecord();

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 加上这句设置为全屏 不加则只隐藏title
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        handler = new MyHandler();
        setContentView(R.layout.unlock);
        doBindService();
        sign_pad = (SignaturePad) findViewById(R.id.signature_pad2);
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
        btn_clear = (Button) findViewById(R.id.btn_clear2);
        btn_confirm = (Button) findViewById(R.id.btn_unlockconfirm);
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
                if (mService != null) {
                    mService.sendSigMessage(records);
                }

//                sign_pad.clearMotionEventRecord();

//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        sign_pad.clearMotionEventRecord();
//
//                    }
//                }).start();

                fullScreenDisplay();


            }
        });
        //TODO:这里为测试传送sigs作了注释 测试后需要改回
        //btn_confirm.setText("等待");
        //btn_confirm.setClickable(false);
        //以下不是测试
//        btn_confirm.setBackgroundColor(Color.parseColor("#FFAF14"));

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void fullScreenDisplay() {
        if (sign_pad != null) {
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

    private void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(this,
                SendMessage.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    private void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
//            mService.onDestroy();
        }
    }

    class MyHandler extends Handler {

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                //成功
                iftokenright = true;
                btn_confirm.setText("确定");
                btn_confirm.setClickable(true);

                Toast.makeText(Unlock.this,
                        "SSSSSSS",
                        Toast.LENGTH_SHORT).show();
                trytimes = 5;
//                btn_confirm.setBackgroundColor(Color.parseColor("#FF7400"));
                //refresh();
            } else if(msg.what == 0) {
                if((trytimes--)>0)
                {
                    String note = "错误的token，您还有"+String.valueOf(trytimes)+"次机会";
                    Toast.makeText(Unlock.this,
                            note,
                            Toast.LENGTH_SHORT).show();
                    //TODO:这里为测试传送sigs作了注释 测试后需要改回
                    //iftokenright = false;
                    //finish();
                }
                else
                {
                    String note = "很抱歉,您总是输错token,我们建议5分钟后再尝试";
                    Toast.makeText(Unlock.this,
                            note,
                            Toast.LENGTH_SHORT).show();
                    //TODO:这里为测试传送sigs作了注释 测试后需要改回
                    //Unlock.this.finish();
                    //finish();
                }
            }
            if(msg.what == 2&&iftokenright==false)
            {
                String note = "当前网络不通，您还有"+String.valueOf(trytimes)+"次机会";
                Toast.makeText(Unlock.this,
                        note,
                        Toast.LENGTH_SHORT).show();
                Log.d("sendmsg", "Gettttttthismsg2");
                //TODO:这里为测试传送sigs作了注释 测试后需要改回
                //finish();

            }
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void refresh() {
        onCreate(null);
    }
}
