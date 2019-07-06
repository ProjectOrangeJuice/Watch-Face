package net.thejuggernaut.simplewatchface;

import android.content.Context;

import java.util.ArrayList;

public class ConfigData {


    public interface ConfigItemType {
        int getConfigType();
    }


    /**
     * Returns Watch Face Service class associated with configuration Activity.
     */
    public static Class getWatchFaceServiceClass() {
        return MyWatchFace.class;
    }

    /**

     */
    public static ArrayList<ConfigItemType> getDataToPopulateAdapter(Context context) {

        ArrayList<ConfigItemType> settingsConfigData = new ArrayList<>();


        // Data for highlight/marker (second hand) color UX in settings Activity.
        ConfigItemType markerColorConfigItem =
                new ColorConfigItem(
                        context.getString(R.string.config_marker_color_label),
                        R.drawable.icn_styles,
                        context.getString(R.string.saved_marker_color)
                        );
        settingsConfigData.add(markerColorConfigItem);

        // Data for Background color UX in settings Activity.
        ConfigItemType backgroundColorConfigItem =
                new ColorConfigItem(
                        context.getString(R.string.config_background_color_label),
                        R.drawable.icn_styles,
                        context.getString(R.string.saved_background_color)
                        );
        settingsConfigData.add(backgroundColorConfigItem);


        return settingsConfigData;
    }



    /**
     * Data for color picker item in RecyclerView.
     */
    public static class ColorConfigItem  implements ConfigItemType {

        private String name;
        private int iconResourceId;
        private String sharedPrefString;

        ColorConfigItem(
                String name,
                int iconResourceId,
                String sharedPrefString
        ) {
            this.name = name;
            this.iconResourceId = iconResourceId;
            this.sharedPrefString = sharedPrefString;

        }

        public String getName() {
            return name;
        }

        public int getIconResourceId() {
            return iconResourceId;
        }

        public String getSharedPrefString() {
            return sharedPrefString;
        }



        @Override
        public int getConfigType() {
            return RecycleView.TYPE_COLOR_CONFIG;
        }
    }

}
