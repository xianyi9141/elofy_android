package com.peatis.elofy.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.peatis.elofy.Elofy;
import com.peatis.elofy.R;
import com.peatis.elofy.model.Filter;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FilterItemAdapter extends ArrayAdapter<Filter> {
    private List<Filter> dataSource;
    private List<Filter> selection;

    public FilterItemAdapter(@NonNull Context context, @NonNull List<Filter> dataSource, List<Filter> selection) {
        super(context, android.R.layout.simple_list_item_1, dataSource);
        this.dataSource = dataSource;
        this.selection = selection;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            ViewGroup.LayoutParams layoutParams = textView.getLayoutParams();
            layoutParams.height = Elofy.pixel(getContext(), 40);
            textView.setLayoutParams(layoutParams);
            textView.setText(dataSource.get(position).getName());

            // selected
            if (selection.contains(dataSource.get(position))) {
                textView.setBackgroundColor(Elofy.color(getContext(), R.color.colorPrimary));
                textView.setTextColor(Color.WHITE);
            } else {
                textView.setBackgroundColor(Color.WHITE);
                textView.setTextColor(Color.BLACK);
            }
        }

        return view;
    }
}
