package com.example.covidapp;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;

public class MyMarkerView extends MarkerView {

    private TextView dateContent, valueContent;

    public MyMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);

        dateContent = (TextView) findViewById(R.id.dateContent);
        valueContent = (TextView) findViewById(R.id.valueContent);
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        int digits = 0;
        String percentString = "";
        boolean [] data = (boolean[]) e.getData();
        if(data[0]) percentString = "%";
        if(data[1]) digits = 2;
            dateContent.setText(DataHolder.getChosenCountryList().get((int)e.getX())[3]);
            valueContent.setText( Utils.formatNumber(e.getY(), digits, true) + percentString );

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}