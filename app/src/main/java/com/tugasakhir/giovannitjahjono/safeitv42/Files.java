package com.tugasakhir.giovannitjahjono.safeitv42;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudrail.si.interfaces.CloudStorage;
import com.cloudrail.si.types.CloudMetaData;
import com.cloudrail.si.types.SpaceAllocation;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.tugasakhir.giovannitjahjono.safeitv42.RSA2048.buildKeyPair;

/**
 * Created by Giovanni Tjahjono on 2/3/2018.
 */

public class Files extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SERVICE = "service";
    private static final int FILE_SELECT = 0;

    public String sortType = "";
    private int currentService;
    private ListView list = null;
    private String currentPath;
    private View selectedItem;
    private ProgressBar spinner;
    public SharedPreferences sp;
    public SharedPreferences.Editor spe;


    private byte[] keyAES;
    private byte[] publicKeyRSA;
    private byte[] privateKeyRSA;

    private Context context;
    private Activity activity = null;

    private String pasteMethod;
    private String pasteFromPath;
    private String pasteFileName;

    SearchView searchView;

    public Files() {
        // Required empty public constructor
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case FILE_SELECT: {
                if(resultCode == Activity.RESULT_OK) {
                    final Uri uri = data.getData();
                    final String name;
                    String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
                    Cursor metaCursor = getOwnActivity().getContentResolver().query(uri, projection, null, null, null);

                    if(metaCursor == null) {
                        throw new RuntimeException("Could not read file name.");
                    }

                    try {
                        metaCursor.moveToFirst();
                        name = metaCursor.getString(0);
                    } finally {
                        metaCursor.close();
                    }

                    this.uploadItem(name, uri);
                }
                break;
            }
            default: super.onActivityResult(requestCode, resultCode, data);
        }
    }
    public static Files newInstance(int service) {
        Files fragment = new Files();
        //Memanggil fragment secara dimanis
        Bundle args = new Bundle();
        args.putInt(ARG_SERVICE, service);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            currentService = getArguments().getInt(ARG_SERVICE);
            sp = PreferenceManager.getDefaultSharedPreferences(context);
            //searchView = (SearchView) getOwnActivity().findViewById(R.id.action_search);


        }
        setHasOptionsMenu(true);
    }

    public void refresh(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateList();

            }
        }, 3000);
    }

    public void AtoZ(){
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Set the main view
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        final SwipeRefreshLayout swipe = (SwipeRefreshLayout) v.findViewById(R.id.swipeContainer);
        final TextView serviceName = (TextView) v.findViewById(R.id.service_name);
        final TextView allocationSpace = (TextView) v.findViewById(R.id.allocation);
        final Integer currentServiceName = currentService;
        //Set the pull to refresh
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipe.setHapticFeedbackEnabled(true);
                updateList();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Get allocation space and name from cloud
                        final SpaceAllocation alloc = getService().getAllocation();
                        final String username = getService().getUserName();

                        getOwnActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String used = getSizeString(alloc.getUsed());
                                String total = getSizeString(alloc.getTotal());
                                allocationSpace.setText(used + " used of " + total);
                                //Set username + cloud storage account
                                switch (currentServiceName) {
                                    case 1: {
                                        serviceName.setText(username + "'s Dropbox:");
                                        break;
                                    }
                                    case 2: {
                                        serviceName.setText(username + "'s GoogleDrive:");
                                        break;
                                    }
                                    case 3: {
                                        serviceName.setText(username + "'s Box:");
                                        break;
                                    }
                                    case 4: {
                                        serviceName.setText(username + "'s OneDrive:");
                                        break;
                                    }
                                }
                            }
                        });
                    }
                }).start();
                swipe.setRefreshing(false);
            }
        });
        FloatingActionButton fab2 = (FloatingActionButton) v.findViewById(R.id.fab);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(Intent.createChooser(intent, "Select MainActivity File to Upload"), FILE_SELECT);
                } catch(android.content.ActivityNotFoundException e) {
                    Toast.makeText(context, "Please install MainActivity file manager to perform this action!", Toast.LENGTH_LONG).show();
                }
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                final SpaceAllocation alloc = getService().getAllocation();
                final String username = getService().getUserName();

                getOwnActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String used = getSizeString(alloc.getUsed());
                        String total = getSizeString(alloc.getTotal());
                        allocationSpace.setText(used + " used of " + total);
                        switch (currentServiceName) {
                            case 1: {
                                serviceName.setText(username + "'s Dropbox:");
                                break;
                            }
                            case 2: {
                                serviceName.setText(username + "'s GoogleDrive:");
                                break;
                            }
                            case 3: {
                                serviceName.setText(username + "'s Box:");
                                break;
                            }
                            case 4: {
                                serviceName.setText(username + "'s OneDrive:");
                                break;
                            }
                        }
                    }
                });
            }
        }).start();

        //Set the list of file in view
        this.list = (ListView) v.findViewById(R.id.listView);
        this.list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, int position, long id) {
                //Start spinner to indicate process start
                startSpinner();
                LinearLayout ll = (LinearLayout) view;
                TextView tv = (TextView) ll.findViewById(R.id.txtListItemName);
                final String name = (String) tv.getText();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Clear the sharepreference that save metadata first
                        spe = sp.edit();
                        spe.remove("knackered_data").commit();
                        //Check the path
                        String next = currentPath;
                        if (!currentPath.equals("/")) {
                            next += "/";
                        }
                        next += name;
                        //Get metadata from database
                        final String url = "https://safeitdatabase.000webhostapp.com/getData.php?dataname=" + name +"&id_user_data=" + sp.getString("id_user", "")+"&service_data="+ currentService;
                        try {
                            //Communicate with web service
                            URL url1 = new URL(url);
                            HttpURLConnection con = (HttpURLConnection) url1.openConnection();
                            StringBuilder sb = new StringBuilder();
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                            String json;
                            while ((json = bufferedReader.readLine()) != null) {
                                sb.append(json);
                            }
                            String result = sb.toString().trim();
                            //Parse JSON to array, and the put it on sharePreference
                            JSONArray jsonArray = new JSONArray(result);
                            for (int i = 0; i < result.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);
                                //Store metadata from database to sharepreferences
                                spe = sp.edit();
                                spe.putString("id_data",String.valueOf(obj.getInt("id_data")));
                                spe.putString("dataname", obj.getString("dataname"));
                                spe.putString("knackered_data", obj.getString("knackered_data"));
                                spe.apply();
                            }
                        } catch (Exception e){
                            System.out.println(e.getMessage());
                        }
                        //Get metadata file from cloud
                        CloudMetaData info = getService().getMetadata(next);
                        //Checking process based on metadata, it's Folder or File
                        if (info.getFolder()) {
                            //If it's folder, that means open that folder, not download
                            setNewPath(next);
                        } else {
                            //Download data
                            InputStream data = getService().download(next);
                            //Set download destination directory
                            final File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Safe It File");
                            //If download directory named Safe It Download doesn't exist, create it
                            if (!path.exists()){
                                path.mkdir();
                            }
                            File file = new File(path, name);
                            try {
                                if (!file.exists()) {
                                    file.createNewFile();
                                } else {
                                    String[] filePlusExtension = name.split("\\.");
                                    String newName = "";
                                    String originalName = "";
                                    String fileExtension = "";
                                    originalName = filePlusExtension[0].toString();
                                    fileExtension = filePlusExtension[1].toString();

                                    Boolean safeNameNotFound = true;
                                    int numberFile = 1;
                                    while (safeNameNotFound) {
                                        newName = "(" + String.valueOf(numberFile) + ")";
                                        file = new File(path, originalName + newName + "." + fileExtension);
                                        if (!file.exists()) {
                                            file.createNewFile();
                                            safeNameNotFound = false;
                                        } else {
                                            safeNameNotFound = true;
                                            numberFile++;
                                        }
                                    }
                                }
                            } catch (Exception e){}
                            try {
                                //Set the file, if file doesn,t exist, create it
                                FileOutputStream stream = new FileOutputStream(file, true);

                                int lenghtData = data.available();
                                byte[] contentInBytes = new byte[lenghtData];

                                String rsaPrivateKey = "";
                                String aesPublicKey = "";

                                //Get knackered data
                                String knackered_data = sp.getString("knackered_data", "");
                                //If knackered data is empty, it's means the file is not encrypted with Safe It application, so just download it
                                if(knackered_data.equals("")){
                                    InputStream is;
                                    byte[] buffer = new byte[8192];
                                    int bytesRead;
                                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                                    //Convert data to array of bytes
                                    while ((bytesRead = data.read(buffer)) != -1)
                                    {
                                        output.write(buffer, 0, bytesRead);
                                    }
                                    byte[] bytes = output.toByteArray();
                                    //Write file
                                    stream.write(bytes);
                                }
                                else {
                                    //seperate knackered data into 3 part of key, in this case, we just need 2 key, aes public key and rsa public key to decrypt
                                    aesPublicKey = knackered_data.substring(392, 736);
                                    rsaPrivateKey = knackered_data.substring(736, knackered_data.length());
                                    KeyFactory kf = KeyFactory.getInstance("RSA");
                                    RSA2048 rsa2048 = new RSA2048();
                                    AES256 aes256 = new AES256();

                                    //Transform RSA private key from String to PrivateKey
                                    PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.decode(rsaPrivateKey, Base64.DEFAULT));
                                    PrivateKey privKey = kf.generatePrivate(keySpecPKCS8);

                                    //Decrypt AES256 key
                                    byte[] secret = rsa2048.decrypt(privKey, Base64.decode(aesPublicKey, Base64.DEFAULT));
                                    String kunciAESSetelahDekrip = new String(secret);

                                    InputStream is;
                                    byte[] buffer = new byte[8192];
                                    int bytesRead;
                                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                                    //Convert file to array of bytes
                                    while ((bytesRead = data.read(buffer)) != -1)
                                    {
                                        output.write(buffer, 0, bytesRead);
                                    }
                                    byte[] bytes = output.toByteArray();

                                    //Decrypt the file
                                    AES256 decr = new AES256();
                                    byte[] decry = decr.decrypt(Base64.decode(kunciAESSetelahDekrip, Base64.DEFAULT), bytes);
                                    //Write it
                                    stream.write(decry);

                                }
                                //Close the connection
                                data.close();
                                stream.close();
                                //Show snackbar download complete
                                getOwnActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Snackbar.make(getView(), "Download complete and stored to Safe It Download in Download Directory", Snackbar.LENGTH_LONG).show();
                                    }
                                });
                                stopSpinner();
                            } catch (Exception e) {
                                stopSpinner();
                                getOwnActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Indicate failed to download
                                        Snackbar.make(getView(), "Download Failed", Snackbar.LENGTH_LONG).show();
                                    }
                                });
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
        });

        this.list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                selectedItem = view;
                PopupMenu popupMenu = new PopupMenu(getOwnActivity(), view);
                MenuInflater menuInflater = getOwnActivity().getMenuInflater();
                menuInflater.inflate(R.menu.pop_up_selected_item, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_delete: {
                                deleteFile();
                                return true;
                            }

                            case R.id.action_copy: {
                                copyItem();
                                return true;
                            }

                            case R.id.action_cut: {
                                cutItem();
                                return true;
                            }
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
                return true;
            }
        });

        this.spinner = (ProgressBar) v.findViewById(R.id.spinner);
        this.spinner.setVisibility(View.GONE);
        this.currentPath = "/";
        this.updateList();
        return v;
    }


    private String getSizeString(Long size) {
        String units[] = {"Bytes", "kB", "MB", "GB", "TB"};
        String unit = units[0];
        for (int i = 1; i < 5; i++) {
            if (size > 1024) {
                size /= 1024;
                unit = units[i];
            }
        }
        return size + unit;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        this.context = context;
        this.activity = context;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        TextView usernameDisplay = (TextView) getOwnActivity().findViewById(R.id.txtUsernameLogin);
        TextView emailDisplay = (TextView) getOwnActivity().findViewById(R.id.txtEmailLogin);
        try{
            usernameDisplay.setText(sp.getString("username","unknown"));
            emailDisplay.setText(sp.getString("email_user","unknown"));
        }
        catch (Exception e){

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_folder: {
                clickCreateFolder();
                break;
            }
            case R.id.paste: {
                pasteItem();
                break;
            }
            case R.id.action_search: {
                getOwnActivity().onSearchRequested();
                break;
            }
            case R.id.log_out: {
                //Log out process start with clear the share preference
                sp.edit().clear().commit();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //concat by log out cloud storage
                        CloudServices.getInstance().clearAll(getOwnActivity());
                    }
                }).start();
                //start intent to go to login activity
                Intent intent = new Intent(context, LoginActivity.class);
                //Clean up all activities
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //Start login activity
                startActivity(intent);
                getOwnActivity().finish();
                break;
            }
            case R.id.A_Z: {
                sortType = "A_Z";
                updateList();
                break;
            }
            case R.id.Z_A: {
                sortType = "Z_A";
                updateList();
                break;
            }
            case R.id.newest_modified: {
                sortType = "newest";
                updateList();
                break;
            }
            case R.id.oldest_modified: {
                sortType = "oldest";
                updateList();
                break;
            }
            case R.id.biggest: {
                sortType = "biggest";
                updateList();
                break;
            }
            case R.id.smallest: {
                sortType = "smallest";
                updateList();
                break;
            }
            case R.id.file_type: {
                sortType = "fileType";
                updateList();
                break;
            }
        }
        return true;
    }
    private void clickCreateFolder() {
        //First, if user choose create folder on the top right, that will appear alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        //Enter the folder name
        builder.setTitle("Enter Folder Name");
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startSpinner();
                createFolder(input.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void createFolder(String name) {
        String next = currentPath;
        if(!currentPath.equals("/")) {
            next += "/";
        }
        next += name;
        final String finalNext = next;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getService().createFolder(finalNext);
                    updateList();
                } catch (Exception e){
                    Snackbar.make(getView(), "Folder or File with that name is already exist, try different name", Snackbar.LENGTH_LONG).show();
                }

            }
        }).start();
        stopSpinner();
    }

    public boolean onBackPressed() {
        if(this.currentPath.equals("/")) {
            return true;
        } else {
            int pos = this.currentPath.lastIndexOf("/");
            String newPath = "/";
            if(pos != 0) {
                newPath = this.currentPath.substring(0, pos);
            }
            this.setNewPath(newPath);
        }
        return false;
    }

    private CloudStorage getService() {
        return CloudServices.getInstance().getService(this.currentService);
    }
    private CloudStorage getService(int service) {
        return CloudServices.getInstance().getService(service);
    }

    public void updateList() {
        this.startSpinner();
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Get the list of files in path
                List<CloudMetaData> items = getService().getChildren(currentPath);
                switch (sortType){
                    case "Z_A":
                        Collections.sort(items, new Comparator<CloudMetaData>() {
                            @Override
                            public int compare(CloudMetaData o1, CloudMetaData o2) {
                                return o1.getName().compareTo(o2.getName());
                            }
                        });
                        break;
                    case "A_Z":
                        Collections.sort(items, new Comparator<CloudMetaData>() {
                            @Override
                            public int compare(CloudMetaData o1, CloudMetaData o2) {
                                return o2.getName().compareTo(o1.getName());
                            }
                        });
                        break;
                    case "oldest":
                        Collections.sort(items, new Comparator<CloudMetaData>() {
                            @Override
                            public int compare(CloudMetaData o1, CloudMetaData o2) {

                                return o1.getModifiedAt().compareTo(o2.getModifiedAt());
                            }
                        });
                        break;
                    case "newest":
                        Collections.sort(items, new Comparator<CloudMetaData>() {
                            @Override
                            public int compare(CloudMetaData o1, CloudMetaData o2) {

                                return o2.getModifiedAt().compareTo(o1.getModifiedAt());
                            }
                        });
                        break;
                    case "biggest":
                        Collections.sort(items, new Comparator<CloudMetaData>() {
                            @Override
                            public int compare(CloudMetaData o1, CloudMetaData o2) {
                                return o2.getSize() - o1.getSize();
                            }
                        });
                        break;
                    case "smallest":
                        Collections.sort(items, new Comparator<CloudMetaData>() {
                            @Override
                            public int compare(CloudMetaData o1, CloudMetaData o2) {
                                return o1.getSize() - o2.getSize();
                            }
                        });
                        break;
                    case "fileType":
                        Collections.sort(items, new Comparator<CloudMetaData>() {
                            @Override
                            public int compare(CloudMetaData o1, CloudMetaData o2) {
                                String[] filePlusExtension = o1.getName().split("\\.");
                                String fileExtension = filePlusExtension[filePlusExtension.length - 1];
                                String[] filePlusExtension2 = o2.getName().split("\\.");
                                String fileExtension2 = filePlusExtension2[filePlusExtension2.length - 1];
                                return fileExtension.compareTo(fileExtension2);
                            }
                        });
                        break;

                }
                final List<CloudMetaData> files = sortList(items);
                getOwnActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CloudListItemAdaptor listAdapter = new CloudListItemAdaptor(context, R.layout.item_main,files);
                        list.setAdapter(listAdapter);
                        stopSpinner();
                    }
                });
            }
        }).start();
    }

    public void search(final String search) {
        this.startSpinner();
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<CloudMetaData> items = getService().search(search);
                final List<CloudMetaData> files = sortList(items);

                getOwnActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CloudListItemAdaptor listAdapter = new CloudListItemAdaptor(context, R.layout.item_main, files);
                        list.setAdapter(listAdapter);
                        stopSpinner();
                    }
                });


            }
        }).start();
    }

    private void setNewPath(String path) {
        this.currentPath = path;
        this.updateList();
    }


    //Add folders or files to current path
    @TargetApi(Build.VERSION_CODES.N)
    private List<CloudMetaData> sortList(List<CloudMetaData> list) {
        List<CloudMetaData> folders = new ArrayList<>();
        List<CloudMetaData> files = new ArrayList<>();
        for(CloudMetaData cmd : list) {
            if(cmd == null) continue;

            if(cmd.getFolder()) {
                folders.add(cmd);
            } else {
                files.add(cmd);
            }
        }
        folders.addAll(files);
        return folders;
    }

    private void deleteFile() {
        this.startSpinner();
        TextView fileName = (TextView) this.selectedItem.findViewById(R.id.txtListItemName);
        final String name = (String) fileName.getText();
        CloudMetaData cloudMetaData = new CloudMetaData();
        cloudMetaData.setName(name);
        ArrayAdapter<CloudMetaData> adapter = (ArrayAdapter<CloudMetaData>) this.list.getAdapter();
        adapter.remove(cloudMetaData);

        new Thread(new Runnable() {
            @Override
            public void run() {
                String next = currentPath;
                if(!currentPath.equals("/")) {
                    next += "/";
                }
                next += name;
                getService().delete(next);
                updateList();
            }
        }).start();
    }

    private void copyItem() {
        TextView itemName = (TextView) this.selectedItem.findViewById(R.id.txtListItemName);
        //Get file name
        final String name = (String) itemName.getText();
        //Get path that file
        String path = currentPath;
        if(!currentPath.equals("/")) {
            path += "/";
        }
        path += name;
        pasteFromPath = path;
        pasteFileName = name;
        pasteMethod = "copy";
        //pasteItem method won't call right now because there's no paste destination yet
    }

    private void cutItem() {
        TextView itemName = (TextView) this.selectedItem.findViewById(R.id.txtListItemName);
        //Get file name
        final String name = (String) itemName.getText();
        //Get path that file
        String path = currentPath;
        if(!currentPath.equals("/")) {
            path += "/";
        }
        path += name;
        pasteFromPath = path;
        pasteFileName = name;
        pasteMethod = "cut";
        //pasteItem method won't call right now because there's no paste destination yet
    }
    private void pasteItem() {
        //Make sure each parameter exist
        if (pasteFromPath == null || pasteMethod == null || pasteFileName == null) {
            getOwnActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(getView(), "There's no file to paste", Snackbar.LENGTH_LONG).show();
                }
            });
            return;
        }
        this.startSpinner();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = currentPath;
                List<CloudMetaData> cmds = getService().getChildren(path);
                if(!currentPath.equals("/")) {
                    path += "/";
                }
                String[] pasteFileNameParts = pasteFileName.split("\\.", 2);
                pasteFileNameParts[0] = path + pasteFileNameParts[0];
                String to = findSafeName(cmds, pasteFileNameParts[0], pasteFileNameParts[1], 0);

                switch (pasteMethod) {
                    case "cut":
                        getService().move(pasteFromPath, to);
                        break;
                    case "copy":
                        System.out.println(to);
                        getService().copy(pasteFromPath, to);
                        break;
                    default:
                        break;
                }
                updateList();
            }
        }).start();
    }

    private String findSafeName(List<CloudMetaData> cmds, String fileNameStart, String fileNameEnd, int i) {
        String checkName = fileNameStart;
        //If file exist, add (i) behind
        if (i!=0) {
            checkName += "(" + i + ")";
        }
        checkName += "." + fileNameEnd;
        for (CloudMetaData cmd : cmds) {
            if (checkName.equals(cmd.getPath())) {
                return findSafeName(cmds, fileNameStart, fileNameEnd, i+1);
            }
        }
        return checkName;
    }

    //Prepare variable to save encrypted data
    byte [] incrept;
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void uploadItem(final String name, final Uri uri) {
        //start spinner to indicate process is running

        startSpinner();


        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] byteData = null;
                InputStream fs = null;
                byte[] inarry = null;
                final File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                final String filePath = path.getAbsolutePath();
                AssetManager am = context.getAssets();
                try {
                    //Get the file from local directory
                    fs = getOwnActivity().getContentResolver().openInputStream(uri);
                    //Get the file lenght from InputStream via .available()
                    int lenghtByteData = fs.available();
                    //Set the lenght array of byteData as length as lenghtByteData
                    byteData = new byte[lenghtByteData];
                    int bytesRead;
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    //write byteArrayOutputStream with byteData

                    while ((bytesRead = fs.read(byteData)) != -1)
                    {
                        output.write(byteData, 0, bytesRead);
                    }
                    String pathFinal = null;
                    //set innarry (variable array of byte data)
                    inarry = output.toByteArray();

                    //Initianlize Encryption Class
                    AES256 aes256 = new AES256();
                    RSA2048 rsa2048 = new RSA2048();

                    //Make AES256 Key
                    aes256.KeyMaker();
                    String aesKey = aes256.aesKeyString;
                    System.out.println(aesKey);
                    //Encrypt file with aesKey
                    incrept = aes256.encrypt(Base64.decode(aesKey, Base64.DEFAULT), inarry);

                    //Prepare the temporary encrypted folder directory
                    File folder = new File(Environment.getExternalStorageDirectory() +
                            File.separator + "safeittemporaryfolder");
                    boolean success = true;
                    if (!folder.exists()) {
                        success = folder.mkdir();
                    }
                    if (success) {
                        pathFinal = folder.getAbsolutePath().toString();
                    }

                    //Get a pair RSA2048 key of public and private key
                    KeyPair keyPair = buildKeyPair();
                    PublicKey pubKey = keyPair.getPublic();
                    PrivateKey privateKey = keyPair.getPrivate();

                    //convert key to string to store keys into database
                    String publicKeyString = Base64.encodeToString(pubKey.getEncoded(), Base64.DEFAULT);
                    String privateKeyString = Base64.encodeToString(privateKey.getEncoded(), Base64.DEFAULT);

                    //Encrypt the AES256 key with RSA2048
                    byte[] encrypted = rsa2048.encrypt(pubKey, aesKey);
                    String result = Base64.encodeToString(encrypted, Base64.DEFAULT);

                    String id_user_data = sp.getString("id_user", "");
                    String dataname = name;
                    //combination key
                    String aes_public_key_data = publicKeyString + result + privateKeyString;
                    //Upload metadata to database
                    String url = "https://safeitdatabase.000webhostapp.com/inputData.php?id_user_data=" + id_user_data+"&dataname=" +dataname.replace(" ", "+")+"&knackered_data="+aes_public_key_data+"&service_data="+currentService;
                    postJSON(url);
                    //Store file to temporary folder
                    FileOutputStream fos = new FileOutputStream(new File(pathFinal + "/" + name));
                    fos.write(incrept);
                    fos.close();
                    //Get file in temporary folder to upload it to database
                    fs = new FileInputStream(pathFinal + "/" + name);

                } catch (Exception e) {
                    //If failed above process, it will show snackbar
                    stopSpinner();
                    getOwnActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(getView(), "Failed upload file", Snackbar.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                String next = currentPath;
                if(!currentPath.equals("/")) {
                    next += "/";
                }
                next += name;
                //Upload File
                    getService().upload(next, fs, incrept.length, true);

                getOwnActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Snackbar will appear show that upload complete
                        updateList();
                        Snackbar.make(getView(), "Upload complete", Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }
    private void startSpinner() {
        getOwnActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setVisibility(View.VISIBLE);
            }
        });
    }

    private void stopSpinner() {
        getOwnActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setVisibility(View.GONE);
            }
        });
    }

    private Activity getOwnActivity() {
        if(this.activity == null) {
            return this.getActivity();
        } else {
            return this.activity;
        }
    }
    public void postJSON(final String urlWebService){
        class PostJSON extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //progressBar.setVisibility(View.VISIBLE);
            }


            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                try {
                    System.out.print(s);

                } catch (Exception e) {

                }
            }

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(urlWebService);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String result;
                    while ((result = bufferedReader.readLine()) != null) {
                        result = bufferedReader.readLine();
                    }
                    return result;

                } catch (Exception e) {
                    return null;
                }
            }
        }
        PostJSON post = new PostJSON();

        post.execute();
    }

    public void getJSON(final String urlWebService) {
        class GetJSON extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                try {
                    StoreMetaDataToSP(s);
                } catch (JSONException e) {
                    System.out.println("Failed parse JSON");
                }
            }

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(urlWebService);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();

                } catch (Exception e) {
                    return null;
                }
            }
        }
        GetJSON getJSON = new GetJSON();

        getJSON.execute();


    }
    private void loadIntoListView(String json) throws JSONException {
        JSONArray jsonArray = new JSONArray(json);
        try{
            for (int i = 0; i < json.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                spe = sp.edit();
                spe.putString("username",obj.getString("username"));
                spe.putString("email_user", obj.getString("email_user"));
                spe.putString("password_user", obj.getString("password_user"));
                spe.putString("id_user", obj.getString("id_user"));
                spe.putString("photo_user",obj.getString("photo_user"));
                spe.apply();
            }

        } catch (Exception e){}
    }

    private void StoreMetaDataToSP(String json2) throws JSONException {
        JSONArray jsonArray2 = new JSONArray(json2);
        try{
            for (int i = 0; i < json2.length(); i++) {
                JSONObject obj2 = jsonArray2.getJSONObject(i);

                spe = sp.edit();
                spe.putString("id_data",String.valueOf(obj2.getInt("id_data")));
                spe.putString("dataname", obj2.getString("dataname"));
                spe.putString("knackered_data", obj2.getString("knackered_data"));
                spe.apply();
            }

        } catch (Exception e){
            System.out.println("Failed " + e.getMessage());
        }
    }
}
