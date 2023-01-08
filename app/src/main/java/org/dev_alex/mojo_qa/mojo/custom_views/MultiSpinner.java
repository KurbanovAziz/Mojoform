package org.dev_alex.mojo_qa.mojo.custom_views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.models.Organisation;

import java.util.List;

public class MultiSpinner extends androidx.appcompat.widget.AppCompatSpinner implements OnCancelListener,
        AdapterView.OnItemClickListener {
    public class MultiSpinnerListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public String getItem(int position) {
            return items.get(position).getName();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO: Update the following to use your own custom view.

            if (convertView == null) {
                convertView = activity.getLayoutInflater().inflate(
                        R.layout.custom_multiple_choice,
                        parent, false);
            }

            CheckedTextView textView = convertView.findViewById(R.id.text1);
            textView.setText(items.get(position).getName());
            textView.setChecked(selected[position]);
            ImageView imageView = convertView.findViewById(R.id.point1);
            if(items.get(position).getColor() != null){
            imageView.setImageResource(R.drawable.point);
            imageView.setColorFilter(Color.parseColor(items.get(position).getColor()));}


            return convertView;
        }
    }

    private static MultiSpinnerListAdapter adapter;
    private  Activity activity;
    private Context context;
    private static List<Organisation> items;
    private static boolean[] selected;
    private static String defaultText;
    private static MultiSpinnerListener listener;

    public MultiSpinner(Context context) {
        super(context);
    }

    public MultiSpinner(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
    }

    public MultiSpinner(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        CheckedTextView textView = (CheckedTextView) view.findViewById(R.id.text1);

        textView.setChecked(!textView.isChecked());
        selected[position] = textView.isChecked();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        // refresh text on spinner
        StringBuffer spinnerBuffer = new StringBuffer();
        boolean someUnselected = false;
        for (int i = 0; i < items.size(); i++) {
            if (selected[i] == true) {
                spinnerBuffer.append(items.get(i));
                spinnerBuffer.append(", ");
            } else {
                someUnselected = true;
            }
        }
        String spinnerText;
        if (someUnselected) {
            spinnerText = spinnerBuffer.toString();
            if (spinnerText.length() > 2)
                spinnerText = spinnerText
                        .substring(0, spinnerText.length() - 2);
        } else {
            spinnerText = defaultText;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,
                new String[] { spinnerText });
        setAdapter(adapter);
        listener.onItemsSelected(selected);
    }

    @Override
    public boolean performClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setAdapter(adapter, null);
        builder.setPositiveButton(R.string.choose,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.setOnCancelListener(this);
        try{
        AlertDialog dialog = builder.create();
        dialog.getListView().setOnItemClickListener(this);
        dialog.show();
        Button positive_button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positive_button.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_view));
       // positive_button.setHeight(30);
        positive_button.setTextColor(Color.parseColor("#f0f8ff"));
        positive_button.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, .2f));
        return true;}
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public void setItems(Activity activity, Context context, List<Organisation> items, String allText,
                         MultiSpinnerListener listener) {
        this.adapter = new MultiSpinnerListAdapter();
        this.activity = activity;
        this.items = items;
        this.defaultText = allText;
        this.listener = listener;
        this.context = context;
        // all selected by default
        selected = new boolean[items.size()];
        for (int i = 0; i < selected.length; i++)
            selected[i] = true;
        // all text on the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, new String[] { allText });
        setAdapter(adapter);
    }

    public interface MultiSpinnerListener {
        public void onItemsSelected(boolean[] selected);
    }
}