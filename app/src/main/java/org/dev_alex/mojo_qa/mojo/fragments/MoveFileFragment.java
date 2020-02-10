package org.dev_alex.mojo_qa.mojo.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.activities.AuthActivity;
import org.dev_alex.mojo_qa.mojo.activities.MainActivity;
import org.dev_alex.mojo_qa.mojo.models.File;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.Response;

public class MoveFileFragment extends Fragment {
    private int defaultLeftOffsetDp;

    private View rootView;
    private ArrayList<File> fileList;
    private ProgressDialog loopDialog;
    private String selectedFolderId;
    private ArrayList<FolderEntry> folders;
    private LayoutInflater inflater;

    private LinearLayout rootContainer;
    private ScrollView scrollView;
    private HorizontalScrollView horizontalScrollView;


    public static MoveFileFragment newInstance(ArrayList<File> fileList) {
        Bundle args = new Bundle();
        try {
            args.putString("files_array", new ObjectMapper().writeValueAsString(fileList));
        } catch (Exception exc) {
            exc.printStackTrace();
        }


        MoveFileFragment fragment = new MoveFileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.inflater = inflater;
        if (rootView == null) {
            try {
                rootView = inflater.inflate(R.layout.fragment_move_file, container, false);
                rootContainer = (LinearLayout) rootView.findViewById(R.id.folders_container);
                scrollView = (ScrollView) rootView.findViewById(R.id.scroll_view);
                horizontalScrollView = (HorizontalScrollView) rootView.findViewById(R.id.horizontal_scroll_view);

                defaultLeftOffsetDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, getResources().getDisplayMetrics());
                fileList = new ObjectMapper().readValue(getArguments().getString("files_array"), new TypeReference<ArrayList<File>>() {
                });
                setListeners();
                setupHeader();
                initDialog();
                ((MainActivity) getActivity()).drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                folders = new ArrayList<>();
                new GetFoldersTask(null).execute();
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
        return rootView;
    }


    private void setListeners() {
        rootView.findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });

        rootView.findViewById(R.id.move_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MoveFileTask().execute();
            }
        });
    }

    private void setupHeader() {
        ((TextView) getActivity().findViewById(R.id.title)).setText(getString(R.string.move));
        getActivity().findViewById(R.id.back_btn).setVisibility(View.VISIBLE);

        getActivity().findViewById(R.id.grid_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.sandwich_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.group_by_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.search_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.notification_btn).setVisibility(View.GONE);

        getActivity().findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });
    }

    private void close() {
        getActivity().getSupportFragmentManager().popBackStack();
        ((MainActivity) getActivity()).drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    private void initDialog() {
        loopDialog = new ProgressDialog(getContext(), R.style.ProgressDialogStyle);
        loopDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loopDialog.setMessage(getString(R.string.loading_please_wait));
        loopDialog.setIndeterminate(true);
        loopDialog.setCanceledOnTouchOutside(false);
        loopDialog.setCancelable(false);
    }

    private void drawFolders() {
        rootContainer.removeAllViewsInLayout();
        rootContainer.setMinimumWidth(horizontalScrollView.getWidth());
        drawFolderChildren("", defaultLeftOffsetDp);
    }

    private void drawFolderChildren(String id, int offset) {
        ArrayList<FolderEntry> childFolders = getFoldersByParentId(id);
        for (final FolderEntry folder : childFolders) {
            LinearLayout folderView = (LinearLayout) inflater.inflate(R.layout.card_folder_no_background, rootContainer, false);

            String folderName = folder.name.length() > 20 ? folder.name.substring(0, 20) + "..." : folder.name;
            ((TextView) folderView.findViewById(R.id.folder_name)).setText(folderName);

            if (folder.id.equals(selectedFolderId))
                folderView.setBackgroundColor(Color.parseColor("#ffdfd7e2"));

            folderView.setPadding(offset, 0, 0, 0);

            folderView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedFolderId = folder.id;
                    if (folder.isChildrenDownloaded) {
                        folder.isExpanded = !folder.isExpanded;
                        drawFolders();
                    } else
                        new GetFoldersTask(folder.id).execute();
                }
            });

            int separatorHeightDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
            View separator = new View(getContext());
            separator.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, separatorHeightDp));
            separator.setBackgroundColor(Color.parseColor("#ffdfd7e2"));
            rootContainer.addView(separator);

            rootContainer.addView(folderView);
            if (folder.isChildrenDownloaded && folder.hasChildren() && folder.isExpanded)
                drawFolderChildren(folder.id, offset + defaultLeftOffsetDp);
        }
    }

    private void setFolderStateDownloaded(String id, boolean state) {
        for (FolderEntry folder : folders)
            if (folder.id.equals(id))
                folder.isChildrenDownloaded = state;
    }

    private void setFolderStateExpanded(String id, boolean state) {
        for (FolderEntry folder : folders)
            if (folder.id.equals(id))
                folder.isExpanded = state;
    }

    private ArrayList<FolderEntry> getFoldersByParentId(String parentId) {
        ArrayList<FolderEntry> searchResult = new ArrayList<>();
        for (FolderEntry folder : folders)
            if (folder.parentId.equals(parentId))
                searchResult.add(folder);

        return searchResult;
    }

    private class GetFoldersTask extends AsyncTask<Void, Void, Integer> {
        private String folderId;

        GetFoldersTask(String folderId) {
            this.folderId = folderId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loopDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                String url;
                if (folderId == null) {
                    url = "/api/user/root";
                } else
                    url = "/api/fs/children/" + folderId;

                Response response = RequestService.createGetRequest(url);

                if (response.code() == 200) {
                    JSONArray tasksEntriesJson;
                    if (folderId == null) {
                        tasksEntriesJson = new JSONArray(response.body().string());
                        for (int i = 0; i < tasksEntriesJson.length(); i++) {
                            JSONObject object = tasksEntriesJson.getJSONObject(i);
                            folders.add(new FolderEntry(object.getString("id"), "", object.getString("name"), false, false));
                        }
                    } else {
                        JSONObject resultJson = new JSONObject(response.body().string());
                        tasksEntriesJson = resultJson.getJSONObject("list").getJSONArray("entries");
                        for (int i = 0; i < tasksEntriesJson.length(); i++) {
                            JSONObject object = tasksEntriesJson.getJSONObject(i).getJSONObject("entry");
                            if (object.getBoolean("isFolder"))
                                folders.add(new FolderEntry(object.getString("id"), folderId == null ? "" : folderId, object.getString("name"), false, false));
                        }
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
                if (loopDialog != null && loopDialog.isShowing())
                    loopDialog.dismiss();

                if (responseCode == null) {
                    Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_LONG).show();
                } else if (responseCode == 401) {
                    startActivity(new Intent(getContext(), AuthActivity.class));
                    getActivity().finish();
                } else {
                    if (folderId == null && !folders.isEmpty())
                        selectedFolderId = folders.get(folders.size() - 1).id;

                    if (folderId != null) {
                        setFolderStateDownloaded(folderId, true);
                        setFolderStateExpanded(folderId, true);
                    }

                    drawFolders();
                }
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }

    private class MoveFileTask extends AsyncTask<Void, Void, Integer> {
        private final int NO_ERRORS_CODE = 1234;
        private int movedFilesCt;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loopDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                movedFilesCt = 0;
                for (File file : fileList) {
                    String url = "/api/fs/move/" + file.id;
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("parent_id", selectedFolderId);

                    Response response = RequestService.createPostRequest(url, jsonObject.toString());
                    if (response.code() == 401)
                        return response.code();

                    movedFilesCt++;
                }
                return NO_ERRORS_CODE;
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
                } else if (responseCode == 409)
                    Toast.makeText(getContext(), R.string.file_or_folder_already_exists, Toast.LENGTH_SHORT).show();
                else if (responseCode == NO_ERRORS_CODE) {
                    if (movedFilesCt == 0)
                        Toast.makeText(getContext(), R.string.move_error, Toast.LENGTH_SHORT).show();
                    else if (movedFilesCt == fileList.size())
                        Toast.makeText(getContext(), R.string.moved_successfully, Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getContext(), R.string.not_all_files_or_folders_were_moved_due_to_conflicts, Toast.LENGTH_SHORT).show();

                    close();
                } else
                    Toast.makeText(getContext(), R.string.unknown_error, Toast.LENGTH_SHORT).show();
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }

    private class FolderEntry {
        String id;
        String parentId;
        String name;
        boolean isExpanded;
        boolean isChildrenDownloaded;

        FolderEntry(String id, String parentId, String name, boolean isExpanded, boolean isChildrenDownloaded) {
            this.id = id;
            this.parentId = parentId;
            this.name = name;
            this.isExpanded = isExpanded;
            this.isChildrenDownloaded = isChildrenDownloaded;
        }

        boolean hasChildren() {
            return !getFoldersByParentId(id).isEmpty();
        }
    }
}
