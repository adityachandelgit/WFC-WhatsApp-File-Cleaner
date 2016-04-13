package com.retrospectivecreations.wfc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Aditya on 23-02-2015.
 */
public class ReceivedImageFragment extends Fragment {
    ArrayList<Uri> filePaths;
    int screenWidth, screenHeight;
    private boolean[] thumbnailsselection;
    FloatingActionButton btnDeleteSelected, btnDeleteAll;
    BaseAdapter adapter;
    GridView gridview;
    TextView tvTotalSize, tvNumFiles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_received_image, container, false);

        tvTotalSize = (TextView) v.findViewById(R.id.rec_TotalSize_tv);
        tvNumFiles = (TextView) v.findViewById(R.id.rec_NumFile_tv);

        populatePaths();
        thumbnailsselection = new boolean[filePaths.size()];

        gridview = (GridView) v.findViewById(R.id.gridview);
        adapter = new ImageAdapter(getActivity());
        gridview.setAdapter(adapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, final int position, long id) {

                ImageView image = new ImageView(getActivity());
                Picasso.with(getActivity())
                        .load(filePaths.get(position))
                        .resize(screenWidth, (screenHeight * 3) / 4)
                        .placeholder(R.drawable.white)
                        .centerInside()
                        .into(image);

                AlertDialog.Builder builder =
                        new AlertDialog.Builder(getActivity()).setTitle("Select Action!")
                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new AlertDialog.Builder(getActivity())
                                                .setTitle("Please Confirm")
                                                .setMessage("Are you sure you want to delete this picture?")
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        File file = new File(filePaths.get(position).getPath());
                                                        file.delete();
                                                        populatePaths();
                                                        thumbnailsselection = new boolean[filePaths.size()];
                                                        gridview.invalidateViews();
                                                        adapter.notifyDataSetChanged();
                                                        gridview.setAdapter(adapter);
                                                        dialog.dismiss();
                                                        Toast.makeText(getActivity(), "Image deleted!", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                }).setCancelable(false).show();
                                    }
                                })
                                .setNeutralButton("Open", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        File file = new File(filePaths.get(position).getPath());
                                        Uri uri = Uri.fromFile(file);
                                        Intent intent = new Intent();
                                        intent.setAction(android.content.Intent.ACTION_VIEW);
                                        intent.setDataAndType(uri, "image/*");
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .setView(image);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                alertDialog.getWindow().setLayout(screenWidth, screenHeight);
            }
        });

        btnDeleteSelected = (FloatingActionButton) v.findViewById(R.id.btn_delete_selected);
        btnDeleteSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ArrayList<File> fileToDelete = new ArrayList<>();
                for (int i = 0; i < thumbnailsselection.length; i++) {
                    if (thumbnailsselection[i]) {
                        File file = new File(filePaths.get(i).getPath());
                        fileToDelete.add(file);
                    }
                }
                if (fileToDelete.size() <= 0) {
                    Toast.makeText(getActivity(), "First select some files to delete!", Toast.LENGTH_LONG).show();
                } else {
                    final String totalFilesToDelete = String.valueOf(fileToDelete.size());
                    new AlertDialog.Builder(getActivity()).setTitle("Delete " + totalFilesToDelete + " image(s)?").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for (File f : fileToDelete) {
                                f.delete();
                            }
                            populatePaths();
                            thumbnailsselection = new boolean[filePaths.size()];
                            gridview.invalidateViews();
                            adapter.notifyDataSetChanged();
                            gridview.setAdapter(adapter);
                            btnDeleteSelected.setColorNormal(getResources().getColor(R.color.material_blue_grey_800));
                            Toast.makeText(getActivity(), totalFilesToDelete + " image(s) deleted!", Toast.LENGTH_SHORT).show();
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


        btnDeleteAll = (FloatingActionButton) v.findViewById(R.id.btn_delete_all);
        btnDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int totalFilesToDelete = filePaths.size();
                if (totalFilesToDelete > 0) {
                    new AlertDialog.Builder(getActivity()).setTitle("Delete all " + totalFilesToDelete + " images?").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for (Uri uri : filePaths) {
                                File f = new File(uri.getPath());
                                f.delete();
                            }
                            populatePaths();
                            thumbnailsselection = new boolean[filePaths.size()];
                            gridview.invalidateViews();
                            adapter.notifyDataSetChanged();
                            gridview.setAdapter(adapter);
                            Toast.makeText(getActivity(), totalFilesToDelete + " images Deleted!", Toast.LENGTH_SHORT).show();
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


    public class ImageAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private Context mContext;

        public ImageAdapter(Context c) {
            mInflater = (LayoutInflater) getActivity().getSystemService(c.LAYOUT_INFLATER_SERVICE);
            mContext = c;
        }

        public int getCount() {
            return filePaths.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {  // if it's not recycled, initialize some attributes
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.galleryitem, null);
                holder.imageview = (ImageView) convertView.findViewById(R.id.thumbImage);
                holder.checkbox = (CheckBox) convertView.findViewById(R.id.itemCheckBox);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.checkbox.setId(position);
            holder.imageview.setId(position);

            holder.checkbox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    int id = cb.getId();
                    if (thumbnailsselection[id]) {
                        cb.setChecked(false);
                        thumbnailsselection[id] = false;
                    } else {
                        cb.setChecked(true);
                        thumbnailsselection[id] = true;
                    }

                    int numChecked = 0;
                    for (boolean aThumbnailsselection : thumbnailsselection) {
                        if (aThumbnailsselection) {
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

            Picasso.with(getActivity())
                    .load(filePaths.get(position))
                    .resize(screenWidth / 3, screenWidth / 3)
                    .placeholder(R.drawable.white)
                    .centerCrop()
                    .into(holder.imageview);

            holder.checkbox.setChecked(thumbnailsselection[position]);
            holder.id = position;

            return convertView;
        }
    }

    class ViewHolder {
        ImageView imageview;
        CheckBox checkbox;
        int id;
    }


    private void populatePaths() {
        File[] files = new File(Environment.getExternalStorageDirectory() + "/WhatsApp/Media/WhatsApp Images/").listFiles();
        int totalSize = 0;
        int counter = 0;
        filePaths = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                counter++;
                filePaths.add(Uri.fromFile(files[i]));
                totalSize += files[i].length() / 1024;
            }
        }
        int numFile = counter;
        tvTotalSize.setText("Total File Size: " + String.format("%.2f", totalSize / 1024.0) + " MB");
        tvNumFiles.setText(" Number of Files: " + numFile + " ");
    }

}
