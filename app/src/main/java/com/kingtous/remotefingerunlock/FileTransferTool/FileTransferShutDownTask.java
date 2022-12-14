package com.kingtous.remotefingerunlock.FileTransferTool;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.kingtous.remotefingerunlock.Common.FunctionTool;
import com.kingtous.remotefingerunlock.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class FileTransferShutDownTask extends AsyncTask<String, String, Void> implements DialogInterface.OnClickListener{

    private Context context;
    public FileTransferShutDownTask(Context context, String IP, String MAC){
        this.context=context;
        dialog=new ProgressDialog(context);
        this.IP=IP;
        this.MAC=MAC;
    }
    String message="";
    ProgressDialog dialog;
    private int resultCode=-1;
    String path;
    private String IP;
    private String recvStr;
    private String MAC;

    public interface ReturnListener{
        void onReturnListener(int resultCode,String message);
    }

    private FileTransferShutDownTask.ReturnListener mReturnListener;

    public void setmReturnListener(FileTransferShutDownTask.ReturnListener listener){
        this.mReturnListener=listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "取消", this);
        dialog.setMessage("正在请求");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    protected void onPostExecute(Void aModel) {
        dialog.dismiss();
        if (mReturnListener!=null){
            mReturnListener.onReturnListener(resultCode,message);
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        dialog.setMessage(values[0]);
    }

    @Override
    protected Void doInBackground(String... strings) {
            //检查可用性
            try {
                if (SocketHolder.getSocket()==null || SocketHolder.getSocket().isClosed())
                    SocketHolder.setSocket(FileTransferActivity.CreateSocket(context,IP));
                if (SocketHolder.getSocket() != null) {
                    OutputStream stream=SocketHolder.getSocket().getOutputStream();
                    //发送目录请求
                    JSONObject object=new JSONObject();
                    object.put("action","shutdown");
                    object.put("mac",MAC);
                    if (FunctionTool.detectModes(context)==1){
                        object.put("oriMac",MAC);
                    }
                    stream.write(object.toString().getBytes(StandardCharsets.UTF_8));
//                      stream.close();
                    //读入数据
//                        SocketHolder.getSocket().setSoTimeout(5000);
                    BufferedInputStream buffered = new BufferedInputStream(SocketHolder.getSocket().getInputStream());
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    int r=-1;
                    byte buff[] =new byte[1024];
                    while((r=buffered.read(buff,0,1024))!=-1)
                    {
                        byteArrayOutputStream.write(buff,0,r);
//                            if(buffered.available() <=0) //添加这里的判断
//                            {
//                                break;
//                            }
                    }
                    SocketHolder.getSocket().close();
                    recvStr =new String(byteArrayOutputStream.toByteArray());
                    JsonObject object1;
                    try {
                        object1=new Gson().fromJson(recvStr,JsonObject.class);
                        if (object1==null){
                            resultCode=0;
                            throw new IOException(context.getString(R.string.msg_shutdown_success));
                        }
                    } catch (JsonSyntaxException e){
                        throw new IOException(context.getString(R.string.msg_invalid_data));
                    }
//                        return new Gson().fromJson(object1,FileModel.class);
                    if (!object1.has("status")){
                        throw new IOException(context.getString(R.string.msg_no_responce_state));
                    }
                    if (object1.get("status").getAsString().equals("0")){
                        message=recvStr;
                        resultCode=0;
                    }
                    else {
                        switch (object1.get("status").getAsString()){
                            case "-2":
                                throw new IOException(context.getString(R.string.msg_device_offline));
                            case "-5":
                                throw new IOException(context.getString(R.string.msg_shutdown_error));
                            default:
                                throw new IOException(context.getString(R.string.msg_unknown_error));
                        }
                    }

                }
            } catch (IOException e) {
                message=e.getMessage();
            } catch (JSONException e) {
                message=e.getMessage();
            }
            finally {
                if (recvStr!=null && !recvStr.equals("")){
                    // 预留 debug 项 ，可加 recvStr 在 message中
                    message=message;
                }
            }

        return null;
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        this.cancel(true);
    }

}
