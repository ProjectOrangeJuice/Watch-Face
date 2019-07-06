package net.thejuggernaut.simplewatchface;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jraf.android.androidwearcolorpicker.ColorPickActivity;

import java.util.ArrayList;
import java.util.Arrays;

public class RecycleView extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ComponentName mWatchFaceComponentName;
    private Context mContext;
    public static final int TYPE_COLOR_CONFIG = 1;
    private static final String TAG = "CompConfigAdapter";
    SharedPreferences mSharedPref;

    private ArrayList<ConfigData.ConfigItemType> mSettingsDataSet;

    public RecycleView(
            Context context,
            Class watchFaceServiceClass,
            ArrayList<ConfigData.ConfigItemType> settingsDataSet) {

        mSettingsDataSet = settingsDataSet;


        mContext = context;

        mWatchFaceComponentName = new ComponentName(mContext, watchFaceServiceClass);
        mSharedPref =
                context.getSharedPreferences(
                        context.getString(R.string.settings_key),
                        Context.MODE_PRIVATE);
        Log.d(TAG,"Finished constructor");
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder(): viewType: " + viewType);

        RecyclerView.ViewHolder viewHolder = null;

        switch (viewType) {
            case TYPE_COLOR_CONFIG:
                viewHolder =
                        new ColorPickerViewHolder(
                                LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.config_list_color_item, parent, false));
                break;

        }

        Log.d(TAG,"Type is.. "+viewType+" I'm giving .. "+viewHolder);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "Element " + position + " set.");

        ConfigData.ConfigItemType configItemType = mSettingsDataSet.get(position);

        switch (holder.getItemViewType()) {


            case TYPE_COLOR_CONFIG:
                ColorPickerViewHolder colorPickerViewHolder = (ColorPickerViewHolder) holder;
               ConfigData.ColorConfigItem colorConfigItem = (ConfigData.ColorConfigItem) configItemType;

                int iconResourceId = colorConfigItem.getIconResourceId();
                String name = colorConfigItem.getName();
                String sharedPrefString = colorConfigItem.getSharedPrefString();

                colorPickerViewHolder.setIcon(iconResourceId);
                colorPickerViewHolder.setName(name);
                colorPickerViewHolder.setSharedPrefString(sharedPrefString);

                break;


        }
    }

    @Override
    public int getItemCount() {
        return mSettingsDataSet.size();
    }

    @Override
    public int getItemViewType(int position) {
        ConfigData.ConfigItemType configItemType = mSettingsDataSet.get(position);
        return configItemType.getConfigType();
    }


    /**
     * Displays color options for the an item on the watch face. These could include marker color,
     * background color, etc.
     */
    public class ColorPickerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Button mAppearanceButton;

        private String mSharedPrefResourceString;


        public ColorPickerViewHolder(View view) {
            super(view);

            mAppearanceButton = view.findViewById(R.id.color_picker_button);
            view.setOnClickListener(this);
        }

        public void setName(String name) {
            mAppearanceButton.setText(name);
        }

        public void setIcon(int resourceId) {
            Context context = mAppearanceButton.getContext();
            mAppearanceButton.setCompoundDrawablesWithIntrinsicBounds(
                    context.getDrawable(resourceId), null, null, null);
        }

        public void setSharedPrefString(String sharedPrefString) {
            mSharedPrefResourceString = sharedPrefString;
        }


            @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Log.d(TAG, "Complication onClick() position: " + position);

            ArrayList<Integer> myColours = new ArrayList<>();
            int r = 0;
            int g = 0;
            int b = 0;
            for(int i = 0; i < 4; i++){
                myColours.add(Color.rgb(r,0,0));
                myColours.add(Color.rgb(0,g,0));
                myColours.add(Color.rgb(0,0,b));

                myColours.add(Color.rgb(r,64,0));
                myColours.add(Color.rgb(r,128,0));
                myColours.add(Color.rgb(r,192,0));
                myColours.add(Color.rgb(r,255,0));


                myColours.add(Color.rgb(r,0,64));
                myColours.add(Color.rgb(r,0,128));
                myColours.add(Color.rgb(r,0,192));
                myColours.add(Color.rgb(r,0,255));

                myColours.add(Color.rgb(r,64,64));
                myColours.add(Color.rgb(r,128,128));
                myColours.add(Color.rgb(r,192,192));
                myColours.add(Color.rgb(r,255,255));



                r += 64;
                g += 64;
                b += 64;

            }


            Arrays.sort(myColours.toArray());




          Log.d(TAG,"shared pref is.."+mSharedPrefResourceString);
            Intent intent = new ColorPickActivity.IntentBuilder()
                    .oldColor(Color.WHITE)
                    .colors(myColours)
                    .build(mContext);

            Activity activity = (Activity) view.getContext();

            switch (mSharedPrefResourceString){
                case "TIME":
                    activity.startActivityForResult(
                            intent, MySettings.TIME_COLOURS);
                    break;
                case "DATE":
                    activity.startActivityForResult(
                            intent, MySettings.DATE_COLOURS);
                    break;
                case "BATTERY":
                    activity.startActivityForResult(
                            intent, MySettings.BATTERY_COLOURS);
                    break;
                }



        }
    }



}
