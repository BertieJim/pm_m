package com.example.bertie.pm_m;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import service.SendMessage;
import util.IpAutoRemove;
import views.SignaturePad;

/**
 * Created by Bertie on 17/3/14.
 */

public class Register extends Activity {
    private SignaturePad sign_pad;
    private Button btn_clear;
    private Button btn_confirm;
    private TextView txt_times;
    private static ArrayList mRecords = new ArrayList();
    public int times = 0;
    public static MyHandler handler2;
    private SendMessage mService = null;
    public boolean iftokenright = false;
    private PopupMenu popupMenu = null;

    private boolean mIsBound = false;
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

    //设置 多次按确定，有猜测嫌疑，应锁定 取消机会
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 加上这句设置为全屏 不加则只隐藏title
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.register);
        doBindService();
        handler2 = new MyHandler();
        sign_pad = (SignaturePad) findViewById(R.id.signature_pad);
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
                if(mRecords.size() < 5)
                {
                    btn_confirm.setEnabled(false);
                }
                btn_clear.setEnabled(false);
            }
        });
        btn_clear = (Button) findViewById(R.id.btn_clear);
        btn_confirm = (Button) findViewById(R.id.btn_regiconfirm);
        txt_times = (TextView) findViewById(R.id.signature_pad_times);
        txt_times.setText("已经输入 " + String.valueOf(times) + "/5 个签名");

        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sign_pad.clear();
                sign_pad.getMotionEventRecord().clear();
            }
        });
        btn_confirm.setText("确定");
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                if (mRecords.size() == 5) {

                    //注册
                    if (mService != null) {
                        mService.sendRegisterMessage(mRecords);
                    }

                   // mRecords.clear();
                    sign_pad.setEnabled(false);
                    btn_confirm.setText("等待");
                    btn_confirm.setEnabled(false);
                    btn_clear.setEnabled(false);
                }
                if (mRecords.size() > 5) {
                    times = 0;
                    mRecords.clear();
                }
                times++;
                if(mRecords.size() < 5) {
                    mRecords.add(sign_pad.getMotionEventRecord().clone());
                    txt_times.setText("已经输入 " + String.valueOf(mRecords.size()) + "/5 个签名");
                }
                sign_pad.clearMotionEventRecord();
                sign_pad.clear();
                fullScreenDisplay();

                if (mRecords.size() == 5) {
                    btn_confirm.setText("注册");
                }
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
        mRecords.clear();
        times = 0;
        sign_pad.clearMotionEventRecord();

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
    //选项按钮
    public void onOptionClicked(View view) {
        if (popupMenu == null) {
            popupMenu = new PopupMenu(this, view);

            getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(
                    new PopupMenu.OnMenuItemClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public boolean onMenuItemClick(final MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.style_fu:
                                    mRecords.clear();
                                    sign_pad.clear();
                                    sign_pad.getMotionEventRecord().clear();
                                    sign_pad.setPenstyle(1);
                                    //可能没有这么简单 对比color
                                    item.setChecked(true);
                                    fullScreenDisplay();
                                    break;
                                case R.id.style_wang:
                                    mRecords.clear();
                                    sign_pad.clear();
                                    sign_pad.getMotionEventRecord().clear();
                                    sign_pad.setPenstyle(2);

                                    item.setChecked(true);

                                    fullScreenDisplay();
                                    break;
                                case R.id.style_yan:
                                    mRecords.clear();
                                    sign_pad.clear();
                                    sign_pad.getMotionEventRecord().clear();
                                    sign_pad.setPenstyle(5);

                                    item.setChecked(true);

                                    fullScreenDisplay();
                                    break;
                                case R.id.style_zhao:
                                    mRecords.clear();
                                    sign_pad.clear();
                                    sign_pad.getMotionEventRecord().clear();
                                    sign_pad.setPenstyle(3);

                                    item.setChecked(true);

                                    fullScreenDisplay();
                                    break;
                                case R.id.style_zhong:
                                    mRecords.clear();
                                    sign_pad.clear();
                                    sign_pad.getMotionEventRecord().clear();
                                    sign_pad.setPenstyle(4);

                                    item.setChecked(true);

                                    fullScreenDisplay();
                                    break;
                                default:
                                    break;
                            }
                            return true;
                        }
                    });

        }

        // Reflect to invoke setForceShowIcon function to show menu icon.
        // we may get IllegalAccessException: access to field not allowed here,
        // but it's ok, we just catch it and ignore it.
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper
                            .getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod(
                            "setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        MenuItem item=popupMenu.getMenu().findItem(R.id.style_fu);
        popupMenu.show();
    }

    class MyHandler extends Handler {

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                //成功

                Toast.makeText(Register.this,
                        "发送成功",
                        Toast.LENGTH_SHORT).show();
//                btn_confirm.setBackgroundColor(Color.parseColor("#FF7400"));
                //refresh();
                finish();

            }
        }
    }
}
