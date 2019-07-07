package net.thejuggernaut.simplewatchface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 * <p>
 * Important Note: Because watch face apps do not have a default Activity in
 * their project, you will need to set your Configurations to
 * "Do not launch Activity" for both the Wear and/or Application modules. If you
 * are unsure how to do this, please review the "Run Starter project" section
 * in the Google Watch Face Code Lab:
 * https://codelabs.developers.google.com/codelabs/watchface/index.html#0
 */
public class MyWatchFace extends CanvasWatchFaceService {
    SharedPreferences mSharedPref;
    public int batteryLevel = 0;
    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    /**
     * Update rate in milliseconds for interactive mode. Defaults to one second
     * because the watch face needs to update seconds in interactive mode.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {

        mSharedPref =
                this.getSharedPreferences(
                        this.getString(R.string.settings_key),
                        Context.MODE_PRIVATE);
        return new Engine();

    }



    private static class EngineHandler extends Handler {
        private final WeakReference<MyWatchFace.Engine> mWeakReference;

        public EngineHandler(MyWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            MyWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        String TAG = "HelloWatch ";

        private final Handler mUpdateTimeHandler = new EngineHandler(this);
        private Calendar mCalendar;
        private final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };
        private boolean mRegisteredTimeZoneReceiver = false;
        private float mXOffset;
        private float mYOffset;
        private Paint mBackgroundPaint;
        private Paint mTextPaint;
        private Paint dateTextPaint;
        private Paint batteryTextPaint;


        private Paint mTextPaintOff;
        private Paint otherTextPaintOff;


        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        private boolean mLowBitAmbient;
        private boolean mBurnInProtection;
        private boolean mAmbient;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);



            setWatchFaceStyle(new WatchFaceStyle.Builder(MyWatchFace.this)
                    .build());

            mCalendar = Calendar.getInstance();

            Resources resources = MyWatchFace.this.getResources();
            mYOffset = resources.getDimension(R.dimen.digital_y_offset);

            // Initializes background.
            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(
                    ContextCompat.getColor(getApplicationContext(), R.color.background));


            // Initializes Watch Face.
            mTextPaint = new Paint();
            mTextPaint.setTypeface(NORMAL_TYPEFACE);
            mTextPaint.setAntiAlias(true);
            //Log.d(TAG,"Getting stored colour.. Value is "+mSharedPref.getInt("timecolour",-1));
            mTextPaint.setColor(mSharedPref.getInt("timecolour",Color.WHITE));

            dateTextPaint = new Paint();
            dateTextPaint.setTypeface(NORMAL_TYPEFACE);
            dateTextPaint.setAntiAlias(true);
            dateTextPaint.setColor(mSharedPref.getInt("datecolour",Color.WHITE));

            batteryTextPaint = new Paint();
            batteryTextPaint.setTypeface(NORMAL_TYPEFACE);
            batteryTextPaint.setAntiAlias(true);
            batteryTextPaint.setColor(mSharedPref.getInt("batterycolour",Color.WHITE));


            mTextPaintOff = new Paint();
            mTextPaintOff.setTypeface(NORMAL_TYPEFACE);
            mTextPaintOff.setAntiAlias(true);
            mTextPaintOff.setColor(
                    ContextCompat.getColor(getApplicationContext(),  R.color.white));

            otherTextPaintOff = new Paint();
            otherTextPaintOff.setTypeface(NORMAL_TYPEFACE);
            otherTextPaintOff.setAntiAlias(true);
            otherTextPaintOff.setColor(
                    ContextCompat.getColor(getApplicationContext(),  R.color.white));

        }

        public void updateColours(){
            mTextPaint.setColor(mSharedPref.getInt("timecolour",Color.WHITE));
            dateTextPaint.setColor(mSharedPref.getInt("datecolour",Color.WHITE));
            batteryTextPaint.setColor(mSharedPref.getInt("batterycolour",Color.WHITE));
        }

        private final BroadcastReceiver batteryWork = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

            }
        };

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);

            super.onDestroy();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            } else {
                unregisterReceiver();

            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }



        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            MyWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);

            IntentFilter filter1 = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

            MyWatchFace.this.registerReceiver(batteryWork, filter1);

            IntentFilter updater = new IntentFilter("UPDATED");
            MyWatchFace.this.registerReceiver(updateIntent,updater);

        }

        private final BroadcastReceiver updateIntent = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Log.d(TAG,"Got update request?");
                updateColours();


            }
        };

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            MyWatchFace.this.unregisterReceiver(mTimeZoneReceiver);

            MyWatchFace.this.unregisterReceiver(batteryWork);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = MyWatchFace.this.getResources();
            boolean isRound = insets.isRound();
            mXOffset = resources.getDimension(isRound
                    ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
            float textSize = resources.getDimension(isRound
                    ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);

            mTextPaint.setTextSize(textSize);
            dateTextPaint.setTextSize(textSize/2);
            batteryTextPaint.setTextSize(textSize/2);
            mTextPaintOff.setTextSize(textSize);
            otherTextPaintOff.setTextSize(textSize/2);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            mAmbient = inAmbientMode;
            if (mLowBitAmbient) {
                mTextPaint.setAntiAlias(!inAmbientMode);
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // Draw the background.
            if (isInAmbientMode()) {
                canvas.drawColor(Color.BLACK);
            } else {
                canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
            }



            // Draw H:MM in ambient mode or H:MM:SS in interactive mode.
            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);
            int hour = mCalendar.get(Calendar.HOUR);
            int min = mCalendar.get(Calendar.MINUTE);
            int half = mCalendar.get(Calendar.AM_PM);
            NumberFormat f = new DecimalFormat("00");
            int month = mCalendar.get(Calendar.DAY_OF_MONTH);

            if (half == Calendar.PM && hour == 0) {
                hour = 12;
            }
            String halfText = (half == Calendar.AM) ? "AM" : "PM";
            String text = String.format("%d:%02d", hour, min);
            String date = String.format("%s-%02d-%02d", f.format(month),
                    mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.YEAR));
           /* String text = mAmbient
                    ? String.format("%d:%02d", mCalendar.get(Calendar.HOUR),
                    mCalendar.get(Calendar.MINUTE))
                    : String.format("%d:%02d", mCalendar.get(Calendar.HOUR),
                    mCalendar.get(Calendar.MINUTE));*/
            if(!mAmbient) {
                canvas.drawText(text, mXOffset, mYOffset, mTextPaint);

                canvas.drawText(halfText,mXOffset+mTextPaint.measureText(text),mYOffset,dateTextPaint);
                canvas.drawText(date, mXOffset + 30, mYOffset + 40, dateTextPaint);
                String battery = batteryLevel + "%";
                canvas.drawText(battery, mXOffset + 30, mYOffset + 80, batteryTextPaint);
            } else {
                canvas.drawText(text, mXOffset, mYOffset, mTextPaintOff);
                canvas.drawText(halfText,mXOffset+mTextPaint.measureText(text),mYOffset,otherTextPaintOff);
                canvas.drawText(date, mXOffset + 30, mYOffset + 40, otherTextPaintOff);
            }

        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }
    }
}
