package org.dev_alex.mojo_qa.mojo.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.activities.AuthActivity;
import org.dev_alex.mojo_qa.mojo.adapters.SearchFileAdapter;
import org.dev_alex.mojo_qa.mojo.models.File;
import org.dev_alex.mojo_qa.mojo.services.BitmapCacheService;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.dev_alex.mojo_qa.mojo.services.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

public class SearchFragment extends Fragment {
    private final int FILE_OPEN_REQUEST_CODE = 1;

    private View rootView;
    private RecyclerView recyclerView;
    private RelativeLayout recyclerViewBlock;
    private DownloadImagesTask downloadTask;
    private ProgressBar progressBar;
    private ProgressDialog loopDialog;
    private SearchFilesTask searchTask = null;

    public BitmapCacheService bitmapCacheService;
    private java.io.File openingFile;


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
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.colorAccent), android.graphics.PorterDuff.Mode.MULTIPLY);

        bitmapCacheService = new BitmapCacheService();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewBlock = (RelativeLayout) rootView.findViewById(R.id.recycler_view_block);

        Utils.setupCloseKeyboardUI(getActivity(), rootView);
        initDialog();
        setListeners();
        return rootView;
    }

    private void initDialog() {
        loopDialog = new ProgressDialog(getContext(), R.style.ProgressDialogStyle);
        loopDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loopDialog.setMessage(getString(R.string.loading_please_wait));
        loopDialog.setIndeterminate(true);
        loopDialog.setCanceledOnTouchOutside(false);
        loopDialog.setCancelable(false);
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
                Utils.hideSoftKeyboard(getActivity());
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
                    if (searchTask != null && searchTask.getStatus() != AsyncTask.Status.FINISHED)
                        searchTask.cancel(true);
                    searchTask = new SearchFilesTask(s.toString());
                    searchTask.execute();
                }
            }
        });
    }

    private class SearchFilesTask extends AsyncTask<Void, Void, Integer> {
        ArrayList<String> fileIdsWithPreviews;
        private ArrayList<File> files;
        private String searchStr;

        SearchFilesTask(String searchStr) {
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
                            Log.d("mojo-log", "folder found");
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
            try {
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
                        recyclerViewBlock.setVisibility(View.GONE);
                    } else
                        recyclerViewBlock.setVisibility(View.VISIBLE);


                    recyclerView.setAdapter(new SearchFileAdapter(SearchFragment.this, files));
                }
            } catch (Exception exc) {
                exc.printStackTrace();
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

    public class OpenFileTask extends AsyncTask<Void, Void, Integer> {
        private File file;
        private java.io.File resultFile;

        public OpenFileTask(File file) {
            this.file = file;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loopDialog.show();
            String resultFileName = UUID.randomUUID().toString() + "." + Utils.getFileExtension(file.name);
            resultFile = new java.io.File(getContext().getCacheDir(), resultFileName);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                String url = "/api/fs/content/" + file.id;
                Response response = RequestService.createGetRequest(url);

                if (response.code() == 200) {
                    BufferedSink sink = Okio.buffer(Okio.sink(resultFile));
                    sink.writeAll(response.body().source());
                    sink.close();
                }
                response.body().close();

                return response.code();
            } catch (Exception exc) {
                exc.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            super.onPostExecute(responseCode);
            try {
                if (loopDialog != null && loopDialog.isShowing())
                    loopDialog.dismiss();

                if (responseCode == null)
                    Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_LONG).show();
                else if (responseCode == 401) {
                    startActivity(new Intent(getContext(), AuthActivity.class));
                    getActivity().finish();
                } else if (responseCode == 200) {
                    try {
                        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                        Uri fileUri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", resultFile);
                        viewIntent.setDataAndType(fileUri, Utils.getMimeType(resultFile.getAbsolutePath()));
                        viewIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        openingFile = resultFile;
                        startActivityForResult(viewIntent, FILE_OPEN_REQUEST_CODE);
                    } catch (Exception exc) {
                        exc.printStackTrace();
                        Toast.makeText(getContext(), R.string.no_app_to_open, Toast.LENGTH_LONG).show();
                        try {
                            resultFile.delete();
                        } catch (Exception exc1) {
                            exc1.printStackTrace();
                        }
                    }
                } else
                    Toast.makeText(getContext(), R.string.unknown_error, Toast.LENGTH_LONG).show();
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }
}
