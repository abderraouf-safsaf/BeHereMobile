package com.example.teamloosers.behereandroid.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.wooplr.spotlight.SpotlightConfig;
import com.wooplr.spotlight.SpotlightView;
import com.wooplr.spotlight.prefs.PreferencesManager;
import com.wooplr.spotlight.utils.SpotlightListener;

import java.util.LinkedList;
import java.util.Queue;

public class SpotlightSequence {

    private Activity activity;
    private SpotlightConfig config;
    private Queue<SpotlightView.Builder> queue;

    private static SpotlightSequence instance;
    private final String TAG = "Tour Sequence";

    public SpotlightSequence(Activity activity){
        this.activity = activity;
        setConfig();
        queue = new LinkedList<>();
    }

    public static SpotlightSequence getInstance(Activity activity, SpotlightConfig config){
        if(instance == null){
            instance = new SpotlightSequence(activity);
        }
        return instance;
    }

    public SpotlightSequence addSpotlight(View target, String subtitle, String usageId){
        Log.d(TAG, "Adding " + usageId);
        SpotlightView.Builder builder = new SpotlightView.Builder(activity)
                .setConfiguration(config)
                .headingTvText("Tip")
                .usageId(usageId)
                .subHeadingTvText(subtitle)
                .target(target)
                .setListener(new SpotlightListener() {
                    @Override
                    public void onUserClicked(String s) {
                        playNext();
                    }
                })
                .enableDismissAfterShown(true);
        queue.add(builder);
        return instance;
    }

    public SpotlightSequence addSpotlight(@NonNull View target, int titleResId, int subTitleResId, String usageId){
        String title = activity.getString(titleResId);
        String subtitle = activity.getString(subTitleResId);
        SpotlightView.Builder builder = new SpotlightView.Builder(activity)
                .setConfiguration(config)
                .headingTvText(title)
                .usageId(usageId)
                .subHeadingTvText(subtitle)
                .target(target)
                .setListener(new SpotlightListener() {
                    @Override
                    public void onUserClicked(String s) {
                        playNext();
                    }
                })
                .enableDismissAfterShown(true);
        queue.add(builder);
        return instance;
    }

    public void startSequence(){
        if(!queue.isEmpty()) {

            queue.poll().show();

        }
    }

    private void resetTour() {
        instance = null;
        queue.clear();
        this.activity = null;
        config = null;
    }

    private void playNext(){
        SpotlightView.Builder next = queue.poll();
        if(next != null){
            next.show().setReady(true);
        }else {
            resetTour();
        }
    }

    public static void resetSpotlights(@NonNull Context context){
        new PreferencesManager(context).resetAll();
    }

    private void setConfig() {

        SpotlightConfig config;

        config = new SpotlightConfig();
        config.setLineAndArcColor(Color.parseColor("#eb273f"));
        config.setDismissOnTouch(true);
        config.setMaskColor(Color.argb(240,0,0,0));
        config.setHeadingTvColor(Color.parseColor("#eb273f"));
        config.setHeadingTvSize(32);
        config.setSubHeadingTvSize(16);
        config.setSubHeadingTvColor(Color.parseColor("#ffffff"));
        config.setPerformClick(false);
        config.setRevealAnimationEnabled(true);
        config.setLineAnimationDuration(200);

        this.config = config;
    }
}