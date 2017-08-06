package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import static com.example.android.inventoryapp.MainActivity.mCursorAdapter;
import static com.example.android.inventoryapp.data.ItemContract.ItemEntry;

/**
 * Created by Timo on 23.07.2017.
 */

public class ItemCursorAdapter extends CursorAdapter {

    private static final String TAG = "Main";

    /**
     * Constructs a new {@link ItemCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {

        final MainActivity activity = (MainActivity) context;

        // Find views to update later
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        ImageView pictureView = (ImageView) view.findViewById(R.id.picture);

        // Find the columns of item attributes
        int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
        int pictureColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PICTURE);

        // read attributes from cursor
        String itemName = cursor.getString(nameColumnIndex);
        String itemPrice = cursor.getString(priceColumnIndex);
        int itemQuantity = cursor.getInt(quantityColumnIndex);
        String itemPicture = cursor.getString(pictureColumnIndex);

        // put data of current item into views
        nameTextView.setText(itemName);
        priceTextView.setText(itemPrice);
        quantityTextView.setText(String.valueOf(itemQuantity));

        Uri pictureUri = Uri.parse(itemPicture);
        pictureView.setImageURI(pictureUri);

        // handle sale button
        ImageButton sale = (ImageButton) view.findViewById(R.id.sale);
        final int currentPosition = cursor.getPosition();
        sale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Cursor cursor = (Cursor) mCursorAdapter.getItem(currentPosition);
                int quantity = cursor.getInt(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY));
                long id = cursor.getInt(cursor.getColumnIndex(ItemEntry._ID));
                Uri uri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);

                if (quantity > 0) {
                    quantity--;
                    ContentValues values = new ContentValues();
                    values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);

                    activity.getContentResolver().update(uri, values, null, null);

                }
            }
        });
    }
}
