package com.kunal.reader;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import java.io.File;

public class PDFViewActivity extends FragmentActivity {
    public PDFViewFragment pdfViewFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        pdfViewFragment = new PDFViewFragment();

        // File to load
        Bundle b = this.getIntent().getExtras();
        pdfViewFragment.setArguments(b);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.root_layout, pdfViewFragment);
        transaction.commit();
    }
}