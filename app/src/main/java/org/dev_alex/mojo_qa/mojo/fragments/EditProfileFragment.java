package org.dev_alex.mojo_qa.mojo.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Image;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.dev_alex.mojo_qa.mojo.App;
import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.activities.MainActivity;
import org.dev_alex.mojo_qa.mojo.custom_views.camera.CustomCamera2Activity;
import org.dev_alex.mojo_qa.mojo.models.User;
import org.dev_alex.mojo_qa.mojo.services.BitmapService;
import org.dev_alex.mojo_qa.mojo.services.LoginHistoryService;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.dev_alex.mojo_qa.mojo.services.Utils;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;

public class EditProfileFragment extends Fragment {
    private final int PHOTO_REQUEST_CODE = 11;

    private View rootView;
    private ProgressDialog loopDialog;

    public static EditProfileFragment newInstance() {
        return new EditProfileFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);

            Utils.setupCloseKeyboardUI(getActivity(), rootView);

            initDialog();
            bindData();

            rootView.findViewById(R.id.btSave).setOnClickListener(v -> onSaveClick());
            rootView.findViewById(R.id.btAvatar).setOnClickListener(v -> onChangeAvatarClick());
        }

        setupHeader();
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        User currentUser = LoginHistoryService.getCurrentUser();
        if (requestCode == Constants.REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
            for (Image image : images) {
                File resFile = processImageFile(new File(image.path));
                new UpdateAvatarTask(resFile, currentUser.username).execute();
            }
        }

        if (requestCode == PHOTO_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String photoPath = data.getStringExtra("photo_path");

            File resFile = processImageFile(new File(photoPath));
            new UpdateAvatarTask(resFile, currentUser.username).execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (checkCameraPermissions() && checkExternalPermissions()) {
            showGalleryOrPhotoPickDialog();
        }
    }

    private void setupHeader() {
        ((TextView) getActivity().findViewById(R.id.title)).setText(getString(R.string.profile));
        getActivity().findViewById(R.id.back_btn).setVisibility(View.VISIBLE);

        getActivity().findViewById(R.id.grid_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.sandwich_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.group_by_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.search_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.notification_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.qr_btn).setVisibility(View.GONE);

        getActivity().findViewById(R.id.back_btn).setOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());
    }

    private void initDialog() {
        loopDialog = new ProgressDialog(getContext(), R.style.ProgressDialogStyle);
        loopDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loopDialog.setMessage(getString(R.string.loading_please_wait));
        loopDialog.setIndeterminate(true);
        loopDialog.setCanceledOnTouchOutside(false);
        loopDialog.setCancelable(false);
    }

    private void bindData() {
        updateAvatar();

        User currentUser = LoginHistoryService.getCurrentUser();
        ((EditText) rootView.findViewById(R.id.etName)).setText(currentUser.firstName);
        ((EditText) rootView.findViewById(R.id.etSurName)).setText(currentUser.lastName);
    }

    private void updateAvatar() {
        User currentUser = LoginHistoryService.getCurrentUser();

        if (currentUser.has_avatar)
            new DownloadUserAvatar((ImageView) rootView.findViewById(R.id.profile_image)).execute();
        else {
            rootView.findViewById(R.id.profile_image).setVisibility(View.GONE);

            String userInitials;
            if (TextUtils.isEmpty(currentUser.firstName) && TextUtils.isEmpty(currentUser.lastName))
                userInitials = currentUser.username;
            else
                userInitials = String.format(Locale.getDefault(),
                        "%s%s", TextUtils.isEmpty(currentUser.firstName) ? "" : currentUser.firstName.charAt(0),
                        TextUtils.isEmpty(currentUser.lastName) ? "" : currentUser.lastName.charAt(0));

            ((TextView) rootView.findViewById(R.id.user_initials)).setText(userInitials);
        }
    }

    private void onSaveClick() {
        String name = ((EditText) rootView.findViewById(R.id.etName)).getText().toString();
        String surname = ((EditText) rootView.findViewById(R.id.etSurName)).getText().toString();
        String userName = LoginHistoryService.getCurrentUser().username;
        new SaveUserDataTask(name, surname, userName).execute();
    }

    private void onChangeAvatarClick() {
        if (checkExternalPermissions() && checkCameraPermissions()) {
            showGalleryOrPhotoPickDialog();
        } else {
            requestCameraPermissions();
        }
    }

    private void requestCameraPermissions() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0);
    }

    private boolean checkExternalPermissions() {
        int permissionCheckWrite = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheckRead = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        return (permissionCheckRead == PackageManager.PERMISSION_GRANTED && permissionCheckWrite == PackageManager.PERMISSION_GRANTED);
    }

    private boolean checkCameraPermissions() {
        int permissionCheckCamera = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);
        return (permissionCheckCamera == PackageManager.PERMISSION_GRANTED);
    }


    private void showGalleryOrPhotoPickDialog() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.to_add_photo)
                .cancelable(true)
                .content(R.string.select_photo_source)
                .buttonsGravity(GravityEnum.CENTER)
                .autoDismiss(true)
                .positiveText(R.string.camera)
                .negativeText(R.string.gallery)
                .onPositive((dialog, which) -> CustomCamera2Activity.startForResult(this, PHOTO_REQUEST_CODE))
                .onNegative((dialog, which) -> {
                    Intent intent = new Intent(getContext(), AlbumSelectActivity.class);
                    intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 1);
                    startActivityForResult(intent, Constants.REQUEST_CODE);
                })
                .build()
                .show();
    }

    private File processImageFile(File file) {
        try {
            final int imgSize = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 93, App.getContext().getResources().getDisplayMetrics()));
            final int bigImgSize = 1300;

            String picturePath = file.getAbsolutePath();

            BitmapFactory.Options tmpOptions = new BitmapFactory.Options();
            BitmapFactory.Options options = new BitmapFactory.Options();

            tmpOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(picturePath, tmpOptions);
            options.inSampleSize = BitmapService.calculateInSampleSize(tmpOptions, imgSize);
            options.inJustDecodeBounds = false;

            String resPicturePath;
            tmpOptions = new BitmapFactory.Options();
            options = new BitmapFactory.Options();

            tmpOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(picturePath, tmpOptions);
            options.inSampleSize = BitmapService.calculateInSampleSize(tmpOptions, bigImgSize);
            options.inJustDecodeBounds = false;

            Bitmap resBitmap = BitmapFactory.decodeFile(picturePath, options);
            resBitmap = BitmapService.modifyOrientation(resBitmap, picturePath);

            File newFile = BitmapService.saveBitmapToFile(getContext(), resBitmap);
            resPicturePath = newFile.getAbsolutePath();
            return new File(resPicturePath);
        } catch (Exception exc) {
            exc.printStackTrace();
            return file;
        }
    }


    private class DownloadUserAvatar extends AsyncTask<Void, Void, Void> {
        private ImageView avatarImageView;
        private Bitmap avatar;

        DownloadUserAvatar(ImageView avatarImageView) {
            this.avatarImageView = avatarImageView;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Response thumbResponse = RequestService.createGetRequest("/api/user/" + LoginHistoryService.getCurrentUser().username + "/avatar.png");
                byte[] imageBytes = thumbResponse.body().bytes();
                avatar = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            } catch (Exception exc) {
                exc.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                if (avatar != null) {
                    avatarImageView.setImageBitmap(avatar);
                    LoginHistoryService.addAvatar(LoginHistoryService.getCurrentUser().username, avatar);
                }
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }

    private class UpdateAvatarTask extends AsyncTask<Void, Void, Integer> {
        private final File avatar;
        private final String username;

        private User user;

        public UpdateAvatarTask(File avatar, String username) {
            this.avatar = avatar;
            this.username = username;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Glide.with(getContext())
                    .load(avatar)
                    .into(((ImageView) rootView.findViewById(R.id.profile_image)));

            loopDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                Response response = RequestService.createSendFilePutRequest("/api/user/" + username + "/avatar", MediaType.parse("image/jpg"), avatar);
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

                bindData();

                Activity activity = getActivity();
                if (activity != null) {
                    ((MainActivity) activity).initDrawer();
                }

            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }

    private class SaveUserDataTask extends AsyncTask<Void, Void, Integer> {
        private final String name;
        private final String surname;
        private final String username;

        private User user;

        public SaveUserDataTask(String name, String surname, String username) {
            this.name = name;
            this.surname = surname;
            this.username = username;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loopDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                JSONObject requestJson = new JSONObject();
                requestJson.put("firstName", name);
                requestJson.put("lastName", surname);

                Response response = RequestService.createPutRequest("/api/user/" + username, requestJson.toString());

                if (response.code() == 202 || response.code() == 200) {
                    String userJson = response.body().string();
                    user = new ObjectMapper().readValue(userJson, User.class);
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

                if (user != null) {
                    LoginHistoryService.setCurrentUser(user);
                    bindData();

                    Activity activity = getActivity();
                    if (activity != null) {
                        ((MainActivity) activity).initDrawer();
                    }
                }

            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }
}
