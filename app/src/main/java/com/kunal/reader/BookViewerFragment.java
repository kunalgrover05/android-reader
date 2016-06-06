package com.kunal.reader;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

//import com.artifex.mupdfdemo.SQLHelper;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingDeque;

public class BookViewerFragment extends Fragment {
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

    private Queue<FileClass> download_queue;
    private Stack<String> folder_files;

//    private SQLHelper db;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        files = new ArrayList<>();
        String folder = ((HomeActivity) getActivity()).booksFolder;

        // Intialize view
        listView = (ListView) getActivity().findViewById(R.id.listview);
        listView.setAdapter(listAdapter = new ListAdapter());

        // Dropbox module
        DropboxAPI = ((HomeActivity) getActivity()).DropboxAPI;

        download_queue = new LinkedBlockingDeque<FileClass>() {};
        folder_files = new Stack<String>();

        // Make a Stack of folders to process
        folder_files.push(folder);

        // Create DB connection
//        db = new SQLHelper(getActivity());

        new ListClass().execute();
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

    class FileClass {
        String path;
        long bytes;
    }

    // List all folders here
    private class ListClass extends AsyncTask<Void, Integer, DropboxAPI.Entry> {
        protected DropboxAPI.Entry doInBackground(Void... s) {
            String folder = folder_files.pop();

            try {
                String hash = null;
                String rev = null;
                DropboxAPI.Entry entry = DropboxAPI.metadata(folder, 10000, hash, true, rev);
                return entry;
            } catch (DropboxException e) {
                e.printStackTrace();
                return null;
            }
        }

        private void addFiles(DropboxAPI.Entry e) {
            if (Objects.equals(e.mimeType, "application/pdf")) {
                files.add(e);

                FileClass c = new FileClass();
                c.bytes = e.bytes;
                c.path = e.path;
                download_queue.add(c);

                listAdapter.notifyDataSetChanged();
            } else if (e.isDir) {
                // Empty directory
                if (e.contents == null)
                    return;

                for (com.dropbox.client2.DropboxAPI.Entry file : e.contents) {
                    if (file.isDir) {
                        folder_files.push(file.path);
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
            if (folder_files.size() > 0) {
                new ListClass().execute();
            } else {
                // Start a download Queue
                new DownloadFile().execute(download_queue.remove());
            }
        }
    };

    public class DownloadFile extends AsyncTask<FileClass, Integer, File> {
        protected File doInBackground(FileClass... s) {
            mNotifyManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(getActivity());
            mBuilder.setContentTitle("Downloading "+s[0].path)
                    .setContentText("Download in progress")
                    .setSmallIcon(R.drawable.icon);

            // Displays the progress bar for the first time.
            mBuilder.setProgress(100, 0, false);
            mNotifyManager.notify(download_id, mBuilder.build());

            File sdcard = Environment.getExternalStorageDirectory();
            File f = new File(sdcard+"/myBooks");
            if (!f.exists() && f.length() != 0) {
                // TODO Add message for failure
                f.mkdir();
            }

            File file = new File (f + "/" + s[0].path.replaceAll("/", ""));
            if (file.exists() && file.length()==s[0].bytes) {
                return file;
            }
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                DropboxAPI.DropboxFileInfo info = DropboxAPI.getFile(s[0].path, null, outputStream, new ProgressListener(){
                    @Override
                    public long progressInterval() {
                        return 100;
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
            Intent resultIntent = new Intent(getActivity(), PDFViewActivity_.class);
            resultIntent.putExtra("file", result);
            // Because clicking the notification opens a new ("special") activity, there's
            // no need to create an artificial back stack.
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            getActivity(),
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            mBuilder.setContentText("Download complete"+result.getName());
            // Removes the progress bar
            mBuilder.setProgress(0, 0, false);
            mNotifyManager.notify(download_id, mBuilder.build());
//            db.addBook(result.getName(), 0);

            try {
                download_id += 1;
                new DownloadFile().execute(download_queue.remove());
            } catch (NoSuchElementException e) {
            }
        }
    }
}
