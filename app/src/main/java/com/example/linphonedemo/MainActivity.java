package com.example.linphonedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.linphone.core.AccountCreator;
import org.linphone.core.AccountCreatorListenerStub;
import org.linphone.core.Address;
import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.ConfiguringState;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.GlobalState;

public class MainActivity extends AppCompatActivity {

    private String TAG = "linphoneDemo";
    private Core core;
    private CallParams params;
    private AccountCreator accountCreator;
    CoreListenerStub coreListenerStub = new CoreListenerStub(){
        @Override
        public void onGlobalStateChanged(Core lc, GlobalState gstate, String message) {
            super.onGlobalStateChanged(lc, gstate, message);
            Log.e(TAG,"onGlobalStateChanged - " + message + "global state - " + gstate);
        }

        @Override
        public void onConfiguringStatus(Core lc, ConfiguringState status, String message) {
            super.onConfiguringStatus(lc, status, message);
            Log.e(TAG,"onConfiguringStatus - " + message + " configuring status - " + status);
        }

        @Override
        public void onCallStateChanged(Core lc, Call call, Call.State cstate, String message) {
            super.onCallStateChanged(lc, call, cstate, message);
            Log.e(TAG,"onCallStateChanged - " + message);
            if (cstate == Call.State.IncomingReceived || cstate == Call.State.IncomingEarlyMedia){
                Log.e(TAG,"onCallStateChanged - " + "Incoming call");
            }else if (cstate == Call.State.OutgoingInit) {
                Log.e(TAG,"onCallStateChanged - " + "Outgoing call");
            } else if (cstate == Call.State.Connected) {
                Log.e(TAG,"onCallStateChanged - " + "Connected");
            } else if (cstate == Call.State.End
                    || cstate == Call.State.Released
                    || cstate == Call.State.Error) {
                Log.e(TAG,"onCallStateChanged - " + "End - Released - Error");

                if (cstate == Call.State.Released
                        && call.getCallLog().getStatus() == Call.Status.Missed) {
                    Log.e(TAG,"onCallStateChanged - " + "Released - Missed");
                }
            }
        }
    };

    AccountCreatorListenerStub accountCreatorListenerStub = new AccountCreatorListenerStub(){
        @Override
        public void onIsAccountExist(AccountCreator creator, AccountCreator.Status status, String resp) {
            super.onIsAccountExist(creator, status, resp);
            Log.e(TAG, "onIsAccountExist");
            if (status.equals(AccountCreator.Status.AccountExist)
                    || status.equals(AccountCreator.Status.AccountExistWithAlias)) {
                Log.e(TAG, "onIsAccountExist - AccountExist");
            }else if (status.equals(AccountCreator.Status.AccountNotExist)) {
                Log.e(TAG, "onIsAccountExist - AccountNotExist");
            }
        }

        @Override
        public void onCreateAccount(AccountCreator creator, AccountCreator.Status status, String resp) {
            super.onCreateAccount(creator, status, resp);
            Log.e(TAG, "onCreateAccount");
            if (status.equals(AccountCreator.Status.AccountCreated)) {
                Log.e(TAG, "onCreateAccount - AccountCreated");
            }
        }

        @Override
        public void onActivateAccount(AccountCreator creator, AccountCreator.Status status, String resp) {
            super.onActivateAccount(creator, status, resp);
            Log.e(TAG, "onActivateAccount");
        }

        @Override
        public void onActivateAlias(AccountCreator creator, AccountCreator.Status status, String resp) {
            super.onActivateAlias(creator, status, resp);
            Log.e(TAG, "onActivateAlias");
        }

        @Override
        public void onIsAccountLinked(AccountCreator creator, AccountCreator.Status status, String resp) {
            super.onIsAccountLinked(creator, status, resp);
            Log.e(TAG, "onIsAccountLinked");
        }

        @Override
        public void onLinkAccount(AccountCreator creator, AccountCreator.Status status, String resp) {
            super.onLinkAccount(creator, status, resp);
            Log.e(TAG, "onLinkAccount");
        }

        @Override
        public void onIsAliasUsed(AccountCreator creator, AccountCreator.Status status, String resp) {
            super.onIsAliasUsed(creator, status, resp);
            Log.e(TAG, "onIsAliasUsed");
        }

        @Override
        public void onIsAccountActivated(AccountCreator creator, AccountCreator.Status status, String resp) {
            super.onIsAccountActivated(creator, status, resp);
            Log.e(TAG, "onIsAccountActivated");
        }

        @Override
        public void onLoginLinphoneAccount(AccountCreator creator, AccountCreator.Status status, String resp) {
            super.onLoginLinphoneAccount(creator, status, resp);
            Log.e(TAG, "onLoginLinphoneAccount");
        }

        @Override
        public void onUpdateAccount(AccountCreator creator, AccountCreator.Status status, String resp) {
            super.onUpdateAccount(creator, status, resp);
            Log.e(TAG, "onUpdateAccount");
        }

        @Override
        public void onRecoverAccount(AccountCreator creator, AccountCreator.Status status, String resp) {
            super.onRecoverAccount(creator, status, resp);
            Log.e(TAG, "onRecoverAccount");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        core = Factory.instance().createCore("/data/user/0/org.linphone.debug/files/.linphonerc","/data/user/0/org.linphone.debug/files/linphonerc",MainActivity.this);
        core.addListener(coreListenerStub);
        params = core.createCallParams(null);
//        mBandwidthManager.updateWithProfileSettings(params);

        params.enableVideo(false);

        params.enableLowBandwidth(true);
//        Log.d("[Call Manager] Low bandwidth enabled in call params");


        accountCreator = core.createAccountCreator("https://subscribe.linphone.org:444/wizard.php");
        accountCreator.addListener(accountCreatorListenerStub);


        Button btnCall = findViewById(R.id.btnCall);
        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accountCreator.reset();
                accountCreator.setPhoneNumber("8802508993", "+91");
                accountCreator.setUsername("8802508993");
                AccountCreator.Status status = accountCreator.recoverAccount();
                Log.e(TAG, "MainActivity - status " + status + accountCreator.isAccountExist());
            }
        });
        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


        /*if (forceZRTP) {
            params.setMediaEncryption(MediaEncryption.ZRTP);
        }

        String recordFile =
                FileUtils.getCallRecordingFilename(
                        LinphoneContext.instance().getApplicationContext(), address);
        params.setRecordFile(recordFile);*/

                Address address = core.interpretUrl("8802508993");
                core.inviteAddressWithParams(address, params);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
