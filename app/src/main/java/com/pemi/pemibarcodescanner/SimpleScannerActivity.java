package com.pemi.pemibarcodescanner;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.pemi.pemibarcodescanner.model.BarcodeResponse;
import com.pemi.pemibarcodescanner.network.ApiClient;
import com.pemi.pemibarcodescanner.network.ApiInterface;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.os.Vibrator;

public class SimpleScannerActivity extends BaseScannerActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    private CoordinatorLayout coordinatorLayout;
    private RecyclerView recyclerView;
    private TextView noScannerView;


    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_simple_scanner);
        setupToolbar();

        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.content_frame);
        mScannerView = new ZXingScannerView(this);
        contentFrame.addView(mScannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
//        Toast.makeText(this, "Contents : " + rawResult.getText() +
//                ", Format : " + rawResult.getBarcodeFormat().toString(), Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "Berhasil Scan : " + rawResult.getText(), Toast.LENGTH_SHORT).show();
        saveScanned(rawResult.getBarcodeFormat().toString(), rawResult.getText());
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 200 milliseconds
        v.vibrate(200);
        // Note:
        // * Wait 2 seconds to resume the preview.
        // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
        // * I don't know why this is the case but I don't have the time to figure out.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScannerView.resumeCameraPreview(SimpleScannerActivity.this);
            }
        }, 2000);
    }

    private void saveScanned(String type, String value){

        ApiInterface apiInterface = ApiClient.create();
        final Call<BarcodeResponse> barcodeResponseCall;
        barcodeResponseCall = apiInterface.addBarcode(
                type,
                value
        );
        barcodeResponseCall.enqueue(new Callback<BarcodeResponse>() {
            @Override
            public void onResponse(Call<BarcodeResponse> call, Response<BarcodeResponse> response) {
                if(response.body() != null) {
                    if (response.body().getBarcode() != null) {
                        Toast.makeText(SimpleScannerActivity.this, "Berhasil", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<BarcodeResponse> call, Throwable t) {

            }
        });

    }
}