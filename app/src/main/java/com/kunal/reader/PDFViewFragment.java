package com.kunal.reader;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.joanzapata.pdfview.PDFView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class PDFViewFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pdf_viewer, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Render for showing on the screen
        PDFView pdfView = (PDFView) getActivity().findViewById(R.id.pdfview);

        // File to load
        File file = ((HomeActivity) getActivity()).current_file;

        pdfView.fromFile(file)
                .defaultPage(0)
                .showMinimap(true)
                .enableSwipe(true)
                .swipeVertical(true)
//                .onDraw(onDrawListener)
//                .onLoad(onLoadCompleteListener)
//                .onPageChange(onPageChangeListener)
                .load();
    }
}