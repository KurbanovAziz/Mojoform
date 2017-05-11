package org.dev_alex.mojo_qa.mojo.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import org.dev_alex.mojo_qa.mojo.custom_views.MaxHeightRecycleView;
import org.dev_alex.mojo_qa.mojo.custom_views.RelativeLayoutWithPopUp;
import org.dev_alex.mojo_qa.mojo.models.File;
import org.dev_alex.mojo_qa.mojo.models.FileSystemStackEntry;
import org.dev_alex.mojo_qa.mojo.services.BitmapCacheService;
import org.dev_alex.mojo_qa.mojo.services.BlurHelper;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import okhttp3.Response;

public class DocumentsFragment extends Fragment {
    private final int SORT_BY_NAME = 1;
    private final int SORT_BY_CREATED_AT = 2;
    private final int SORT_BY_UPDATED_AT = 3;
    private int sortType = SORT_BY_NAME;

    private RelativeLayoutWithPopUp rootView;
    private RelativeLayout itemPopupWindow;
    private RelativeLayout sortTypePopupWindow;
    private LinearLayout selectionMenu;

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
    private boolean selectModeEnabled = false;

    public static DocumentsFragment newInstance() {
        Bundle args = new Bundle();
        DocumentsFragment fragment = new DocumentsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void startSelectionMode() {
        selectModeEnabled = true;
        fileAdapter.startSelectionMode();
        folderAdapter.startSelectionMode();
        filesRecyclerView.setBackgroundResource(R.drawable.selection_mode_background);
        folderRecyclerView.setBackgroundResource(R.drawable.selection_mode_background);
        selectionMenu.setVisibility(View.VISIBLE);
        updateSelectionMenuData();

        rootView.findViewById(R.id.create_dir_btn).setVisibility(View.GONE);
    }

    public void stopSelectionMode() {
        selectModeEnabled = false;

        if (fileAdapter != null)
            fileAdapter.stopSelectionMode();
        if (folderAdapter != null)
            folderAdapter.stopSelectionMode();

        filesRecyclerView.setBackgroundColor(Color.TRANSPARENT);
        folderRecyclerView.setBackgroundColor(Color.TRANSPARENT);

        selectionMenu.setVisibility(View.GONE);
        rootView.findViewById(R.id.create_dir_btn).setVisibility(foldersStack.size() > 1 ? View.VISIBLE : View.GONE);
    }

    public void checkIfSelectionModeFinished() {
        if (fileAdapter.getSelectedIds().isEmpty() && folderAdapter.getSelectedIds().isEmpty())
            stopSelectionMode();
        else
            updateSelectionMenuData();
    }

    private void updateSelectionMenuData() {
        int foldersCt = folderAdapter.getSelectedIds().size();
        int filesCt = fileAdapter.getSelectedIds().size();

        ((TextView) selectionMenu.findViewById(R.id.folder_count)).setText(String.format(Locale.getDefault(),
                "%d %s", foldersCt, foldersCt % 10 == 1 ? "папка" : "папки(ок)"));

        ((TextView) selectionMenu.findViewById(R.id.files_count)).setText(String.format(Locale.getDefault(),
                "%d %s", filesCt, filesCt % 10 == 1 ? "файл" : "файла(ов)"));

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
        if (rootView == null) {
            rootView = (RelativeLayoutWithPopUp) inflater.inflate(R.layout.fragment_documents, container, false);

            itemPopupWindow = (RelativeLayout) rootView.findViewById(R.id.item_popup_layout);
            sortTypePopupWindow = (RelativeLayout) rootView.findViewById(R.id.sort_popup_layout);
            selectionMenu = (LinearLayout) rootView.findViewById(R.id.selection_menu);
            rootView.addPopUpWindow(itemPopupWindow);
            rootView.addPopUpWindow(sortTypePopupWindow);

            sortTypePopupWindow.setVisibility(View.GONE);
            itemPopupWindow.setVisibility(View.GONE);
            selectionMenu.setVisibility(View.GONE);

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
        } else {
            setupHeader();
            updateCurrentFolder();
        }
        return rootView;
    }

    private void updateCurrentFolder() {
        FileSystemStackEntry lastEntry = foldersStack.get(foldersStack.size() - 1);
        foldersStack.remove(foldersStack.size() - 1);
        if (foldersStack.size() == 0)
            new GetFilesTask(null, null).execute();
        else
            new GetFilesTask(lastEntry.id, lastEntry.parentName).execute();
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

        getActivity().findViewById(R.id.group_by_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectModeEnabled)
                    stopSelectionMode();
                sortTypePopupWindow.setVisibility(View.VISIBLE);
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
                        if (selectModeEnabled) {
                            stopSelectionMode();
                            return true;
                        } else
                            return popFileStack();

                return false;
            }
        });

        final NestedScrollView scrollView = (NestedScrollView) rootView.findViewById(R.id.scroll_view);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                filesRecyclerView.setNestedScrollingEnabled(!scrollView.canScrollVertically(1));
                folderRecyclerView.setNestedScrollingEnabled(!scrollView.canScrollVertically(-1));
            }
        });

        rootView.findViewById(R.id.create_dir_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateDirDialog();
            }
        });

        selectionMenu.findViewById(R.id.selection_close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelectionMode();
            }
        });

        selectionMenu.findViewById(R.id.selection_delete_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<File> selectedFiles = new ArrayList<>();
                selectedFiles.addAll(fileAdapter.getSelectedFiles());
                selectedFiles.addAll(folderAdapter.getSelectedFolders());

                boolean isAnyoneFileLocked = false;
                for (File file : selectedFiles) {
                    if (file.isLocked != null && file.isLocked) {
                        isAnyoneFileLocked = true;
                        break;
                    }
                }
                if (isAnyoneFileLocked)
                    Toast.makeText(getContext(), R.string.cannot_delete_some_files_or_folders, Toast.LENGTH_SHORT).show();
                else {
                    new RemoveFileTask(selectedFiles).execute();
                    stopSelectionMode();
                }
            }
        });

        selectionMenu.findViewById(R.id.selection_move_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<File> selectedFiles = new ArrayList<>();
                selectedFiles.addAll(fileAdapter.getSelectedFiles());
                selectedFiles.addAll(folderAdapter.getSelectedFolders());

                boolean isAnyoneFileLocked = false;
                for (File file : selectedFiles) {
                    if (file.isLocked != null && file.isLocked) {
                        isAnyoneFileLocked = true;
                        break;
                    }
                }
                if (isAnyoneFileLocked)
                    Toast.makeText(getContext(), R.string.cannot_move_some_files_or_folders, Toast.LENGTH_SHORT).show();
                else {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, MoveFileFragment.newInstance(selectedFiles)).addToBackStack("documents").commit();
                    stopSelectionMode();
                }
            }
        });


        sortTypePopupWindow.findViewById(R.id.sort_by_name).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortTypePopupWindow.findViewById(R.id.sort_by_create_time_tick).setVisibility(View.GONE);
                sortTypePopupWindow.findViewById(R.id.sort_by_update_time_tick).setVisibility(View.GONE);

                sortTypePopupWindow.findViewById(R.id.sort_by_name_tick).setVisibility(View.VISIBLE);
                sortType = SORT_BY_NAME;
                sortTypePopupWindow.setVisibility(View.GONE);
                updateCurrentFolder();
            }
        });

        sortTypePopupWindow.findViewById(R.id.sort_by_create_time).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortTypePopupWindow.findViewById(R.id.sort_by_name_tick).setVisibility(View.GONE);
                sortTypePopupWindow.findViewById(R.id.sort_by_update_time_tick).setVisibility(View.GONE);

                sortTypePopupWindow.findViewById(R.id.sort_by_create_time_tick).setVisibility(View.VISIBLE);

                sortType = SORT_BY_CREATED_AT;
                sortTypePopupWindow.setVisibility(View.GONE);
                updateCurrentFolder();
            }
        });

        sortTypePopupWindow.findViewById(R.id.sort_by_update_time).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortTypePopupWindow.findViewById(R.id.sort_by_create_time_tick).setVisibility(View.GONE);
                sortTypePopupWindow.findViewById(R.id.sort_by_name_tick).setVisibility(View.GONE);

                sortTypePopupWindow.findViewById(R.id.sort_by_update_time_tick).setVisibility(View.VISIBLE);

                sortType = SORT_BY_UPDATED_AT;
                sortTypePopupWindow.setVisibility(View.GONE);
                updateCurrentFolder();
            }
        });
    }

    private File convertOrganizationToFolder(JSONObject orgJson) {
        try {
            File orgFolder = new File();
            orgFolder.isFolder = true;
            orgFolder.isLocked = true;
            orgFolder.isFile = false;
            orgFolder.nodeType = "cm:org";
            orgFolder.id = orgJson.getJSONObject("site").getString("node_id");
            orgFolder.name = orgJson.getJSONObject("site").getString("title");

            return orgFolder;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
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
            setAdapters(foldersStack.get(foldersStack.size() - 1).files, foldersStack.get(foldersStack.size() - 1).folders, true);
    }

    private void setAdapters(ArrayList<File> files, ArrayList<File> folders, boolean withSelection) {
        rootView.findViewById(R.id.create_dir_btn).setVisibility(foldersStack.size() > 1 ? View.VISIBLE : View.GONE);

        if ((files == null || files.isEmpty()) && (folders == null || folders.isEmpty()))
            rootView.findViewById(R.id.empty_block).setVisibility(View.VISIBLE);
        else
            rootView.findViewById(R.id.empty_block).setVisibility(View.GONE);

        ((MaxHeightRecycleView) folderRecyclerView).setMaxHeightEnabled(true);
        ((MaxHeightRecycleView) filesRecyclerView).setMaxHeightEnabled(true);
        LinearLayout.LayoutParams folderParams = (LinearLayout.LayoutParams) folderRecyclerView.getLayoutParams();
        folderParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        LinearLayout.LayoutParams filesParams = (LinearLayout.LayoutParams) filesRecyclerView.getLayoutParams();
        filesParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;

        if (files == null || files.isEmpty()) {
            rootView.findViewById(R.id.files_block).setVisibility(View.GONE);
            rootView.findViewById(R.id.files_recycler_view).setVisibility(View.GONE);

            ((MaxHeightRecycleView) folderRecyclerView).setMaxHeightEnabled(false);
            folderParams.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.84);
        } else {
            rootView.findViewById(R.id.files_block).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.files_recycler_view).setVisibility(View.VISIBLE);
        }

        if (folders == null || folders.isEmpty()) {
            rootView.findViewById(R.id.folders_block).setVisibility(View.GONE);
            rootView.findViewById(R.id.folders_recycler_view).setVisibility(View.GONE);

            ((MaxHeightRecycleView) filesRecyclerView).setMaxHeightEnabled(false);
            filesParams.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.84);
        } else {
            rootView.findViewById(R.id.folders_block).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.folders_recycler_view).setVisibility(View.VISIBLE);
        }

        if (selectModeEnabled && withSelection) {
            folderAdapter = new FolderAdapter(this, folders, isGridView, folderAdapter.getSelectedIds());
            fileAdapter = new FileAdapter(DocumentsFragment.this, files, isGridView, fileAdapter.getSelectedIds());
        } else {
            stopSelectionMode();
            folderAdapter = new FolderAdapter(this, folders, isGridView);
            fileAdapter = new FileAdapter(DocumentsFragment.this, files, isGridView);
        }

        folderRecyclerView.setLayoutParams(folderParams);
        filesRecyclerView.setLayoutParams(filesParams);

        folderRecyclerView.setAdapter(folderAdapter);
        filesRecyclerView.setAdapter(fileAdapter);
    }

    private void showRenameDialog(final File file) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        final View dialogView = layoutInflater.inflate(R.layout.dialog_folder_management, null, false);

        ((TextView) dialogView.findViewById(R.id.dialog_title)).setText(getString(R.string.rename) + " " + (file.isFolder ? getString(R.string.folder) : getString(R.string.file)));
        ((EditText) dialogView.findViewById(R.id.text_input)).setHint(R.string.new_name);
        ((EditText) dialogView.findViewById(R.id.text_input)).setText(file.name);
        ((TextView) dialogView.findViewById(R.id.right_btn_text)).setText(R.string.rename);

        final AlertDialog renameDialog = createDialogWithBlur(dialogView);

        dialogView.findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renameDialog.dismiss();
            }
        });
        dialogView.findViewById(R.id.right_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((EditText) dialogView.findViewById(R.id.text_input)).getText().toString().trim().isEmpty())
                    Toast.makeText(getContext(), R.string.input_folder_name, Toast.LENGTH_LONG).show();
                else
                    new RenameFileTask(renameDialog, file, ((EditText) dialogView.findViewById(R.id.text_input)).getText().toString().trim()).execute();
            }
        });
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
                if (((EditText) dialogView.findViewById(R.id.text_input)).getText().toString().trim().isEmpty())
                    Toast.makeText(getContext(), R.string.input_folder_name, Toast.LENGTH_LONG).show();
                else
                    new CreateDirTask(createDirDialog, ((EditText) dialogView.findViewById(R.id.text_input)).getText().toString().trim()).execute();
            }
        });
    }

    private AlertDialog createDialogWithBlur(View dialogView) {
        Bitmap screenShot = BlurHelper.takeScreenShot(getActivity());
        final Bitmap blurScreenShot = BlurHelper.fastBlur(screenShot, 0.1f, 10);

        final AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setView(dialogView);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                getActivity().findViewById(R.id.blur_background).setVisibility(View.GONE);
            }
        });
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                getActivity().findViewById(R.id.blur_background).setVisibility(View.VISIBLE);
                ((ImageView) getActivity().findViewById(R.id.blur_background)).setImageBitmap(blurScreenShot);
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);
        dialog.show();

        return dialog;
    }

    private boolean popFileStack() {
        if (foldersStack != null && foldersStack.size() > 1) {
            foldersStack.remove(foldersStack.size() - 1);
            updateHeader();
            setAdapters(foldersStack.get(foldersStack.size() - 1).files, foldersStack.get(foldersStack.size() - 1).folders, false);

            if (downloadTask != null && downloadTask.getStatus() != AsyncTask.Status.FINISHED)
                downloadTask.cancel(false);

            downloadTask = new DownloadImagesTask(foldersStack.get(foldersStack.size() - 1).files, true);
            downloadTask.execute();
            return true;
        } else
            return false;
    }

    public void showPopUpWindow(final File item) {
        if (item.isLocked != null && item.isLocked) {
            String text = item.isFolder ? getString(R.string.folder_locked) : getString(R.string.file_locked);
            Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
        } else {
            selectedItemId = item.id;
            itemPopupWindow.findViewById(R.id.deselect_block).setVisibility(selectModeEnabled ? View.VISIBLE : View.GONE);
            itemPopupWindow.setVisibility(View.VISIBLE);

            itemPopupWindow.findViewById(R.id.move_block).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<File> fileArrayList = new ArrayList<>();
                    fileArrayList.add(item);

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, MoveFileFragment.newInstance(fileArrayList)).addToBackStack("documents").commit();
                    itemPopupWindow.setVisibility(View.GONE);
                }
            });

            itemPopupWindow.findViewById(R.id.delete_block).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemPopupWindow.setVisibility(View.GONE);
                    ArrayList<File> fileArrayList = new ArrayList<>();
                    fileArrayList.add(item);
                    new RemoveFileTask(fileArrayList).execute();
                }
            });

            itemPopupWindow.findViewById(R.id.edit_block).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemPopupWindow.setVisibility(View.GONE);
                    showRenameDialog(item);
                }
            });
        }
    }


    private class CreateDirTask extends AsyncTask<Void, Void, Integer> {
        private AlertDialog dialog;
        private String parentId;
        private String name;
        private File folder;

        CreateDirTask(AlertDialog dialog, String name) {
            this.name = name;
            this.dialog = dialog;
            parentId = foldersStack.get(foldersStack.size() - 1).id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loopDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                String url = "/api/fs/create_folder/" + parentId;
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", name);

                Response response = RequestService.createPostRequest(url, jsonObject.toString());

                if (response.code() == 201) {
                    JSONObject resultJson = new JSONObject(response.body().string());
                    folder = new ObjectMapper().readValue(resultJson.getJSONObject("entry").toString(), File.class);
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
            } else if (responseCode == 409)
                Toast.makeText(getContext(), R.string.file_or_folder_already_exists, Toast.LENGTH_SHORT).show();
            else {
                foldersStack.get(foldersStack.size() - 1).folders.add(folder);
                setAdapters(foldersStack.get(foldersStack.size() - 1).files, foldersStack.get(foldersStack.size() - 1).folders, false);
                dialog.dismiss();
                Toast.makeText(getContext(), R.string.folder_successfully_created, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class RenameFileTask extends AsyncTask<Void, Void, Integer> {
        private AlertDialog dialog;
        private File file;
        private String newName;

        RenameFileTask(AlertDialog dialog, File file, String newName) {
            this.newName = newName;
            this.dialog = dialog;
            this.file = file;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loopDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                String url = "/api/fs/rename/" + file.id;
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", newName);

                Response response = RequestService.createPostRequest(url, jsonObject.toString());

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
            } else if (responseCode == 409)
                Toast.makeText(getContext(), R.string.file_or_folder_already_exists, Toast.LENGTH_SHORT).show();
            else {
                if (file.isFolder) {
                    for (File folder : foldersStack.get(foldersStack.size() - 1).folders)
                        if (folder.id.equals(file.id))
                            folder.name = newName;
                    folderAdapter.notifyDataSetChanged();
                } else {
                    for (File searchFile : foldersStack.get(foldersStack.size() - 1).files)
                        if (searchFile.id.equals(file.id))
                            searchFile.name = newName;
                    fileAdapter.notifyDataSetChanged();
                }

                dialog.dismiss();
                Toast.makeText(getContext(), file.isFolder ? getString(R.string.folder_successfully_renamed)
                        : getString(R.string.file_successfully_renamed), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class RemoveFileTask extends AsyncTask<Void, Void, Integer> {
        private final int NO_ERRORS_CODE = 12345;

        private ArrayList<File> files;
        private ArrayList<File> deletedFiles;

        RemoveFileTask(ArrayList<File> files) {
            this.files = files;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loopDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                deletedFiles = new ArrayList<>();
                for (File file : files) {
                    String url = "/api/fs/delete/" + file.id;
                    Response response = RequestService.createCustomTypeRequest(url, "DELETE", "");
                    if (response.code() == 404)
                        return response.code();

                    deletedFiles.add(file);
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
            if (loopDialog != null && loopDialog.isShowing())
                loopDialog.dismiss();

            if (responseCode == null)
                Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_LONG).show();
            else if (responseCode == 401) {
                startActivity(new Intent(getContext(), AuthActivity.class));
                getActivity().finish();
            } else if (responseCode == NO_ERRORS_CODE) {
                if (deletedFiles.isEmpty())
                    Toast.makeText(getContext(), R.string.not_deleted, Toast.LENGTH_SHORT).show();
                else if (deletedFiles.size() == files.size())
                    Toast.makeText(getContext(), R.string.removed_successfully, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getContext(), R.string.not_all_files_or_folders_were_deleted, Toast.LENGTH_SHORT).show();

                for (File file : deletedFiles)
                    if (file.isFolder) {
                        for (int i = 0; i < foldersStack.get(foldersStack.size() - 1).folders.size(); i++)
                            if (foldersStack.get(foldersStack.size() - 1).folders.get(i).id.equals(file.id)) {
                                foldersStack.get(foldersStack.size() - 1).folders.remove(i);
                                break;
                            }
                    } else {
                        for (int i = 0; i < foldersStack.get(foldersStack.size() - 1).files.size(); i++)
                            if (foldersStack.get(foldersStack.size() - 1).files.get(i).id.equals(file.id)) {
                                foldersStack.get(foldersStack.size() - 1).files.remove(i);
                                break;
                            }
                    }

                setAdapters(foldersStack.get(foldersStack.size() - 1).files, foldersStack.get(foldersStack.size() - 1).folders, false);

            } else
                Toast.makeText(getContext(), R.string.unknown_error, Toast.LENGTH_SHORT).show();

        }
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

                String sortParameter = "?orderBy=";
                if (sortType == SORT_BY_CREATED_AT)
                    sortParameter += "createdAt DESC";
                else if (sortType == SORT_BY_UPDATED_AT)
                    sortParameter += "modifiedAt DESC";
                else if (sortType == SORT_BY_NAME)
                    sortParameter += "name ASC";

                String url;
                if (fileId == null) {
                    url = "//api/user/orgs" + sortParameter;
                    foldersStack = new ArrayList<>();
                } else
                    url = "/api/fs/children/" + fileId + sortParameter;

                Response response = RequestService.createGetRequest(url);

                if (response.code() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    JSONObject resultJson = new JSONObject(response.body().string());
                    JSONArray tasksEntriesJson = resultJson.getJSONObject("list").getJSONArray("entries");

                    for (int i = 0; i < tasksEntriesJson.length(); i++)
                        if (fileId == null) {
                            JSONObject organization = tasksEntriesJson.getJSONObject(i).getJSONObject("entry");
                            allFiles.add(convertOrganizationToFolder(organization));
                        } else
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

                foldersStack.add(new FileSystemStackEntry(folders, files, fileName == null ? "" : fileName, fileId));

                setAdapters(files, folders, false);
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
