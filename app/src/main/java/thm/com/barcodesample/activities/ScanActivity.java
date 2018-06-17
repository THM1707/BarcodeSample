package thm.com.barcodesample.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.lang.reflect.Field;

import thm.com.barcodesample.R;
import thm.com.barcodesample.utils.Constants;

public class ScanActivity extends AppCompatActivity implements View.OnClickListener {
    private SurfaceView mCameraView;
    private BarcodeDetector mDetector;
    private CameraSource mCameraSource;
    private Camera mCamera;
    private boolean isFlash = false;
    private ImageButton mButtonFlash;

    public static void start(Context context) {
        Intent scanIntent = new Intent(context, ScanActivity.class);
        context.startActivity(scanIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        initViews();
        initComps();
    }

    private void initComps() {
        mDetector =
            new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build();
        mCameraSource = new CameraSource.Builder(this, mDetector)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedFps(35.0f)
            .setAutoFocusEnabled(true)
            .build();
        mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @SuppressLint("MissingPermission")
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    mCameraSource.start(mCameraView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCameraSource.release();
            }
        });
        mDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes != null && barcodes.size() > 0) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(Constants.BUNDLE_BARCODE, barcodes.valueAt(0));
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
            }
        });
    }

    private void initViews() {
        mCameraView = findViewById(R.id.sv_scan);
        findViewById(R.id.ib_picture).setOnClickListener(this);
        mButtonFlash = findViewById(R.id.ib_flash);
        mButtonFlash.setOnClickListener(this);
    }

    private void checkStoragePermission() {
        if (ActivityCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.REQUEST_STORAGE);
        } else {
            choseImage();
        }
    }

    private void choseImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, Constants.REQUEST_LOAD_IMG);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.REQUEST_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    choseImage();
                } else {
                    Toast.makeText(this, R.string.msg_permission_storage, Toast.LENGTH_SHORT)
                        .show();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_LOAD_IMG:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    try {
                        Bitmap bitmap =
                            MediaStore.Images.Media.
                                getBitmap(this.getContentResolver(), selectedImage);
                        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                        SparseArray<Barcode> barcodes;
                        barcodes = mDetector.detect(frame);
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra(Constants.BUNDLE_BARCODE, barcodes.valueAt(0));
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    } catch (IOException e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_picture:
                checkStoragePermission();
                break;
            case R.id.ib_flash:
                mCamera = getCamera(mCameraSource);
                if (mCamera != null) {
                    try {
                        Camera.Parameters param = mCamera.getParameters();
                        param.setFlashMode(!isFlash ? Camera.Parameters.FLASH_MODE_TORCH :
                            Camera.Parameters.FLASH_MODE_OFF);
                        mCamera.setParameters(param);
                        isFlash = !isFlash;
                        if (isFlash){
                            mButtonFlash.setImageResource(R.drawable.ic_flash);
                        } else {
                            mButtonFlash.setImageResource(R.drawable.ic_flash_off);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private static Camera getCamera(@NonNull CameraSource cameraSource) {
        Field[] declaredFields = CameraSource.class.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.getType() == Camera.class) {
                field.setAccessible(true);
                try {
                    Camera camera = (Camera) field.get(cameraSource);
                    if (camera != null) {
                        return camera;
                    }
                    return null;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return null;
    }

    @Override
    public void finish() {
        if (mCamera != null) {
            try {
                Camera.Parameters param = mCamera.getParameters();
                if (isFlash){
                    param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(param);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.finish();
    }
}
