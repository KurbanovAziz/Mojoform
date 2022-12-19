package org.dev_alex.mojo_qa.mojo.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import org.dev_alex.mojo_qa.mojo.Data;
import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.activities.AuthActivity;
import org.dev_alex.mojo_qa.mojo.activities.OnboardingActivity;
import org.dev_alex.mojo_qa.mojo.activities.OpenLinkActivity;
import org.dev_alex.mojo_qa.mojo.services.LoginHistoryService;
import org.dev_alex.mojo_qa.mojo.services.Utils;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;

public class LoginFragment extends Fragment {
    private View rootView;

    private final int SCAN_CODE_REQUEST_CODE = 5222;

    public static LoginFragment newInstance() {
        Bundle args = new Bundle();
        LoginFragment fragment = new LoginFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_login, container, false);
            setListeners();
            Utils.setupCloseKeyboardUI(getActivity(), rootView);

            if (LoginHistoryService.isFirstLaunch()) {
                showFirstLaunchHint();
                LoginHistoryService.setFirstLaunch(false);
            }
        }
        return rootView;
    }
    public void scanСode(View v) {
        IntentIntegrator intentIntegrator = new IntentIntegrator(getActivity());
        intentIntegrator.setCaptureActivity(CaptureActivity.class);
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        intentIntegrator.setPrompt("Сканируем...");
        intentIntegrator.initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {

            IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            String contents = intentResult.getContents();
            String UUID = contents.substring(contents.lastIndexOf("/")).replace("/", "");
            if (contents.contains("reports")) {
                Data.pendingOpenTaskUUID = UUID;
                Data.isReportTaskMode = true;
                Toast.makeText(getContext(), R.string.log_in_to_execute_task, Toast.LENGTH_LONG).show();
            } else {
                startActivity(OpenLinkActivity.getActivityIntent(getContext(), UUID, false));
            }
        }
        catch (Exception e){e.printStackTrace();}
    }

    private void showFirstLaunchHint() {
        rootView.findViewById(R.id.tvFirstLaunchHint).setVisibility(View.VISIBLE);

        String message = getString(R.string.first_launch_hint);
        SpannableString ss = new SpannableString(message);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                String url = "https://mojoform.com/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };

        ss.setSpan(clickableSpan, message.indexOf("Mojoform"), message.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView textView = (TextView) rootView.findViewById(R.id.tvFirstLaunchHint);
        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.TRANSPARENT);
    }

    private void setListeners() {
        final EditText login = (EditText) rootView.findViewById(R.id.username);
        final EditText password = (EditText) rootView.findViewById(R.id.password);
        rootView.findViewById(R.id.login_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (login.getText().toString().trim().isEmpty() || password.getText().toString().trim().isEmpty())
                    Toast.makeText(getContext(), R.string.not_all_fields_filled, Toast.LENGTH_LONG).show();
                else
                    ((AuthActivity) getActivity()).new LoginTask(login.getText().toString().trim(),
                            password.getText().toString()).execute();
            }
        });

        //rootView.findViewById(R.id.btScanQr).setOnClickListener(this::scanСode);

    }


}
