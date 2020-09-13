package com.baseit.beaconcontroller;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.zip.Inflater;

import static com.baseit.beaconcontroller.DeviceControl.hedgehog;
import static com.baseit.beaconcontroller.DeviceControl.selectedbeacon;

public class BeaconInfo extends RecyclerView.Adapter<BeaconInfo.BeaconViewHolder> {

    Map<String,Float> beaconnstates ;
    Map<String,Float> beaconnbattery ;
    String[] beaconadds ;
    Context context ;

    public BeaconInfo(Context context,Map<String,Float> beaconnstates,Map<String,Float> beaconnbattery)
    {
        this.context=context;
        this.beaconnstates=beaconnstates;
        this.beaconnbattery=beaconnbattery;

        beaconadds=new String[this.beaconnstates.size()];
        beaconnstates.keySet().toArray(beaconadds);
    }

    @NonNull
    @Override
    public BeaconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.beaconinforow,parent,false);
        BeaconViewHolder vh = new BeaconViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull BeaconViewHolder holder, final int position) {

        if(beaconnstates.get(beaconadds[position])>0)
        {
            holder.beaconstatus.setText("Alive");
            holder.beaconstatus.setTextColor(context.getResources().getColor(R.color.success));
        }
        else
        {
            holder.beaconstatus.setText("Dead");
            holder.beaconstatus.setTextColor(context.getResources().getColor(R.color.failure));
        }

        if(beaconnbattery.get(beaconadds[position])<0)
        {
            holder.beaconbattery.setText("n/a");
            holder.beaconbattery.setTextColor(context.getResources().getColor(R.color.failure));
        }
        else
        {
            if(beaconnbattery.get(beaconadds[position])>3.14)
            {
                holder.beaconbattery.setText(String.valueOf(beaconnbattery.get(beaconadds[position])));
                holder.beaconbattery.setTextColor(context.getResources().getColor(R.color.success));
            }
            else
            {
                holder.beaconbattery.setText(String.valueOf(beaconnbattery.get(beaconadds[position])));
                holder.beaconbattery.setTextColor(context.getResources().getColor(R.color.failure));
            }
        }

        if(beaconadds[position].equals(hedgehog))
        {
            holder.ishedgehog.setText("Hedghog");
            holder.ishedgehog.setTextColor(context.getResources().getColor(R.color.success));
        }

        holder.beaconaddress.setText(beaconadds[position]);

        holder.beaconinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedbeacon=beaconadds[position];
//                Toast.makeText(context,"selected beacon is "+selectedbeacon,Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {

        return beaconnstates.size();
    }

    public class BeaconViewHolder  extends  RecyclerView.ViewHolder{
        TextView beaconaddress;
        TextView beaconstatus;
        TextView beaconbattery;
        TextView ishedgehog;
        View beaconinfo;

        public BeaconViewHolder(@NonNull View itemView) {
            super(itemView);

            beaconinfo = itemView;
            beaconaddress= itemView.findViewById(R.id.beaconaddress);
            beaconstatus= itemView.findViewById(R.id.beaconstatus);
            beaconbattery= itemView.findViewById(R.id.batterystatus);
            ishedgehog = itemView.findViewById(R.id.hedgehog);
        }
    }
}
