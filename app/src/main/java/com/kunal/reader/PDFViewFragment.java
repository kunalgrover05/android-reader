package com.kunal.reader;
import android.content.Intent;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.artifex.mupdfdemo.SQLHelper;

import java.io.File;

import com.artifex.mupdfdemo.MuPDFActivity;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class PDFViewFragment extends Fragment {
    private int currentPage;
    private PdfRenderer renderer;
    private boolean color_mode;
    private ImageViewTouch pdfView;
    private SQLHelper db;
    private String fileName;

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
        Bundle b = getActivity().getIntent().getExtras();
        File file = (File) b.get("file");
        fileName = file.getName();
        db = new SQLHelper(getActivity());
        currentPage = db.getBook(fileName);
        db.addBook(fileName, currentPage);

//        PDFView p = (PDFView)getActivity().findViewById(R.id.pdfpage_pdfview);
//        p.fromFile(file)
//                .showMinimap(true)
//                .defaultPage(db.getBook(fileName))
//                .enableSwipe(true)
//                .onPageChange(new OnPageChangeListener() {
//                    @Override
//                    public void onPageChanged(int page, int pageCount) {
//                        db.addBook(fileName, page);
//                    }
//                })
//                .load();

        Intent fileIntent = new Intent(getActivity(), MuPDFActivity.class);
        fileIntent.setAction(Intent.ACTION_VIEW);
        fileIntent.setData(Uri.parse(file.getPath()));
        fileIntent.putExtra("Mode", false);
        startActivity(fileIntent);

//        ParcelFileDescriptor pfd = null;
//        try {
//            pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            renderer = new PdfRenderer(pfd);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        loadPage();
    }

    private void modify() {
        if(!color_mode) {
            pdfView.clearColorFilter();
        } else {
            pdfView.setColorFilter(new ColorMatrixColorFilter(NEGATIVE));
        }
    }

//    private void loadPage() {
//        if (renderer.getPageCount() <= currentPage) return;
//
//        PdfRenderer.Page page = renderer.openPage(currentPage);
//
//        // Render for showing on the screen
//        pdfView = (ImageViewTouch) getActivity().findViewById(R.id.pdfpage);
//        pdfView.setSingleTapListener(
//            new ImageViewTouch.OnImageViewTouchSingleTapListener() {
//                @Override
//                public void onSingleTapConfirmed() {
//                    currentPage += 1;
//                    db.addBook(fileName, currentPage);
//                    loadPage();
//                }
//            }
//        );
//
//        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
//        int screenWidth = displayMetrics.widthPixels;
//        int screenHeight = displayMetrics.heightPixels;
//
//        Bitmap mBitmap = Bitmap.createBitmap(screenWidth * 2, screenHeight * 2,
//                Bitmap.Config.ARGB_8888);
//        page.render(mBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
//
//        // Background for the image
//        Bitmap image = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(),mBitmap.getConfig());
//        image.eraseColor(Color.WHITE);
//        Canvas canvas = new Canvas(image);
//        canvas.drawBitmap(mBitmap, 0f, 0f, null);
//
//        pdfView.setImageDrawable(new BitmapDrawable(getResources(), image));
//        if (color_mode) {
//            pdfView.setColorFilter(new ColorMatrixColorFilter(NEGATIVE));
//        }
//        page.close();
//    }
}