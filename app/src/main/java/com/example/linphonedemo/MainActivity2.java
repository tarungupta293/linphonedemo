package com.example.linphonedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.linphone.core.AccountCreator;
import org.linphone.core.AccountCreatorListenerStub;
import org.linphone.core.Address;
import org.linphone.core.AuthInfo;
import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.ConfiguringState;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.GlobalState;
import org.linphone.core.ProxyConfig;
import org.linphone.core.tools.H264Helper;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity2 extends AppCompatActivity {

    private String TAG = "linphoneDemo";
    private Core core;
    private CallParams params;
    private AccountCreator accountCreator;
    private Timer mTimer;
    private Handler sHandler = new Handler(Looper.getMainLooper());

    CoreListenerStub coreListenerStub = new CoreListenerStub(){
        @Override
        public void onGlobalStateChanged(Core lc, GlobalState gstate, String message) {
            super.onGlobalStateChanged(lc, gstate, message);
            Log.e(TAG,"onGlobalStateChanged - " + message + " global state - " + gstate);
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

    private AccountCreatorListenerStub accountCreatorListenerStub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accountCreatorListenerStub = new AccountCreatorListenerStub(){
            @Override
            public void onIsAccountExist(AccountCreator creator, AccountCreator.Status status, String resp) {
                super.onIsAccountExist(creator, status, resp);
                Log.e(TAG, "onIsAccountExist");
                Toast.makeText(MainActivity2.this, "onIsAccountExist",Toast.LENGTH_SHORT).show();
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
                Toast.makeText(MainActivity2.this, "onCreateAccount",Toast.LENGTH_SHORT).show();
                if (status.equals(AccountCreator.Status.AccountCreated)) {
                    Log.e(TAG, "onCreateAccount - AccountCreated");
                }
            }
        };


        Button btnCall = findViewById(R.id.btnCall);
        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accountCreator.reset();
                accountCreator.setLanguage(Locale.getDefault().getLanguage());
                accountCreator.setPhoneNumber("9993491699", "+91");
                accountCreator.setUsername("shikarkutta");
                AccountCreator.Status status = accountCreator.recoverAccount();
                Toast.makeText(MainActivity2.this, "MainActivity2 - status " + status + accountCreator.isAccountExist(),Toast.LENGTH_SHORT).show();
                Log.e(TAG, "MainActivity2 - status " + status + accountCreator.isAccountExist());

//                createProxyConfig();

            }
        });
        btnCall.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                Address address=core.createAddress("sip:+918802508993@sip.linphone.org");

//                Address address = core.interpretUrl("8802508993");

                address.setDisplayName(null);
                params = core.createCallParams(null);
//        mBandwidthManager.updateWithProfileSettings(params);

                params.enableVideo(false);

                params.enableLowBandwidth(true);
//        Log.d("[Call Manager] Low bandwidth enabled in call params");
                core.inviteAddressWithParams(address, params);
            }
        });
    }

    private void createProxyConfig() {

        core.setZrtpSecretsFile("/data/data/org.linphone.debug/files/zrtp_secrets");
//        String deviceName = "Lenovo VIBE K5";
        String deviceName = "RMX1801";
        String appName = "LinphoneAndroid";
        String androidVersion = BuildConfig.VERSION_NAME;
        String userAgent = appName + "/" + androidVersion + " (" + deviceName + ") LinphoneSDK";

        core.setUserAgent(
                userAgent,
                getString(R.string.linphone_sdk_version)
                        + " ("
                        + getString(R.string.linphone_sdk_branch)
                        + ")");
//        core.loadConfigFromXml("/data/data/org.linphone.debug/files/linphone_assistant_create.rc");


//        core.createProxyConfig();
//        ProxyConfig proxyConfig = core.getDefaultProxyConfig();
        // Migrate existing linphone accounts to have conference factory uri and LIME X3Dh url set
        String uri = getString(R.string.default_conference_factory_uri);
        for (ProxyConfig lpc : core.getProxyConfigList()) {
            if (lpc.getIdentityAddress().getDomain().equals(getString(R.string.default_domain))) {
                if (lpc.getConferenceFactoryUri() == null) {
                    lpc.edit();
                    Log.i(
                            TAG,
                            "[Manager] Setting conference factory on proxy config "
                                    + lpc.getIdentityAddress().asString()
                                    + " to default value: "
                                    + uri);
                    lpc.setConferenceFactoryUri(uri);
                    lpc.done();
                }

                if (core.limeX3DhAvailable()) {
                    String url = core.getLimeX3DhServerUrl();
                    if (url == null || url.isEmpty()) {
                        url = getString(R.string.default_lime_x3dh_server_url);
                        Log.i(
                                TAG,
                                "[Manager] Setting LIME X3Dh server url to default value: " + url);
                        core.setLimeX3DhServerUrl(url);
                    }
                }
            }
        }

        /*ProxyConfig proxyConfig = core.createProxyConfig();


        proxyConfig = core.getDefaultProxyConfig();

        Address address=core.createAddress("sip:shikarkutta@sip.linphone.org");
        AuthInfo authInfo=core.createAuthInfo(address.getUsername(), "an011kit", null,null, null,  address.getDomain());
        core.addAuthInfo(authInfo);
        proxyConfig=core.createProxyConfig();
        // Migrate existing linphone accounts to have conference factory uri and LIME X3Dh url set
        String uri2 = getString(R.string.default_conference_factory_uri);*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        core = Factory.instance().createCore("/data/user/0/org.linphone.debug/files/.linphonerc","/data/user/0/org.linphone.debug/files/linphonerc", MainActivity2.this);
//        core = Factory.instance().createCore(null,null, MainActivity2.this);

        core.addListener(coreListenerStub);
        core.start();

        final Runnable mIterateRunnable =
                new Runnable() {
                    @Override
                    public void run() {
                        if (core != null) {
                            core.iterate();
                        }
                    }
                };
        TimerTask lTask =
                new TimerTask() {
                    @Override
                    public void run() {
                        sHandler.post(mIterateRunnable);
//                        LinphoneUtils.dispatchOnUIThread(mIterateRunnable);
                    }
                };
        /*use schedule instead of scheduleAtFixedRate to avoid iterate from being call in burst after cpu wake up*/
        mTimer = new Timer("Linphone scheduler");
        mTimer.schedule(lTask, 0, 20);


        createProxyConfig();

        H264Helper.setH264Mode(H264Helper.MODE_AUTO, core);

        accountCreator = core.createAccountCreator("https://subscribe.linphone.org:444/wizard.php");
//        accountCreator.removeListener(accountCreatorListenerStub);
//        accountCreator.addListener(accountCreatorListenerStub);
        accountCreator.setListener(accountCreatorListenerStub);
    }

    @Override
    protected void onPause() {
        super.onPause();
        core.stop();
        if (mTimer != null)
            mTimer.cancel();
        accountCreator.removeListener(accountCreatorListenerStub);
    }
}
