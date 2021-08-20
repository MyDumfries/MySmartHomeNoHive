package com.mydumfries.mysmarthomenohive;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.widget.RemoteViews;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Implementation of App Widget functionality.
 */
public class SmartHomeWidget extends AppWidgetProvider {
    public static String ACTION_WIDGET_TEMPDOWN = "ACTION_WIDGET_TEMPDOWN";
    public static String ACTION_WIDGET_TEMPUP = "ACTION_WIDGET_TEMPUP";
    public static String ACTION_WIDGET_LOGCABIN = "ACTION_WIDGET_LOGCABIN";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        int appWidgetId = appWidgetIds[0];
            RemoteViews remoteView = new RemoteViews(
                    context.getPackageName(), R.layout.smart_home_widget);

        //Refresh Button
        Intent intentUpdate = new Intent(context, SmartHomeWidget.class);
        intentUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] idArray = new int[]{appWidgetId};
        intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray);
        PendingIntent pendingUpdate = PendingIntent.getBroadcast(
                context, appWidgetId, intentUpdate,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
            remoteView.setOnClickPendingIntent(R.id.refresh_button, pendingUpdate);

            //Temp Down Button
            Intent intentUpdate2 = new Intent(context, SmartHomeWidget.class);
            intentUpdate2.setAction(ACTION_WIDGET_TEMPDOWN);
            intentUpdate2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray);
            PendingIntent pendingUpdate2 = PendingIntent.getBroadcast(context, appWidgetId, intentUpdate2, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteView.setOnClickPendingIntent(R.id.tempdown_button, pendingUpdate2);

            //Temp Up Button
            Intent intentUpdate3 = new Intent(context, SmartHomeWidget.class);
            intentUpdate3.setAction(ACTION_WIDGET_TEMPUP);
            intentUpdate3.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray);
            PendingIntent pendingUpdate3 = PendingIntent.getBroadcast(context, 0, intentUpdate3, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteView.setOnClickPendingIntent(R.id.tempup_button, pendingUpdate3);

            //Log Cabin Button
            Intent intentUpdate4 = new Intent(context, SmartHomeWidget.class);
            intentUpdate4.setAction(ACTION_WIDGET_LOGCABIN);
            intentUpdate4.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray);
            PendingIntent pendingUpdate4 = PendingIntent.getBroadcast(context, appWidgetId, intentUpdate4, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteView.setOnClickPendingIntent(R.id.logcabin_button, pendingUpdate4);

            appWidgetManager.updateAppWidget(appWidgetId, remoteView);
        }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action=intent.getAction();
        if (!"android.appwidget.action.APPWIDGET_ENABLED".equals(action) && !"android.appwidget.action.APPWIDGET_DELETED".equals(action) && !"android.appwidget.action.APPWIDGET_DISABLED".equals(action)) {
            UpDateWidget(context, intent);
        }
        Intent intentUpdate = new Intent(context, SmartHomeWidget.class);
        intentUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intentUpdate);

        } else {
            context.startService(intentUpdate);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Intent serviceIntent = new Intent(context, WidgetUpdateService.class);
        context.stopService(serviceIntent);
        super.onDeleted(context, appWidgetIds);
    }

    private static String getTuyaInfo(Context context) throws IOException {
        String result = "";
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String tuyauser = pref.getString("TuyaUser", "");
        String tuyapassword = pref.getString("TuyaPassword", "");
        String tuyasession = pref.getString("TuyasessionID", "");
        String tuyaDeviceId = pref.getString("TuyadeviceID", "");
        String tuyaURL = pref.getString("TuyaappServerUrl", "");
        tuyaURL = "https://px1.tuyaeu.com/homeassistant/auth.do";
//        getKasaDeviceID(kasasession, context);
        String data = "{\"userName\": \"stuartmclaren268@gmail.com\",\"password\": \"QOSfan01\",\"countryCode\": \"eu\",\"bizType\": \"smart_life\",\"from\": \"tuya\", }";
        HttpURLConnection con = (HttpURLConnection) new URL(tuyaURL).openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        out.writeBytes(data);
        out.flush();
        out.close();
        BufferedReader in3 = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder response2 = new StringBuilder();
        String output2;
        while ((output2 = in3.readLine()) != null) {
            response2.append(output2);
        }
        in3.close();
        int tokenlength = response2.indexOf("relay_state");
        if (tokenlength < 0) {
            return "OFFLINE";
        } else {
            String KasaState = response2.substring(tokenlength);
            tokenlength = KasaState.indexOf(":");
            KasaState = KasaState.substring(tokenlength + 1);
            tokenlength = KasaState.indexOf(",");
            KasaState = KasaState.substring(0, tokenlength);
            if (KasaState.equals("0")) {
                result = "OFF";
            }
            if (KasaState.equals("1")) {
                result = "ON";
            }
            return result;
        }
    }

    private static String getKasaInfo(Context context) throws IOException {
        String result = "";
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String kasasession = pref.getString("KasasessionID", "");
        String KasaDeviceId = pref.getString("kasadeviceID", "");
        String KasaURL = pref.getString("kasaappServerUrl", "");
        KasaURL = KasaURL + "/?token=" + kasasession;
//        getKasaDeviceID(kasasession, context);
        String data = "{\"method\":\"passthrough\", \"params\": {\"deviceId\": \"" + KasaDeviceId + "\", \"requestData\": \"{\\\"system\\\":{\\\"get_sysinfo\\\":null},\\\"emeter\\\":{\\\"get_realtime\\\":null}}\" }}";
        HttpURLConnection con = (HttpURLConnection) new URL(KasaURL).openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        out.writeBytes(data);
        out.flush();
        out.close();
        BufferedReader in3 = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder response2 = new StringBuilder();
        String output2;
        while ((output2 = in3.readLine()) != null) {
            response2.append(output2);
        }
        in3.close();
        int tokenlength = response2.indexOf("relay_state");
        if (tokenlength < 0) {
            return "OFFLINE";
        } else {
            String KasaState = response2.substring(tokenlength);
            tokenlength = KasaState.indexOf(":");
            KasaState = KasaState.substring(tokenlength + 1);
            tokenlength = KasaState.indexOf(",");
            KasaState = KasaState.substring(0, tokenlength);
            if (KasaState.equals("0")) {
                result = "OFF";
            }
            if (KasaState.equals("1")) {
                result = "ON";
            }
            return result;
        }
    }

    public static String toggleKasa(String state, Context context) throws IOException {
        String result = null;
        if (state.equals("OFF")) {
            state = "1";
        } else {
            state = "0";
        }
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String kasasession = pref.getString("KasasessionID", "");
        String KasaDeviceId = pref.getString("kasadeviceID", "");
        String KasaURL = pref.getString("kasaappServerUrl", "");
        KasaURL = KasaURL + "/?token=" + kasasession;
        String data = "{\"method\":\"passthrough\", \"params\": {\"deviceId\": \"" + KasaDeviceId + "\", \"requestData\": \"{\\\"system\\\":{\\\"set_relay_state\\\":{\\\"state\\\":" + state + "}}}\" }}";
        HttpURLConnection con = (HttpURLConnection) new URL(KasaURL).openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        out.writeBytes(data);
        out.flush();
        out.close();
        if (state.equals("0")) {
            result = "OFF";
        }
        if (state.equals("1")) {
            result = "ON";
        }
        return result;
    }

    private static String getTadoInfo(Context context) throws IOException {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String TadoSessionID = pref.getString("TadosessionID", "");
        String TadoURL = "https://my.tado.com/api/v1/me";
        HttpURLConnection con = (HttpURLConnection) new URL(TadoURL).openConnection();
        con.setRequestProperty("Authorization", "Bearer " + TadoSessionID);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestMethod("GET");
        BufferedReader in2 = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String output;
        StringBuffer response = new StringBuffer();
        while ((output = in2.readLine()) != null) {
            response.append(output);
        }
        in2.close();
        int tokenlength = response.indexOf("homeId");
        String homeId = response.substring(tokenlength);
        tokenlength = homeId.indexOf(":");
        homeId = homeId.substring(tokenlength + 1);
        tokenlength = homeId.indexOf(",");
        homeId = homeId.substring(0, tokenlength);
        String homeURL = "https://my.tado.com/api/v2/homes/" + homeId + "/zones/1/state";
        con = (HttpURLConnection) new URL(homeURL).openConnection();
        con.setRequestProperty("Authorization", "Bearer " + TadoSessionID);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestMethod("GET");
        BufferedReader in3 = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String output2;
        StringBuffer response2 = new StringBuffer();
        while ((output2 = in3.readLine()) != null) {
            response2.append(output2);
        }
        in3.close();
        tokenlength = response2.indexOf("insideTemperature");
        String ActualTemp = response2.substring(tokenlength);
        ActualTemp = ActualTemp.substring(30);
        tokenlength = ActualTemp.indexOf(",");
        ActualTemp = ActualTemp.substring(0, tokenlength);
        tokenlength = response2.indexOf("temperature");
        String TargetTemp = response2.substring(tokenlength);
        TargetTemp = TargetTemp.substring(24);
        tokenlength = TargetTemp.indexOf(",");
        TargetTemp = TargetTemp.substring(0, tokenlength);
        return ActualTemp + ":" + TargetTemp;
    }

    private static String putTadoInfo(String target, Context context) throws IOException {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String TadoSessionID = pref.getString("TadosessionID", "");
        String TadoURL = "https://my.tado.com/api/v1/me";
        HttpURLConnection con = (HttpURLConnection) new URL(TadoURL).openConnection();
        con.setRequestProperty("Authorization", "Bearer " + TadoSessionID);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestMethod("GET");
        BufferedReader in2 = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String output;
        StringBuffer response = new StringBuffer();
        while ((output = in2.readLine()) != null) {
            response.append(output);
        }
        in2.close();
        int tokenlength = response.indexOf("homeId");
        String homeId = response.substring(tokenlength);
        tokenlength = homeId.indexOf(":");
        homeId = homeId.substring(tokenlength + 1);
        tokenlength = homeId.indexOf(",");
        homeId = homeId.substring(0, tokenlength);
        double far = (Double.parseDouble(target) * 9 / 5) + 32;
        String data = "{\"setting\":{\"type\":\"HEATING\",\"power\":\"ON\",\"temperature\":{\"celsius\":" + target + ",\"fahrenheit\":" + far + "}},\"termination\":{\"type\":\"TADO_MODE\"}}";
        String homeURL = "https://my.tado.com/api/v2/homes/" + homeId + "/zones/1/overlay/";
        URL myHomeURL = new URL(homeURL);
        HttpURLConnection urlConnection = (HttpURLConnection) myHomeURL.openConnection();
        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod("PUT");
        urlConnection.setRequestProperty("Authorization", "Bearer " + TadoSessionID);
        urlConnection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());
        out.writeBytes(data);
        out.flush();
        out.close();
        BufferedReader in3 = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String output2;
        StringBuffer response2 = new StringBuffer();
        while ((output2 = in3.readLine()) != null) {
            response2.append(output2);
        }
        in3.close();
        return "";
    }

    public static class WidgetUpdateService extends JobIntentService {
        public static final int JOB_ID = 1;

        public static void enqueueWork(Context context, Intent work) {
            enqueueWork(context, WidgetUpdateService.class, JOB_ID, work);
        }
        Thread widgetUpdateThread = null;

        @Override
        protected void onHandleWork(@NonNull Intent intent) {
            widgetUpdateThread = new Thread() {
                public void run() {
                    Context context = WidgetUpdateService.this;
                    // prep the RemoteView
                    String timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date());
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                    long kasasessiontime = pref.getLong("Kasasessionstart", 0);
                    long tadosessiontime = pref.getLong("Tadosessionstart", 0);
                    long HOURINMILLISECONDS = 60 * 60 * 1000;
                    long TADOSESSIONLENGTH = 600 * 1000;   //600 seconds
                    long KASASESSIONLENGTH = 24 * HOURINMILLISECONDS;
//                    KASASESSIONLENGTH=24;
                    long HIVESESSIONLENGTH = 24 * HOURINMILLISECONDS;
                    long now = System.currentTimeMillis();
                    String result = null;
                    RemoteViews remoteView = null;
                    ComponentName smarthomeWidget = null;
                    AppWidgetManager appWidgetManager = null;
                    remoteView = new RemoteViews(
                            context.getPackageName(), R.layout.smart_home_widget);
                    remoteView.setTextViewText(R.id.Timetext, "REFRESHING");
                    remoteView.setInt(R.id.Timetext, "setTextColor",
                            android.graphics.Color.RED);
                    smarthomeWidget = new ComponentName(context,
                            SmartHomeWidget.class);
                    appWidgetManager = AppWidgetManager
                            .getInstance(context);
                    appWidgetManager.updateAppWidget(smarthomeWidget,
                            remoteView);
                    if (now - kasasessiontime > KASASESSIONLENGTH) {
                        try {
                            getKasaSessionID(context);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (now - tadosessiontime > TADOSESSIONLENGTH) {
                        try {
                            getTadoSessionID(context);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (ACTION_WIDGET_LOGCABIN.equals(intent.getAction())) {
                        String logcabinstatus = pref.getString("logcabin", "");
                        if (!logcabinstatus.equalsIgnoreCase("Offline")) {
                            try {
                                result = toggleKasa(logcabinstatus, context);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            remoteView = new RemoteViews(
                                    context.getPackageName(), R.layout.smart_home_widget);
                            remoteView.setTextViewText(R.id.logcabin_button, result);
                            if ("OFF".equals(result)) {
                                remoteView.setInt(R.id.logcabin_button, "setBackgroundResource", R.drawable.roundedbuttonoff);
                            }
                            if ("ON".equals(result)) {
                                remoteView.setInt(R.id.logcabin_button, "setBackgroundResource", R.drawable.roundedbuttonon);
                            }
                            editor.putString("logcabin", result).commit();
                            smarthomeWidget = new ComponentName(context,
                                    SmartHomeWidget.class);
                            appWidgetManager = AppWidgetManager
                                    .getInstance(context);
                            appWidgetManager.updateAppWidget(smarthomeWidget,
                                    remoteView);
                        }
                    }

                    String target = null;
                    if (ACTION_WIDGET_TEMPDOWN.equals(intent.getAction())) {
                        if (pref.contains("targettemp")) {
                            target = pref.getString("targettemp", "");
                            double targettemp = Double.parseDouble(target);
                            targettemp = targettemp - 0.5;
                            target = targettemp + "";
                            editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                            editor.putString("targettemp", target);
                            editor.commit();
                            try {
                                putTadoInfo(target, context);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            remoteView = new RemoteViews(
                                    context.getPackageName(), R.layout.smart_home_widget);
                            target=target.substring(0,4);
                            remoteView.setTextViewText(R.id.targettemperature, target + "°C");
                            smarthomeWidget = new ComponentName(context,
                                    SmartHomeWidget.class);
                            appWidgetManager = AppWidgetManager
                                    .getInstance(context);
                            appWidgetManager.updateAppWidget(smarthomeWidget,
                                    remoteView);
                        }
                    }

                    if (ACTION_WIDGET_TEMPUP.equals(intent.getAction())) {
                        if (pref.contains("targettemp")) {
                            target = pref.getString("targettemp", "");
                            double targettemp = Double.parseDouble(target);
                            targettemp = targettemp + 0.5;
                            target = targettemp + "";
                            editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                            editor.putString("targettemp", target);
                            editor.commit();
                            try {
                                putTadoInfo(target, context);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            remoteView = new RemoteViews(
                                    context.getPackageName(), R.layout.smart_home_widget);
                            target=target.substring(0,4);
                            remoteView.setTextViewText(R.id.targettemperature, target + "°C");
                            smarthomeWidget = new ComponentName(context,
                                    SmartHomeWidget.class);
                            appWidgetManager = AppWidgetManager
                                    .getInstance(context);
                            appWidgetManager.updateAppWidget(smarthomeWidget,
                                    remoteView);
                        }
                    }

                    String logcabin = null;
                    try {
                        logcabin = getKasaInfo(context);
                    } catch (IOException e) {
                        e.printStackTrace();
                        try {
                            getKasaSessionID(context);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        try {
                            logcabin = getKasaInfo(context);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    remoteView = new RemoteViews(
                            context.getPackageName(), R.layout.smart_home_widget);
                    remoteView.setTextViewText(R.id.logcabin_button, logcabin);
                    if ("OFF".equals(logcabin)) {
                        remoteView.setInt(R.id.logcabin_button, "setBackgroundResource", R.drawable.roundedbuttonoff);
                    }
                    if ("ON".equals(logcabin)) {
                        remoteView.setInt(R.id.logcabin_button, "setBackgroundResource", R.drawable.roundedbuttonon);
                    }
                    if ("OFFLINE".equalsIgnoreCase(logcabin)) {
                        remoteView.setInt(R.id.logcabin_button, "setBackgroundResource", R.drawable.roundedbutton);
                    }
                    editor.putString("logcabin", logcabin).commit();
                    smarthomeWidget = new ComponentName(context,
                            SmartHomeWidget.class);
                    appWidgetManager = AppWidgetManager
                            .getInstance(context);
                    appWidgetManager.updateAppWidget(smarthomeWidget,
                            remoteView);

                    try {
                        result = getTadoInfo(context);
                    } catch (IOException e) {
                        e.printStackTrace();
                        try {
                            getTadoSessionID(context);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        try {
                            result = getTadoInfo(context);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    String actual = result.substring(0, 5);
                    target = result.substring(6, 11);
                    editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                    editor.putString("targettemp", target);
                    editor.commit();
                    // prep the RemoteView
                    remoteView = new RemoteViews(
                            context.getPackageName(), R.layout.smart_home_widget);
                    remoteView.setTextViewText(R.id.actualtemperature, actual + "°C");
                    target=target.substring(0,4);
                    remoteView.setTextViewText(R.id.targettemperature, target + "°C");
                    remoteView.setTextViewText(R.id.Timetext, timeString);
                    remoteView.setInt(R.id.Timetext, "setTextColor",
                            R.color.purple);
                    smarthomeWidget = new ComponentName(context,
                            SmartHomeWidget.class);
                    appWidgetManager = AppWidgetManager
                            .getInstance(context);
                    appWidgetManager.updateAppWidget(smarthomeWidget,
                            remoteView);
                }


                /**
                 * Download data for displaying in the Widget
                 *
                 * @param widgetData
                 */
            };

            // start the background thread
            widgetUpdateThread.start();
        }

        @Override
        public void onDestroy() {
            widgetUpdateThread.interrupt();
            super.onDestroy();
        }

        @Override
        public IBinder onBind(Intent intent) {
            // no binding; can't from an App Widget
            return null;
        }
    }

    //NOT NEEDED JUST NOW BUT LEAVE IN IN CASE NEW DEVICE ADDED
    static String getKasaDeviceID(String KasaId, Context context) throws IOException {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String result = null;

        String KasaURL = "https://wap.tplinkcloud.com?token=" + KasaId;
        HttpURLConnection con = (HttpURLConnection) new URL(KasaURL).openConnection();
        con.setRequestMethod("POST");
        String data = "{\"method\":\"getDeviceList\"}";
        con.setRequestProperty("Content-Type", "application/json");
        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        out.writeBytes(data);
        out.flush();
        out.close();
        BufferedReader in3 = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuffer response2 = new StringBuffer();
        String output2;
        while ((output2 = in3.readLine()) != null) {
            response2.append(output2);
        }
        in3.close();
        int tokenlength = response2.indexOf("deviceId");
        String KasaDeviceId = response2.substring(tokenlength);
        tokenlength = KasaDeviceId.indexOf(":");
        KasaDeviceId = KasaDeviceId.substring(tokenlength + 2);
        tokenlength = KasaDeviceId.indexOf(",");
        KasaDeviceId = KasaDeviceId.substring(0, tokenlength - 1);
        editor.putString("kasadeviceID", KasaDeviceId).commit();

        tokenlength = response2.indexOf("appServerUrl");
        KasaURL = response2.substring(tokenlength);
        tokenlength = KasaURL.indexOf(":");
        KasaURL = KasaURL.substring(tokenlength + 2);
        tokenlength = KasaURL.indexOf(",");
        KasaURL = KasaURL.substring(0, tokenlength - 1);
        editor.putString("kasaappServerUrl", KasaURL).commit();
        return result;
    }

    static void getKasaSessionID(Context context) throws IOException {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
//        Map<String, ?> all=pref.getAll();
//        editor.putString("kasaUUID","d5119e7e-5a5a-4628-b49c-40387acc0e33").commit();
//        editor.putString("kasauser","stuart@qosfan.co.uk").commit();
//        editor.putString("kasapassword","QOSfan01").commit();
//        editor.putString("tadopassword","QOSfan01").commit();
//        editor.putString("tadoclientsecret","wZaRN7rpjn3FoNyF5IFuxg9uMzYJcvOoQ8QWiIqS3hfk6gLhVlG57j5YNoZL2Rtc").commit();
//        editor.putString("tadouser","stuart@qosfan.co.uk").commit();
//        editor.putString("kasaappServerUrl","https://eu-wap.tplinkcloud.com").commit();
//        editor.putString("kasadeviceID","800675B8F4FD7A3B920E3DDAACD59A1C184F722A").commit();
//        editor.putString("Bathroomid","e1da9cd4-9022-4a40-8bc3-cf139abb4470").commit();
//        editor.putString("hivepassword","QOSfan01").commit();
//        editor.putString("Hive Plugid","eae0eafe-c801-42b9-b0b9-2d5fa47c4f89").commit();
//        editor.putString("hiveuser","i.harkin269@btinternet.com").commit();
//        editor.putString("Our Roomid","ec12d5f6-e8ac-4568-ad6d-035ac8d798b7").commit();
//        editor.putString("Outside Lightid","9d27733f-552b-4eb6-879a-0f70c6e14c10").commit();
//        editor.putString("Bedside Lightid","bea609b5-c11e-490a-9a85-84534e542711").commit();

//        editor.putString("Hiveuser","i.harkin269@btinternet.com").commit();
        String kasauser = pref.getString("kasauser", "");
        String kasapassword = pref.getString("kasapassword", "");
        String kasaUUID = pref.getString("kasaUUID", "");
        String KasaURL = "https://wap.tplinkcloud.com";
        HttpURLConnection con = (HttpURLConnection) new URL(KasaURL).openConnection();
        con.setRequestMethod("POST");
        String data = "{\n" +
                " \"method\": \"login\",\n" +
                " \"params\": {\n" +
                " \"appType\": \"Kasa_Android\",\n" +
                " \"cloudUserName\": \"" + kasauser + "\",\n" +
                " \"cloudPassword\": \"" + kasapassword + "\",\n" +
                " \"terminalUUID\": \"" + kasaUUID + "\"\n" +
                " }\n" +
                "}";
        con.setRequestProperty("Content-Type", "application/json");
        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        out.writeBytes(data);
        out.flush();
        out.close();
        BufferedReader in3 = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String output2;
        StringBuffer response2 = new StringBuffer();
        while ((output2 = in3.readLine()) != null) {
            response2.append(output2);
        }
        in3.close();
        int tokenlength = response2.indexOf("token");
        String KasaId = response2.substring(tokenlength);
        tokenlength = KasaId.indexOf(":");
        KasaId = KasaId.substring(tokenlength + 2);
        tokenlength = KasaId.indexOf("}");
        KasaId = KasaId.substring(0, tokenlength - 1);
        editor.putString("KasasessionID", KasaId).commit();
        long time = System.currentTimeMillis();
        editor.putLong("Kasasessionstart", time).commit();
    }

    static void getTuyaSessionID(Context context) throws IOException {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String tuyauser = pref.getString("TuyaUser", "");
        String tuyapassword = pref.getString("TuyaPassword", "");
        String tuyasession = pref.getString("TuyasessionID", "");
        String tuyaDeviceId = pref.getString("TuyadeviceID", "");
        String tuyaURL = pref.getString("TuyaappServerUrl", "");
        String tuyaClientID = "rgmwjg7uamnzslq0o0gx";
        String tuyaClientSecret = "aeebde28d97648389937bcdae931cbeb";
        tuyaURL = "https://openapi.tuyaeu.com/v1.0/token?grant_type=1";
        long time2= System.currentTimeMillis();
        String t = Long. toString(time2);
        String hash="";
        try {
            hash = generateHash(tuyaClientID+t, tuyaClientSecret);
            hash = hash.toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        getTuyaDeviceID(tuyasession, context);
        String headers_pattern = "'client_id': " + tuyaClientID+ ", 'secret': " + tuyaClientSecret + ",'sign_method': HMAC-SHA256, 'sign': " + hash + ",'t': "+ t;
        String data = "'userName': 'stuartmclaren268@gmail.com','password': 'QOSfan01','countryCode': 'eu', 'bizType': 'smart_life','from': 'tuya'";
        HttpURLConnection con = (HttpURLConnection) new URL(tuyaURL).openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("client_id", tuyaClientID);
        con.setRequestProperty("secret", tuyaClientSecret);
        con.setRequestProperty("sign-method", "HMAC-SHA256");
        con.setRequestProperty("sign", hash);
        con.setRequestProperty("t", t);
//        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//        dataType: "json",
//                async: false,
        DataOutputStream out = new DataOutputStream(con.getOutputStream());
//        out.writeBytes(headers_pattern);
        out.flush();
        out.close();
        BufferedReader in3 = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String output2;
        StringBuffer response2 = new StringBuffer();
        while ((output2 = in3.readLine()) != null) {
            response2.append(output2);
        }
        in3.close();
        int tokenlength = response2.indexOf("token");
        String TuyaId = response2.substring(tokenlength);
        tokenlength = TuyaId.indexOf(":");
        TuyaId = TuyaId.substring(tokenlength + 2);
        tokenlength = TuyaId.indexOf("}");
        TuyaId = TuyaId.substring(0, tokenlength - 1);
        editor.putString("TuyasessionID", TuyaId).commit();
        long time = System.currentTimeMillis();
        editor.putLong("Tuyasessionstart", time).commit();
    }

    static void getTadoSessionID(Context context) throws IOException {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String tadoclientSecret = pref.getString("tadoclientsecret", "");
        String tadoUser = pref.getString("tadouser", "");
        String tadoPassword = pref.getString("tadopassword", "");
        String TadoURL = "https://auth.tado.com/oauth/token?client_id=tado-web-app&grant_type=password&scope=home.user&username=" + tadoUser + "&password=" + tadoPassword + "&client_secret=" + tadoclientSecret;
        HttpURLConnection con = (HttpURLConnection) new URL(TadoURL).openConnection();
        con.setRequestMethod("POST");
        con.getOutputStream().write("LOGIN".getBytes());
        BufferedReader in = new BufferedReader(new InputStreamReader(
                con.getInputStream()));
        String inputLine;
        inputLine = in.readLine();
        in.close();
        inputLine = inputLine.substring(17);
        int tokenlength = inputLine.indexOf('"');
        inputLine = inputLine.substring(0, tokenlength);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString("TadosessionID", inputLine).commit();
        long time = System.currentTimeMillis();
        editor.putLong("Tadosessionstart", time).commit();
    }

    void UpDateWidget(Context context, Intent intent)
    {
        Thread widgetUpdateThread = null;
        widgetUpdateThread = new Thread() {
            public void run() {
                // prep the RemoteView
                String timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date());
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                try {
                    getTuyaSessionID(context);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                long kasasessiontime = pref.getLong("Kasasessionstart", 0);
                long tadosessiontime = pref.getLong("Tadosessionstart", 0);
                long HOURINMILLISECONDS = 60 * 60 * 1000;
                long TADOSESSIONLENGTH = 600 * 1000;   //600 seconds
                long KASASESSIONLENGTH = 24 * HOURINMILLISECONDS;
                long now = System.currentTimeMillis();
                String result = null;
                RemoteViews remoteView = null;
                ComponentName smarthomeWidget = null;
                AppWidgetManager appWidgetManager = null;
                remoteView = new RemoteViews(
                        context.getPackageName(), R.layout.smart_home_widget);
                remoteView.setTextViewText(R.id.Timetext, "REFRESHING");
                remoteView.setInt(R.id.Timetext, "setTextColor",
                        android.graphics.Color.RED);
                smarthomeWidget = new ComponentName(context,
                        SmartHomeWidget.class);
                appWidgetManager = AppWidgetManager
                        .getInstance(context);
                appWidgetManager.updateAppWidget(smarthomeWidget,
                        remoteView);
                if (now - kasasessiontime > KASASESSIONLENGTH) {
                    try {
                        getKasaSessionID(context);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (now - tadosessiontime > TADOSESSIONLENGTH) {
                    try {
                        getTadoSessionID(context);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (ACTION_WIDGET_LOGCABIN.equals(intent.getAction())) {
                    String logcabinstatus = pref.getString("logcabin", "");
                    if (!logcabinstatus.equalsIgnoreCase("Offline")) {
                        try {
                            result = toggleKasa(logcabinstatus, context);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        remoteView = new RemoteViews(
                                context.getPackageName(), R.layout.smart_home_widget);
                        remoteView.setTextViewText(R.id.logcabin_button, result);
                        if ("OFF".equals(result)) {
                            remoteView.setInt(R.id.logcabin_button, "setBackgroundResource", R.drawable.roundedbuttonoff);
                        }
                        if ("ON".equals(result)) {
                            remoteView.setInt(R.id.logcabin_button, "setBackgroundResource", R.drawable.roundedbuttonon);
                        }
                        editor.putString("logcabin", result).commit();
                        smarthomeWidget = new ComponentName(context,
                                SmartHomeWidget.class);
                        appWidgetManager = AppWidgetManager
                                .getInstance(context);
                        appWidgetManager.updateAppWidget(smarthomeWidget,
                                remoteView);
                    }
                }

                String target = null;
                if (ACTION_WIDGET_TEMPDOWN.equals(intent.getAction())) {
                    if (pref.contains("targettemp")) {
                        target = pref.getString("targettemp", "");
                        double targettemp = Double.parseDouble(target);
                        targettemp = targettemp - 0.5;
                        if (targettemp<10) targettemp=10;
                        target = targettemp + "";
                        editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                        editor.putString("targettemp", target);
                        editor.commit();
                        try {
                            putTadoInfo(target, context);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        remoteView = new RemoteViews(
                                context.getPackageName(), R.layout.smart_home_widget);
                        target=target.substring(0,4);
                        remoteView.setTextViewText(R.id.targettemperature, target + "°C");
                        smarthomeWidget = new ComponentName(context,
                                SmartHomeWidget.class);
                        appWidgetManager = AppWidgetManager
                                .getInstance(context);
                        appWidgetManager.updateAppWidget(smarthomeWidget,
                                remoteView);
                    }
                }

                if (ACTION_WIDGET_TEMPUP.equals(intent.getAction())) {
                    if (pref.contains("targettemp")) {
                        target = pref.getString("targettemp", "");
                        double targettemp = Double.parseDouble(target);
                        targettemp = targettemp + 0.5;
                        target = targettemp + "";
                        editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                        editor.putString("targettemp", target);
                        editor.commit();
                        try {
                            putTadoInfo(target, context);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        remoteView = new RemoteViews(
                                context.getPackageName(), R.layout.smart_home_widget);
                        target=target.substring(0,4);
                        remoteView.setTextViewText(R.id.targettemperature, target + "°C");
                        smarthomeWidget = new ComponentName(context,
                                SmartHomeWidget.class);
                        appWidgetManager = AppWidgetManager
                                .getInstance(context);
                        appWidgetManager.updateAppWidget(smarthomeWidget,
                                remoteView);
                    }
                }

                String logcabin = null;
                try {
                    logcabin = getKasaInfo(context);
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        getKasaSessionID(context);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    try {
                        logcabin = getKasaInfo(context);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                remoteView = new RemoteViews(
                        context.getPackageName(), R.layout.smart_home_widget);
                remoteView.setTextViewText(R.id.logcabin_button, logcabin);
                if ("OFF".equals(logcabin)) {
                    remoteView.setInt(R.id.logcabin_button, "setBackgroundResource", R.drawable.roundedbuttonoff);
                }
                if ("ON".equals(logcabin)) {
                    remoteView.setInt(R.id.logcabin_button, "setBackgroundResource", R.drawable.roundedbuttonon);
                }
                if ("OFFLINE".equalsIgnoreCase(logcabin)) {
                    remoteView.setInt(R.id.logcabin_button, "setBackgroundResource", R.drawable.roundedbutton);
                }
                editor.putString("logcabin", logcabin).commit();
                smarthomeWidget = new ComponentName(context,
                        SmartHomeWidget.class);
                appWidgetManager = AppWidgetManager
                        .getInstance(context);
                appWidgetManager.updateAppWidget(smarthomeWidget,
                        remoteView);

                try {
                    result = getTadoInfo(context);
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        getTadoSessionID(context);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    try {
                        result = getTadoInfo(context);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                String actual = result.substring(0, 5);
                target = result.substring(6, 11);
                editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                editor.putString("targettemp", target);
                editor.commit();
                // prep the RemoteView
                remoteView = new RemoteViews(
                        context.getPackageName(), R.layout.smart_home_widget);
                remoteView.setTextViewText(R.id.actualtemperature, actual + "°C");
                target=target.substring(0,4);
                remoteView.setTextViewText(R.id.targettemperature, target + "°C");
                remoteView.setTextViewText(R.id.Timetext, timeString);
                remoteView.setInt(R.id.Timetext, "setTextColor",
                        R.color.purple);
                smarthomeWidget = new ComponentName(context,
                        SmartHomeWidget.class);
                appWidgetManager = AppWidgetManager
                        .getInstance(context);
                appWidgetManager.updateAppWidget(smarthomeWidget,
                        remoteView);
            }


            /**
             * Download data for displaying in the Widget
             *
             * @param widgetData
             */
        };

        // start the background thread
        widgetUpdateThread.start();
    }

    public static String generateHash(String secret, String message) throws Exception {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            byte[] bytes = sha256_HMAC.doFinal(message.getBytes());
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for(byte b: bytes)
            sb.append(String.format("%02x", b));
            String hash = sb.toString();
            return hash;
        }
        catch (Exception e){
            return "Error";
        }
    }
}