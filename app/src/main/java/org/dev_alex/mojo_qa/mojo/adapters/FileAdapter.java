package org.dev_alex.mojo_qa.mojo.adapters;


import static org.dev_alex.mojo_qa.mojo.App.getContext;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.fragments.DocumentsFragment;
import org.dev_alex.mojo_qa.mojo.models.Content;
import org.dev_alex.mojo_qa.mojo.models.File;
import org.dev_alex.mojo_qa.mojo.models.Indicator;
import org.dev_alex.mojo_qa.mojo.services.LoginHistoryService;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.dev_alex.mojo_qa.mojo.services.Utils;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;


public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {
    private DocumentsFragment parentFragment;
    private ArrayList<File> files;
    private boolean isGrid;
    private boolean selectionModeEnabled;
    private ArrayList<String> selectedIds;
    private Context context;
    private DocumentClickListener documentClickListener;
    private ProgressDialog loopDialog;


    static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView fileName;
        TextView fileDate;
        ImageView moreBtn;
        ImageView fileIcon;
        ImageView filePreview;
        ImageView selectedTick;
        View card;

        FileViewHolder(View itemView) {
            super(itemView);
            fileName = (TextView) itemView.findViewById(R.id.file_name);
            fileDate = (TextView) itemView.findViewById(R.id.file_date);
            moreBtn = (ImageView) itemView.findViewById(R.id.more_btn);
            fileIcon = (ImageView) itemView.findViewById(R.id.file_icon);
            filePreview = (ImageView) itemView.findViewById(R.id.file_preview);
            card = itemView.findViewById(R.id.card);
            selectedTick = (ImageView) itemView.findViewById(R.id.selected_tick);
        }
    }


    public FileAdapter(DocumentsFragment parentFragment, ArrayList<File> files, boolean isGrid, Context context, DocumentClickListener documentClickListener) {
        this.parentFragment = parentFragment;
        this.files = files;
        this.isGrid = isGrid;
        selectionModeEnabled = false;
        this.context = context;
        this.documentClickListener = documentClickListener;
    }

    public FileAdapter(DocumentsFragment parentFragment, ArrayList<File> files, boolean isGrid, ArrayList<String> selectedIds, Context context, DocumentClickListener documentClickListener) {
        this.parentFragment = parentFragment;
        this.files = files;
        this.isGrid = isGrid;
        this.context = context;
        this.selectedIds = selectedIds;
        selectionModeEnabled = true;
        this.documentClickListener = documentClickListener;

    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v;
        initDialog();
        if (isGrid)
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_file_grid, viewGroup, false);
        else
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_file, viewGroup, false);

        return new FileViewHolder(v);
    }

    @Override
    public void onBindViewHolder(FileViewHolder viewHolder, int i) {
        final File file = files.get(i);
        viewHolder.fileName.setText(file.name);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy | HH.mm", Locale.getDefault());

        if (isGrid) {
            if (selectionModeEnabled)
                viewHolder.card.setBackgroundResource(selectedIds.contains(file.id) ?
                        R.drawable.file_card_grid_selection_checked : R.drawable.file_card_grid_selection_unchecked);
            else
                viewHolder.card.setBackgroundResource(R.drawable.file_card_grid_background);

            FrameLayout.LayoutParams imageLayoutParams = (FrameLayout.LayoutParams) viewHolder.filePreview.getLayoutParams();
            if (parentFragment.bitmapCacheService.hasThumbnailInMemCache(file.id)) {
                imageLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;

                Bitmap bitmap = parentFragment.bitmapCacheService.getThumbnailFromMemCache(file.id);
                viewHolder.filePreview.setImageBitmap(bitmap);
            } else {
                viewHolder.filePreview.setImageResource(R.drawable.no_image);
                imageLayoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50,
                        parentFragment.getResources().getDisplayMetrics());
            }
            viewHolder.filePreview.setLayoutParams(imageLayoutParams);
        } else {
            viewHolder.fileDate.setText(sdf.format(file.createdAt));
            if (selectionModeEnabled)
                viewHolder.card.setBackgroundResource(selectedIds.contains(file.id) ?
                        R.drawable.file_card_selection_checked : R.drawable.file_card_selection_unchecked);
            else
                viewHolder.card.setBackgroundResource(R.drawable.task_card_background);
        }

        String mimeType = file.nodeType;
        switch (mimeType) {
            case "cm:content":
                String fileExt = Utils.getFileExtension(file.name);
                if (!fileExt.isEmpty()) {
                    Resources resources = parentFragment.getResources();
                    final int resourceId = resources.getIdentifier("_" + fileExt, "drawable",
                            parentFragment.getContext().getPackageName());
                    if (resourceId != 0)
                        viewHolder.fileIcon.setImageResource(resourceId);
                    else
                        viewHolder.fileIcon.setImageResource(R.drawable.unknown);
                } else
                    viewHolder.fileIcon.setImageResource(R.drawable.unknown);
                break;
            case "mojo:template":
                viewHolder.fileIcon.setImageResource(R.drawable.icon_template);
                break;
            case "mojo:document":
                viewHolder.fileIcon.setImageResource(R.drawable.icon_filled_doc);
                break;
            case "mojo:analytic":
                viewHolder.fileIcon.setImageResource(R.drawable.icon_analytics);
                break;
            default:
                viewHolder.fileIcon.setImageResource(R.drawable.icon_mojo_file);
                break;
        }

        viewHolder.selectedTick.setVisibility(selectionModeEnabled && selectedIds.contains(file.id) ? View.VISIBLE : View.GONE);

        if (selectionModeEnabled) {
            viewHolder.moreBtn.setOnClickListener(null);
            viewHolder.itemView.setOnClickListener(v -> {
                if (selectedIds.contains(file.id))
                    selectedIds.remove(file.id);
                else
                    selectedIds.add(file.id);

                notifyDataSetChanged();
                parentFragment.checkIfSelectionModeFinished();
            });
        } else {
            viewHolder.moreBtn.setOnClickListener(v -> parentFragment.showPopUpWindow(file));

            if (file.nodeType.equals("cm:content")) {
                viewHolder.itemView.setOnClickListener(v -> parentFragment.new OpenFileTask(file).execute());
            }
        }

        String id = "";
        if (file.properties != null) {
            id = file.properties.mojoId;
        }
        if (id != null && !id.equals("")) {
            String finalId = id;
            viewHolder.itemView.setOnClickListener(v -> documentClickListener.onDownloadPdfClick(Long.parseLong(finalId)));

        }


        viewHolder.itemView.setOnLongClickListener(v -> {
            if (!selectionModeEnabled) {
                parentFragment.startSelectionMode();
                selectedIds.add(file.id);
                parentFragment.checkIfSelectionModeFinished();
            }
            return true;
        });

        viewHolder.moreBtn.setVisibility(View.VISIBLE);
        if ((LoginHistoryService.getCurrentUser().is_manager == null || !LoginHistoryService.getCurrentUser().is_manager) && (LoginHistoryService.getCurrentUser().is_orgowner == null || !LoginHistoryService.getCurrentUser().is_orgowner)) {
            viewHolder.moreBtn.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public void startSelectionMode() {
        selectionModeEnabled = true;
        selectedIds = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void stopSelectionMode() {
        selectionModeEnabled = false;
        notifyDataSetChanged();
    }

    public ArrayList<String> getSelectedIds() {
        return selectedIds;
    }

    public ArrayList<File> getSelectedFiles() {
        ArrayList<File> selectedFiles = new ArrayList<>();
        for (File file : files)
            if (selectedIds.contains(file.id))
                selectedFiles.add(file);

        return selectedFiles;
    }

    private void initDialog() {
        loopDialog = new ProgressDialog(context, R.style.ProgressDialogStyle);
        loopDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loopDialog.setMessage("Загрузка, пожалуйста подождите...");
        loopDialog.setIndeterminate(true);
        loopDialog.setCanceledOnTouchOutside(false);
        loopDialog.setCancelable(false);
    }

    private class DownloadPdf extends AsyncTask<Void, Void, Integer> {
        private java.io.File resultFile;
        private String id;
        private String name;

        DownloadPdf(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loopDialog.show();
            java.io.File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            resultFile = new java.io.File(downloadsDir, name + ".pdf");
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                if (resultFile.exists())
                    return 200;

                String url = "/api/fs-mojo/document/id/" + id + "/pdf";
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
                if (loopDialog != null && loopDialog.isShowing()) loopDialog.dismiss();


                if (responseCode == null)
                    Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_LONG).show();
                else if (responseCode == 200) {
                    try {
                        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                        Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", resultFile);
                        viewIntent.setDataAndType(fileUri, "application/pdf");
                        viewIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        viewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(viewIntent);
                    } catch (Exception exc) {
                        exc.printStackTrace();
                        Toast.makeText(getContext(), "Нет приложения, которое может открыть этот файл", Toast.LENGTH_LONG).show();
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

    public interface DocumentClickListener {
        void onDownloadPdfClick(long id);
    }

}

