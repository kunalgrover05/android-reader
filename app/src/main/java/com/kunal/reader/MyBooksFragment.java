package com.kunal.reader;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.artifex.mupdfdemo.PDFViewActivity;
import com.artifex.mupdfdemo.SQLHelper;

import java.io.File;
import java.util.List;


public class MyBooksFragment extends Fragment {
    private SQLHelper db;
    private List<String> books_list;
    private ListView listView;
    private ListAdapter listAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_view, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        books_list = ((HomeActivity)getActivity()).books_list;

        // Intialize view
        listView = (ListView) getActivity().findViewById(R.id.listview);
        listView.setAdapter(listAdapter = new ListAdapter());
        listView.setOnItemClickListener(new FileClickListener());
    }

    class ListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return books_list.size();
        }

        @Override
        public String getItem(int position) {
            return books_list.get(position);
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
            holder.textView.setText(books_list.get(position));
            holder.imageView.setImageResource(R.drawable.icon);

            return convertView;
        }

    }

    class ViewHolder {
        TextView textView;
        ImageView imageView;
    }



    private class FileClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // File to load
            File file = new File(Environment.getExternalStorageDirectory() + "/myBooks/" + books_list.get(position));

            // Launch Viewer activity
            Intent fileIntent = new Intent(getActivity(), PDFViewActivity.class);
            fileIntent.setAction(Intent.ACTION_VIEW);
            fileIntent.setData(Uri.parse(file.getPath()));
            startActivity(fileIntent);
        }
    }
}
