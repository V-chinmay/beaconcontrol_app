package com.baseit.beaconcontroller;

import androidx.appcompat.app.AppCompatActivity;

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
    String hedgehog= "0";
    Map<String,Integer> devnstates = null;
    Toast actiontoast = null;
    TextView batterystatus =null;
    String selectedbeacon = null;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            client.close();
            Log.i("socket", "onDestroy: Closed correctly!!");
        } catch (IOException e) {

            e.printStackTrace();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);


        Intent searchdevices = getIntent();

        actiontoast = Toast.makeText(this,"dummy",Toast.LENGTH_SHORT);

        if(searchdevices.getStringExtra("response").trim().matches(getString(R.string.DEVLISTPATTERN)))
        {
            setupSpinner(searchdevices.getStringExtra("response").trim());
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


    public  void setupSpinner(String devpack)
    {
        spinner = (Spinner) findViewById(R.id.beaconslist);
        String[] responseraw = devpack.split(",");
        final String[] devlist = new String[responseraw.length-1];

        devnstates = new HashMap<String, Integer>();

        for(String res:responseraw)
        {
            if(res.split("-")[0].equals("HED"))
            {
                hedgehog=res.split("-")[1];
            }
            else {
                devnstates.put(res.split("-")[0], Integer.valueOf(res.split("-")[1]));
            }
        }

        devnstates.keySet().toArray(devlist);

        ArrayAdapter<String> ad = new ArrayAdapter<String>(this,R.layout.spinner_item,devlist);
        ad.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(ad);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                onSelectedBeacon(devlist[i]);
            }


            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void updateSpinner()
    {
        String response = basicSendCommand(getString(R.string.DEVLISTCOMMAND));

        if(response.trim().matches(getString(R.string.DEVLISTPATTERN)))
        {
            Log.i("info", "updateSpinner: Got valid packet");
            setupSpinner(response);
//            actiontoast.setText("Updated Device List");
//            actiontoast.show();
        }
        else
        {
            Log.i("info", "updateSpinner: got invalid packet");
        }


    }
    public  void onSelectedBeacon(String beacon)
    {
        TextView devicestate = (TextView) findViewById(R.id.beaconstate);
        TextView beaconaddress = (TextView) findViewById(R.id.hedgehog);
        batterystatus = (TextView) findViewById(R.id.battery);

        beaconaddress.setText(hedgehog);

        selectedbeacon = beacon;

        batterystatus.setText(getBatteryStatus());

        if(devnstates.get(beacon)>0)
        {
            devicestate.setText("Dead");
            devicestate.setTextColor(getResources().getColor(R.color.failure));

        }
        else
        {
            devicestate.setText("Alive");
            devicestate.setTextColor(getResources().getColor(R.color.success));
        }

    }

    public void returnHome()
    {
        SearchDevices.home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(SearchDevices.home);
        SearchDevices.toast.setText("Connection Lost!!");
        SearchDevices.toast.show();
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

            } else {
                actiontoast.setText("Failed To Wake Up " + selectedbeacon);

            }

            updateSpinner();
            actiontoast.show();

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

            updateSpinner();
            actiontoast.show();

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

            updateSpinner();
            actiontoast.show();

        }
        else
        {
            actiontoast.setText("Invalid response for Reset");
            actiontoast.show();
        }
    }
}