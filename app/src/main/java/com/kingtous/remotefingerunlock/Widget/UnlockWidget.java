package com.kingtous.remotefingerunlock.Widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.kingtous.remotefingerunlock.Common.Connect;
import com.kingtous.remotefingerunlock.DataStoreTool.DataQueryHelper;
import com.kingtous.remotefingerunlock.DataStoreTool.RecordData;
import com.kingtous.remotefingerunlock.DataStoreTool.RecordSQLTool;
import com.kingtous.remotefingerunlock.R;

/**
 * Implementation of App Widget functionality.
 */
public class UnlockWidget extends AppWidgetProvider {


    static DataQueryHelper helper;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
//        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
//        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.unlock_widget);
//        views.setTextViewText(R.id.appwidget_text, widgetText);
//        views.setOnClickPendingIntent(R.id.appwidget_text,getPendingIntent(context,R.id.appwidget_text));
        RemoteViews views = updateViewMethod(helper, context);
        views.setOnClickPendingIntent(R.id.appwidget, getPendingIntent(context, R.id.appwidget));
//        context.startService(new Intent(context, UnlockWidgetService.class));
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }

    public static void update(Context context) {
        if (helper == null) {
            helper = new DataQueryHelper(context, context.getString(R.string.sqlDBName), null, 1);
        }
        RemoteViews views = updateViewMethod(helper, context);
        views.setOnClickPendingIntent(R.id.appwidget_text, getPendingIntent(context, R.id.appwidget_text));
//        context.startService(new Intent(context, UnlockWidgetService.class));
        // Instruct the widget manager to update the widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(new ComponentName(context, UnlockWidget.class), views);
    }

    public static RemoteViews updateViewMethod(DataQueryHelper helper, Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.unlock_widget);
        if (helper == null) {
            helper = new DataQueryHelper(context, context.getString(R.string.sqlDBName), null, 1);
        }
        SQLiteDatabase d = helper.getWritableDatabase();
        RecordData defaultRecordData = RecordSQLTool.getDefaultRecordData(d);
        if (defaultRecordData != null) {
            views.setTextViewText(R.id.appwidget_user, "?????????: " + defaultRecordData.getUser());
            views.setTextViewText(R.id.appwidget_method, "????????????: " + defaultRecordData.getType());
        } else {
            views.setTextViewText(R.id.appwidget_text, context.getString(R.string.appwidget_text_0));
        }
        d.close();
        return views;
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
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Uri data = intent.getData();
        int resId = -1;
        if (data != null) {
            resId = Integer.parseInt(data.getSchemeSpecificPart());
        }
        switch (resId) {
            case R.id.appwidget:
                if (helper == null) {
                    helper = new DataQueryHelper(context, context.getString(R.string.sqlDBName), null, 1);
                }
                SQLiteDatabase d = helper.getReadableDatabase();
                RecordData defaultRecordData = RecordSQLTool.getDefaultRecordData(d);
                if (defaultRecordData != null) {
                    //??????????????????WiFi????????????????????????????????????
                    if (defaultRecordData.getType().equals("WLAN")) {
                        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        WifiInfo info = manager.getConnectionInfo();
                        SupplicantState state = info.getSupplicantState();
                        if (state.name().equals("DISCONNECTED")) {
                            Toast.makeText(context, "WiFi?????????...", Toast.LENGTH_SHORT).show();
                            return;
                        }

                    } else if (defaultRecordData.getType().equals("Bluetooth")) {
                        BluetoothManager manager = (BluetoothManager) context.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
                        BluetoothAdapter adapter = manager.getAdapter();
                        if (!adapter.isEnabled()) {
                            Toast.makeText(context, "???????????????...", Toast.LENGTH_SHORT).show();
                            return;
                        }

                    }

                    Toast.makeText(context, "????????????????????????...", Toast.LENGTH_SHORT).show();
                    Connect.start(context.getApplicationContext(), defaultRecordData);
                } else
                    Toast.makeText(context, "?????????????????????????????????????????????????????????????????????????????????", Toast.LENGTH_LONG).show();
                d.close();
                break;
        }
    }

    private static PendingIntent getPendingIntent(Context context, int resID) {
        Intent intent = new Intent();
        intent.setClass(context, UnlockWidget.class);//??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        intent.setAction("unlock");
        //??????data????????????????????????id?????????????????????
        // ??????????????????????????????????????????id?????????????????????????????????????????????????????????intent???data??????id?????????????????????id
        intent.setData(Uri.parse("id:" + resID));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        return pendingIntent;
    }

}

