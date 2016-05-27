package com.kunal.reader;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class HomeActivity extends FragmentActivity {
    final static private String APP_KEY = "l6q5yf2mly4tz01";
    final static private String APP_SECRET = "vvp74yjcgghxo2x";

    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCESS_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";

    public DropboxAPI<AndroidAuthSession> DropboxAPI;

    AppKeyPair appKeys;
    AndroidAuthSession session;

    public List<String> folders;
    public List<List<com.dropbox.client2.DropboxAPI.Entry>> files_list;
    public File current_file;
    public String booksFolder;
    public List<String> books_list;

    public FilesFragment filesFragment;
    public SelectorFragment selectorFragment;
    public PDFViewFragment pdfViewFragment;
    public MyBooksFragment myBooksFragment;

    private GoogleApiClient client;

    private SQLHelper db;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        folders = new ArrayList<>();
        folders.add("/");

        files_list = new ArrayList<>();

        db = new SQLHelper(this);
        // Check if DB has any books. If no, launch the original fragments

        books_list = db.getBooks();
        if (books_list.size() != 0) {
            myBooksFragment = new MyBooksFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.root_layout, myBooksFragment);
            transaction.commit();
        } else {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            filesFragment = new FilesFragment();
            transaction.add(R.id.root_layout, filesFragment);
            transaction.commit();

            selectorFragment = new SelectorFragment();
            transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.root_layout, selectorFragment);
            transaction.commit();
        }

        login();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AndroidAuthSession session = DropboxAPI.getSession();

        // This gets called after completing Authentication
        if (session.authenticationSuccessful()) {
            try {
                // Mandatory call to complete the auth
                session.finishAuthentication();
                storeAuth(session);
            } catch (IllegalStateException e) {
                Log.i("AUTH", "Error authenticating", e);
            }
        }
    }

    private void login() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String token = prefs.getString(ACCESS_SECRET_NAME, null);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        session = new AndroidAuthSession(appKeys);
        DropboxAPI = new DropboxAPI<AndroidAuthSession>(session);

        if (token != null) {
            // Reuse auth
            DropboxAPI.getSession().setOAuth2AccessToken(token);
        } else {
            DropboxAPI.getSession().startOAuth2Authentication(HomeActivity.this);
        }
    }

    public void goBack() {
        filesFragment.goBack();
    }

    public void setFolder() {
        booksFolder = folders.get(folders.size()-1);

        // Call the book viewer fragment
        // Show all files after recursively searching the folder
        BookViewerFragment bookViewerFragment = new BookViewerFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.root_layout, bookViewerFragment);
        transaction.commit();
    }

    private void storeAuth(AndroidAuthSession session) {
        String oauth2AccessToken = session.getOAuth2AccessToken();
        if (oauth2AccessToken != null) {
            SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, "oauth2:");
            edit.putString(ACCESS_SECRET_NAME, oauth2AccessToken);
            edit.apply();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Home Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.kunal.reader/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Home Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.kunal.reader/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}