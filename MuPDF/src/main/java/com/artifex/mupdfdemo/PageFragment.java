package com.artifex.mupdfdemo;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class PageFragment extends Fragment {
    private ImageViewTouch imageViewTouch;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.page, container, false);
        final int page = getArguments().getInt("page");

        imageViewTouch = (ImageViewTouch) rootView.findViewById(R.id.imageView);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        final int width = displaymetrics.widthPixels;
        float ratio = ((PDFViewActivity)getActivity()).muPDF.ratio;
        final int height = (int)(displaymetrics.heightPixels);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                MuPDFCore.Cookie ck = ((PDFViewActivity)getActivity()).muPDF.new Cookie();
                ((PDFViewActivity)getActivity()).muPDF.drawPage(bm, page, width, height, 0, 0, width, height, ck);
                update(bm);
                return null;
            }
        }.execute();
        return rootView;
    }

    public void update(Bitmap bm) {
        imageViewTouch.setImageBitmap(bm);
        imageViewTouch.invalidate();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Set the correct page
        int page = getArguments().getInt("page");

        ImageViewTouch imageViewTouch = (ImageViewTouch) getActivity().findViewById(R.id.imageView);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        float ratio = ((PDFViewActivity)getActivity()).muPDF.ratio;
        int height = (int)(displaymetrics.heightPixels);

        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        MuPDFCore.Cookie ck = ((PDFViewActivity)getActivity()).muPDF.new Cookie();
        ((PDFViewActivity)getActivity()).muPDF.drawPage(bm, page, width, height, 0, 0, width, height, ck);
        imageViewTouch.setImageBitmap(bm);
    }
}
