package com.kingtous.remotefingerunlock.WLANConnectTool;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.renderscript.ScriptGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kingtous.remotefingerunlock.Common.FunctionTool;
import com.kingtous.remotefingerunlock.Common.ToastMessageTool;
import com.kingtous.remotefingerunlock.DataStoreTool.DataQueryHelper;
import com.kingtous.remotefingerunlock.DataStoreTool.RecordData;
import com.kingtous.remotefingerunlock.DataStoreTool.RecordSQLTool;
import com.kingtous.remotefingerunlock.FileTransferTool.FileModel;
import com.kingtous.remotefingerunlock.FileTransferTool.FileTransferActivity;
import com.kingtous.remotefingerunlock.FileTransferTool.SocketHolder;
import com.kingtous.remotefingerunlock.R;
import com.kingtous.remotefingerunlock.Security.SSLSecurityClient;
import com.kingtous.remotefingerunlock.Security.SSLSecurityDoubleClient;
import com.kingtous.remotefingerunlock.Security.VersionChecker;
import com.stealthcopter.networktools.ARPInfo;
import com.stealthcopter.networktools.Ping;
import com.stealthcopter.networktools.SubnetDevices;
import com.stealthcopter.networktools.ping.PingResult;
import com.stealthcopter.networktools.subnet.Device;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import androidx.appcompat.app.AlertDialog;

public class WLANClient extends AsyncTask<Void,String,Void> {

    String host;
    int port;
    int flags;
    RecordData data;
    private Context context;
    String message;
    int resultCode = -1;

    String pre_message;


    // ===========
    ProgressDialog dialog;

    WLANClient(Context context, String host, int port, RecordData data, int flags) {
        this.context = context;
        this.host = host;
        this.port = port;
        this.data = data;
        this.flags = flags;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(context);
        dialog.setMessage("????????????????????????");
        dialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancel(true);
            }
        });
        dialog.setCancelable(false);
        if (context!=context.getApplicationContext())
            dialog.show();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        for (String str : values) {
            dialog.setMessage(str);
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        dialog.dismiss();
        if (context!=context.getApplicationContext()){
            new AlertDialog.Builder(context)
                    .setMessage(pre_message)
                    .setPositiveButton("??????",null)
                    .show();
        }
        else{
            ToastMessageTool.ttl(context,pre_message);
        }
    }

    private void log(String text) {
        if (context != null) {
            if (context != context.getApplicationContext()) {
                publishProgress(new String[]{text});
            }
            pre_message=text;
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            Ping p = Ping.onAddress(host);
            p.setTimeOutMillis(1000);
            PingResult result = p.doPing();
//??????            if (!result.isReachable() || (macinfo!=null && !ARPInfo.getMACFromIPAddress(host).equals(data.getMac()))) {
            if (flags == 0) {
                if (!result.isReachable()) {
                    if (data.getMac().equals("") || data.getMac() == null) {
                    } else {
                        log("IP??????????????????????????????");
                        // ?????????Mac????????????host
                        RecordData dataTmp = data;
                        String ip = ARPInfo.getIPAddressFromMAC(dataTmp.getMac());
                        if (ip == null) {
                            final String[] ipTmp = new String[1];
                            //????????????
                            SubnetDevices devices = SubnetDevices.fromLocalAddress();
                            devices.findDevices(new SubnetDevices.OnSubnetDeviceFound() {
                                @Override
                                public void onDeviceFound(Device device) {
                                    if (device.mac != null && device.mac.toUpperCase().equals(data.getMac())) {
                                        ipTmp[0] = device.ip;
                                    }
                                }

                                @Override
                                public void onFinished(ArrayList<Device> arrayList) {
                                }
                            });
                            if (ipTmp[0] == null) {
                                //TODO ??????????????????WiFi??????ping??????????????????
                                log("?????????????????????????????????????????????????????????");
                                return null;
                            } else {
                                ip = ipTmp[0];
                                host = ip;
                            }
                        }
                        dataTmp.setIp(ip.toUpperCase());
                        // ???????????????
                        DataQueryHelper helper = new DataQueryHelper(context, context.getString(R.string.sqlDBName), null, 1);
                        if (RecordSQLTool.updatetoSQL(helper.getWritableDatabase(), data, dataTmp)) {
                            log("IP????????????????????????");
                        }
                    }
                }
            }
            FileTransferActivity.CreateSocket(context, host);
            Socket socket = SSLSecurityClient.CreateSocket(context, host, port);//new Socket(host,unlock_port);//SSLSecurityClient.CreateSocket(context,host,unlock_port);
            if (socket == null) {
                log("?????????????????????????????????????????????????????????");
                return null;
            }
//            InputStream istream=socket.getInputStream();
//            if (istream!=null){
//                InputStreamReader reader=new InputStreamReader(istream);
//                BufferedReader br=new BufferedReader(reader);
//                String version=br.readLine();
//                if (!VersionChecker.versionAvaliable(version)){
//                    log("??????????????????????????????????????????"+VersionChecker.versionRequirement);
//                    return;
//                }
//            }
            OutputStream stream = socket.getOutputStream();
            JSONObject object = new JSONObject();
            object.put("oriMac", FunctionTool.macAddressAdjust(data.getMac()));
            object.put("username", data.getUser());
            object.put("passwd", data.getPasswd());
            stream.write(object.toString().getBytes(StandardCharsets.UTF_8));

            if (flags == 1) {
                // ?????????????????????
                BufferedInputStream buffered = new BufferedInputStream(socket.getInputStream());
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                int r = -1;
                byte buff[] = new byte[1024];
                while ((r = buffered.read(buff, 0, 1024)) != -1) {
                    byteArrayOutputStream.write(buff, 0, r);
//                            if(buffered.available() <=0) //?????????????????????
//                            {
//                                break;
//                            }
                }
                socket.close();
                String recvStr = new String(byteArrayOutputStream.toByteArray());
                JsonObject object1 = new Gson().fromJson(recvStr, JsonObject.class);

                if (object1==null){
                    // ?????????
                    message=recvStr;
                    resultCode=0;
                }
                else {
//                    return new Gson().fromJson(object1,FileModel.class);
                    if (!object1.has("status")) {
                        throw new IOException("??????????????????");
                    }

                    if (object1.get("status").getAsString().equals("0")) {
                        message = recvStr;
                        resultCode = 0;
                    } else {
                        switch (object1.get("status").getAsString()) {
                            case "-1":
                                throw new IOException("????????????");
                            case "-2":
                                throw new IOException("???????????????");
                            default:
                                throw new IOException("????????????");
                        }
                    }
                }

            }
            stream.close();
            log("?????????????????????????????????");
        } catch (IOException e) {
            log("?????????????????????????????????\n" + e.getMessage());
        } catch (JSONException e) {
            log("????????????\n" + e.getMessage());
        }
        return null;
    }
}
