package org.dev_alex.mojo_qa.mojo.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.services.BitmapService;
import org.dev_alex.mojo_qa.mojo.services.Utils;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.Locale;

public class ImageViewActivity extends AppCompatActivity {
    private JSONArray images;
    private ViewPager viewPager;
    private JSONArray deletedImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        setListeners();
        deletedImages = new JSONArray();

        try {
            images = new JSONArray(getIntent().getStringExtra("images"));
            ImagePagerAdapter imagePagerAdapter = new ImagePagerAdapter(this);
            viewPager = (ViewPager) findViewById(R.id.pager);
            viewPager.setAdapter(imagePagerAdapter);
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    ((TextView) findViewById(R.id.page_counter)).setText(String.format(Locale.getDefault(), "%d %s %d", position + 1, getString(R.string.of), images.length()));
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            ((TextView) findViewById(R.id.page_counter)).setText(String.format(Locale.getDefault(), "%d %s %d", 1, getString(R.string.of), images.length()));
        } catch (JSONException e) {
            e.printStackTrace();
            finish();
        }
    }

    private void deleteImage(int pos) {
        try {
            String path = images.getString(pos);
            File imageFile = new File(path);
            if (!imageFile.delete())
                imageFile.deleteOnExit();

            images = Utils.removeItemAt(images, pos);
            deletedImages.put(path);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("deleted_images", deletedImages.toString());
            setResult(RESULT_OK, resultIntent);
            if (images.length() == 0)
                finish();
            else {
                int lastPos = viewPager.getCurrentItem();
                ImagePagerAdapter imagePagerAdapter = new ImagePagerAdapter(this);
                viewPager.setAdapter(imagePagerAdapter);
                ((TextView) findViewById(R.id.page_counter)).setText(String.format(Locale.getDefault(), "%d %s %d", 1, getString(R.string.of), images.length()));
                viewPager.setCurrentItem(Math.max(0, lastPos - 1));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setListeners() {
        findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.delete_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteImage(viewPager.getCurrentItem());
            }
        });
    }

    private class ImagePagerAdapter extends PagerAdapter {

        Context mContext;
        LayoutInflater mLayoutInflater;

        ImagePagerAdapter(Context context) {
            mContext = context;
            mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return images.length();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);
            try {
                ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
                final BitmapFactory.Options tmpOptions = new BitmapFactory.Options();
                final BitmapFactory.Options options = new BitmapFactory.Options();

                String picturePath = images.getString(position);
                tmpOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(picturePath, tmpOptions);
                options.inSampleSize = BitmapService.calculateInSampleSize(tmpOptions, (int) (getResources().getDisplayMetrics().heightPixels * 0.9));
                options.inJustDecodeBounds = false;
                options.inSampleSize = BitmapService.calculateInSampleSize(tmpOptions, (int) (getResources().getDisplayMetrics().heightPixels * 0.9));
                Bitmap bitmap = BitmapService.modifyOrientation(BitmapFactory.decodeFile(picturePath, options), picturePath);
                imageView.setImageBitmap(bitmap);
                container.addView(itemView);

            } catch (Exception exc) {
                exc.printStackTrace();
            }
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }
    }
}
