package com.kunal.reader;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;


public class FilesFragment extends Fragment {
    private ListAdapter listAdapter;
    private ListView listView;

    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotifyManager;

    public List<DropboxAPI.Entry> files;
    public DropboxAPI<AndroidAuthSession> DropboxAPI;
    public List<String> folders;
    public List<List<com.dropbox.client2.DropboxAPI.Entry>> file_list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Empty files list
        files = new ArrayList<>();
        return inflater.inflate(R.layout.list_view, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Intialize view
        listView = (ListView) getActivity().findViewById(R.id.listview);
        listView.setAdapter(listAdapter = new ListAdapter());

        // On click listener
        listView.setOnItemClickListener(new FileClickListener());
        listView.setOnItemLongClickListener(new FileLongClickListener());

        // Dropbox module
        DropboxAPI = ((HomeActivity) getActivity()).DropboxAPI;

        folders = ((HomeActivity) getActivity()).folders;
        file_list = ((HomeActivity) getActivity()).files_list;
        loadFiles(folders.get(folders.size()-1));
    }

    public void loadFiles() {
        new ListClass().execute(folders.get(folders.size()-1));
    }

    public void loadFiles(String filename) {
        new ListClass().execute(filename);
    }

    public void goBack() {
        // Remove from lists that we have
        file_list.remove(file_list.size()-1);
        folders.remove(folders.size()-1);

        // Finally set the file to the latest structure
        files = file_list.get(file_list.size()-1);
        listAdapter.notifyDataSetChanged();
    }

    class ListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return files.size();
        }

        @Override
        public String getItem(int position) {
            return files.get(position).path;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                ViewHolder viewHolder = new ViewHolder();
                convertView = getActivity().getLayoutInflater().inflate(R.layout.listview, parent, false);

                viewHolder.textView = (TextView) convertView.findViewById(R.id.firstLine);
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.icon);
                convertView.setTag(viewHolder);
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.textView.setText(files.get(position).path);
            holder.imageView.setImageResource(R.drawable.icon);

            return convertView;
        }

    }

    class ViewHolder {
        TextView textView;
        ImageView imageView;
    }

    class FileClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Add to list of folders traversed
            folders.add(listAdapter.getItem(position));
            new ListClass().execute(listAdapter.getItem(position));
        }
    }

    class FileLongClickListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            mNotifyManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(getActivity());

            mBuilder.setContentTitle("Download")
                    .setContentText("Download in progress")
                    .setSmallIcon(R.drawable.icon);

            new DownloadFile().execute(listAdapter.getItem(position));
            return true;
        }
    }

    // List all folders here
    private class ListClass extends AsyncTask<String, Integer, DropboxAPI.Entry> {
        protected DropboxAPI.Entry doInBackground(String... s) {
            try {
                String hash = null;
                String rev = null;
                DropboxAPI.Entry entry = DropboxAPI.metadata(s[0], 10000, hash, true, rev);
                return entry;
            } catch (DropboxException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(DropboxAPI.Entry e) {
            super.onPostExecute(e);
            if (e == null)
                return;
            files = e.contents;
            file_list.add(files);
            listAdapter.notifyDataSetChanged();
        }
    };

    public class DownloadFile extends AsyncTask<String, Integer, File> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Displays the progress bar for the first time.
            mBuilder.setProgress(100, 0, false);
            mNotifyManager.notify(100, mBuilder.build());
        }

        protected File doInBackground(String... s) {
            File file = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + s[0].replaceAll("/", ""));
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                DropboxAPI.DropboxFileInfo info = DropboxAPI.getFile(s[0], null, outputStream, new ProgressListener(){
                    @Override
                    public long progressInterval() {
                        return 500;
                    }

                    @Override
                    public void onProgress(long totalSize, long downloadedSize) {
                        publishProgress((int)(totalSize*100/downloadedSize));
                    }
                });
            } catch (DropboxException e) {
                e.printStackTrace();
            }
            return file;
        }

        protected void onProgressUpdate(Integer... progress) {
            mBuilder.setProgress(100, progress[0], false);
            mNotifyManager.notify(100, mBuilder.build());
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(File result) {
            super.onPostExecute(result);
            mBuilder.setContentText("Download complete");
            // Removes the progress bar
            mBuilder.setProgress(0, 0, false);
            mNotifyManager.notify(100, mBuilder.build());

            PDFViewFragment pdfFragment = new PDFViewFragment();
            pdfFragment.setArguments(getActivity().getIntent().getExtras());
            // Add the fragment to the 'fragment_container' FrameLayout
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.root_layout, pdfFragment);
            transaction.addToBackStack(null);
            transaction.commit();
            ((HomeActivity) getActivity()).current_file = result;
        }
    }
}