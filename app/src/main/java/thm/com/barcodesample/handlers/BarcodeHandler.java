package thm.com.barcodesample.handlers;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;

import java.util.Calendar;

import thm.com.barcodesample.fragments.ScanFragment;
import thm.com.barcodesample.views.MyDialogFragment;

import static android.content.Context.WIFI_SERVICE;

public class BarcodeHandler {
    private AppCompatActivity mActivity;
    private ScanFragment mFragment;

    public BarcodeHandler(AppCompatActivity activity, ScanFragment fragment) {
        mActivity = activity;
        mFragment = fragment;
    }

    public BarcodeHandler(AppCompatActivity activity) {
        mActivity = activity;
    }

    public void readBarcode(Barcode barcode) {
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
                DialogFragment dialog = MyDialogFragment.newInstance(barcode.rawValue);
                dialog.show(mActivity.getSupportFragmentManager(), "Text");
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
            (WifiManager) mActivity.getApplicationContext().getSystemService(WIFI_SERVICE);
        int networkId = wifiManager.addNetwork(wfc);
        if (networkId != -1) {
            if (!wifiManager.enableNetwork(networkId, true)) {
                Toast.makeText(mActivity, "Can't connect", Toast.LENGTH_SHORT).show();
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
        mActivity.startActivity(calendarIntent);
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
        mActivity.startActivity(mapIntent);
    }

    private void addContact(Barcode barcode) {
        Intent contactIntent = new Intent(ContactsContract.Intents.Insert.ACTION);
        contactIntent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
        contactIntent.
            putExtra(ContactsContract.Intents.Insert.NAME, barcode.contactInfo.name.formattedName);
        if (barcode.contactInfo.phones.length > 0) {
            contactIntent.putExtra(ContactsContract.Intents.Insert.PHONE, barcode
                .contactInfo.phones[0].number);
        }
        contactIntent
            .putExtra(ContactsContract.Intents.Insert.COMPANY, barcode.contactInfo.organization);
        if (barcode.contactInfo.emails.length > 0) {
            contactIntent.putExtra(ContactsContract.Intents.Insert.EMAIL, barcode.contactInfo
                .emails[0].address);
        }
        contactIntent
            .putExtra(ContactsContract.Intents.Insert.COMPANY, barcode.contactInfo.organization);
        mActivity.startActivity(contactIntent);
    }

    private void startDial(Barcode barcode) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(barcode.rawValue));
        mActivity.startActivity(browserIntent);
    }

    private void openUrl(Barcode barcode) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(barcode.rawValue));
        mActivity.startActivity(browserIntent);
    }

    private void openEmail(Barcode barcode) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        String mailUri = "mailto:" + barcode.email.address
            + "?subject=" + Uri.encode(barcode.email.subject)
            + "&body=" + Uri.encode(barcode.email.body);
        emailIntent.setData(Uri.parse(mailUri));
        mActivity.startActivity(emailIntent);
    }

    private boolean isPackageExisted(String targetPackage) {
        PackageManager pm = mActivity.getPackageManager();
        try {
            pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }
}
