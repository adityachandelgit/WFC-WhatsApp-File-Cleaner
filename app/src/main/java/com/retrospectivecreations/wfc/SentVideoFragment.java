package com.retrospectivecreations.wfc;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Aditya on 25-02-2015.
 */
public class SentVideoFragment extends Fragment {
    ArrayList<VideoProperties> list_VideoPropFiles;
    GridView gridview;
    ArrayAdapter adapter;
    FloatingActionButton btnDeleteSelected, btnDeleteAll;
    TextView totalsizeTV, numOfFilesTV;
    ProgressDialog progressDialog;
    int width;
    long totalSize = 0, numofFiles = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sent_video, container, false);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        width = metrics.widthPixels;

        numOfFilesTV = (TextView) v.findViewById(R.id.sentVideo_NumFile_tv);
        totalsizeTV = (TextView) v.findViewById(R.id.sentVideo_TotalSize_tv);

        new MyAsyncPopulateVideos().execute(0);

        gridview = (GridView) v.findViewById(R.id.gridview_sent_video);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(getActivity()).setTitle("Play Video?")
                        .setNeutralButton("Play!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri uri = Uri.fromFile(new File(list_VideoPropFiles.get(position).getFilePath()));
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(uri, "video*//*");
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        });

        btnDeleteSelected = (FloatingActionButton) v.findViewById(R.id.btn_delete_selected_sentVideo);
        btnDeleteSelected.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity()).setTitle("Delete Selected Videos?").setMessage("Are you sure you want to delete the selected videos?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Iterator<VideoProperties> iterator = list_VideoPropFiles.iterator();
                                while (iterator.hasNext()) {
                                    VideoProperties vp = iterator.next();
                                    if (vp.isChecked) {
                                        totalSize -= Long.parseLong(vp.getFileSize());
                                        numofFiles -= 1;
                                        deleteCacheAndVideo(vp.getFilePath(), vp.getCachePath());
                                        iterator.remove();
                                    }
                                }
                                numOfFilesTV.setText("Number of Videos: " + numofFiles);
                                totalsizeTV.setText("Total Video Size: " + String.format("%.2f", totalSize / 1024.0 / 1024.0) + " MB");
                                Toast.makeText(getActivity(), "Video(s) deleted!", Toast.LENGTH_SHORT).show();
                                btnDeleteSelected.setColorNormal(getResources().getColor(R.color.material_blue_grey_800));
                                new MyAsyncPopulateVideos().execute(1);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false).show();
            }
        });

        btnDeleteAll = (FloatingActionButton) v.findViewById(R.id.btn_delete_all_sentVideo);
        btnDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity()).setTitle("Delete All Videos").setMessage("Are you sure you want to delete all the videos?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Iterator<VideoProperties> iterator = list_VideoPropFiles.iterator();
                                while (iterator.hasNext()) {
                                    VideoProperties vp = iterator.next();
                                    deleteCacheAndVideo(vp.getFilePath(), vp.getCachePath());
                                    iterator.remove();
                                }
                                numofFiles = 0;
                                totalSize = 0;
                                numOfFilesTV.setText("Number of Videos: " + numofFiles);
                                totalsizeTV.setText("Total Video Size: " + String.format("%.2f", totalSize / 1024.0 / 1024.0) + " MB");
                                Toast.makeText(getActivity(), "All videos deleted!", Toast.LENGTH_SHORT).show();
                                new MyAsyncPopulateVideos().execute(1);
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setCancelable(false).show();
            }
        });

        return v;
    }

    class VideoAdapter extends ArrayAdapter<VideoProperties> {

        private LayoutInflater inflater;

        public VideoAdapter(Context context, List<VideoProperties> objects) {
            super(context, 0, objects);
            inflater = LayoutInflater.from(getActivity());
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.row_video_properties, parent, false);
                holder = new ViewHolder();
                holder.imageView = (ImageView) convertView.findViewById(R.id.imageView_video_row);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox_video_row);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    if (cb.isChecked()) {
                        list_VideoPropFiles.get(position).setChecked(true);
                    } else {
                        list_VideoPropFiles.get(position).setChecked(false);
                    }

                    int numChecked = 0;
                    for(int i = 0; i < list_VideoPropFiles.size(); i++) {
                        if(list_VideoPropFiles.get(i).isChecked) {
                            numChecked++;
                        }
                    }
                    if(numChecked >= 1) {
                        btnDeleteSelected.setColorNormal(getResources().getColor(R.color.fab_delete_selected));
                    } else {
                        btnDeleteSelected.setColorNormal(getResources().getColor(R.color.material_blue_grey_800));
                    }
                }
            });

            holder.checkBox.setChecked(list_VideoPropFiles.get(position).isChecked);

            Picasso.with(getActivity())
                    .load(Uri.fromFile(new File(list_VideoPropFiles.get(position).getCachePath())))
                    .resize(width / 3, width / 3)
                    .placeholder(R.drawable.white)
                    .centerCrop()
                    .into(holder.imageView);

            return convertView;
        }
    }

    static class ViewHolder {
        ImageView imageView;
        CheckBox checkBox;
    }

    class MyAsyncPopulateVideos extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Please wait...");
            progressDialog.setMessage("Videos are being loaded!");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Integer... params) {
            if (params[0] == 0) {
                populateVideoFiles();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            gridview.invalidateViews();
            adapter = new VideoAdapter(getActivity(), list_VideoPropFiles);
            adapter.notifyDataSetChanged();
            gridview.setAdapter(adapter);
            progressDialog.dismiss();
        }
    }

    private void populateVideoFiles() {
        String videoReceivedPath = Environment.getExternalStorageDirectory() + "/WhatsApp/Media/WhatsApp Video/Sent/";
        File file = new File(videoReceivedPath);
        if (!file.exists()) {
            file.mkdirs();
        }

        String videoCache = Environment.getExternalStorageDirectory() + "/WhatsApp/WFC/Temp/";
        list_VideoPropFiles = new ArrayList<>();
        File[] allFiles = new File(videoReceivedPath).listFiles();
        for (File currentFile : allFiles) {
            if (currentFile.isFile() && !currentFile.getName().equalsIgnoreCase(".nomedia")) {
                String cacheFileName = videoCache + currentFile.getName() + ".jpeg";
                Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(currentFile.toString(), MediaStore.Video.Thumbnails.MINI_KIND);
                writeExternalToCache(bitmap, cacheFileName);
                list_VideoPropFiles.add(new VideoProperties(currentFile.getAbsolutePath(), cacheFileName, String.valueOf(currentFile.length()), false));
                totalSize += currentFile.length();
                numofFiles += 1;
            }
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                numOfFilesTV.setText("Number of Videos: " + numofFiles);
                totalsizeTV.setText("Total Video Size: " + String.format("%.2f", totalSize / 1024.0 / 1024.0) + " MB");
            }
        });
    }

    static void writeExternalToCache(Bitmap bitmap, String fileName) {
        int BUFFER_SIZE = 1024 * 4;
        try {
            File file = new File(fileName);
            FileOutputStream fos = new FileOutputStream(file);
            final BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER_SIZE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {

        }
    }

    private void deleteCacheAndVideo(String video, String cache) {
        new File(video).delete();
        new File(cache).delete();
    }
}
