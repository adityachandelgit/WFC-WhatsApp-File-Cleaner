package com.retrospectivecreations.wfc;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.appnext.appnextsdk.Appnext;
import com.astuetz.PagerSlidingTabStrip;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class ImageActivity extends ActionBarActivity {
    private PagerSlidingTabStrip tabs;
    ViewPager pager;
    MyPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        pager = (ViewPager) findViewById(R.id.pager);
        tabs.setShouldExpand(true);
        tabs.setIndicatorColor(0xFFbc3874);

        /*AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);*/

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        if (width <= 480) {
            tabs.setIndicatorHeight(8);
        }
        if (width > 480 && width <= 800) {
            tabs.setIndicatorHeight(12);
        }
        if (width > 800) {
            tabs.setIndicatorHeight(20);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tabs.setElevation(20);
        }

        adapter = new MyPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);

        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        pager.setPageMargin(pageMargin);
        tabs.setViewPager(pager);
    }


    public class MyPagerAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = {"Received Images", "Sent Images"};

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new ReceivedImageFragment();
            } else {
                return new SentImageFragment();
            }
        }
    }


}
