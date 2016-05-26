package com.kunal.reader;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class BookViewer extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_view, container, false);
    }

    private int download_id = 0;

    private ListAdapter listAdapter;
    private ListView listView;

    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotifyManager;

    public List<com.dropbox.client2.DropboxAPI.Entry> files;
    public DropboxAPI<AndroidAuthSession> DropboxAPI;

    private Queue<String> download_queue;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        files = new ArrayList<>();
        String folder = ((HomeActivity) getActivity()).booksFolder;

        // Intialize view
        listView = (ListView) getActivity().findViewById(R.id.listview);
        listView.setAdapter(listAdapter = new ListAdapter());

        // Dropbox module
        DropboxAPI = ((HomeActivity) getActivity()).DropboxAPI;

        download_queue = new LinkedBlockingDeque<String>() {};

        new ListClass().execute(folder);
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

        private void addFiles(DropboxAPI.Entry e) {
            if (Objects.equals(e.mimeType, "application/pdf")) {
                files.add(e);
                download_queue.add(e.path);
                if (download_id == 0) {
                    download_id += 1;
                    new DownloadFile().execute(download_queue.remove());
                }
                listAdapter.notifyDataSetChanged();
            } else if (e.isDir) {
                // Empty directory
                if (e.contents == null)
                    return;

                for (com.dropbox.client2.DropboxAPI.Entry file : e.contents) {
                    if (file.isDir) {
                        new ListClass().execute(file.path);
                    } else {
                        addFiles(file);
                    }
                }
            }
        }

        protected void onPostExecute(DropboxAPI.Entry e) {
            super.onPostExecute(e);
            if (e == null)
                return;
            addFiles(e);
        }
    };

    public class DownloadFile extends AsyncTask<String, Integer, File> {
        protected File doInBackground(String... s) {
            mNotifyManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(getActivity());
            mBuilder.setContentTitle("Downloading "+s[0])
                    .setContentText("Download in progress")
                    .setSmallIcon(R.drawable.icon);

            // Displays the progress bar for the first time.
            mBuilder.setProgress(100, 0, false);
            mNotifyManager.notify(download_id, mBuilder.build());


            File sdcard = Environment.getExternalStorageDirectory();
            File f = new File(sdcard+"/myBooks");
            if (!f.exists()) {
                boolean created = f.mkdir();
            }

            File file = new File (f + "/" + s[0].replaceAll("/", ""));
            if (file.exists()) {
                return file;
            }
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
            mNotifyManager.notify(download_id, mBuilder.build());
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(File result) {
            super.onPostExecute(result);
            mBuilder.setContentText("Download complete"+result.getName());
            // Removes the progress bar
            mBuilder.setProgress(0, 0, false);
            mNotifyManager.notify(download_id, mBuilder.build());

            try {
                download_id += 1;
                new DownloadFile().execute(download_queue.remove());
            } catch (NoSuchElementException e) {
            }
        }
    }
}
