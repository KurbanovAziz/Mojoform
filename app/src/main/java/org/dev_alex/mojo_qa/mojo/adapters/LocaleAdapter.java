package org.dev_alex.mojo_qa.mojo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dev_alex.mojo_qa.mojo.R;

import java.util.Locale;

public class LocaleAdapter extends ArrayAdapter<Locale> {

    public LocaleAdapter(@NonNull Context context, int resource, @NonNull Locale[] objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_locale, parent, false);
        }

        Locale l = getItem(position);
        String localeName = l.getDisplayLanguage(l);
        String nameFormatted = Character.toUpperCase(localeName.charAt(0)) + localeName.substring(1);

        TextView textView = convertView.findViewById(R.id.locale_title_tv);
        if (textView != null) {
            textView.setText(nameFormatted);
        }

        return convertView;
    }


}
