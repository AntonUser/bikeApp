package com.example.bikeapp.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.bikeapp.Constants;
import com.example.bikeapp.R;

import java.util.ArrayList;
import java.util.List;

public class BtAdapter extends ArrayAdapter<ListItem> {
    public static final String ITEM_SAVED = "saved";
    public static final String ITEM_SEARCHED = "searched";
    private final List<ListItem> btList;
    private final List<ViewHolder> viewHoldersList;
    private final SharedPreferences pref;
    private boolean isDiscoveryItemType = false;

    public BtAdapter(@NonNull Context context, int resource, List<ListItem> btList) {
        super(context, resource, btList);
        this.btList = btList;
        this.viewHoldersList = new ArrayList<>();
        pref = context.getSharedPreferences(Constants.MY_PREFERENCE, Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return defaultItem(position, convertView, parent);
    }

    private void savePreference(int position) {
        SharedPreferences.Editor edit = pref.edit();
        edit.putString(Constants.MAC_KEY, btList.get(position).getBluetoothDevice().getAddress());
        edit.apply();
    }

    static class ViewHolder {
        TextView tvBtName;
        CheckBox chBtSelected;
    }

    private View defaultItem(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        boolean isConvertView = false;
        if (convertView != null) isConvertView = (convertView.getTag() instanceof ViewHolder);
        if (convertView == null || !isConvertView) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bt_list_item, null);
            viewHolder.tvBtName = convertView.findViewById(R.id.tvBtName);
            viewHolder.chBtSelected = convertView.findViewById(R.id.checkBox);
            convertView.setTag(viewHolder);
            viewHoldersList.add(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.chBtSelected.setChecked(false);
        }
        if (btList.get(position).getItemType().equals(BtAdapter.ITEM_SEARCHED)) {
            viewHolder.chBtSelected.setVisibility(View.GONE);
            isDiscoveryItemType = true;
        } else {
            viewHolder.chBtSelected.setVisibility(View.VISIBLE);
            isDiscoveryItemType = false;
        }

        viewHolder.tvBtName.setText(btList.get(position).getBluetoothDevice().getName());
        viewHolder.chBtSelected.setOnClickListener(v -> {
            if (!isDiscoveryItemType) {
                for (ViewHolder holder : viewHoldersList) {
                    holder.chBtSelected.setChecked(false);
                }
                viewHolder.chBtSelected.setChecked(true);
                savePreference(position);
            }
        });
        if (pref.getString(Constants.MAC_KEY, "no bt selected")
                .equals(btList.get(position).getBluetoothDevice().getAddress()))
            viewHolder.chBtSelected.setChecked(true);
        isDiscoveryItemType = false;
        return convertView;
    }
}
