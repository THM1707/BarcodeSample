package thm.com.barcodesample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.ContactsContract.Intents;
import android.provider.ContactsContract.RawContacts;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_LOAD_IMG = 1;
    private static final int REQUEST_SCAN = 0;
    private static final int REQUEST_CAMERA = 101;
    private static final int REQUEST_STORAGE = 102;
    private static final String TAG = "MainActivity";
    private ImageView mImageQR;
    private TextView mTextDisplay;
    private BarcodeDetector mDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setDetector();
    }

    private void setDetector() {
        mDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode
            .QR_CODE).build();
        if (!mDetector.isOperational()) {
            mTextDisplay.setText(R.string.err_detector);
        }
    }

    private void initViews() {
        mImageQR = findViewById(R.id.image_qr);
        mTextDisplay = findViewById(R.id.tx_content);
        findViewById(R.id.bt_chose).setOnClickListener(this);
        findViewById(R.id.bt_scan).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_chose:
                if (mImageQR.getVisibility() == View.GONE) {
                    mImageQR.setVisibility(View.VISIBLE);
                }
                if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE);
                } else {
                    choseImage();
                }
                break;
            case R.id.bt_scan:
                if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
                } else {
                    startScan();
                }
                break;
            default:
                break;
        }
    }

    private void startScan() {
        if (mImageQR.getVisibility() == View.VISIBLE) {
            mImageQR.setVisibility(View.GONE);
        }
        Intent scanIntent = new Intent(this, ScanActivity.class);
        startActivityForResult(scanIntent, REQUEST_SCAN);
    }

    private void choseImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_LOAD_IMG:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    try {
                        Bitmap bitmap =
                            MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                        mImageQR.setImageBitmap(bitmap);
                        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                        SparseArray<Barcode> barcodes;
                        barcodes = mDetector.detect(frame);
                        if (barcodes.size() > 0) {
                            Barcode barcode = barcodes.valueAt(0);
                            switch (barcode.valueFormat) {
                                case Barcode.EMAIL:
                                    openEmail(barcode);
                                    break;
                                case Barcode.URL:
                                    openUrl(barcode);
                                    break;
                                case Barcode.PHONE:
                                    startDial(barcode);
                                    break;
                                case Barcode.CONTACT_INFO:
                                    addContact(barcode);
                                    break;
                                case Barcode.GEO:
                                    openGeo(barcode);
                                    break;
                                case Barcode.CALENDAR_EVENT:
                                    addCalendarEvent(barcode);
                                    break;
                                case Barcode.WIFI:
                                    connectWifi(barcode);
                                    break;
                                default:
                                    mTextDisplay.setText(barcode.rawValue);
                                    break;
                            }
                        } else {
                            mTextDisplay.setText(R.string.err_detec);
                        }
                    } catch (IOException e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case REQUEST_SCAN:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        Barcode barcode = data.getParcelableExtra("barcode");
                        switch (barcode.valueFormat) {
                            case Barcode.EMAIL:
                                openEmail(barcode);
                                break;
                            case Barcode.URL:
                                openUrl(barcode);
                                break;
                            case Barcode.PHONE:
                                startDial(barcode);
                                break;
                            case Barcode.CONTACT_INFO:
                                addContact(barcode);
                                break;
                            case Barcode.GEO:
                                openGeo(barcode);
                                break;
                            case Barcode.CALENDAR_EVENT:
                                addCalendarEvent(barcode);
                                break;
                            case Barcode.WIFI:
                                connectWifi(barcode);
                                break;
                            default:
                                mTextDisplay.setText(barcode.rawValue);
                                break;
                        }
                    }
                }
                break;
        }
    }

    private void connectWifi(Barcode barcode) {
        String ssid = barcode.wifi.ssid;
        String password = barcode.wifi.password;
        WifiConfiguration wfc = new WifiConfiguration();
        wfc.SSID = "\"".concat(ssid).concat("\"");
        wfc.status = WifiConfiguration.Status.DISABLED;
        wfc.priority = 40;
        switch (barcode.wifi.encryptionType) {
            case Barcode.WiFi.WEP:
                wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                if (isHexString(password)) wfc.wepKeys[0] = password;
                else wfc.wepKeys[0] = "\"".concat(password).concat("\"");
                wfc.wepTxKeyIndex = 0;
                break;
            case Barcode.WiFi.OPEN:
                wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                wfc.allowedAuthAlgorithms.clear();
                wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                break;
            case Barcode.WiFi.WPA:
                wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                wfc.preSharedKey = "\"".concat(password).concat("\"");
                break;
        }
        WifiManager wifiManager =
            (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        int networkId = wifiManager.addNetwork(wfc);
        if (networkId != -1) {
            if (!wifiManager.enableNetwork(networkId, true)) {
                Toast.makeText(this, "Can't connect", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static boolean isHexString(String string) {
        try {
            Long.parseLong(string, 16);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private void addCalendarEvent(Barcode barcode) {
        String description = barcode.calendarEvent.description;
        String organizer = barcode.calendarEvent.organizer;
        String evenLocation = barcode.calendarEvent.location;
        int startYear = barcode.calendarEvent.start.year;
        int startMonth = barcode.calendarEvent.start.month;
        int startDay = barcode.calendarEvent.start.day;
        int startHour = barcode.calendarEvent.start.hours;
        int startMinute = barcode.calendarEvent.start.minutes;
        int startSecond = barcode.calendarEvent.start.seconds;
        int endYear = barcode.calendarEvent.end.year;
        int endMonth = barcode.calendarEvent.end.month;
        int endDay = barcode.calendarEvent.end.day;
        int endHour = barcode.calendarEvent.end.hours;
        int endMinute = barcode.calendarEvent.end.minutes;
        int endSecond = barcode.calendarEvent.end.seconds;
        Calendar beginTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();
        beginTime.set(startYear, startMonth, startDay, startHour,
            startMinute, startSecond);
        endTime.set(endYear, endMonth, endDay, endHour, endMinute,
            endSecond);
        Intent calendarIntent = new Intent(Intent.ACTION_INSERT);
        calendarIntent.setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                beginTime.getTimeInMillis())
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                endTime.getTimeInMillis())
            .putExtra(CalendarContract.Events.DESCRIPTION, description)
            .putExtra(CalendarContract.Events.ORGANIZER, organizer)
            .putExtra(CalendarContract.Events.EVENT_LOCATION, evenLocation);
        startActivity(calendarIntent);
    }

    private void openGeo(Barcode barcode) {
        double lng = barcode.geoPoint.lng;
        double lat = barcode.geoPoint.lat;
        String location = lat + "," + lng;
        Uri gmmIntentUri = Uri.parse("geo:" + location);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        if (isPackageExisted("com.google.android.apps.maps")) {
            mapIntent.setPackage("com.google.android.apps.maps");
        }
        startActivity(mapIntent);
    }

    private void addContact(Barcode barcode) {
        Intent contactIntent = new Intent(Intents.Insert.ACTION);
        contactIntent.setType(RawContacts.CONTENT_TYPE);
        setupContact(contactIntent, barcode);
        startActivity(contactIntent);
    }

    private void startDial(Barcode barcode) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(barcode.rawValue));
        startActivity(browserIntent);
    }

    private void openUrl(Barcode barcode) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(barcode.rawValue));
        startActivity(browserIntent);
    }

    private void openEmail(Barcode barcode) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        String mailUri = "mailto:" + barcode.email.address
            + "?subject=" + Uri.encode(barcode.email.subject)
            + "&body=" + Uri.encode(barcode.email.body);
        emailIntent.setData(Uri.parse(mailUri));
        startActivity(emailIntent);
    }

    private void setupContact(Intent contactIntent, Barcode barcode) {
        contactIntent.
            putExtra(Intents.Insert.NAME, barcode.contactInfo.name.formattedName);
        if (barcode.contactInfo.phones.length > 0) {
            contactIntent.putExtra(Intents.Insert.PHONE, barcode
                .contactInfo.phones[0].number);
        }
        contactIntent.putExtra(Intents.Insert.COMPANY, barcode.contactInfo.organization);
        if (barcode.contactInfo.emails.length > 0) {
            contactIntent.putExtra(Intents.Insert.EMAIL, barcode.contactInfo
                .emails[0].address);
        }
        contactIntent.putExtra(Intents.Insert.COMPANY, barcode.contactInfo.organization);
    }

    public boolean isPackageExisted(String targetPackage) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startScan();
                } else {
                    Toast.makeText(this, R.string.msg_permission_cam, Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    choseImage();
                } else {
                    Toast.makeText(this, R.string.msg_permission_storage, Toast.LENGTH_SHORT)
                        .show();
                }
                break;
        }
    }
}
