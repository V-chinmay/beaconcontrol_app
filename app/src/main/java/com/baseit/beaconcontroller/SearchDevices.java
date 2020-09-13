package com.baseit.beaconcontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static com.baseit.beaconcontroller.MainActivity.client;

public class SearchDevices extends AppCompatActivity {

    public  static Toast toast = null;
    public static Intent home = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_devices);
        home= new Intent(this,MainActivity.class);

        Log.i("oncreate", "entered second activity ");

    }

    public void returnHome()
    {
        home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(home);
        toast.setText("Connection Failed!!");
        toast.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        final Intent devicecontrol = new Intent(this,DeviceControl.class);

        Thread getdevinfo= new Thread(new Runnable() {
            @Override
            public void run() {

                String devlist=basicSendCommand("DEVLIST");

                Log.i("SearchDevices","Received response for devlist is "+devlist);
                devicecontrol.putExtra("devlist", devlist);

                String allbatt = basicSendCommand("ALLBATT");

                Log.i("SearchDevices","Received response for allbatt is "+devlist);
                devicecontrol.putExtra("allbatt", allbatt);
                startActivity(devicecontrol);
            }
        });
        getdevinfo.start();
    }

    public  String basicSendCommand(final String command)
    {
        final String[] response = new String[1];
        response[0]=null;

        final PrintWriter finalClientout = MainActivity.clientout;

        Thread sender = new Thread(new Runnable() {
            @Override
            public void run() {
                if(client!=null)
                {
                    try {
                        finalClientout.println(command);
                        Log.i("Socket","Sent command::" + command);
                        response[0] = MainActivity.clientin.readLine();
                        if(response[0]==null)
                        {
                            Log.e("error", "run: connection closed by server!!!");
                            response[0]="n/a";
                            returnHome();
                        }
                        else
                        {
                            Log.i("response", "run: received response!!!"+ response[0]);
                        }

                    } catch (IOException e) {
                        Log.e("Socket","failed to recv/send input");
                        e.printStackTrace();
                        returnHome();
                    }

                }
            }
        });

        sender.start();
        try {
            sender.join();
            Log.i("info", "basicSendCommand: joined!!!");
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e("thread error::", "basicSendCommand: ");
        }

        return(response[0]);
    }

}