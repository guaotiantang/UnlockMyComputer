package com.kingtous.remotefingerunlock.DataStoreTool;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.kingtous.remotefingerunlock.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    public static int BOOT_BUTTON = 0;
    public static int UNLOCK_BUTTON = 1;
    public static int SHUTDOWN_BUTTON = 2;
    public static int MORE_BUTTON = 3;

    ArrayList<RecordData> recordDataArrayList;

    public RecordAdapter(ArrayList<RecordData> list) {
        recordDataArrayList = list;
    }


    //========接口============
    public interface OnItemClickListener {
        void OnClick(View view, int type, RecordData recordData);
    }

    private RecordAdapter.OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(RecordAdapter.OnItemClickListener mOnLongItemClickListener) {
        this.mOnItemClickListener = mOnLongItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_list_item, parent, false);
        return new RecordAdapter.recordHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        ((recordHolder) holder).type.setText(recordDataArrayList.get(position).getType());
        ((recordHolder) holder).name.setText(recordDataArrayList.get(position).getName());
        ((recordHolder) holder).user.setText(recordDataArrayList.get(position).getUser());
        ((recordHolder) holder).mac.setText(recordDataArrayList.get(position).getMac());
        ((recordHolder) holder).toBoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.OnClick(v, BOOT_BUTTON, recordDataArrayList.get(position));
            }
        });

        ((recordHolder) holder).toUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.OnClick(v, UNLOCK_BUTTON, recordDataArrayList.get(position));
            }
        });
        ((recordHolder) holder).toPoweroff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.OnClick(v, SHUTDOWN_BUTTON, recordDataArrayList.get(position));
            }
        });
        ((recordHolder) holder).toMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.OnClick(v, MORE_BUTTON, recordDataArrayList.get(position));
            }
        });

        //判断默认，是则显示默认
        if (recordDataArrayList.get(position).getIsDefault() == RecordData.TRUE) {
            ((recordHolder) holder).showDefault.setVisibility(View.VISIBLE);
        } else ((recordHolder) holder).showDefault.setVisibility(View.GONE);

    }


    @Override
    public int getItemCount() {
        return recordDataArrayList.size();
    }


    public class recordHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public TextView type;
        public TextView user;
        public TextView mac;
        public Button toBoot;
        public Button toUnlock;
        public Button toPoweroff;
        public TextView showDefault;
        public Button toMore;

        public recordHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.record_name);
            user = itemView.findViewById(R.id.record_user);
            mac = itemView.findViewById(R.id.record_mac);
            type = itemView.findViewById(R.id.record_type);
            toBoot = itemView.findViewById(R.id.record_boot);
            toUnlock = itemView.findViewById(R.id.record_unlock);
            toPoweroff = itemView.findViewById(R.id.record_poweroff);
            showDefault = itemView.findViewById(R.id.record_sign_default);
            toMore = itemView.findViewById(R.id.record_more);
        }

    }
}
