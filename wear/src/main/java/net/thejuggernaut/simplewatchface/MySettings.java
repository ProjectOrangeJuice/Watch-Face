package net.thejuggernaut.simplewatchface;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import org.jraf.android.androidwearcolorpicker.ColorPickActivity;

public class MySettings extends Activity {

    private static final String TAG = "Settings thing";


    static final int TIME_COLOURS = 205;
    static final int DATE_COLOURS = 206;
    static final int BATTERY_COLOURS = 207;

    private WearableRecyclerView mWearableRecyclerView;
    private RecycleView mAdapter;
    SharedPreferences mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mSharedPref =
                this.getSharedPreferences(
                        this.getString(R.string.settings_key),
                        Context.MODE_PRIVATE);

        setContentView(R.layout.mysettings);

        mAdapter = new RecycleView(
                getApplicationContext(),
                ConfigData.getWatchFaceServiceClass(),
                ConfigData.getDataToPopulateAdapter(this));

        mWearableRecyclerView =
                findViewById(R.id.wearable_recycler_view);

        // Aligns the first and last items on the list vertically centered on the screen.
        mWearableRecyclerView.setEdgeItemsCenteringEnabled(true);

        mWearableRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Improves performance because we know changes in content do not change the layout size of
        // the RecyclerView.
        mWearableRecyclerView.setHasFixedSize(true);

        mWearableRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int pickedColor;
        Intent updated = new Intent("UPDATED");
        //Log.d(TAG,data.toString());
        if (data != null) {
            switch (requestCode) {
                case TIME_COLOURS:
                    pickedColor = ColorPickActivity.Companion.getPickedColor(data);
                    mSharedPref.edit().putInt("timecolour", pickedColor).commit();
                    sendBroadcast(updated);
                    break;

                case DATE_COLOURS:
                    pickedColor = ColorPickActivity.Companion.getPickedColor(data);
                    mSharedPref.edit().putInt("datecolour", pickedColor).commit();
                    sendBroadcast(updated);
                    break;

                case BATTERY_COLOURS:
                    pickedColor = ColorPickActivity.Companion.getPickedColor(data);
                    mSharedPref.edit().putInt("batterycolour", pickedColor).commit();
                    sendBroadcast(updated);
                    break;
            }

        }



    }
}
