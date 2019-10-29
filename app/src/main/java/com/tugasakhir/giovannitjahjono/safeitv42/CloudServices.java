package com.tugasakhir.giovannitjahjono.safeitv42;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.cloudrail.si.CloudRail;
import com.cloudrail.si.exceptions.ParseException;
import com.cloudrail.si.interfaces.CloudStorage;
import com.cloudrail.si.services.Box;
import com.cloudrail.si.services.Dropbox;
import com.cloudrail.si.services.GoogleDrive;
import com.cloudrail.si.services.OneDrive;
import com.cloudrail.si.services.OneDriveBusiness;

import java.util.concurrent.atomic.AtomicReference;

public class CloudServices {
    //Set the license key of CloudRail API
    private final static String cloudeRailLicenseKey = "5a4615a4fd458621e42822d2";
    private final static CloudServices ourInstance = new CloudServices();

    //Prepare for the atomic reference. it's class of java that update a value
    private final AtomicReference<CloudStorage> dropbox = new AtomicReference<>();
    private final AtomicReference<CloudStorage> box = new AtomicReference<>();
    private final AtomicReference<CloudStorage> googledrive = new AtomicReference<>();
    private final AtomicReference<CloudStorage> onedrive = new AtomicReference<>();
    private Activity context = null;


    static CloudServices getInstance() {
        return ourInstance;
    }

    //Prepare for Dropbox. set the client id, client secret, and redirect url
    private void initDropbox() {

        Dropbox dropboxInit = new Dropbox(context,
                "ujz7ovt7gxpack4",
                "mpzr0ppe7tg8p49",
                "https://auth.cloudrail.com/com.tugasakhir.giovannitjahjono.safeitv42",
                "someState");
        dropboxInit.useAdvancedAuthentication();
        //update the dropbox atomic reference value with the new value
        dropbox.set(dropboxInit);

    }

    //Prepare for Google Drive. set the client id and redirect url
    private void initGoogleDrive() {
        GoogleDrive googleDriveInit = new GoogleDrive(context,
                "21823897176-elss2k7cdftgbr24qs9gi4ij6vo5qfb5.apps.googleusercontent.com",
                "",
                "com.tugasakhir.giovannitjahjono.safeitv42:/oauth2redirect",
                "");
        googledrive.set(googleDriveInit);
        //update the Google Drive atomic reference value with the new value
        ((GoogleDrive) googledrive.get()).useAdvancedAuthentication();
    }

    //Prepare for Box. set the client id, client secret, and redirect url
    private void initBox() {
        Box boxInit = new Box(context,
                "pnldm1jyk5lv0eou67w5i6oga78q56ig",
                "1SAvRE4fT6N6A9UzbPfc1gxBoqYMIucV",
                "https://auth.cloudrail.com/com.tugasakhir.giovannitjahjono.safeit4",
                "someState");
        //update the Box atomic reference value with the new value
        box.set(boxInit);
    }

    //Prepare for Ondrive. set the client id, client secret
    private void initOneDrive() {

        OneDrive oneDriveInit = new OneDrive(context,
                "a17d160f-850d-4256-83e5-2f42cd9d8334",
                "ieyugVZU410|-ozXHKK07}#");
        onedrive.set(oneDriveInit);

        //OneDrive oneDriveInit = new OneDrive(context,"f5756d50-603c-4aec-986f-6220a9114838","zwbqOTTN21#_%#xlbKGJ725");
        //onedrive.set(oneDriveInit);
    }

    void clearAll(Activity context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        dropbox.get().logout();
        googledrive.get().logout();
        box.get().logout();
        onedrive.get().logout();

        dropbox.set(null);
        googledrive.set(null);
        box.set(null);
        onedrive.set(null);

        sharedPreferences.edit().clear().commit();
    }
    //Public method, work like constructor without make a new class model
    void prepare(Activity context) {
        this.context = context;
        CloudRail.setAppKey(cloudeRailLicenseKey);
        this.initDropbox();
        this.initGoogleDrive();
        this.initBox();
        this.initOneDrive();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        //String persistent will save the authentication user to stay login when the app closed
        try {
            String persistent = sharedPreferences.getString("dropboxPersistent", null);
            if (persistent != null){
                dropbox.get().loadAsString(persistent);
            }
            persistent = sharedPreferences.getString("googledrivePersistent", null);
            if (persistent != null){
                googledrive.get().loadAsString(persistent);
            }
            persistent = sharedPreferences.getString("boxPersistent", null);
            if (persistent != null){
                box.get().loadAsString(persistent);
            }
            persistent = sharedPreferences.getString("onedrivePersistent", null);
            if (persistent != null){
                onedrive.get().loadAsString(persistent);
            }
        } catch (ParseException e) {}
    }

    //This method use to get the cloud storage service depend on the parameter and return it
    CloudStorage getService(int service) {
        AtomicReference<CloudStorage> ret = new AtomicReference<>();
        switch (service) {
            case 1:
                ret = this.dropbox;
                break;
            case 2:
                ret = this.googledrive;
                break;
            case 3:
                ret = this.box;
                break;
            case 4:
                ret = this.onedrive;
                break;
            default:
                throw new IllegalArgumentException("Unknown service!");
        }
        return ret.get();
    }

    //this method use to save the persistent into sharedPreference when user login into their cloud storage account
    void storePersistent() {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("dropboxPersistent", dropbox.get().saveAsString());
            editor.putString("boxPersistent", box.get().saveAsString());
            editor.putString("googledrivePersistent", googledrive.get().saveAsString());
            editor.putString("onedrivePersistent", onedrive.get().saveAsString());
            editor.apply();
        } catch (Exception e){}

    }
}
