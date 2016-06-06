package com.artifex.mupdfdemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.IntegerRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class PDFViewActivity extends FragmentActivity {
    public MuPDFCore muPDF;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    private class ScreenSlidePagerAdapter extends PagerAdapter {
        Context context;

        ScreenSlidePagerAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return muPDF.numPages;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LayoutInflater inflater = LayoutInflater.from(context);
            ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.page, container, false);
            ImageViewTouch imageViewTouch = (ImageViewTouch) layout.findViewById(R.id.imageView);
            new LoadImage(imageViewTouch).execute(position);
            container.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
        }

        private class LoadImage extends AsyncTask<Integer, Integer, Bitmap> {
            ImageViewTouch img = null;

                public LoadImage(ImageViewTouch img) {
                    this.img = img;
                }

                protected Bitmap doInBackground(Integer... args) {
                    DisplayMetrics displaymetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                    int width = displaymetrics.widthPixels;
                    int height = (int)(width/muPDF.ratio);

                    Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    try {

                        MuPDFCore.Cookie ck = muPDF.new Cookie();
                        muPDF.drawPage(bm, args[0], width, height, 0, 0, width, height, ck);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return bm;
                }

                protected void onPostExecute(Bitmap bm) {
                    if (bm != null) {
                        img.setImageBitmap(bm);
                    }
                }
            }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfview);

        // Set the image
        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            System.out.println("URI to open is: " + uri);
            try {
                muPDF = new MuPDFCore(uri.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(this);
        mPager.setAdapter(mPagerAdapter);

    }
}
