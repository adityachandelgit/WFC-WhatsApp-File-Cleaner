package com.retrospectivecreations.wfc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.appnext.appnextsdk.Appnext;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.startapp.android.publish.StartAppAd;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class VoiceNotesActivity extends ActionBarActivity {
    private boolean[] itemChecked;
    ArrayList<AudioProperties> arrayListAudioProps;
    ListView lv;
    CustomAdapter adapter;
    FloatingActionButton btnDelSelected, btnDelAll;
    TextView tvTotalSize, tvNumFiles;

    //Appnext appnext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_notes);

        /*appnext = new Appnext(this);
        appnext.setAppID("4da1b4c3-9a8a-4ea1-ac56-ead824352f21"); // Set your AppID
        appnext.cacheAd();
        appnext.showBubble(); // show the interstitial*/


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(30);
        getSupportActionBar().setTitle(" Manage Voice Notes");
        getSupportActionBar().setIcon(R.drawable.ic_action_whiteicon);

        tvTotalSize = (TextView) findViewById(R.id.voiceNotes_TotalSize_tv);
        tvNumFiles = (TextView) findViewById(R.id.voiceNotes_NumFile_tv);

        arrayListAudioProps = new ArrayList<>();
        new MyAsync().execute();

        lv = (ListView) findViewById(R.id.listView_VoiceNotes);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(VoiceNotesActivity.this).setTitle("Select Action!").setPositiveButton("Delete File", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File file = new File(arrayListAudioProps.get(position).getFilePath());
                        file.delete();
                        arrayListAudioProps.clear();
                        new MyAsync().execute();
                        Toast.makeText(VoiceNotesActivity.this, file.getName() + " Deleted!", Toast.LENGTH_SHORT).show();
                    }
                }).setNeutralButton("Play Audio", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = Uri.fromFile(new File(arrayListAudioProps.get(position).getFilePath()));
                        Intent intent = new Intent();
                        intent.setAction(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "audio/*");
                        startActivity(intent);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();

            }
        });

        btnDelSelected = (FloatingActionButton) findViewById(R.id.btn_delete_selected_VoiceNotes);
        btnDelSelected.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final ArrayList<File> fileToDelete = new ArrayList<>();
                for (int i = 0; i < itemChecked.length; i++) {
                    if (itemChecked[i]) {
                        File file = new File(arrayListAudioProps.get(i).getFilePath());
                        fileToDelete.add(file);
                    }
                }
                if (fileToDelete.size() <= 0) {
                    Toast.makeText(VoiceNotesActivity.this, "First select some files to delete!", Toast.LENGTH_LONG).show();
                } else {
                    final String totalFilesToDelete = String.valueOf(fileToDelete.size());
                    new AlertDialog.Builder(VoiceNotesActivity.this).setTitle("Delete " + totalFilesToDelete + " file(s)?").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for (File f : fileToDelete) {
                                f.delete();
                            }
                            arrayListAudioProps.clear();
                            new MyAsync().execute();
                            btnDelSelected.setColorNormal(getResources().getColor(R.color.material_blue_grey_800));
                            Toast.makeText(VoiceNotesActivity.this, totalFilesToDelete + " file(s) deleted!", Toast.LENGTH_SHORT).show();
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
                }
            }
        });

        btnDelAll = (FloatingActionButton) findViewById(R.id.btn_delete_all_VoiceNotes);
        btnDelAll.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final int totalFilesToDelete = arrayListAudioProps.size();
                if (totalFilesToDelete > 0) {
                    new AlertDialog.Builder(VoiceNotesActivity.this).setTitle("Delete all " + totalFilesToDelete + " file(s)?").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for (AudioProperties ap : arrayListAudioProps) {
                                File f = new File(ap.getFilePath());
                                f.delete();
                            }
                            arrayListAudioProps.clear();
                            new MyAsync().execute();
                            Toast.makeText(VoiceNotesActivity.this, totalFilesToDelete + " file(s) deleted!", Toast.LENGTH_SHORT).show();
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
                }
            }
        });

    }

    class CustomAdapter extends ArrayAdapter<AudioProperties> {
        public CustomAdapter(Context context, List<AudioProperties> objects) {
            super(context, 0, objects);
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(VoiceNotesActivity.this).inflate(R.layout.row_file_properties, parent, false);
            }

            CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
            checkBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    if (itemChecked[position]) {
                        cb.setChecked(false);
                        itemChecked[position] = false;
                    } else {
                        cb.setChecked(true);
                        itemChecked[position] = true;
                    }

                    int numChecked = 0;
                    for (boolean aItemChecked : itemChecked) {
                        if (aItemChecked) {
                            numChecked++;
                        }
                    }

                    if(numChecked >= 1) {
                        btnDelSelected.setColorNormal(getResources().getColor(R.color.fab_delete_selected));
                    } else {
                        btnDelSelected.setColorNormal(getResources().getColor(R.color.material_blue_grey_800));
                    }
                }
            });

            checkBox.setChecked(itemChecked[position]);

            TextView fileName = (TextView) convertView.findViewById(R.id.row_file_name_tv);
            TextView fileSize = (TextView) convertView.findViewById(R.id.row_file_size_tv);
            //TextView fileLength = (TextView) convertView.findViewById(R.id.audio_length_tv);
            TextView fileLastModified = (TextView) convertView.findViewById(R.id.row_date_tv);
            ImageView icon = (ImageView) convertView.findViewById(R.id.row_icon_imv);

            AudioProperties ap = getItem(position);
            fileName.setText(ap.getFileName());
            fileSize.setText("Size: " + Integer.parseInt(ap.getFileSize()) / 1024  + " KB");
            icon.setImageResource(R.drawable.audionotes_row64);

            File file = new File(ap.getFilePath());
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
            fileLastModified.setText("Last Modified: " + sdf.format(file.lastModified()));

            return convertView;
        }
    }

    /*@Override
    public void onBackPressed() {
        if(appnext.isBubbleVisible()){
            appnext.hideBubble();
        }
        else{
            super.onBackPressed();
        }
    }*/

    class MyAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String rootDir = Environment.getExternalStorageDirectory() + "/WhatsApp/Media/WhatsApp Voice Notes/";
            populate(rootDir, arrayListAudioProps);
            itemChecked = new boolean[arrayListAudioProps.size()];
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            int totalSize = 0;
            int count = 0;
            for(AudioProperties ap : arrayListAudioProps) {
                totalSize += Integer.parseInt(ap.getFileSize()) / 1024;
                count++;
            }
            tvTotalSize.setText("Total File Size: " + totalSize + " KB");
            tvNumFiles.setText(" Number of Files: " + count);

            adapter = new CustomAdapter(VoiceNotesActivity.this, arrayListAudioProps);
            adapter.notifyDataSetChanged();
            lv.invalidateViews();
            lv.setAdapter(adapter);
        }
    }

    public void populate(String directoryName, ArrayList<AudioProperties> files) {
        File directory = new File(directoryName);
        File[] fList = directory.listFiles();
        for (File f : fList) {
            if (f.isFile()) {
                if(!f.getName().equalsIgnoreCase(".nomedia")) {
                    AudioProperties ap = new AudioProperties(f.getName(), String.valueOf(f.lastModified()), String.valueOf(f.length()), f.getAbsolutePath());
                    files.add(ap);
                }
            } else if (f.isDirectory()) {
                populate(f.getAbsolutePath(), files);
            }
        }
    }

}
