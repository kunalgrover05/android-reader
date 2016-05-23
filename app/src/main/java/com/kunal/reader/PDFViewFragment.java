package com.kunal.reader;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.ButtonBarLayout;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class PDFViewFragment extends Fragment {
    private int currentPage;
    private PdfRenderer renderer;
    private boolean color_mode;
    private ImageViewTouch pdfView;

    private static float[] NEGATIVE = {
        -1, 0, 0, 0, 255,
        0, -1, 0, 0, 255,
        0, 0, -1, 0, 255,
        0, 0, 0, 1, 0
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pdf_viewer, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Button color_button = (Button) getActivity().findViewById(R.id.color_mode);
        color_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                color_mode = !color_mode;
                modify();
            }
        });

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

    private void modify() {
        if(!color_mode) {
            pdfView.clearColorFilter();
        } else {
            pdfView.setColorFilter(new ColorMatrixColorFilter(NEGATIVE));
        }
    }

    private void loadPage() {
        if (renderer.getPageCount() <= currentPage) return;

        PdfRenderer.Page page = renderer.openPage(currentPage);

        // Render for showing on the screen
        pdfView = (ImageViewTouch) getActivity().findViewById(R.id.pdfpage);
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

        // Background for the image
        Bitmap image = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(),mBitmap.getConfig());
        image.eraseColor(Color.WHITE);
        Canvas canvas = new Canvas(image);
        canvas.drawBitmap(mBitmap, 0f, 0f, null);

        pdfView.setImageDrawable(new BitmapDrawable(getResources(), image));
        if (color_mode) {
            pdfView.setColorFilter(new ColorMatrixColorFilter(NEGATIVE));
        }
        page.close();
    }
}