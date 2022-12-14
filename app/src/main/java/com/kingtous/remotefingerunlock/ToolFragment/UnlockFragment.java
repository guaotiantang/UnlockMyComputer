package com.kingtous.remotefingerunlock.ToolFragment;

import android.app.Activity;

import android.app.KeyguardManager;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import android.os.CancellationSignal;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kingtous.remotefingerunlock.Common.Connect;
import com.kingtous.remotefingerunlock.DataStoreTool.DataQueryHelper;
import com.kingtous.remotefingerunlock.DataStoreTool.RecordData;
import com.kingtous.remotefingerunlock.DataStoreTool.RecordSQLTool;
import com.kingtous.remotefingerunlock.R;

import java.util.Objects;


public class UnlockFragment extends Fragment {

    FingerprintManager fingerprintManager;
    KeyguardManager keyguardManager;
    CancellationSignal cancellationSignal;
    FingerprintManager.AuthenticationCallback authenticationCallback;
    SQLiteOpenHelper helper;
    FrameLayout messageFrame;
    View messageView;

    Context context;

    public boolean isVisible;

    public UnlockFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        messageView = inflater.inflate(R.layout.unlock_status, container, false);
        return inflater.inflate(R.layout.unlock, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        helper = new DataQueryHelper(context, getString(R.string.sqlDBName), null, 1);
        messageFrame = Objects.requireNonNull(getActivity()).findViewById(R.id.FingerMessage);
        messageFrame.addView(messageView);
        updateStatus();
    }

    private void updateStatus() {
        RecordData data = RecordSQLTool.getDefaultRecordData(helper.getReadableDatabase());
        if (data == null) {
            ((TextView) messageView.findViewById(R.id.appwidget_user)).setText(getString(R.string.defaultUser));
            ((TextView) messageView.findViewById(R.id.appwidget_method)).setText(getString(R.string.defaultUnlockMethod));
        } else {
            String type = data.getType();
            String user = data.getUser();
            ((TextView) messageView.findViewById(R.id.appwidget_user)).setText("??????????????????" + user);
            ((TextView) messageView.findViewById(R.id.appwidget_method)).setText("???????????????" + type);
        }


    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fingerprintManager = (FingerprintManager) Objects.requireNonNull(getActivity()).getSystemService(Activity.FINGERPRINT_SERVICE);
        keyguardManager = (KeyguardManager) getActivity().getSystemService(Activity.KEYGUARD_SERVICE);
        cancellationSignal = new CancellationSignal();
        context = getContext();
        //?????????????????????
        if (!fingerprintManager.isHardwareDetected()) {
            Toast.makeText(getContext(), "????????????????????????????????????????????????????????????", Toast.LENGTH_LONG).show();
        }
        initCallBack();
        startFingerListening();

    }

    private void initCallBack() {
        authenticationCallback = new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                //Toast.makeText(context, errString, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                Toast.makeText(context, helpString, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                RecordData data = RecordSQLTool.getDefaultRecordData(helper.getReadableDatabase());
                if (data != null) {
                    Toast.makeText(context, "????????????????????????...", Toast.LENGTH_SHORT).show();
                    Connect.start(context, data);
                } else
                    Toast.makeText(context, "?????????????????????????????????????????????????????????????????????????????????", Toast.LENGTH_LONG).show();
                startFingerListening();
            }

            @Override
            public void onAuthenticationFailed() {
                Toast.makeText(context, "??????????????????", Toast.LENGTH_LONG).show();
            }
        };
    }

    private void startFingerListening() {
//        if (isVisible){
            fingerprintManager.authenticate(null,
                    cancellationSignal,
                    0,
                    authenticationCallback,
                    null);
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancellationSignal.cancel();
    }


    @Override
    public void onResume() {
        super.onResume();
        startFingerListening();
        updateStatus();
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getUserVisibleHint()) {
            isVisible = true;
        } else {
            isVisible = false;
        }
    }

}
