package com.retrospectivecreations.wfc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import com.getbase.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aditya on 24-02-2015.
 */
public class ReceivedAudioFragment extends Fragment {
    private boolean[] itemChecked;
    ArrayList<AudioProperties> arrayListAudioProps;
    ListView lv;
    CustomAdapter adapter;
    FloatingActionButton btnDelSelected, btnDelAll;
    TextView tvTotalSize, tvNumFiles;
    int totalSize, counter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_received_audio, container, false);

        tvTotalSize = (TextView) v.findViewById(R.id.recAudio_TotalSize_tv);
        tvNumFiles = (TextView) v.findViewById(R.id.recAudio_NumFile_tv);

        arrayListAudioProps = new ArrayList<>();
        new MyAsync().execute();

        lv = (ListView) v.findViewById(R.id.listView_recAudio);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(getActivity()).setTitle("Select Action!").setPositiveButton("Delete File", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File file = new File(arrayListAudioProps.get(position).getFilePath());
                        file.delete();
                        arrayListAudioProps.clear();
                        new MyAsync().execute();
                        Toast.makeText(getActivity(), file.getName() + " Deleted!", Toast.LENGTH_SHORT).show();
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

        btnDelSelected = (FloatingActionButton) v.findViewById(R.id.btn_delete_selected_RecAudio);
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
                    Toast.makeText(getActivity(), "First select some files to delete!", Toast.LENGTH_LONG).show();
                } else {
                        final String totalFilesToDelete = String.valueOf(fileToDelete.size());
                        new AlertDialog.Builder(getActivity()).setTitle("Delete " + totalFilesToDelete + " file(s)?").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (File f : fileToDelete) {
                                    f.delete();
                                }
                                arrayListAudioProps.clear();
                                new MyAsync().execute();
                                btnDelSelected.setColorNormal(getResources().getColor(R.color.material_blue_grey_800));
                                Toast.makeText(getActivity(), totalFilesToDelete + " file(s) deleted!", Toast.LENGTH_SHORT).show();
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


        btnDelAll = (FloatingActionButton) v.findViewById(R.id.btn_delete_all_RecAudio);
        btnDelAll.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final int totalFilesToDelete = arrayListAudioProps.size();
                if (totalFilesToDelete > 0) {
                    new AlertDialog.Builder(getActivity()).setTitle("Delete all " + totalFilesToDelete + " file(s)?").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for (AudioProperties ap : arrayListAudioProps) {
                                File f = new File(ap.getFilePath());
                                f.delete();
                            }
                            arrayListAudioProps.clear();
                            new MyAsync().execute();
                            Toast.makeText(getActivity(), totalFilesToDelete + " file(s) deleted!", Toast.LENGTH_SHORT).show();
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

        return v;
    }

    class CustomAdapter extends ArrayAdapter<AudioProperties> {
        public CustomAdapter(Context context, List<AudioProperties> objects) {
            super(context, 0, objects);
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.row_file_properties, parent, false);
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
            fileSize.setText("Size: " + String.format("%.2f", Float.parseFloat(ap.getFileSize()) / 1024.0 / 1024.0) + " MB");
            icon.setImageResource(R.drawable.audio_row64);

            File file = new File(ap.getFilePath());
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
            fileLastModified.setText("Last Modified: " + sdf.format(file.lastModified()));

            return convertView;
        }
    }

    class MyAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            populate();
            itemChecked = new boolean[arrayListAudioProps.size()];
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter = new CustomAdapter(getActivity(), arrayListAudioProps);
            adapter.notifyDataSetChanged();
            lv.invalidateViews();
            lv.setAdapter(adapter);
        }
    }

    private void populate() {
        File[] files = new File(Environment.getExternalStorageDirectory() + "/WhatsApp/Media/WhatsApp Audio/").listFiles();
        totalSize = 0;
        counter = 0;
        if(files.length > 0) {
            for (File f : files) {
                if (f.isFile()) {
                    counter++;
                    totalSize += f.length() / 1024;
                    AudioProperties ap = new AudioProperties(f.getName(), String.valueOf(f.lastModified()), String.valueOf(f.length()), f.getAbsolutePath());
                    arrayListAudioProps.add(ap);
                }
            }
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvTotalSize.setText("Total File Size: " + String.format("%.2f", totalSize / 1024.0) + " MB");
                tvNumFiles.setText(" Number of Files: " + counter);

            }
        });
    }

}
