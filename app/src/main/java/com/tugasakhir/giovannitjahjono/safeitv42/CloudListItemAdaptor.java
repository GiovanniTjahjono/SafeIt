package com.tugasakhir.giovannitjahjono.safeitv42;

import android.content.Context;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cloudrail.si.interfaces.CloudStorage;
import com.cloudrail.si.types.CloudMetaData;

import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CloudListItemAdaptor extends ArrayAdapter<CloudMetaData>{
    //Prepare required variable
    private List<CloudMetaData> data;
    private CloudStorage service;

    //Prepare the Adaptor
    public CloudListItemAdaptor(Context context, int resource, List<CloudMetaData> objects) {
        super(context, resource, objects);
        this.data = objects;
    }
    public CloudListItemAdaptor(Context context, int resource, List<CloudMetaData> objects, CloudStorage service) {
        super(context, resource, objects);
        this.data = objects;
        this.service = service;
    }

    //Method to convert size unit, default set as byte
    private String getSizeString(Long size) {
        String units[] = {"Bytes", "KB", "MB", "GB", "TB"};
        String unit = units[0];
        for (int i = 1; i < 5; i++) {
            if (size > 1024) {
                size /= 1024;
                unit = units[i];
            }
        }

        return size + " " + unit;
    }

    @Override
    public void remove(CloudMetaData object) {
        String target = object.getName();

        for(int i = 0; i < this.data.size(); ++i) {
            CloudMetaData cloudMetaData = this.data.get(i);
            if(cloudMetaData.getName().equals(target)) {
                this.data.remove(i);
                break;
            }
        }
    }

    //Prepare for the View
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        // Check to see if the view is null. if so, we have to inflate it.
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.item_main, null);
        }
        final CloudMetaData cmd = this.data.get(position);

        //Check metadata available or not
        if(cmd != null) {
            //Set the image and file size in List
            final ImageView img = (ImageView) v.findViewById(R.id.icon);
            final TextView sizeFile = (TextView) v.findViewById(R.id.txtSize);
            //Check the image null or not
            if(img != null) {
                //if the the metadata getFolder = true, it's mean the data is a folder
                if(cmd.getFolder()) {
                    img.setImageResource(R.drawable.ic_file_folder_blue);
                    sizeFile.setText("Folder");

                //If the getFolder return false, it's mean the data is a file, and every single extension file will set by the extension image that prepared
                } else {
                    try{
                        String fileName = cmd.getName();
                        String fileSize = getSizeString((long) cmd.getSize());
                        //Set the file size
                        sizeFile.setText(fileSize);
                        //Get the extension
                        String[] filePlusExtension = fileName.split("\\.");
                        String fileExtension = "";
                        if(filePlusExtension.length > 2){
                            fileExtension = filePlusExtension[filePlusExtension.length - 1].toString();
                        }

                        else {
                            fileExtension = filePlusExtension[1].toString();
                        }
                        //Set the icon file based on its extension
                        switch (fileExtension.toUpperCase()){
                            case "ZIP":
                                img.setImageResource(R.drawable.icon_file_zip);
                                break;
                            case "RAR":
                                img.setImageResource(R.drawable.icon_file_rar);
                                break;
                            case "JPG":
                                img.setImageResource(R.drawable.icon_file_pic);
                                break;
                            case "PNG":
                                img.setImageResource(R.drawable.icon_file_pic);
                                break;
                            case "JPEG":
                                img.setImageResource(R.drawable.icon_file_pic);
                                break;
                            case "RAW":
                                img.setImageResource(R.drawable.icon_file_pic);
                                break;
                            case "MP3":
                                img.setImageResource(R.drawable.icon_file_music);
                                break;
                            case "M4A":
                                img.setImageResource(R.drawable.icon_file_music);
                                break;
                            case "FLAC":
                                img.setImageResource(R.drawable.icon_file_music);
                                break;
                            case "WAV":
                                img.setImageResource(R.drawable.icon_file_music);
                                break;
                            case "AIFF":
                                img.setImageResource(R.drawable.icon_file_music);
                                break;
                            case "DOCX":
                                img.setImageResource(R.drawable.icon_file_doc);
                                break;
                            case "DOC":
                                img.setImageResource(R.drawable.icon_file_doc);
                                break;
                            case "XLS":
                                img.setImageResource(R.drawable.icon_file_xls);
                                break;
                            case "XLSX":
                                img.setImageResource(R.drawable.icon_file_xls);
                                break;
                            case "PPT":
                                img.setImageResource(R.drawable.icon_file_pptpng);
                                break;
                            case "PPTX":
                                img.setImageResource(R.drawable.icon_file_pptpng);
                                break;
                            case "PSD":
                                img.setImageResource(R.drawable.icon_file_psd);
                                break;
                            case "AI":
                                img.setImageResource(R.drawable.icon_file_ai);
                                break;
                            case "TXT":
                                img.setImageResource(R.drawable.icon_file_txt);
                                break;
                            case "GIF":
                                img.setImageResource(R.drawable.icon_file_pic);
                                break;
                            case "PDF":
                                img.setImageResource(R.drawable.icon_file_pdf);
                                break;
                            case "AVI":
                                img.setImageResource(R.drawable.icon_file_video);
                                break;
                            case "MP4":
                                img.setImageResource(R.drawable.icon_file_video);
                                break;
                            case "MKV":
                                img.setImageResource(R.drawable.icon_file_video);
                                break;
                            default:
                                img.setImageResource(R.drawable.icon_file_etc);
                                break;
                        }
                    }
                    catch (Exception e) {
                        //If the extension file doesn't prepared, set the image by icon_file_etc
                        img.setImageResource(R.drawable.icon_file_etc);
                    }
                }
            }
            //Set the list in main view by all file or folder in cloud storage
            TextView eachListingFile = (TextView) v.findViewById(R.id.txtListItemName);
            eachListingFile.setText(cmd.getName());
        }
        return v;
    }
}
