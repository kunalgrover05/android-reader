package com.kunal.reader;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class PDFViewFragment extends Fragment {
    private int currentPage;
    private PdfRenderer renderer;

    private GestureDetectorCompat mDetector;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pdf_viewer, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // File to load
        File file = ((HomeActivity) getActivity()).current_file;

        ParcelFileDescriptor pfd = null;
        try {
            pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            renderer = new PdfRenderer(pfd);
        } catch (IOException e) {
            e.printStackTrace();
        }

        currentPage = 0;
        loadPage();
    }

    private void loadPage() {
        PdfRenderer.Page page = renderer.openPage(currentPage);

        // Render for showing on the screen
        ImageViewTouch pdfView = (ImageViewTouch) getActivity().findViewById(R.id.pdfpage);
        pdfView.setSingleTapListener(
            new ImageViewTouch.OnImageViewTouchSingleTapListener() {
                @Override
                public void onSingleTapConfirmed() {
                    currentPage += 1;
                    loadPage();
                }
            }
        );

        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        Bitmap mBitmap = Bitmap.createBitmap(screenWidth * 2, screenHeight * 2,
                Bitmap.Config.ARGB_8888);
        page.render(mBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        pdfView.setImageBitmap(mBitmap);

        page.close();
    }
}