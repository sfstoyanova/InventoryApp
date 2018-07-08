package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.BookContract.BookEntry;

public class BookCursorAdapter extends CursorAdapter {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = BookCursorAdapter.class.getSimpleName();

    //used to divide the price from Database
    private static final double BOOK_PRICE_DECIMAL_VALUE = 100.00;

    public BookCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView bookName = (TextView) view.findViewById(R.id.name);
        TextView bookPrice = (TextView) view.findViewById(R.id.price);
        final TextView bookQuantity = (TextView) view.findViewById(R.id.quantity);
        final Button saleButton = (Button) view.findViewById(R.id.sale);


        String currentName = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME));
        double currentPrice = cursor.getInt(cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE)) / BOOK_PRICE_DECIMAL_VALUE;
        final int currentQuantity = cursor.getInt(cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY));
        final int currentID = cursor.getInt(cursor.getColumnIndex(BookEntry._ID));

        bookName.setText(currentName);
        bookPrice.setText(context.getString(R.string.price_item, currentPrice));
        bookQuantity.setText(context.getString(R.string.quantity_item, currentQuantity));

        saleButton.setText(context.getString(R.string.sale_button));

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = Integer.valueOf(currentQuantity);

                //decreases the quantity value by 1
                if (quantity > 0) {
                    quantity = quantity - 1;
                    ContentValues values = new ContentValues();
                    values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantity);
                    context.getContentResolver().update(Uri.withAppendedPath(BookEntry.CONTENT_URI, Integer.toString(currentID)), values, null, null);
                } else {
                    Toast.makeText(context, context.getString(R.string.no_books), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
