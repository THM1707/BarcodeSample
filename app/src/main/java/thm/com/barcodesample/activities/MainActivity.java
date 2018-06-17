package thm.com.barcodesample.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import thm.com.barcodesample.R;
import thm.com.barcodesample.handlers.BarcodeHandler;
import thm.com.barcodesample.utils.Constants;
import thm.com.barcodesample.views.MyDialogFragment;

public class MainActivity extends AppCompatActivity implements MyDialogFragment.MyDialogListener {
    private BarcodeHandler mBarcodeHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
        mBarcodeHandler = new BarcodeHandler(this);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_CAMERA);
            } else {
                Intent scanIntent = new Intent(MainActivity.this, ScanActivity.class);
                startActivityForResult(scanIntent, Constants.REQUEST_SCAN);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.REQUEST_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent scanIntent = new Intent(MainActivity.this, ScanActivity.class);
                    startActivityForResult(scanIntent, Constants.REQUEST_SCAN);
                } else {
                    Toast.makeText(this, "Need camera permission", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_SCAN:
                if (resultCode == RESULT_OK) {
                    mBarcodeHandler.readBarcode(data.getParcelableExtra(Constants.BUNDLE_BARCODE));
                }
                break;
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
    }
}
