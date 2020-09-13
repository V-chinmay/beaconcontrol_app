package com.baseit.beaconcontroller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static com.baseit.beaconcontroller.MainActivity.client;
import static com.baseit.beaconcontroller.MainActivity.ipaddress;
import static com.baseit.beaconcontroller.MainActivity.port;
import static com.baseit.beaconcontroller.MainActivity.startIO;

public class DeviceControl extends AppCompatActivity {

    Spinner spinner = null;
    static String hedgehog= "0";
    Map<String, Float> devnstates = null;
    Map<String,Float> devnbatt = null;
    Toast actiontoast = null;
    TextView batterystatus =null;
    static String selectedbeacon = null;

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        try {
//            client.close();
//            Log.i("socket", "onDestroy: Closed correctly!!");
//        } catch (IOException e) {
//
//            e.printStackTrace();
//
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);


        Intent searchdevices = getIntent();
        String devlist_res=searchdevices.getStringExtra("devlist").trim();
        String allbatt_res=searchdevices.getStringExtra("allbatt").trim();

        Log.i("response", "onCreate: "+devlist_res);
        Log.i("response", "onCreate: "+allbatt_res);

        actiontoast = Toast.makeText(getBaseContext(),"dummy",Toast.LENGTH_SHORT);

        if(devlist_res.matches(getString(R.string.DEVLISTPATTERN)))
        {
            setupSpinner(devlist_res,allbatt_res);
        }
        else
        {
            actiontoast.setText("Invalid devlist packet Exiting!!");
            actiontoast.show();
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            returnHome();
        }

    }

    public Map<String,Float> packToDict(String pack)
    {
        String[] packraw = pack.split(",");

        Map<String,Float> packdict = new HashMap<String, Float>();

        for(String res:packraw)
        {
            if(res.split("-")[0].equals("HED"))
            {
                hedgehog=res.split("-")[1];
            }
            else
                {
                    try{

                        packdict.put(res.split("-")[0], Float.parseFloat(res.split("-")[1]));
                    }
                    catch(NumberFormatException e)
                    {
                        packdict.put(res.split("-")[0], (float) -12.0);
                    }
                }
        }

        return(packdict);
    }

    public  void setupSpinner(String devpack,String battpack)
    {
        Log.i("response", "onCreate: "+devpack);
        Log.i("response", "onCreate: "+battpack);

        RecyclerView beaconlist = (RecyclerView) findViewById(R.id.beaconview);

        devnstates = packToDict(devpack);
        devnbatt = packToDict(battpack);

        LinearLayoutManager beaconlay = new LinearLayoutManager(this);

        beaconlist.setLayoutManager(beaconlay);

        BeaconInfo beaconadap = new BeaconInfo(this,devnstates,devnbatt);

        beaconlist.setAdapter(beaconadap);

    }

    private void updateSpinner()
    {
        String devlist_res = basicSendCommand(getString(R.string.DEVLISTCOMMAND));
        String allbat_res = basicSendCommand("ALLBATT");

        if(devlist_res.trim().matches(getString(R.string.DEVLISTPATTERN)))
        {
            Log.i("info", "updateSpinner: Got valid packet");
            setupSpinner(devlist_res,allbat_res);
//            actiontoast.setText("Updated Device List");
//            actiontoast.show();
        }
        else
        {
            Log.i("info", "updateSpinner: got invalid packet");
        }


    }

    public void returnHome()
    {
        SearchDevices.home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        try {
//            client.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Toast.makeText(this,"Connection lost!!",Toast.LENGTH_SHORT).show();
        startActivity(SearchDevices.home);

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

    public  String getBatteryStatus()
    {
        String response=null;

        String[] responsels;

        response=basicSendCommand(getString(R.string.STATUSCOMMAND)+selectedbeacon);

        responsels=response.split(",");

        if(response==null)
        {
            returnHome();
        }
        else
        {
            try {
                response=responsels[3];
                Log.i("info", "getBatteryStatus: parsed data"+(Float.parseFloat(response)<3.0));
                if(Float.parseFloat(response)<3.0)
                {
                    batterystatus.setTextColor(getResources().getColor(R.color.failure));
                    return ("LOW");
                }
                else
                {
                    batterystatus.setTextColor(getResources().getColor(R.color.success));
                    return ("OK");
                }

            }
            catch (Exception e)
            {
                actiontoast.setText("Invalid response for Battery Status");
                actiontoast.show();
                return (getString(R.string.n_a));
            }

        }

        return (null);
    }

    public  void  onClickWake(View view)
    {

        String response =  basicSendCommand(getString(R.string.WAKECOMMAND)+selectedbeacon);

        if(response.matches("true|false"))
        {
            if (Boolean.parseBoolean(response)) {

                actiontoast.setText("Woke Up " + selectedbeacon);
                Log.i("info", "onClickWake: done waking up");

            } else {

                actiontoast.setText("Failed To Wake Up " + selectedbeacon);
                Log.i("info", "onClickWake: failed waking up");
            }
            actiontoast.show();
            updateSpinner();


        }
        else
        {
            actiontoast.setText("Invalid response for Wake");
            actiontoast.show();
        }
    }

    public  void  onClickSleep(View view)
    {

        String response =  basicSendCommand(getString(R.string.SLEEPCOMMAND)+selectedbeacon);
        if(response.matches("true|false")) {
            if (Boolean.parseBoolean(response)) {
                actiontoast.setText("Sleep successful for  " + selectedbeacon);

            } else {
                actiontoast.setText("Failed to put asleep " + selectedbeacon);

            }
            actiontoast.show();
            updateSpinner();


        }
        else
        {
            actiontoast.setText("Invalid response for Sleep");
            actiontoast.show();
        }

    }

    public  void  onClickReset(View view)
    {

        String response =  basicSendCommand(getString(R.string.RESETCOMMAND)+selectedbeacon);

        if(response.matches("true|false")) {
            if (Boolean.parseBoolean(response)) {
                actiontoast.setText("Sent reset for  " + selectedbeacon);

            } else {
                actiontoast.setText("Failed to send reset for " + selectedbeacon);

            }
            actiontoast.show();
            updateSpinner();


        }
        else
        {
            actiontoast.setText("Invalid response for Reset");
            actiontoast.show();
        }
    }
}