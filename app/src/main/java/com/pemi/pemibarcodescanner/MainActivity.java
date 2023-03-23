package com.pemi.pemibarcodescanner;

import static android.content.ContentValues.TAG;
import static android.provider.ContactsContract.Intents.Insert.ACTION;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.pemi.pemibarcodescanner.adapter.BarcodeAdapter;
import com.pemi.pemibarcodescanner.adapter.SingleAdapter;
import com.pemi.pemibarcodescanner.model.Barcode;
import com.pemi.pemibarcodescanner.model.BarcodeResponse;
import com.pemi.pemibarcodescanner.network.ApiClient;
import com.pemi.pemibarcodescanner.network.ApiInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String EXTRA_PROFILENAME = "DWDataCapture1";

    // DataWedge Extras
    private static final String EXTRA_GET_VERSION_INFO = "com.symbol.datawedge.api.GET_VERSION_INFO";
    private static final String EXTRA_CREATE_PROFILE = "com.symbol.datawedge.api.CREATE_PROFILE";
    private static final String EXTRA_KEY_APPLICATION_NAME = "com.symbol.datawedge.api.APPLICATION_NAME";
    private static final String EXTRA_KEY_NOTIFICATION_TYPE = "com.symbol.datawedge.api.NOTIFICATION_TYPE";
    private static final String EXTRA_SOFT_SCAN_TRIGGER = "com.symbol.datawedge.api.SOFT_SCAN_TRIGGER";
    private static final String EXTRA_RESULT_NOTIFICATION = "com.symbol.datawedge.api.NOTIFICATION";
    private static final String EXTRA_REGISTER_NOTIFICATION = "com.symbol.datawedge.api.REGISTER_FOR_NOTIFICATION";
    private static final String EXTRA_UNREGISTER_NOTIFICATION = "com.symbol.datawedge.api.UNREGISTER_FOR_NOTIFICATION";
    private static final String EXTRA_SET_CONFIG = "com.symbol.datawedge.api.SET_CONFIG";

    private static final String EXTRA_RESULT_NOTIFICATION_TYPE = "NOTIFICATION_TYPE";
    private static final String EXTRA_KEY_VALUE_SCANNER_STATUS = "SCANNER_STATUS";
    private static final String EXTRA_KEY_VALUE_PROFILE_SWITCH = "PROFILE_SWITCH";
    private static final String EXTRA_KEY_VALUE_CONFIGURATION_UPDATE = "CONFIGURATION_UPDATE";
    private static final String EXTRA_KEY_VALUE_NOTIFICATION_STATUS = "STATUS";
    private static final String EXTRA_KEY_VALUE_NOTIFICATION_PROFILE_NAME = "PROFILE_NAME";
    private static final String EXTRA_SEND_RESULT = "SEND_RESULT";

    private static final String EXTRA_EMPTY = "";

    private static final String EXTRA_RESULT_GET_VERSION_INFO = "com.symbol.datawedge.api.RESULT_GET_VERSION_INFO";
    private static final String EXTRA_RESULT = "RESULT";
    private static final String EXTRA_RESULT_INFO = "RESULT_INFO";
    private static final String EXTRA_COMMAND = "COMMAND";

    // DataWedge Actions
    private static final String ACTION_DATAWEDGE = "com.symbol.datawedge.api.ACTION";
    private static final String ACTION_RESULT_NOTIFICATION = "com.symbol.datawedge.api.NOTIFICATION_ACTION";
    private static final String ACTION_RESULT = "com.symbol.datawedge.api.RESULT_ACTION";

    private Boolean bRequestSendResult = false;
    final String LOG_TAG = "PEMIBarcode";

    private static final int ZXING_CAMERA_PERMISSION = 1;
    private Class<?> mClss;
    RecyclerView rv;
    BarcodeAdapter adapter;
    ArrayList<Barcode> models;
    List<String> dataset = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rv = findViewById(R.id.rv_barcode);

        models = new ArrayList<>();

        rv.setAdapter(adapter);
        //getBarcodeData();

        final Button btnSetDecoders = (Button) findViewById(R.id.btnSet);
        btnSetDecoders.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                final String checkCode128 = "Code128";
                String Code128Value = setDecoder(checkCode128);

                final String checkCode39 = "Code39";
                String Code39Value = setDecoder(checkCode39);

                final String checkEAN13 = "EAN13";
                String EAN13Value = setDecoder(checkEAN13);

                final String checkUPCA = "UPCA";
                String UPCAValue = setDecoder(checkUPCA);

                // Main bundle properties
                Bundle profileConfig = new Bundle();
                profileConfig.putString("PROFILE_NAME", EXTRA_PROFILENAME);
                profileConfig.putString("PROFILE_ENABLED", "true");
                profileConfig.putString("CONFIG_MODE", "UPDATE");  // Update specified settings in profile

                // PLUGIN_CONFIG bundle properties
                Bundle barcodeConfig = new Bundle();
                barcodeConfig.putString("PLUGIN_NAME", "BARCODE");
                barcodeConfig.putString("RESET_CONFIG", "true");

                // PARAM_LIST bundle properties
                Bundle barcodeProps = new Bundle();
                barcodeProps.putString("scanner_selection", "auto");
                barcodeProps.putString("scanner_input_enabled", "true");
                barcodeProps.putString("decoder_code128", Code128Value);
                barcodeProps.putString("decoder_code39", Code39Value);
                barcodeProps.putString("decoder_ean13", EAN13Value);
                barcodeProps.putString("decoder_upca", UPCAValue);

                // Bundle "barcodeProps" within bundle "barcodeConfig"
                barcodeConfig.putBundle("PARAM_LIST", barcodeProps);
                // Place "barcodeConfig" bundle within main "profileConfig" bundle
                profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig);

                // Create APP_LIST bundle to associate app with profile
                Bundle appConfig = new Bundle();
                appConfig.putString("PACKAGE_NAME", getPackageName());
                appConfig.putStringArray("ACTIVITY_LIST", new String[]{"*"});
                profileConfig.putParcelableArray("APP_LIST", new Bundle[]{appConfig});
                sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileConfig);
                Toast.makeText(getApplicationContext(), "In profile " + EXTRA_PROFILENAME + " the selected decoders are being set: \nCode128=" + Code128Value + "\nCode39="
                        + Code39Value + "\nEAN13=" + EAN13Value + "\nUPCA=" + UPCAValue, Toast.LENGTH_LONG).show();
            }
        });

        Bundle b = new Bundle();
        b.putString(EXTRA_KEY_APPLICATION_NAME, getPackageName());
        b.putString(EXTRA_KEY_NOTIFICATION_TYPE, "SCANNER_STATUS");     // register for changes in scanner status
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_REGISTER_NOTIFICATION, b);

        registerReceivers();

        // Get DataWedge version
        // Use GET_VERSION_INFO: http://techdocs.zebra.com/datawedge/latest/guide/api/getversioninfo/
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_GET_VERSION_INFO, EXTRA_EMPTY);    // must be called after registering BroadcastReceiver

    }

    public void generateBarcodeList(ArrayList<Barcode> barcodes) {
        adapter = new BarcodeAdapter(getApplicationContext(), barcodes);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(adapter);
        models.addAll(barcodes);
    }

    public void generateLogList(List<String> dataset){
        SingleAdapter singleAdapter = new SingleAdapter(MainActivity.this, dataset);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(singleAdapter);
        singleAdapter.notifyDataSetChanged();
    }

    public void launchSimpleActivity(View v) {
        launchActivity(SimpleScannerActivity.class);
    }


    public void launchActivity(Class<?> clss) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            mClss = clss;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
        } else {
            Intent intent = new Intent(this, clss);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ZXING_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mClss != null) {
                        Intent intent = new Intent(this, mClss);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(this, "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    public void getBarcodeData() {

        ApiInterface apiInterface = ApiClient.create();
        Call<BarcodeResponse> responseCall;
        responseCall = apiInterface.getBarcode();
        Log.wtf("URL Called", responseCall.request().url() + "");
        responseCall.enqueue(new Callback<BarcodeResponse>() {

            @Override
            public void onResponse(Call<BarcodeResponse> call, final Response<BarcodeResponse> response) {
                Log.d(TAG, "Response Body -> " + ((response != null && response.body() != null) ? response.body() : "<---"));
                if (response.body() != null) {
                    if (response.body().getBarcode() != null) {
                        //Toast.makeText(MainActivity.this, response.body().getBook().get(0).getTitle(), Toast.LENGTH_SHORT).show();
                        generateBarcodeList(response.body().getBarcode());
                    }
                }else{
                    Toast.makeText(MainActivity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BarcodeResponse> call, Throwable t) {

            }
        });
    }

    public String setDecoder (String decoder)
    {
        //boolean checkValue = decoder.isChecked();
        String value = "false";
        if (decoder != null)
        {
            value = "true";
            return value;
        }
        else
            return value;
    }

    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle b = intent.getExtras();

            Log.d(LOG_TAG, "DataWedge Action:" + action);

            // Get DataWedge version info
            if (intent.hasExtra(EXTRA_RESULT_GET_VERSION_INFO))
            {
                Bundle versionInfo = intent.getBundleExtra(EXTRA_RESULT_GET_VERSION_INFO);
                String DWVersion = versionInfo.getString("DATAWEDGE");

                //TextView txtDWVersion = (TextView) findViewById(R.id.txtGetDWVersion);
                //txtDWVersion.setText(DWVersion);
                //Log.i(LOG_TAG, "DataWedge Version: " + DWVersion);
            }

            if (action.equals(getResources().getString(R.string.activity_intent_filter_action)))
            {
                //  Received a barcode scan
                try
                {
                    displayScanResult(intent, "via Broadcast");
                }
                catch (Exception e)
                {
                    //  Catch error if the UI does not exist when we receive the broadcast...
                }
            }

            else if (action.equals(ACTION_RESULT))
            {
                // Register to receive the result code
                if ((intent.hasExtra(EXTRA_RESULT)) && (intent.hasExtra(EXTRA_COMMAND)))
                {
                    String command = intent.getStringExtra(EXTRA_COMMAND);
                    String result = intent.getStringExtra(EXTRA_RESULT);
                    String info = "";

                    if (intent.hasExtra(EXTRA_RESULT_INFO))
                    {
                        Bundle result_info = intent.getBundleExtra(EXTRA_RESULT_INFO);
                        Set<String> keys = result_info.keySet();
                        for (String key : keys) {
                            Object object = result_info.get(key);
                            if (object instanceof String) {
                                info += key + ": " + object + "\n";
                            } else if (object instanceof String[]) {
                                String[] codes = (String[]) object;
                                for (String code : codes) {
                                    info += key + ": " + code + "\n";
                                }
                            }
                        }
                        Log.d(LOG_TAG, "Command: "+command+"\n" +
                                "Result: " +result+"\n" +
                                "Result Info: " + info + "\n");
                        Toast.makeText(getApplicationContext(), "Error Resulted. Command:" + command + "\nResult: " + result + "\nResult Info: " +info, Toast.LENGTH_LONG).show();
                    }
                }

            }

            // Register for scanner change notification
            else if (action.equals(ACTION_RESULT_NOTIFICATION))
            {
                if (intent.hasExtra(EXTRA_RESULT_NOTIFICATION))
                {
                    Bundle extras = intent.getBundleExtra(EXTRA_RESULT_NOTIFICATION);
                    String notificationType = extras.getString(EXTRA_RESULT_NOTIFICATION_TYPE);
                    if (notificationType != null)
                    {
                        switch (notificationType) {
                            case EXTRA_KEY_VALUE_SCANNER_STATUS:
                                // Change in scanner status occurred
                                String displayScannerStatusText = extras.getString(EXTRA_KEY_VALUE_NOTIFICATION_STATUS) +
                                        ", profile: " + extras.getString(EXTRA_KEY_VALUE_NOTIFICATION_PROFILE_NAME);
                                //Toast.makeText(getApplicationContext(), displayScannerStatusText, Toast.LENGTH_SHORT).show();
                                final TextView lblScannerStatus = (TextView) findViewById(R.id.tv_scanStatus);
                                lblScannerStatus.setText(displayScannerStatusText);
                                Log.i(LOG_TAG, "Scanner status: " + displayScannerStatusText);
                                break;

                            case EXTRA_KEY_VALUE_PROFILE_SWITCH:
                                // Received change in profile
                                // For future enhancement
                                break;

                            case  EXTRA_KEY_VALUE_CONFIGURATION_UPDATE:
                                // Configuration change occurred
                                // For future enhancement
                                break;
                        }
                    }
                }
            }
        }
    };

    private void displayScanResult(Intent initiatingIntent, String howDataReceived)
    {
        // store decoded data
        String decodedData = initiatingIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_data));
        // store decoder type
        String decodedLabelType = initiatingIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_label_type));

        final TextView lblScanData = (TextView) findViewById(R.id.tv_scanValue1);
        final TextView lblScanLabelType = (TextView) findViewById(R.id.tv_scanValue2);

        saveScanned(decodedLabelType, decodedData);

        lblScanData.setText(decodedData);
        lblScanLabelType.setText(decodedLabelType);
    }

    private void saveScanned(String type, String value){

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(400);

        ApiInterface apiInterface = ApiClient.create();
        Call<BarcodeResponse> barcodeResponseCall;
        barcodeResponseCall = apiInterface.addBarcode(
                type,
                value
        );
        Log.wtf("URL Called", barcodeResponseCall.request().url() + "");
        barcodeResponseCall.enqueue(new Callback<BarcodeResponse>() {
            @Override
            public void onResponse(Call<BarcodeResponse> call, final Response<BarcodeResponse> response) {
                Log.d(TAG, "Response Body -> " + ((response != null && response.body() != null) ? response.body() : "<---"));
                if (response.isSuccessful()) {
                    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    toneGen1.startTone(ToneGenerator.TONE_PROP_BEEP,150);
                    Toast.makeText(MainActivity.this, "Result : " +value+ " berhasil di scan!", Toast.LENGTH_SHORT).show();
                    dataset.add(value + " Sudah Behasil discan!");
                    if (response.body().getBarcode() != null) {

                    }
                }else{
                    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    toneGen1.startTone(ToneGenerator.TONE_SUP_ERROR,150);
                    Toast.makeText(MainActivity.this, "Result : " +value+ " double scan!", Toast.LENGTH_SHORT).show();
                    dataset.add(value + " Double Scan!");
                }
                generateLogList(dataset);
            }

            @Override
            public void onFailure(Call<BarcodeResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this,t.getMessage(),Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, t.getMessage());
            }
        });
    }

    private void registerReceivers() {

        Log.d(LOG_TAG, "registerReceivers()");

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_RESULT_NOTIFICATION);   // for notification result
        filter.addAction(ACTION_RESULT);                // for error code result
        filter.addCategory(Intent.CATEGORY_DEFAULT);    // needed to get version info

        // register to received broadcasts via DataWedge scanning
        filter.addAction(getResources().getString(R.string.activity_intent_filter_action));
        filter.addAction(getResources().getString(R.string.activity_action_from_service));
        registerReceiver(myBroadcastReceiver, filter);
    }

    public void unRegisterScannerStatus() {
        Log.d(LOG_TAG, "unRegisterScannerStatus()");
        Bundle b = new Bundle();
        b.putString(EXTRA_KEY_APPLICATION_NAME, getPackageName());
        b.putString(EXTRA_KEY_NOTIFICATION_TYPE, EXTRA_KEY_VALUE_SCANNER_STATUS);
        Intent i = new Intent();
        i.setAction(ACTION);
        i.putExtra(EXTRA_UNREGISTER_NOTIFICATION, b);
        this.sendBroadcast(i);
    }

    private void sendDataWedgeIntentWithExtra(String action, String extraKey, Bundle extras)
    {
        Intent dwIntent = new Intent();
        dwIntent.setAction(action);
        dwIntent.putExtra(extraKey, extras);
        if (bRequestSendResult)
            dwIntent.putExtra(EXTRA_SEND_RESULT, "true");
        this.sendBroadcast(dwIntent);
    }

    private void sendDataWedgeIntentWithExtra(String action, String extraKey, String extraValue)
    {
        Intent dwIntent = new Intent();
        dwIntent.setAction(action);
        dwIntent.putExtra(extraKey, extraValue);
        if (bRequestSendResult)
            dwIntent.putExtra(EXTRA_SEND_RESULT, "true");
        this.sendBroadcast(dwIntent);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceivers();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(myBroadcastReceiver);
        unRegisterScannerStatus();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }
}