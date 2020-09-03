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

public class SearchDevices extends AppCompatActivity {

    public  static Toast toast = null;
    public static Intent home = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_devices);
        Log.i("oncreate", "entered second activity ");

    }

    public void returnHome()
    {
        home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(home);
        toast.setText("Connection Failed!!");
        toast.show();
    }

    public  String sendCommand(String command)
    {
        final String[] response = new String[1];
        final PrintWriter finalClientout = MainActivity.clientout;
        if(MainActivity.client!=null)
        {
            try {
                finalClientout.println(command);
                Log.i("Socket","Sent command::" + command);
                response[0] = MainActivity.clientin.readLine();
                toast.setText(response[0]);
                toast.show();

                if(response[0]==null)
                {
                    Log.e("error", "run: connection closed by server!!!");
                    returnHome();
                    return(null);
                }
                else
                    {
                    return((String)response[0]);
                }

            } catch (IOException e) {
                Log.e("Socket","failed to recv/send input");
                e.printStackTrace();
                returnHome();
                return(null);
            }

        }
        return(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        searchDevices();
    }

    private void searchDevices()
    {
        final String[] response = new String[1];
        Log.i("Socket","will start devlist");
        final Intent devicecontrol = new Intent(this,DeviceControl.class);

        toast = Toast.makeText(this , "Sent Devlist::", Toast.LENGTH_SHORT);
        home = new Intent(this, MainActivity.class);

        Log.i("SearchDevices","sending command!!");

        final Thread sendcommand = new Thread(new Runnable() {
            @Override
            public void run() {
                response[0] =sendCommand(getString(R.string.DEVLISTCOMMAND));
                if(response[0]!=null)
                {
                    Log.i("SearchDevices","Received response is "+response[0]);
                    devicecontrol.putExtra("response", response[0]);
                    startActivity(devicecontrol);
                }
            }
        });

        sendcommand.start();

    }
}