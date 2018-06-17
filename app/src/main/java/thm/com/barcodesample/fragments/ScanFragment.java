package thm.com.barcodesample.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

import thm.com.barcodesample.R;
import thm.com.barcodesample.handlers.BarcodeHandler;
import thm.com.barcodesample.utils.Constants;
import thm.com.barcodesample.views.MyDialogFragment;

import static android.app.Activity.RESULT_OK;

public class ScanFragment extends Fragment implements MyDialogFragment.MyDialogListener {
    private static final String TAG = "ScanFragment";
    private AppCompatActivity mActivity;
    private SurfaceView mCameraView;
    private BarcodeDetector mDetector;
    private CameraSource mCameraSource;
    private BarcodeHandler mBarcodeHandler;
    private boolean isAllowed;
    private boolean isInit = true;

    public static ScanFragment newInstance(boolean allowed) {
        Bundle args = new Bundle();
        args.putBoolean(Constants.BUNDLE_IS_ALLOWED, allowed);
        ScanFragment fragment = new ScanFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initValues();
    }

    private void initValues() {
        mBarcodeHandler = new BarcodeHandler(mActivity, this);
        if (getArguments() != null) {
            isAllowed = getArguments().getBoolean(Constants.BUNDLE_IS_ALLOWED);
        }
    }

    @Override
    public void onAttach(Context context) {
        mActivity = (AppCompatActivity) context;
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
            R.layout.fragment_scan, container, false);
        initViews(rootView);
        if (isAllowed) {
            initComponents();
        }
        return rootView;
    }

    private void initViews(ViewGroup rootView) {
        mCameraView = rootView.findViewById(R.id.sv_scan);
        if (!isAllowed) {
            mCameraView.setVisibility(View.INVISIBLE);
        }
        rootView.findViewById(R.id.ib_picture).setOnClickListener(v -> checkStoragePermission());
    }

    @SuppressLint("MissingPermission")
    private void initComponents() {
        mDetector =
            new BarcodeDetector.Builder(mActivity).setBarcodeFormats(Barcode.QR_CODE).build();
        mCameraSource = new CameraSource.Builder(mActivity, mDetector)
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
                Log.d(TAG, "surfaceDestroyed: ");
                mCameraSource.stop();
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
                    mBarcodeHandler.readBarcode(barcodes.valueAt(0));
                    mCameraSource.stop();
                }
            }
        });
    }

    private void checkStoragePermission() {
        if (ActivityCompat.checkSelfPermission(mActivity,
            Manifest.permission.READ_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity,
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
                    Toast.makeText(mActivity, R.string.msg_permission_storage, Toast.LENGTH_SHORT)
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
                                getBitmap(mActivity.getContentResolver(), selectedImage);
                        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                        SparseArray<Barcode> barcodes;
                        barcodes = mDetector.detect(frame);
                        mBarcodeHandler.readBarcode(barcodes.valueAt(0));
                    } catch (IOException e) {
                        Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case Constants.REQUEST_BROWSER:
                Log.d(TAG, "onActivityResult: ");
                startCamera();
                break;
        }
    }

    public void stopCamera() {
        mCameraSource.stop();
    }

    @SuppressLint("MissingPermission")
    public void startCamera() {
        try {
            mCameraSource.start(mCameraView.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView: ");
        super.onDestroyView();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: ");
        super.onStop();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
    }
}