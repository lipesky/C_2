package com.example.emailthreadfilterservice;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android .os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import androidx.annotation.NonNull;
import com.example.commons.Email;
import com.example.commons.List;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class EmailThreadFilter extends Service{

    // Handler to process messages from the calling processes
    private class MessageHandler extends Handler{
        MessageHandler(Looper looper){
            super(looper);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            // Remove duplicates
            if(msg.what == 1){

                // parse the email thread from the bytes received
                List<Email> emailList;
                byte[] bytes = msg.getData().getByteArray("emailThread");
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                try{
                    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                    emailList = (List<Email>) objectInputStream.readObject();

                    // Remove duplicates
                    emailList.removeDuplicates();

                    // creates a bundle and stores the filtered email's thread's bytes in it
                    Bundle bundle = new Bundle();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    ObjectOutputStream objectOutputStream;
                    objectOutputStream = new ObjectOutputStream(out);
                    objectOutputStream.writeObject(emailList);
                    objectOutputStream.flush();
                    bundle.putByteArray("emailThread", out.toByteArray());

                    // send back the filtered email thread
                    Message msgx = Message.obtain(null, 1);
                    msgx.setData(bundle);
                    msg.replyTo.send(msgx);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
            super.handleMessage(msg);
        }
    }

    private HandlerThread messageThread;
    private MessageHandler messageHandler;
    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("service created");
        this.messageThread = new HandlerThread("handler thread");
        this.messageThread.start();
        this.messageHandler = new MessageHandler(this.messageThread.getLooper());
    }

    @Override
    public void onDestroy() {
        this.messageThread.getLooper().quit();
        this.messageThread.interrupt();
        System.out.println("service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        System.out.println("service: Service bound. Intent (type: "+intent.getType()+") {dest: "+intent.getStringExtra(Intent.EXTRA_EMAIL)+", "
                +" message: "+ intent.getStringExtra(Intent.EXTRA_TEXT) +"}");
        Messenger messenger = new Messenger(this.messageHandler);
        return messenger.getBinder();
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId){
        //  Does NOT recreate the service when its get killed
        System.out.println("service: onStartCommand called");
        return START_NOT_STICKY;
        //return (super.onStartCommand(intent, flags, startId));
    }

    @Override
    public boolean onUnbind(Intent intent){
        super.onUnbind(intent);
        return false;
    }
}
