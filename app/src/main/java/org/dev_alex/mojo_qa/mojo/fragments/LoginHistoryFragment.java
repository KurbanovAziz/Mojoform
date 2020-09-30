package org.dev_alex.mojo_qa.mojo.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.dev_alex.mojo_qa.mojo.Data;
import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.activities.OpenLinkActivity;
import org.dev_alex.mojo_qa.mojo.adapters.UserAdapter;
import org.dev_alex.mojo_qa.mojo.services.LoginHistoryService;
import org.dev_alex.mojo_qa.mojo.services.Utils;

import static android.app.Activity.RESULT_OK;

public class LoginHistoryFragment extends Fragment {
    private View rootView;
    private RecyclerView recyclerView;

    private final int SCAN_CODE_REQUEST_CODE = 5222;

    public static LoginHistoryFragment newInstance() {
        Bundle args = new Bundle();
        LoginHistoryFragment fragment = new LoginHistoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_login_history, container, false);

            recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(new UserAdapter(this, LoginHistoryService.getLastLoggedUsers()));
            Utils.setupCloseKeyboardUI(getActivity(), rootView);
            setListeners();
        }
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_CODE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                String UUID = contents.substring(contents.lastIndexOf("/")).replace("/", "");

                if (contents.contains("reports")) {
                    Data.pendingOpenTaskUUID = UUID;
                    Data.isReportTaskMode = true;
                    Toast.makeText(getContext(), R.string.log_in_to_execute_task, Toast.LENGTH_LONG).show();
                } else {
                    startActivity(OpenLinkActivity.getActivityIntent(getContext(), UUID, false));
                }
            }
        }
    }


    private void setListeners() {
        rootView.findViewById(R.id.new_user_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, LoginFragment.newInstance()).addToBackStack(null).commit();
            }
        });

        ((Button) rootView.findViewById(R.id.new_user_btn)).setAllCaps(true);

        rootView.findViewById(R.id.btScanQr).setOnClickListener(v -> {
            try {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(intent, SCAN_CODE_REQUEST_CODE);
            } catch (Exception e) {
                showQrAppDownloadDialog();
            }
        });
    }

    private void showQrAppDownloadDialog() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.attention)
                .content(R.string.need_download_app)
                .positiveText("Ok")
                .negativeText(R.string.cancel_)
                .autoDismiss(true)
                .onPositive((dialog, which) -> {
                    Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
                    Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
                    startActivity(marketIntent);
                })
                .show();

    }
}