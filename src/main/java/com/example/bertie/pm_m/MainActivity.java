package com.example.bertie.pm_m;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.text.InputType;
import android.widget.EditText;
import android.app.Activity;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.view.WindowManager;

import service.SendMessage;
import util.IpAutoRemove;

public class MainActivity extends AppCompatActivity {
    //把IP 用户名保存在sharepreferenced里
    //不再直接传值了
    //重置选项
    private Button btn_unlock;
    private Button btn_register;
    private SendMessage mService = null;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 加上这句设置为全屏 不加则只隐藏title
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        // start service here.

        Intent intent = new Intent(MainActivity.this, SendMessage.class);
        startService(intent);

        btn_unlock = (Button)findViewById(R.id.button_unlock);
        btn_register = (Button)findViewById(R.id.button_register);

        btn_unlock.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                showCustomizeDialog(Unlock.class);
            }
        });
        btn_register.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                showCustomizeDialog(Register.class);
            }
        });
        doBindService();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void showCustomizeDialog(final Class gotoclass) {
    /* @setView 装入自定义View ==> R.layout.dialog_customize
     * 由于dialog_customize.xml只放置了一个EditView，因此和图8一样
     * dialog_customize.xml可自定义更复杂的View
     */
        final AlertDialog.Builder customizeDialog =
                new AlertDialog.Builder(MainActivity.this);
        final View dialogView = LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.dialog_customize,null);
//        customizeDialog.setTitle("我是一个自定义Dialog");
        customizeDialog.setView(dialogView);
        final EditText edit_ip_1 =
                (EditText)dialogView.findViewById(R.id.local_gw_edit_1);
        final EditText edit_ip_2 =
                (EditText)dialogView.findViewById(R.id.local_gw_edit_2);
        final EditText edit_ip_3 =
                (EditText)dialogView.findViewById(R.id.local_gw_edit_3);
        final EditText edit_ip_4 =
                (EditText)dialogView.findViewById(R.id.local_gw_edit_4);
        final EditText edit_port =
                (EditText)dialogView.findViewById(R.id.edit_port);
        final Button btn_confirm = (Button)dialogView.findViewById(R.id.btn_confirm);
        final Button btn_cancel = (Button)dialogView.findViewById(R.id.btn_cancel);

        IpAutoRemove automove_listen1 = new IpAutoRemove(edit_ip_1,edit_ip_2,3);
        IpAutoRemove automove_listen2 = new IpAutoRemove(edit_ip_2,edit_ip_3,3);
        IpAutoRemove automove_listen3 = new IpAutoRemove(edit_ip_3,edit_ip_4,3);
        IpAutoRemove automove_listen4 = new IpAutoRemove(edit_ip_4,edit_port,3);
        edit_ip_1.addTextChangedListener(automove_listen1);
        edit_ip_2.addTextChangedListener(automove_listen2);
        edit_ip_3.addTextChangedListener(automove_listen3);
        edit_ip_4.addTextChangedListener(automove_listen4);

        SharedPreferences sp2 = getSharedPreferences("PCINFO", Activity.MODE_PRIVATE);
        String IP = sp2.getString("IP",null);
        String PORT = sp2.getString("PORT",null);

        /*
        Toast.makeText(MainActivity.this,
                          IP,
                          Toast.LENGTH_SHORT).show();
        */
        if(IP!=null&&PORT!=null&&!IP.equals(null)&&!PORT.equals(null)&&IP.length()!=0) {
            String[] ip_str = IP.split(",");
            edit_ip_1.setText(ip_str[0]);
            edit_ip_2.setText(ip_str[1]);
            edit_ip_3.setText(ip_str[2]);
            edit_ip_4.setText(ip_str[3]);

            edit_port.setText(PORT);

            edit_ip_1.setInputType(InputType.TYPE_NULL);
            edit_ip_2.setInputType(InputType.TYPE_NULL);
            edit_ip_3.setInputType(InputType.TYPE_NULL);
            edit_ip_4.setInputType(InputType.TYPE_NULL);
            edit_port.setInputType(InputType.TYPE_NULL);
            btn_cancel.setBackground(getDrawable(R.drawable.btn_reset2));

        }
        else
        {
            btn_cancel.setBackground(getDrawable(R.drawable.btn_cancel2));
        }
        final AlertDialog dialog = customizeDialog.show();

        if(IP!=null&&PORT!=null&&!IP.equals(null)&&!PORT.equals(null)&&IP.length()!=0) {
            btn_cancel.setOnClickListener((new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences mySharedPreferences = getSharedPreferences("PCINFO",
                            Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = mySharedPreferences.edit();
                    editor.putString("IP",null);
                    editor.putString("PORT",null);
                    editor.apply();
                    editor.commit();

                    dialog.dismiss();
                    showCustomizeDialog(gotoclass);

                }
            }));
        }
        else{
            btn_cancel.setOnClickListener((new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    dialog.dismiss();

                }
            }));


        }

        btn_confirm.setOnClickListener((new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(edit_ip_1.getText().toString().length()<3||edit_ip_2.getText().toString().length()<3
                        ||edit_ip_3.getText().toString().length()<3||edit_ip_4.getText().toString().length()<3
                ||edit_port.getText().toString().length()<4)
                {
                    Toast.makeText(MainActivity.this,
                            "IP地址及端口必须填写完整",
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    String ip = edit_ip_1.getText().toString();
                    ip = ip.concat(","+edit_ip_2.getText().toString());
                    ip = ip.concat(","+edit_ip_3.getText().toString());
                    ip = ip.concat(","+edit_ip_4.getText().toString());
                    String port = edit_port.getText().toString();
                    SharedPreferences mySharedPreferences = getSharedPreferences("PCINFO",
                            Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = mySharedPreferences.edit();
                    editor.putString("IP",ip);
                    editor.putString("PORT",port);
                    editor.apply();
                    editor.commit() ;
                    /*
                    String test = mySharedPreferences.getString("IP",null);
                    Toast.makeText(MainActivity.this,
                            test,
                          Toast.LENGTH_SHORT).show();
                    */
                    dialog.dismiss();
                    showDialog(gotoclass);

                }

            }
        }));

    }

    private void showDialog(final Class gotoclass) {
    /* @setView 装入自定义View ==> R.layout.dialog_customize
     * 由于dialog_customize.xml只放置了一个EditView，因此和图8一样
     * dialog_customize.xml可自定义更复杂的View
     */
        final android.app.AlertDialog.Builder customizeDialog =
                new android.app.AlertDialog.Builder(MainActivity.this);
        final View dialogView = LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.dialog_customize_reg,null);
//        customizeDialog.setTitle("我是一个自定义Dialog");
        customizeDialog.setView(dialogView);
        final EditText edit_token1 =
                (EditText)dialogView.findViewById(R.id.token_edit_1);
        final EditText edit_token2 =
                (EditText)dialogView.findViewById(R.id.token_edit_2);
        final EditText edit_token3 =
                (EditText)dialogView.findViewById(R.id.token_edit_3);
        final EditText edit_token4 =
                (EditText)dialogView.findViewById(R.id.token_edit_4);
        final EditText edit_token5 =
                (EditText)dialogView.findViewById(R.id.token_edit_5);
        final EditText edit_token6 =
                (EditText)dialogView.findViewById(R.id.token_edit_6);


        final Button btn_confirm = (Button)dialogView.findViewById(R.id.btn_confirmreg);
        final Button btn_cancel = (Button)dialogView.findViewById(R.id.btn_cancelreg);

        IpAutoRemove automov_listen1 = new IpAutoRemove(edit_token1,edit_token2,1);
        IpAutoRemove automov_listen2 = new IpAutoRemove(edit_token2,edit_token3,1);
        IpAutoRemove automov_listen3 = new IpAutoRemove(edit_token3,edit_token4,1);
        IpAutoRemove automov_listen4 = new IpAutoRemove(edit_token4,edit_token5,1);
        IpAutoRemove automov_listen5 = new IpAutoRemove(edit_token5,edit_token6,1);

        edit_token1.addTextChangedListener(automov_listen1);
        edit_token2.addTextChangedListener(automov_listen2);
        edit_token3.addTextChangedListener(automov_listen3);
        edit_token4.addTextChangedListener(automov_listen4);
        edit_token5.addTextChangedListener(automov_listen5);

        final android.app.AlertDialog dialog = customizeDialog.show();

        btn_cancel.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        }));

        btn_confirm.setOnClickListener((new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(edit_token1.getText().toString().length()<1||edit_token2.getText().toString().length()<1
                        ||edit_token3.getText().toString().length()<1||edit_token4.getText().toString().length()<1
                        ||edit_token5.getText().toString().length()<1||edit_token6.getText().toString().length()<1)
                {
                    Toast.makeText(MainActivity.this,
                            "口令未填写完整",
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    String token = edit_token1.getText().toString();
                    token = token.concat(edit_token2.getText().toString());
                    token = token.concat(edit_token3.getText().toString());
                    token = token.concat(edit_token4.getText().toString());
                    token = token.concat(edit_token5.getText().toString());
                    token = token.concat(edit_token6.getText().toString());

                    SharedPreferences mySharedPreferences = getSharedPreferences("PCINFO",
                            Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = mySharedPreferences.edit();
                    editor.putString("TOKEN",token);
                    editor.apply();
                    editor.commit() ;
                    if(mService!=null&&gotoclass==Unlock.class)
                    {
                        //TODO:这里为测试传送sigs作了注释 测试后需要改回
                        mService.sendtokenMessage();
                    }
                    /*
                    String test = mySharedPreferences.getString("IP",null);
                    Toast.makeText(MainActivity.this,
                            test,
                          Toast.LENGTH_SHORT).show();
                    */
                    dialog.dismiss();
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, gotoclass);
                    MainActivity.this.startActivity(intent);
                }

            }
        }));

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


















    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

}

