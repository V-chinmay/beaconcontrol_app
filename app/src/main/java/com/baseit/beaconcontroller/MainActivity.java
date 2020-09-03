package com.baseit.beaconcontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "info" ;
    boolean failflag=false;

    static PrintWriter clientout=null;
    static BufferedReader clientin = null;
    public static String ipaddress = "192.168.29.10" ;
    public static  int port = 3000;


    static Socket client = null;

    ToggleButton connectreq = null;
    Button search =  null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectreq = (ToggleButton) findViewById(R.id.connectreq);
        search = (Button) findViewById(R.id.searchdevices);
        search.setEnabled(false);
        connectreq.setBackground(getResources().getDrawable(R.drawable.roundedfail));
        search.setBackground(getResources().getDrawable(R.drawable.roundedfail));

    }

    public  void onClickDevSearch(View view) {
        Intent searchdevices = new Intent(this,SearchDevices.class);
        startActivity(searchdevices);
        Log.i("Search","started device search");
    }


    public static void startIO()
    {
        try {
            clientout = new PrintWriter(client.getOutputStream(),true);
            clientin = new BufferedReader(new InputStreamReader(client.getInputStream()));

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Socket","Failed to enable write/read");
            return;
        }
    }





    public void onClickConnectSocket(View view) {

        Thread botcon = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(connectreq.isChecked()) {
                        client = new Socket(ipaddress, port);
                        if(client==null)
                        {
                            failflag=true;
                            connectreq.setChecked(false);
                            connectreq.setBackground(getResources().getDrawable(R.drawable.roundedfail));
                            search.setEnabled(false);
                            search.setBackground(getResources().getDrawable(R.drawable.roundedfail));
                            Log.i("Socket","Failed to connect to the server");
                        }
                        Log.i("Socket", "connected to network");
                        connectreq.setBackground(getResources().getDrawable(R.drawable.roundedsuccess));
                        startIO();
                    }
                    else
                    {
                        if(client!=null)
                        {
                            client.shutdownOutput();
                            client.close();
                            Log.i("Socket", "Closed connection!!");
                        }
                        Log.i("Socket", "Disconnected to network");
                        connectreq.setBackground(getResources().getDrawable(R.drawable.roundedfail));
                        search.setEnabled(false);
                        search.setBackground(getResources().getDrawable(R.drawable.roundedfail));
                    }
                    failflag=false;
                } catch (Exception e) {
                    e.printStackTrace();
                    failflag=true;
                    connectreq.setChecked(false);
                    connectreq.setBackground(getResources().getDrawable(R.drawable.roundedfail));
                    search.setEnabled(false);
                    search.setBackground(getResources().getDrawable(R.drawable.roundedfail));
                    Log.i("Socket","Failed to connect to the server");
                }
                Log.i("Socket","finshed");
            }
        });

        botcon.start();
        try {
            botcon.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.i("onclick","executed the socket connection!!");

        if(failflag)
        {
            Toast toast = Toast.makeText(getApplicationContext(), "Connection Failed", Toast.LENGTH_SHORT);
            toast.show();
        }
        else
        {
            search.setEnabled(true);
            search.setBackground(getResources().getDrawable(R.drawable.roundedsuccess));
        }
    }
}