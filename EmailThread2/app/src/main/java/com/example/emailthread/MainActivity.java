package com.example.emailthread;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.res.ResourcesCompat;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.commons.Email;
import com.example.commons.List;
import com.google.android.material.snackbar.Snackbar;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    // Handler to process the messages received by the service
    private class ServiceHandler extends Handler{
        ServiceHandler(Looper looper){
            super(looper);
        };
        @Override
        public void handleMessage(@NonNull Message msg) {

            // Receive the fieltered email thread
            if(msg.what == 1){
                byte[] bytes = msg.getData().getByteArray("emailThread");
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                try{
                    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

                    // desserialize the bytes from email thread
                    final List<Email> emailList = (List<Email>) objectInputStream.readObject();

                    // show on ui the result and unbind from the service
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showEmailThread(emailList);
                            Snackbar.make(parentLayout, "Duplicates removed!", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            unbindAndRelease();
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
            super.handleMessage(msg);
        }
    }

    private ConstraintLayout parentLayout;
    private int lastId;
    private ServiceConnection con;
    private HandlerThread handlerOutputThread;
    private Messenger serviceInput;
    private Messenger serviceOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();

        // Creates and adds the button "remove duplicates" to the action bar
        Button removeDupButton = new Button(this);
        removeDupButton.setText("remove\nDuplicates");
        //removeDupButton.setTextColor(ResourcesCompat.getColor(getResources(),R.color.white,null ));
        //removeDupButton.setBackgroundColor(ResourcesCompat.getColor(getResources(),R.color.btnBackground,null ));
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.addView(removeDupButton);
        linearLayout.setGravity(Gravity.RIGHT);
        actionBar.setCustomView(linearLayout);
        actionBar.setDisplayShowCustomEnabled(true);
        removeDupButton.setOnClickListener(this);

        // saves reference to parent layout of the email views
        this.parentLayout = findViewById(R.id.parentLayout);

        // Show the initial EmailThread with duplicates
        this.showEmailThread(this.getOriginalEmailThread());

    }

    // Show a email thread on UI
    public void showEmailThread(List<Email> emailThread){

        // Remove the old Views from the parent
        this.parentLayout.removeAllViews();

        // Init with parent Id for constraining Top
        this.lastId = this.parentLayout.getId();

        // I could implement the method map here for performance reasons.
        // This is ugly, but as the purpose of this code is to see the comunication between the service and this application
        // i'll let it here.
        for(int i = 0; i < emailThread.length(); i++){
            // Add the Email view Item on the Parent Layout
            addItem(emailThread.get(i));
        }
    }

    // Add the specified email on UI's email thread
    public void addItem(Email email){

        // Inflates the view representing this email item
        ConstraintLayout emailview = (ConstraintLayout) getLayoutInflater().inflate(R.layout.emailview, null);

        // Fill the new view with the data fromEmail
        TextView textView = emailview.findViewById(R.id.subject);
        textView.setText(email.getSubject());
        textView = emailview.findViewById(R.id.from);
        textView.setText(email.getFrom());
        textView = emailview.findViewById(R.id.dest);
        textView.setText(email.getDest());
        textView = emailview.findViewById(R.id.body);
        textView.setText(email.getBody());

        // Adds the email to the parent and add the contraints
        emailview.setId(View.generateViewId());
        this.parentLayout.addView(emailview, 0);
        ConstraintSet set = new ConstraintSet();
        set.clone(this.parentLayout);
        set.setMargin(emailview.getId(), ConstraintSet.TOP, 30);
        set.constrainWidth(emailview.getId(), ConstraintSet.MATCH_CONSTRAINT_SPREAD);
        set.connect(emailview.getId(), ConstraintSet.TOP, this.lastId, this.lastId == this.parentLayout.getId() ? ConstraintSet.TOP: ConstraintSet.BOTTOM);
        set.connect(emailview.getId(), ConstraintSet.START, this.parentLayout.getId(), ConstraintSet.START);
        set.connect(emailview.getId(), ConstraintSet.END, this.parentLayout.getId(), ConstraintSet.END);

        // Apply new contraints
        set.applyTo(this.parentLayout);

        // Updates the last view for constraining Top of nexts views
        this.lastId = emailview.getId();
    }


    @Override
    // Receive clicks in remove duplicates button
    public void onClick(View view) {

        // Creates the new intent targeting the service
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/html");
        intent.putExtra(Intent.EXTRA_EMAIL, "emailaddress@emailaddress.com");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
        intent.putExtra(Intent.EXTRA_TEXT, "I'm email body.");
        intent.setPackage("com.example.emailthreadfilterservice");

        // Sets the ServiceConnection Object for binding
        con = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                serviceInput = new Messenger(iBinder);
                handlerOutputThread = new HandlerThread("Hander Output of service");
                handlerOutputThread.start();
                serviceOutput = new Messenger( new ServiceHandler(handlerOutputThread.getLooper()) );

                // Sends the original EmailThread and request the service to remove it's duplicates
                removeDuplicatesOnService();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                // nothing to do...
            }
        };

        // Binds the service
        boolean bind_result = bindService(intent, con, BIND_AUTO_CREATE);
        System.out.println("bind result: "+bind_result);
    }


    // Sends the original EmailThread and request the service to remove it's duplicates
    public void removeDuplicatesOnService(){

        // Get the original email thread
        List<Email> list = getOriginalEmailThread();

        // Creates the bundle and stores the byte array from the original email thread
        Bundle bundle = new Bundle();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream;
        try{
            objectOutputStream = new ObjectOutputStream(out);
            objectOutputStream.writeObject(list);
            objectOutputStream.flush();
            bundle.putByteArray("emailThread", out.toByteArray());
            Message msg = Message.obtain(null, 1, 0, 0);

            // add the bundle to message
            msg.setData(bundle);

            // add the messenger for retrieving the response from the service
            msg.replyTo = serviceOutput;

            // send the message
            serviceInput.send(msg);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // Retrieves the original email thread with duplicates
    public List<Email> getOriginalEmailThread(){
        List<Email> emailThread = new List<Email>();

        emailThread.addLast(new Email("francisco@gmail.com",
                "felipe@gmail.com",
                "Solicitação de Documentos",
                "Boa tarde Franciso, poderia enviar novamente os documentos solicitados por Telefone?"));
        emailThread.addLast(new Email("felipe@gmail.com",
                "francisco@gmail.com",
                "Solicitação de Documentos",
                "Boa tarde Felipe, Segue em anexo"));
        emailThread.addLast(new Email("felipe@gmail.com",
                "francisco@gmail.com",
                "Solicitação de Documentos",
                "Boa tarde Felipe, esqueci de anexar na mensagem anterior"));
        // Duplicate
        emailThread.addLast(new Email("felipe@gmail.com",
                "francisco@gmail.com",
                "Solicitação de Documentos",
                "Boa tarde Felipe, esqueci de anexar na mensagem anterior"));
        emailThread.addLast(new Email("felipe@gmail.com",
                "francisco@gmail.com",
                "Solicitação de Documentos",
                "ctps_frente"));

        // Duplicate
        emailThread.addLast(new Email("francisco@gmail.com",
                "felipe@gmail.com",
                "Solicitação de Documentos",
                "Boa tarde Franciso, poderia enviar novamente os documentos solicitados por Telefone?"));

        return emailThread;
    }

    // Unbind from service and release resources on Stop;
    @Override
    public void onStop(){
        unbindAndRelease();
        super.onStop();
    }

    // Unbind from service and release resources on destroy;
    @Override
    public  void onDestroy(){
        unbindAndRelease();
        super.onDestroy();
    }


    // Unbind from service and releases the messenger thread
    public void unbindAndRelease(){
        if(con != null){
            unbindService(con);
            con = null;
        }
        if(handlerOutputThread != null){
            handlerOutputThread.interrupt();
            handlerOutputThread = null;
        }
    }
}

