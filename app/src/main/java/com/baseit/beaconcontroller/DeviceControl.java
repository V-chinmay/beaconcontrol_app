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

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

public class DeviceControl extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);

        Spinner spinner = (Spinner) findViewById(R.id.beaconslist);
        Intent searchdevices = getIntent();
        String[] responseraw = searchdevices.getStringExtra("response").split(",");
        final String[] devlist = new String[responseraw.length];

        final Map<String,Integer> devnstates = new HashMap<String, Integer>();
        for(String res:responseraw)
        {
            devnstates.put(res.split("-")[0],Integer.valueOf(res.split("-")[1]));
            Log.i("states",String.valueOf(devnstates.get(res.split("-")[0])));

        }

        devnstates.keySet().toArray(devlist);
        ArrayAdapter<String> ad = new ArrayAdapter<String>(this,R.layout.spinner_item,devlist);
        ad.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(ad);
        final TextView devicestate = (TextView) findViewById(R.id.beaconstate);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if(devnstates.get(devlist[i])>0)
                 {
                     devicestate.setText("Alive");
                     Log.i("state","staet is "+String.valueOf(devnstates.get(devlist[i])));
//                     devicestate.setBackground(getResources().getDrawable(R.color.success));
                 }
                 else
                 {
                     devicestate.setText("Dead");
                     Log.i("state","staet is "+String.valueOf(devnstates.get(devlist[i])));
//                     devicestate.setBackground(getResources().getDrawable(R.color.failure));
                 }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}