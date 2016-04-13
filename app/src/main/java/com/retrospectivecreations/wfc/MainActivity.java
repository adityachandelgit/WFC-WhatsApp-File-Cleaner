package com.retrospectivecreations.wfc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appnext.appnextsdk.Appnext;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.startapp.android.publish.Ad;
import com.startapp.android.publish.AdDisplayListener;
import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppSDK;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    SharedPreferences prefs = null;

    String rootDirPath = Environment.getExternalStorageDirectory() + "/WhatsApp/Media";
    String audioDirPath = Environment.getExternalStorageDirectory() + "/WhatsApp/Media/WhatsApp Audio";
    String audioSentDirPath = Environment.getExternalStorageDirectory() + "/WhatsApp/Media/WhatsApp Audio/Sent";
    String imagesDirPath = Environment.getExternalStorageDirectory() + "/WhatsApp/Media/WhatsApp Images";
    String imagesSentDirPath = Environment.getExternalStorageDirectory() + "/WhatsApp/Media/WhatsApp Images/Sent";
    String profilePicsDirPath = Environment.getExternalStorageDirectory() + "/WhatsApp/Profile Pictures";
    String videoDirPath = Environment.getExternalStorageDirectory() + "/WhatsApp/Media/WhatsApp Video";
    String videoSentDirPath = Environment.getExternalStorageDirectory() + "/WhatsApp/Media/WhatsApp Video/Sent";
    String voiceNotesDirPath = Environment.getExternalStorageDirectory() + "/WhatsApp/Media/WhatsApp Voice Notes";
    String callsDirPath = Environment.getExternalStorageDirectory() + "/WhatsApp/Media/WhatsApp Calls";
    String wallpaperDirPath = Environment.getExternalStorageDirectory() + "/WhatsApp/Media/Wallpaper";
    String backupsDirPath = Environment.getExternalStorageDirectory() + "/WhatsApp/Databases";
    static String videoCacheDirPath = Environment.getExternalStorageDirectory() + "/WhatsApp/WFC/Temp";

    static int NO_ACTIVITY = 0;
    static int IMAGE_ACTIVITY = 1;
    static int PROFILE_PICTURE_ACTIVITY = 2;
    static int VIDEO_ACTIVITY = 3;
    static int AUDIO_ACTIVITY = 4;
    static int VOICE_NOTES_ACTIVITY = 5;
    static int WALLPAPERS_ACTIVITY = 6;
    static int BACKUPS_ACTIVITY = 7;

    int whichTextViewsToUpdate = 0;

    RelativeLayout btnRelativeImage, btnRelativeWallpaper, btnRelativeProfile, btnRelativeVideos, btnRelativeAudio, btnRelativeVoiceNotes, btnRelativeBackups, btnRelativeDeleteAll;
    Button btnImageDelete, btnWallDelete, btnProPicDelete, btnAudioDelete, btnVideoDelete, btnVoiceNotesDelete, btnBackupDelete, btnDeleteAll;
    TextView imageTotalFilesTV, imageTotalSizeTV, wallpaperTotalFilesTV, wallpaperTotalSizeTV, profilePicTotalFilesTV, profilePicTotalSizeTV;
    TextView videosTotalFilesTV, videosTotalSizeTV, audioTotalFilesTV, audioTotalSizeTV, notesTotalFilesTV, notesTotalSizeTV;
    TextView backupsTotalFilesTV, backupsTotalSizeTV, allTotalFilesTV, allTotalSizeTV;
    ImageView refreshButton, noadsButton, wfsButton, rateButton, shareButton;

    int allTotalFiles = 0;
    long allTotalFilesSize = 0;

    Appnext appnext;
    InterstitialAd mInterstitialAd;
    static StartAppAd startAppAd;
    int showAdOnExit = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*StartAppSDK.init(this, "*", "*", true);
        StartAppAd.showSplash(this, savedInstanceState);

        appnext = new Appnext(this);
        appnext.setAppID("*"); // Set your AppID
        appnext.cacheAd();
        appnext.showBubble(); // show the interstitial

        startAppAd = new StartAppAd(this);
        startAppAd.loadAd(StartAppAd.AdMode.OFFERWALL);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("*");
        //mInterstitialAd.setAdUnitId("*"); //TestID
        requestNewInterstitial();

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                MainActivity.this.finish();
                System.exit(0);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(30);
        getSupportActionBar().setIcon(R.drawable.ic_action_whiteicon);
        getSupportActionBar().setTitle(" WFC: WhatzApp File Cleaner");*/

        prefs = getSharedPreferences("com.retrospectivecreations.wfc", MODE_PRIVATE);

        createDirs();
        whichTextViewsToUpdate = NO_ACTIVITY;

///////////////////////////////Delete Everything Start Here ////////////////////////////////////////
        allTotalFilesTV = (TextView) findViewById(R.id.main_all_total_files_tv);
        allTotalSizeTV = (TextView) findViewById(R.id.main_all_total_size_tv);

        updateAllFilesInfo();

        btnRelativeDeleteAll = (RelativeLayout) findViewById(R.id.main_relative_button_all);
        btnRelativeDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Click the dustbin button on the right to delete all the files!", Toast.LENGTH_LONG).show();
            }
        });

        btnDeleteAll = (Button) findViewById(R.id.main_all_delete_button);
        btnDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this).setTitle("Caution!").setMessage("This will delete all your WhatsApp files! Are you sure you want to proceed?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteEverything(rootDirPath);
                                deleteEverything(backupsDirPath);
                                deleteEverything(profilePicsDirPath);
                                allTotalFilesTV.setText("Files: 0");
                                allTotalSizeTV.setText("Total Size: 0.00MB");
                                allTotalFiles = 0;
                                allTotalFilesSize = 0;
                                updateTextViewsVoiceNotes();
                                updateTextViewsAudio();
                                updateTextViewsVideos();
                                updateTextViewsProfilePics();
                                updateTextViewsWallpapers();
                                updateTextViewsImages();
                                updateTextViewsVoiceBackups();
                                Toast.makeText(MainActivity.this, "All file(s) deleted!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(MainActivity.this, "Deletion cancelled!", Toast.LENGTH_SHORT).show();
                            }
                        }).setCancelable(false).show();
            }
        });
///////////////////////////////Delete Everything Ends Here /////////////////////////////////////////


///////////////////////////////Images Settings Start Here /////////////////////////////////////////
        imageTotalFilesTV = (TextView) findViewById(R.id.main_image_total_files_tv);
        imageTotalSizeTV = (TextView) findViewById(R.id.main_image_total_size_tv);

        updateTextViewsImages();

        btnImageDelete = (Button) findViewById(R.id.main_image_delete_button);
        btnImageDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] paths = {imagesDirPath, imagesSentDirPath};
                commonDeleteDialog(MainActivity.this, "images", paths);
            }
        });

        btnRelativeImage = (RelativeLayout) findViewById(R.id.main_relative_button_image);
        btnRelativeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichTextViewsToUpdate = IMAGE_ACTIVITY;
                Intent intent = new Intent(getApplicationContext(), ImageActivity.class);
                startActivity(intent);
            }
        });
///////////////////////////////Images Settings Ends Here /////////////////////////////////////////


///////////////////////////////Wallpaper Settings Start Here ////////////////////////////////////////
        wallpaperTotalFilesTV = (TextView) findViewById(R.id.main_wallpaper_total_files_tv);
        wallpaperTotalSizeTV = (TextView) findViewById(R.id.main_wallpaper_total_size_tv);

        updateTextViewsWallpapers();

        btnWallDelete = (Button) findViewById(R.id.main_wallpaper_delete_button);
        btnWallDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] paths = {wallpaperDirPath};
                commonDeleteDialog(MainActivity.this, "wallpaper", paths);
            }
        });

        btnRelativeWallpaper = (RelativeLayout) findViewById(R.id.main_relative_button_wallpaper);
        btnRelativeWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichTextViewsToUpdate = WALLPAPERS_ACTIVITY;
                Intent intent = new Intent(getApplicationContext(), WallpaperActivity.class);
                startActivity(intent);
            }
        });
///////////////////////////////Wallpaper Settings Ends Here /////////////////////////////////////////


///////////////////////////////ProfilePic Settings Start Here ////////////////////////////////////////
        profilePicTotalFilesTV = (TextView) findViewById(R.id.main_profilepic_total_files_tv);
        profilePicTotalSizeTV = (TextView) findViewById(R.id.main_profilepic_total_size_tv);

        updateTextViewsProfilePics();

        btnProPicDelete = (Button) findViewById(R.id.main_profilepic_delete_button);
        btnProPicDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] paths = {profilePicsDirPath};
                commonDeleteDialog(MainActivity.this, "profile pictures", paths);
            }
        });

        btnRelativeProfile = (RelativeLayout) findViewById(R.id.main_relative_button_profilepic);
        btnRelativeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichTextViewsToUpdate = PROFILE_PICTURE_ACTIVITY;
                Intent intent = new Intent(getApplicationContext(), ProfilePicActivity.class);
                startActivity(intent);
            }
        });
///////////////////////////////ProfilePic Settings Ends Here /////////////////////////////////////////

///////////////////////////////Videos Settings Start Here ////////////////////////////////////////
        videosTotalFilesTV = (TextView) findViewById(R.id.main_video_total_files_tv);
        videosTotalSizeTV = (TextView) findViewById(R.id.main_video_total_size_tv);

        updateTextViewsVideos();


        btnVideoDelete = (Button) findViewById(R.id.main_video_delete_button);
        btnVideoDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] paths = {videoDirPath};
                commonDeleteDialog(MainActivity.this, "videos", paths);
            }
        });

        btnRelativeVideos = (RelativeLayout) findViewById(R.id.main_relative_button_video);
        btnRelativeVideos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichTextViewsToUpdate = VIDEO_ACTIVITY;
                Intent intent = new Intent(getApplicationContext(), VideoActivity.class);
                startActivity(intent);
            }
        });
///////////////////////////////Videos Settings Ends Here /////////////////////////////////////////

///////////////////////////////Audio Settings Start Here ////////////////////////////////////////
        audioTotalFilesTV = (TextView) findViewById(R.id.main_audio_total_files_tv);
        audioTotalSizeTV = (TextView) findViewById(R.id.main_audio_total_size_tv);

        updateTextViewsAudio();

        btnAudioDelete = (Button) findViewById(R.id.main_audio_delete_button);
        btnAudioDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] paths = {audioDirPath, audioSentDirPath};
                commonDeleteDialog(MainActivity.this, "audio", paths);
            }
        });

        btnRelativeAudio = (RelativeLayout) findViewById(R.id.main_relative_button_audio);
        btnRelativeAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichTextViewsToUpdate = AUDIO_ACTIVITY;
                Intent intent = new Intent(getApplicationContext(), AudioActivity.class);
                startActivity(intent);
            }
        });
///////////////////////////////Audio Settings Ends Here /////////////////////////////////////////

///////////////////////////////Voice Notes Settings Start Here ////////////////////////////////////////
        notesTotalFilesTV = (TextView) findViewById(R.id.main_voice_notes_total_files_tv);
        notesTotalSizeTV = (TextView) findViewById(R.id.main_voice_notes_total_size_tv);

        updateTextViewsVoiceNotes();

        btnVoiceNotesDelete = (Button) findViewById(R.id.main_voice_notes_delete_button);
        btnVoiceNotesDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this).setTitle("Caution!").setMessage("Are you sure you want to delete all voice notes?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteEverything(voiceNotesDirPath);
                                setTextViews("voice notes");
                                allTotalFilesSize = 0;
                                allTotalFiles = 0;
                                updateAllFilesInfo();
                                Toast.makeText(MainActivity.this, "All voice notes deleted!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(MainActivity.this, "Deletion cancelled!", Toast.LENGTH_SHORT).show();
                            }
                        }).setCancelable(false).show();
            }
        });

        btnRelativeVoiceNotes = (RelativeLayout) findViewById(R.id.main_relative_button_voiceNotes);
        btnRelativeVoiceNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichTextViewsToUpdate = VOICE_NOTES_ACTIVITY;
                Intent intent = new Intent(getApplicationContext(), VoiceNotesActivity.class);
                startActivity(intent);
            }
        });
////////////////////////-------------Voice Notes Ends Here -----------///////////////////////////////

///////////////////////////////Backups Settings Start Here ////////////////////////////////////////
        backupsTotalFilesTV = (TextView) findViewById(R.id.main_backups_total_files_tv);
        backupsTotalSizeTV = (TextView) findViewById(R.id.main_backups_total_size_tv);

        updateTextViewsVoiceBackups();

        btnBackupDelete = (Button) findViewById(R.id.main_backups_delete_button);
        btnBackupDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] paths = {backupsDirPath};
                commonDeleteDialog(MainActivity.this, "backups", paths);
            }
        });

        btnRelativeBackups = (RelativeLayout) findViewById(R.id.main_relative_button_backups);
        btnRelativeBackups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichTextViewsToUpdate = BACKUPS_ACTIVITY;
                Intent intent = new Intent(getApplicationContext(), BackupsActivity.class);
                startActivity(intent);
            }
        });
///////////////////////////////Backups Settings Ends Here /////////////////////////////////


/////////////////// --------------- Bottom Buttons -------------------- ///////////////////

        refreshButton = (ImageView) findViewById(R.id.imageView_quiz);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Try our 'Quiz Time' trivia app!", Toast.LENGTH_SHORT).show();
                Uri uri = Uri.parse("market://details?id=com.retroid.quiz.time.ultimate.trivia");
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.retroid.quiz.time.ultimate.trivia")));
                }
            }
        });

        noadsButton = (ImageView) findViewById(R.id.imageView_noads);
        noadsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Ads free version of WFC coming soon!", Toast.LENGTH_SHORT).show();
            }
        });


        wfsButton = (ImageView) findViewById(R.id.imageView_main_wfs);
        wfsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Try our 'WFS: WhatsApp File Sender' app!", Toast.LENGTH_SHORT).show();
                Uri uri = Uri.parse("market://details?id=retrospect.aditya.whatzappfilecourierads");
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=retrospect.aditya.whatzappfilecourierads")));
                }
            }
        });

        rateButton = (ImageView) findViewById(R.id.imageView_main_rate);
        rateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Please rate 5 stars to WFC!", Toast.LENGTH_SHORT).show();
                Uri uri = Uri.parse("market://details?id=" + MainActivity.this.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + MainActivity.this.getPackageName())));
                }
            }
        });

        shareButton = (ImageView) findViewById(R.id.imageView_main_share);
        shareButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Clean your Whatsapp's junk files, download 'WFC: WhatsApp File Cleaner' https://play.google.com/store/apps/details?id=com.retrospectivecreations.wfc");
                try {
                    MainActivity.this.startActivity(Intent.createChooser(shareIntent, "Share 'WFC: WhatsApp File Cleaner' using"));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "No messaging apps available.", Toast.LENGTH_SHORT).show();
                }
            }
        });


/////////////////// ------------- Bottom Buttons End ------------------ ///////////////////


    }
////////////////////////---------- On Create Ends Here ------------////////////////////////


    ///////////////////////----------- On Resume Starts Here -----------///////////////////////

    @Override
    protected void onSaveInstanceState (Bundle outState){
        super.onSaveInstanceState(outState);
        startAppAd.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState (Bundle savedInstanceState){
        startAppAd.onRestoreInstanceState(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //startAppAd.onResume();

        if (prefs.getBoolean("firstrun", true)) {
            final Dialog dialog = new Dialog(MainActivity.this);
            dialog.setTitle("Welcome to WFC");
            dialog.setContentView(R.layout.startup_dialog);
            dialog.show();
            Button gotit = (Button) dialog.findViewById(R.id.gotitbtn);
            gotit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            prefs.edit().putBoolean("firstrun", false).commit();
        }

        if (whichTextViewsToUpdate == IMAGE_ACTIVITY) {
            updateTextViewsImages();
            updateAllFilesInfo();
            whichTextViewsToUpdate = NO_ACTIVITY;
        } else if (whichTextViewsToUpdate == WALLPAPERS_ACTIVITY) {
            updateTextViewsWallpapers();
            updateAllFilesInfo();
            whichTextViewsToUpdate = NO_ACTIVITY;
        } else if (whichTextViewsToUpdate == VOICE_NOTES_ACTIVITY) {
            updateTextViewsVoiceNotes();
            updateAllFilesInfo();
            whichTextViewsToUpdate = NO_ACTIVITY;
        } else if (whichTextViewsToUpdate == PROFILE_PICTURE_ACTIVITY) {
            updateAllFilesInfo();
            updateTextViewsProfilePics();
            whichTextViewsToUpdate = NO_ACTIVITY;
        } else if (whichTextViewsToUpdate == VIDEO_ACTIVITY) {
            updateAllFilesInfo();
            updateTextViewsVideos();
            whichTextViewsToUpdate = NO_ACTIVITY;
        } else if (whichTextViewsToUpdate == AUDIO_ACTIVITY) {
            updateAllFilesInfo();
            updateTextViewsAudio();
            whichTextViewsToUpdate = NO_ACTIVITY;
        } else if (whichTextViewsToUpdate == BACKUPS_ACTIVITY) {
            updateAllFilesInfo();
            updateTextViewsBackups();
            whichTextViewsToUpdate = NO_ACTIVITY;
        }
    }


    @Override
    public void onBackPressed() {
        if(appnext.isBubbleVisible()){
            appnext.hideBubble();
        }
        /*else{
            super.onBackPressed();
        }*/
         else if (showAdOnExit == 0) {
            new AlertDialog.Builder(this).setTitle("Exit App!")
                    .setMessage("Are you sure you want to exit app?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mInterstitialAd.isLoaded()) {
                                mInterstitialAd.show();
                            } else {
                                MainActivity.this.finish();
                            }
                            //startAppAd.onBackPressed();
                            //MainActivity.super.onBackPressed();
                            showAdOnExit++;
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).setCancelable(false).show();
        } else {
            super.onBackPressed();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        startAppAd.onPause();
    }

    ///////////////////////----------- On Resume Starts Here -----------///////////////////////

    void updateTextViewsImages() {
        int totalImages = 0;
        totalImages += totalFiles(imagesDirPath);
        totalImages += totalFiles(imagesSentDirPath);

        long totalImageSize = 0;
        totalImageSize += totalSize(imagesDirPath);
        totalImageSize += totalSize(imagesSentDirPath);

        imageTotalFilesTV.setText("Files: " + totalImages);
        imageTotalSizeTV.setText("Total Size: " + String.format("%.2f", totalImageSize / 1024.0 / 1024.0) + " MB");
    }


    void updateTextViewsWallpapers() {
        int totalWallpapers = 0;
        totalWallpapers += totalFiles(wallpaperDirPath);

        long totalWallpapersSize = 0;
        totalWallpapersSize += totalSize(wallpaperDirPath);

        wallpaperTotalFilesTV.setText("Files: " + totalWallpapers);
        wallpaperTotalSizeTV.setText("Total Size: " + String.format("%.2f", totalWallpapersSize / 1024.0 / 1024.0) + " MB");
    }

    void updateTextViewsProfilePics() {
        int totalProPics = 0;
        totalProPics += totalFiles(profilePicsDirPath);

        long totalProfilePicSize = 0;
        totalProfilePicSize += totalSize(profilePicsDirPath);

        profilePicTotalFilesTV.setText("Files: " + totalProPics);
        profilePicTotalSizeTV.setText("Total Size: " + String.format("%.2f", totalProfilePicSize / 1024.0 / 1024.0) + " MB");
    }

    void updateTextViewsVideos() {
        int totalVideos = 0;
        totalVideos += totalFiles(videoDirPath);
        totalVideos += totalFiles(videoSentDirPath);

        long totalVideosSize = 0;
        totalVideosSize += totalSize(videoDirPath);
        totalVideosSize += totalSize(videoSentDirPath);

        videosTotalFilesTV.setText("Files: " + totalVideos);
        videosTotalSizeTV.setText("Total Size: " + String.format("%.2f", totalVideosSize / 1024.0 / 1024.0) + " MB");
    }

    void updateTextViewsAudio() {
        int totalAudio = 0;
        totalAudio += totalFiles(audioDirPath);
        totalAudio += totalFiles(audioSentDirPath);

        long totalAudioSize = 0;
        totalAudioSize += totalSize(audioDirPath);
        totalAudioSize += totalSize(audioSentDirPath);

        audioTotalFilesTV.setText("Files: " + totalAudio);
        audioTotalSizeTV.setText("Total Size: " + String.format("%.2f", totalAudioSize / 1024.0 / 1024.0) + " MB");
    }

    void updateTextViewsBackups() {
        int totalBackups = 0;
        totalBackups += totalFiles(backupsDirPath);

        long totalBackupsSize = 0;
        totalBackupsSize += totalSize(backupsDirPath);

        backupsTotalFilesTV.setText("Files: " + totalBackups);
        backupsTotalSizeTV.setText("Total Size: " + String.format("%.2f", totalBackupsSize / 1024.0 / 1024.0) + " MB");
    }

    void updateTextViewsVoiceNotes() {
        ArrayList<File> audioNotesList = new ArrayList<>();
        audioNotesGetListofFiles(voiceNotesDirPath, audioNotesList);
        int totalNotes = 0;
        long totalNotesSize = 0;
        for (File f : audioNotesList) {
            if (f.isFile() && !f.getName().equals(".nomedia")) {
                totalNotes++;
                totalNotesSize += f.length();
            }
        }
        notesTotalFilesTV.setText("Files: " + totalNotes);
        notesTotalSizeTV.setText("Total Size: " + String.format("%.2f", totalNotesSize / 1024.0 / 1024.0) + " MB");
    }

    void updateTextViewsVoiceBackups() {
        int totalBackupFiles = 0;
        totalBackupFiles += totalFiles(backupsDirPath);

        long totalBackupsSize = 0;
        totalBackupsSize += totalSize(backupsDirPath);

        backupsTotalFilesTV.setText("Files: " + totalBackupFiles);
        backupsTotalSizeTV.setText("Total Size: " + String.format("%.2f", totalBackupsSize / 1024.0 / 1024.0) + " MB");
    }

    private void updateAllFilesInfo() {
        allTotalFiles = 0;
        allTotalFilesSize = 0;
        allFileFunc(rootDirPath);
        allFileFunc(backupsDirPath);
        allFileFunc(profilePicsDirPath);
        allTotalFilesTV.setText("Files: " + allTotalFiles);
        allTotalSizeTV.setText("Total Size: " + String.format("%.2f", allTotalFilesSize / 1024.0 / 1024.0) + " MB");
    }

    private void allFileFunc(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        for (File currrentFile : files) {
            if (currrentFile.isFile() && !currrentFile.getName().equals(".nomedia")) {
                allTotalFilesSize += currrentFile.length();
                allTotalFiles++;
            } else if (currrentFile.isDirectory()) {
                allFileFunc(currrentFile.getAbsolutePath());
            }
        }
    }

    private void deleteEverything(String rootPath) {
        File file = new File(rootPath);
        File[] files = file.listFiles();
        for (File currrentFile : files) {
            if (currrentFile.isFile()) {
                currrentFile.delete();
            } else if (currrentFile.isDirectory()) {
                deleteEverything(currrentFile.getAbsolutePath());
            }
        }
    }

    public void commonDeleteDialog(final Context context, final String message, final String[] paths) {
        new AlertDialog.Builder(context).setTitle("Caution!").setMessage("Are you sure you want to delete all " + message + " ?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (String currentPath : paths) {
                            deleteAllFiles(currentPath);
                            setTextViews(message);
                        }
                        allTotalFilesSize = 0;
                        allTotalFiles = 0;
                        updateAllFilesInfo();

                        Toast.makeText(context, "All " + message + " deleted!", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context, "Deletion cancelled!", Toast.LENGTH_SHORT).show();
            }
        }).setCancelable(false).show();
    }

    private void deleteAllFiles(String path) {
        File[] files = new File(path).listFiles();
        for (File currentFile : files) {
            if (currentFile.isFile()) {
                currentFile.delete();
            }
        }

    }

    private long totalFiles(String path) {
        int count = 0;
        File[] files = new File(path).listFiles();
        for (File currentFile : files) {
            if (currentFile.isFile() && !currentFile.getName().equalsIgnoreCase(".nomedia")) {
                count++;
            }
        }
        return count;
    }

    private long totalSize(String path) {
        long totalLength = 0;
        File[] files = new File(path).listFiles();
        for (File f : files) {
            if (f.isFile() && !f.getName().equalsIgnoreCase(".nomedia")) {
                totalLength += f.length();
            }
        }
        return totalLength;
    }

    private void audioNotesGetListofFiles(String path, ArrayList<File> notesList) {
        File[] files = new File(path).listFiles();
        for (File currentFile : files) {
            if (currentFile.isFile()) {
                notesList.add(currentFile);
            } else if (currentFile.isDirectory()) {
                audioNotesGetListofFiles(currentFile.getAbsolutePath(), notesList);
            }
        }
    }

    private void setTextViews(String message) {
        if (message.equalsIgnoreCase("images")) {
            imageTotalFilesTV.setText("Files: 0");
            imageTotalSizeTV.setText("Total Size: 0.00 MB");
        } else if (message.equalsIgnoreCase("profile pictures")) {
            profilePicTotalFilesTV.setText("Files: 0");
            profilePicTotalSizeTV.setText("Total Size: 0.00 MB");
        } else if (message.equalsIgnoreCase("videos")) {
            videosTotalFilesTV.setText("Files: 0");
            videosTotalSizeTV.setText("Total Size: 0.00 MB");
        } else if (message.equalsIgnoreCase("audio")) {
            audioTotalFilesTV.setText("Files: 0");
            audioTotalSizeTV.setText("Total Size: 0.00 MB");
        } else if (message.equalsIgnoreCase("voice notes")) {
            notesTotalFilesTV.setText("Files: 0");
            notesTotalSizeTV.setText("Total Size: 0.00 MB");
        } else if (message.equalsIgnoreCase("wallpaper")) {
            wallpaperTotalFilesTV.setText("Files: 0");
            wallpaperTotalSizeTV.setText("Total Size: 0.00 MB");
        } else if (message.equalsIgnoreCase("backups")) {
            backupsTotalFilesTV.setText("Files: 0");
            backupsTotalSizeTV.setText("Total Size: 0.00 MB");
        }

    }

    private void createDirs() {
        ArrayList<String> allDirs = new ArrayList<>();
        allDirs.add(audioDirPath);
        allDirs.add(audioSentDirPath);
        allDirs.add(imagesDirPath);
        allDirs.add(imagesSentDirPath);
        allDirs.add(profilePicsDirPath);
        allDirs.add(videoDirPath);
        allDirs.add(videoSentDirPath);
        allDirs.add(voiceNotesDirPath);
        allDirs.add(callsDirPath);
        allDirs.add(videoCacheDirPath);
        allDirs.add(wallpaperDirPath);
        allDirs.add(backupsDirPath);

        ckeckExistanceAndCreate(allDirs);
        cleanVideoCache();
    }

    private void ckeckExistanceAndCreate(ArrayList<String> dirPaths) {
        for (String path : dirPaths) {
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
    }

    protected static void cleanVideoCache() {
        File[] files = new File(videoCacheDirPath).listFiles();
        if (files.length > 0) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

}
