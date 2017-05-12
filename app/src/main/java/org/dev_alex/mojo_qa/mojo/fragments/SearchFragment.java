package org.dev_alex.mojo_qa.mojo.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.activities.AuthActivity;
import org.dev_alex.mojo_qa.mojo.adapters.SearchFileAdapter;
import org.dev_alex.mojo_qa.mojo.models.File;
import org.dev_alex.mojo_qa.mojo.services.BitmapCacheService;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.Response;

public class SearchFragment extends Fragment {
    private View rootView;
    private RecyclerView recyclerView;
    private DownloadImagesTask downloadTask;
    private ProgressBar progressBar;
    public BitmapCacheService bitmapCacheService;


    public static SearchFragment newInstance() {
        Bundle args = new Bundle();
        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_search, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        progressBar = (ProgressBar) rootView.findViewById(R.id.update_progress);
        bitmapCacheService = new BitmapCacheService();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setListeners();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().findViewById(R.id.main_menu_search_block).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.main_menu_buttons_block).setVisibility(View.GONE);
    }

    private void setListeners() {
        getActivity().findViewById(R.id.search_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((EditText) getActivity().findViewById(R.id.search_text)).setText("");
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                getActivity().findViewById(R.id.search_text).clearFocus();
            }
        });

        getActivity().findViewById(R.id.search_back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        ((EditText) getActivity().findViewById(R.id.search_text)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() >= 3) {

                }
            }
        });
    }

    public class SearchFilesTask extends AsyncTask<Void, Void, Integer> {
        ArrayList<String> fileIdsWithPreviews;
        private ArrayList<File> files;
        private String searchStr;

        public SearchFilesTask(String searchStr) {
            this.searchStr = searchStr;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (downloadTask != null && downloadTask.getStatus() != AsyncTask.Status.FINISHED)
                downloadTask.cancel(false);

            rootView.findViewById(R.id.empty_block).setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                ArrayList<File> allFiles = new ArrayList<>();

                files = new ArrayList<>();

                String url = "/api/search/nodes?term=*" + searchStr + "*";
                Response response = RequestService.createGetRequest(url);

                if (response.code() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    JSONObject resultJson = new JSONObject(response.body().string());
                    JSONArray tasksEntriesJson = resultJson.getJSONObject("list").getJSONArray("entries");

                    for (int i = 0; i < tasksEntriesJson.length(); i++)
                        allFiles.add(mapper.readValue(tasksEntriesJson.getJSONObject(i).getJSONObject("entry").toString(), File.class));

                    fileIdsWithPreviews = new ArrayList<>();
                    for (File file : allFiles) {
                        if (file.nodeType.equals("cm:content"))
                            fileIdsWithPreviews.add(file.id);

                        if (file.isFolder) {

                        } else
                            files.add(file);
                    }
                }

                return response.code();
            } catch (Exception exc) {
                exc.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            super.onPostExecute(responseCode);
            progressBar.setVisibility(View.GONE);

            if (responseCode == null)
                Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_LONG).show();
            else if (responseCode == 401) {
                startActivity(new Intent(getContext(), AuthActivity.class));
                getActivity().finish();
            } else {
                downloadTask = new DownloadImagesTask(fileIdsWithPreviews);
                downloadTask.execute();

                if (files.isEmpty()) {
                    rootView.findViewById(R.id.empty_block).setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else
                    recyclerView.setVisibility(View.VISIBLE);


                recyclerView.setAdapter(new SearchFileAdapter(SearchFragment.this, files));
            }
        }
    }

    private class DownloadImagesTask extends AsyncTask<Void, Void, Void> {
        private ArrayList<String> fileIds;

        DownloadImagesTask(ArrayList<String> fileIds) {
            this.fileIds = fileIds;
        }

        DownloadImagesTask(ArrayList<File> files, boolean withCheck) {
            fileIds = new ArrayList<>();
            for (File file : files)
                if (file.nodeType.equals("cm:content"))
                    fileIds.add(file.id);
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (String fileId : fileIds) {
                if (!bitmapCacheService.hasThumbnailInMemCache(fileId))
                    try {
                        Response thumbResponse = RequestService.createGetRequest("/api/fs/thumbnail/" + fileId);
                        byte[] imageBytes = thumbResponse.body().bytes();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        if (bitmap != null)
                            bitmapCacheService.addThumbnailToMemoryCache(fileId, bitmap);
                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }
                if (!bitmapCacheService.hasPreviewInMemCache(fileId))
                    try {
                        Response previewResponse = RequestService.createGetRequest("/api/fs/preview/" + fileId);
                        byte[] imageBytes = previewResponse.body().bytes();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        if (bitmap != null)
                            bitmapCacheService.addPreviewToMemoryCache(fileId, bitmap);
                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }
                publishProgress();
                if (isCancelled())
                    return null;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            if (recyclerView.getAdapter() != null)
                recyclerView.getAdapter().notifyDataSetChanged();
        }

    }
}
