package net.thejuggernaut.simplewatchface;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import org.jraf.android.androidwearcolorpicker.ColorPickActivity;

public class MySettings extends Activity {

    private static final String TAG = "Settings thing";

    static final int COMPLICATION_CONFIG_REQUEST_CODE = 1001;
    static final int UPDATE_COLORS_CONFIG_REQUEST_CODE = 1002;

    private WearableRecyclerView mWearableRecyclerView;
    private RecycleView mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        setContentView(R.layout.mysettings);

        mAdapter = new RecycleView(
                getApplicationContext(),
                ConfigData.getWatchFaceServiceClass(),
                ConfigData.getDataToPopulateAdapter(this));

        mWearableRecyclerView =
                (WearableRecyclerView) findViewById(R.id.wearable_recycler_view);

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
        System.out.println("Result code.. "+resultCode);
        int pickedColor =  ColorPickActivity.Companion.getPickedColor(data);
        System.out.println("Value is "+pickedColor);

    }
}
