package com.example.solar_decathlon_house_numerical;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ItemArrayAdapter extends ArrayAdapter<String[]> {

    private List<String[]> sensorData = new ArrayList<String[]>();

    static class ItemViewHolder{
        TextView timeStamp;
        TextView sensor1;
    }

    public ItemArrayAdapter(Context context, int resource){
        super(context, resource);
    }

    @Override
    public void add(String[] object){
        sensorData.add(object);
        super.add(object);
    }

    @Override
    public int getCount(){
        return this.sensorData.size();
    }

    @Override
    public String[] getItem(int position){
        return this.sensorData.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View row = convertView;
        ItemViewHolder viewHolder;
        if(row == null){
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.single_list_item, parent, false);
            viewHolder = new ItemViewHolder();
            viewHolder.timeStamp = (TextView) row.findViewById(R.id.timeStamp);
            viewHolder.sensor1 = (TextView) row.findViewById(R.id.sensor1);
        } else{
            viewHolder = (ItemViewHolder) row.getTag();
        }

        String[] stat = getItem(position);
        viewHolder.timeStamp.setText(stat[0]);
        viewHolder.sensor1.setText(stat[1]);
        return row;
    }
}
