package com.intellicontrol.bpalvarado.robotcontroller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by bpalvarado on 02/06/2015.
 */
public class GesturesAdapter extends ArrayAdapter<Gesture> {
    private Context context;
    private ArrayList<Gesture> gestures;
    public GesturesAdapter(Context context, ArrayList<Gesture> gestures){
        super(context, 0, gestures);
        this.context = context;
        this.gestures = gestures;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //Gesture gest = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        View rowView = inflater.inflate(R.layout.simple_row, parent, false);

        // Lookup view for data population
        TextView tvName = (TextView) rowView.findViewById(R.id.firstLine);
        TextView tvSecond = (TextView) rowView.findViewById(R.id.secondLine);
        ImageView tvImage = (ImageView) rowView.findViewById(R.id.icon);
        // Populate the data into the template view using the data object
        tvName.setText(gestures.get(position).expressionName);
        tvSecond.setText("Id: " + Integer.toString(gestures.get(position).expressionId));
        tvImage.setImageResource(R.drawable.drawer_shadow);
        // Return the completed view to render on screen
        return rowView;
    }

    @Override
    public int getCount() {
        return gestures.size();
    }
}
