package de.jlab.android.hombot.data;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import de.jlab.android.hombot.R;
import de.jlab.android.hombot.common.data.HombotDataContract;

/**
 * Created by frede_000 on 08.10.2015.
 */
public class BotCursorAdapter extends CursorAdapter {

    static class ViewHolder {
        TextView name;
        TextView address;
        ImageView delete;
    }

    private View.OnClickListener mSecondaryClickListener;

    public BotCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.listitem_bot_manager, parent, false);
        ViewHolder holder = new ViewHolder();
        holder.name = (TextView) view.findViewById(R.id.name);
        holder.address = (TextView) view.findViewById(R.id.address);
        holder.delete = (ImageView) view.findViewById(R.id.bot_delete);

        holder.delete.setOnClickListener(mSecondaryClickListener);
        view.setTag(holder);
        return view;
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Get ViewHolder from view to avoid findViewById calls!
        ViewHolder holder = (ViewHolder)view.getTag();
        // Extract properties from cursor
        holder.name.setText(cursor.getString(cursor.getColumnIndexOrThrow(HombotDataContract.BotEntry.COLUMN_NAME_NAME)));
        holder.address.setText(cursor.getString(cursor.getColumnIndexOrThrow(HombotDataContract.BotEntry.COLUMN_NAME_ADDRESS)));

    }

    public void setSecondaryItemClickListener(View.OnClickListener secondaryClickListener) {
        mSecondaryClickListener = secondaryClickListener;
    }
}
