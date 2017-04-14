package org.dev_alex.mojo_qa.mojo.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.activities.AuthActivity;
import org.dev_alex.mojo_qa.mojo.adapters.FileAdapter;
import org.dev_alex.mojo_qa.mojo.adapters.FolderAdapter;
import org.dev_alex.mojo_qa.mojo.custom_views.RelativeLayoutWithPopUp;
import org.dev_alex.mojo_qa.mojo.models.File;
import org.dev_alex.mojo_qa.mojo.models.FileSystemStackEntry;
import org.dev_alex.mojo_qa.mojo.services.BitmapCacheService;
import org.dev_alex.mojo_qa.mojo.services.BlurHelper;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.Response;

public class DocumentsFragment extends Fragment {
    private RelativeLayoutWithPopUp rootView;
    private RelativeLayout popupWindow;

    private ProgressDialog loopDialog;
    private RecyclerView folderRecyclerView;
    private RecyclerView filesRecyclerView;

    private boolean isGridView;
    private FileAdapter fileAdapter;
    private FolderAdapter folderAdapter;

    private DownloadImagesTask downloadTask;
    private ArrayList<FileSystemStackEntry> foldersStack;
    public BitmapCacheService bitmapCacheService;

    private String selectedItemId;

    public static DocumentsFragment newInstance() {
        Bundle args = new Bundle();
        DocumentsFragment fragment = new DocumentsFragment();
        fragment.setArguments(args);
        return fragment;
    }


    private void initDialog() {
        loopDialog = new ProgressDialog(getContext(), R.style.ProgressDialogStyle);
        loopDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loopDialog.setMessage(getString(R.string.loading_please_wait));
        loopDialog.setIndeterminate(true);
        loopDialog.setCanceledOnTouchOutside(false);
        loopDialog.setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (RelativeLayoutWithPopUp) inflater.inflate(R.layout.fragment_documents, container, false);

        popupWindow = (RelativeLayout) rootView.findViewById(R.id.popup_layout);
        rootView.addPopUpWindow(popupWindow);
        popupWindow.setVisibility(View.GONE);

        bitmapCacheService = new BitmapCacheService();

        isGridView = false;
        folderRecyclerView = (RecyclerView) rootView.findViewById(R.id.folders_recycler_view);
        folderRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        filesRecyclerView = (RecyclerView) rootView.findViewById(R.id.files_recycler_view);
        filesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setupHeader();
        initDialog();
        setListeners();

        new GetFilesTask(null, null).execute();
        return rootView;
    }

    private void setupHeader() {
        ((TextView) getActivity().findViewById(R.id.title)).setText(getString(R.string.documents));
        getActivity().findViewById(R.id.back_btn).setVisibility(View.GONE);

        getActivity().findViewById(R.id.grid_btn).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.sandwich_btn).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.group_by_btn).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.search_btn).setVisibility(View.VISIBLE);

        getActivity().findViewById(R.id.grid_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swapAdapterType();
            }
        });

        getActivity().findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popFileStack();
            }
        });
    }

    private void updateHeader() {
        if (getActivity() != null) {
            if (foldersStack != null && foldersStack.size() > 1) {
                ((TextView) getActivity().findViewById(R.id.title))
                        .setText(foldersStack.get(foldersStack.size() - 1).parentName);

                getActivity().findViewById(R.id.sandwich_btn).setVisibility(View.GONE);
                getActivity().findViewById(R.id.back_btn).setVisibility(View.VISIBLE);

            } else {
                ((TextView) getActivity().findViewById(R.id.title)).setText(getString(R.string.documents));
                getActivity().findViewById(R.id.sandwich_btn).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.back_btn).setVisibility(View.GONE);
            }
        }
    }

    private void setListeners() {
        rootView.setFocusableInTouchMode(true);
        rootView.requestFocus();
        rootView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP)
                    if (keyCode == KeyEvent.KEYCODE_BACK)
                        return popFileStack();

                return false;
            }
        });

        final NestedScrollView scrollView = (NestedScrollView) rootView.findViewById(R.id.scroll_view);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                filesRecyclerView.setNestedScrollingEnabled(!scrollView.canScrollVertically(1));
            }
        });

        rootView.findViewById(R.id.create_dit_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateDirDialog();
            }
        });
    }

    private void swapAdapterType() {
        isGridView = !isGridView;
        ((ImageView) getActivity().findViewById(R.id.list_grid_icon))
                .setImageResource(isGridView ? R.drawable.list_icon : R.drawable.grid_icon);

        if (isGridView) {
            folderRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            filesRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        } else {
            folderRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            filesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        if (folderAdapter != null && fileAdapter != null)
            setAdapters(foldersStack.get(foldersStack.size() - 1).files, foldersStack.get(foldersStack.size() - 1).folders);
    }

    private void setAdapters(ArrayList<File> files, ArrayList<File> folders) {
        if ((files == null || files.isEmpty()) && (folders == null || folders.isEmpty()))
            rootView.findViewById(R.id.empty_block).setVisibility(View.VISIBLE);
        else
            rootView.findViewById(R.id.empty_block).setVisibility(View.GONE);


        if (files == null || files.isEmpty()) {
            rootView.findViewById(R.id.files_block).setVisibility(View.GONE);
            rootView.findViewById(R.id.files_recycler_view).setVisibility(View.GONE);

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) folderRecyclerView.getLayoutParams();
            layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
            folderRecyclerView.setLayoutParams(layoutParams);
        } else {
            rootView.findViewById(R.id.files_block).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.files_recycler_view).setVisibility(View.VISIBLE);

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) folderRecyclerView.getLayoutParams();
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            folderRecyclerView.setLayoutParams(layoutParams);
        }

        if (folders == null || folders.isEmpty()) {
            rootView.findViewById(R.id.folders_block).setVisibility(View.GONE);
            rootView.findViewById(R.id.folders_recycler_view).setVisibility(View.GONE);
        } else {
            rootView.findViewById(R.id.folders_block).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.folders_recycler_view).setVisibility(View.VISIBLE);
        }

        folderAdapter = new FolderAdapter(this, folders, isGridView);
        fileAdapter = new FileAdapter(DocumentsFragment.this, files, isGridView);

        folderRecyclerView.setAdapter(folderAdapter);
        filesRecyclerView.setAdapter(fileAdapter);
    }

    private void showRenameDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        final View dialogView = layoutInflater.inflate(R.layout.dialog_folder_management, null, false);
        final AlertDialog renameDialog = new AlertDialog.Builder(getContext()).create();
        renameDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        renameDialog.setView(dialogView);
        renameDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialogView.findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renameDialog.dismiss();
            }
        });
        dialogView.findViewById(R.id.right_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renameDialog.dismiss();
            }
        });

        renameDialog.show();
    }

    private void showCreateDirDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        final View dialogView = layoutInflater.inflate(R.layout.dialog_folder_management, null, false);

        ((TextView) dialogView.findViewById(R.id.dialog_title)).setText(R.string.new_folder);
        ((EditText) dialogView.findViewById(R.id.text_input)).setHint(R.string.folder_name);
        ((TextView) dialogView.findViewById(R.id.right_btn_text)).setText(R.string.create);

        final AlertDialog createDirDialog = createDialogWithBlur(dialogView);

        dialogView.findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDirDialog.dismiss();
            }
        });
        dialogView.findViewById(R.id.right_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDirDialog.dismiss();
            }
        });
    }

    private AlertDialog createDialogWithBlur(View dialogView) {
        final AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setView(dialogView);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                getActivity().findViewById(R.id.blur_background).setVisibility(View.GONE);
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Bitmap screenShot = BlurHelper.takeScreenShot(getActivity());
        Bitmap blurScreenShot = BlurHelper.fastBlur(screenShot, 0.14f, 10);
        getActivity().findViewById(R.id.blur_background).setVisibility(View.VISIBLE);
        ((ImageView) getActivity().findViewById(R.id.blur_background)).setImageBitmap(blurScreenShot);

        dialog.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);
        return dialog;
    }

    private boolean popFileStack() {
        if (foldersStack != null && foldersStack.size() > 1) {
            foldersStack.remove(foldersStack.size() - 1);
            updateHeader();
            setAdapters(foldersStack.get(foldersStack.size() - 1).files, foldersStack.get(foldersStack.size() - 1).folders);

            if (downloadTask != null && downloadTask.getStatus() != AsyncTask.Status.FINISHED)
                downloadTask.cancel(false);

            downloadTask = new DownloadImagesTask(foldersStack.get(foldersStack.size() - 1).files, true);
            downloadTask.execute();
            return true;
        } else
            return false;
    }

    public void showPopUpWindow(String itemId) {
        selectedItemId = itemId;
        popupWindow.setVisibility(View.VISIBLE);
    }

    public class GetFilesTask extends AsyncTask<Void, Void, Integer> {
        ArrayList<String> fileIdsWithPreviews;
        private ArrayList<File> folders, files;
        private String fileId;
        private String fileName;

        public GetFilesTask(String fileId, String fileName) {
            this.fileId = fileId;
            this.fileName = fileName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (downloadTask != null && downloadTask.getStatus() != AsyncTask.Status.FINISHED)
                downloadTask.cancel(false);
            loopDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                ArrayList<File> allFiles = new ArrayList<>();

                files = new ArrayList<>();
                folders = new ArrayList<>();

                String url;
                if (fileId == null) {
                    url = "/api/fs/childrenmy";
                    foldersStack = new ArrayList<>();
                } else
                    url = "/api/fs/children/" + fileId;

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

                        if (file.isFolder)
                            folders.add(file);
                        else
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
            if (loopDialog != null && loopDialog.isShowing())
                loopDialog.dismiss();

            if (responseCode == null)
                Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_LONG).show();
            else if (responseCode == 401) {
                startActivity(new Intent(getContext(), AuthActivity.class));
                getActivity().finish();
            } else {
                downloadTask = new DownloadImagesTask(fileIdsWithPreviews);
                downloadTask.execute();

                foldersStack.add(new FileSystemStackEntry(folders, files, fileId == null ? "" : fileName));

                setAdapters(files, folders);
                updateHeader();
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
            fileAdapter.notifyDataSetChanged();
            folderAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (downloadTask != null && downloadTask.getStatus() != AsyncTask.Status.FINISHED)
            downloadTask.cancel(false);
    }
}
