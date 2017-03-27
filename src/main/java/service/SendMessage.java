package service;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Message;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import pm_m.SignaturesOuterClass;
import pm_m.SignaturesOuterClass.Signature;
import pm_m.SignaturesOuterClass.Signatures;
import pm_m.SignaturesOuterClass.Point;

import java.lang.StringBuilder;
import views.SignaturePad;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.example.bertie.pm_m.Unlock;
import com.google.protobuf.ByteString;

import zeromq.ZMQ;

/**
 * Created by Bertie on 17/3/16.
 */

public class SendMessage extends Service {
    private static final String TAG = "sendmsq_service";

    ZMQ.Context zcontext = ZMQ.context(1);
    public class LocalBinder extends Binder {
        public SendMessage getService() {
            return SendMessage.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public boolean sendtokenMessage()
    {
        new Thread(new Runnable() {
            //TODO:加入线程池
            @Override
            public void run() {
                SharedPreferences sp2 = getSharedPreferences("PCINFO", Activity.MODE_PRIVATE);

                String token = sp2.getString("TOKEN",null);
                String ip = sp2.getString("IP",null);
                String port = sp2.getString("PORT",null);
                Log.d(TAG, "sendTokenmsg...");

                sendToken(token,ip,port);
            }
        }).start();

        return true;



    }
    public boolean sendSigMessage(final SparseArray<SignaturePad.MotionEventRecorder> records) {
        new Thread(new Runnable() {
            //TODO:加入线程池
            @Override
            public void run() {
                SharedPreferences sp2 = getSharedPreferences("PCINFO", Activity.MODE_PRIVATE);
                String ip = sp2.getString("IP", null);
                String port = sp2.getString("PORT", null);
                String token = sp2.getString("TOKEN", null);

                Signatures.Builder sigsbuilder = Signatures.newBuilder();
                sigsbuilder.setId(token); //pc端还要解析下 token 来看是不是该机器发送的信息。


                // read all records
                Signature.Builder sig_builder = Signature.newBuilder();
                for (int i = 0; i < records.size(); i++) {
                    Point.Builder pbuilder = Point.newBuilder();
                    pbuilder.setX(records.get(i).getX());
                    pbuilder.setY(records.get(i).getY());
                    pbuilder.setP(records.get(i).getZ());
                    pbuilder.setT(records.get(i).getTime());
                    sig_builder.addPoints(pbuilder);
                }
                sigsbuilder.addSignatures(sig_builder);//只有一个 因为这里是 解锁 调用的函数
                sendData(sigsbuilder.build(), ip, port);
                //Log.d(TAG, records.toString());


            }
        }).start();

        return true;
    }
    /*
    public boolean sendSigMessage()
    {
        new Thread(new Runnable() {
            //TODO:加入线程池
            @Override
            public void run() {
                SharedPreferences sp2 = getSharedPreferences("PCINFO", Activity.MODE_PRIVATE);
                String ip = sp2.getString("IP",null);
                String port = sp2.getString("PORT",null);
                String token = sp2.getString("TOKEN",null);
                Signatures.Builder sigsbuilder = Signatures.newBuilder();
                sigsbuilder.setId(token);
                sendData(sigsbuilder.build(),ip,port);
            }
        }).start();
        return true;

    }
    */
    @Override
    public void onDestroy() {
        super.onDestroy();
        zcontext.term();
    }

    private boolean sendData(Signatures sigs,String ip2,String port) {
        // zmq
        ZMQ.Socket mRequest = zcontext.socket(ZMQ.REQ);
        // bind address
        Log.d(TAG, "Connecting to desktop...");
        String ip = ip2.replace(',','.');
        Log.d(TAG, ip+"Connecting to desktop...");

        mRequest.connect("tcp://"+ip+":"+port);

        // send data.
        Log.d(TAG, "Connected! Sending data...");
        Log.d(TAG, "tcp://"+ip+":"+port);

        mRequest.send(sigs.toByteArray(), 0);
        Log.d(TAG, "All data sent!");

        mRequest.close();

        return true;
    }
    private void sendToken(String token,String ip2,String port) {
        // zmq
        ZMQ.Socket mRequest = zcontext.socket(ZMQ.REQ);
        // bind address
        Log.d(TAG, "token Connecting to desktop...");
        String ip = ip2.replace(',','.');
        Log.d(TAG, ip+"token Connecting to desktop...");

        mRequest.connect("tcp://"+ip+":"+port);

        // send data.
        Log.d(TAG, "token Connected! Sending data...");
        Log.d(TAG, "token tcp://"+ip+":"+port);

        mRequest.send(token, 0);
        Log.d(TAG, "token All data sent!");

        String tokenback = mRequest.recvStr();
        mRequest.close();
        Handler h = Unlock.handler;
//        Message msg2 = new Message();
//        msg2.what = 3;
//        h.sendMessageDelayed(msg2,40000);//与服务器相同
        String strtokenback[] = tokenback.split(",");
        Log.d(TAG, "----"+strtokenback[0]+"---"+strtokenback[1]+"---"+tokenback);
        if(strtokenback[1].equals(token))
        {
            if(strtokenback[0].equals("Y"))
            {
                Message msg = new Message();
                msg.what = 1;
                Log.d(TAG, "token yes");
                h.sendMessage(msg);

            }
            else
            {
                Message msg = new Message();
                msg.what = 0;
                h.sendMessage(msg);
            }
        }




    }

}
