package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventoryapp.data.BookContract.BookEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = EditorActivity.class.getSimpleName();
    //used to ensure that the price is saved as Integer in the Database
    private static final double INTEGER_BOOK_PRICE = 100;
    //used to divide the price from Database
    private static final double EDITOR_BOOK_PRICE_DECIMAL_VALUE = 100.00;
    //max allowed value of the quantity field
    private static final int MAX_BOOK_QUANTITY = 9999;
    //constant for the Loader
    private static final int EXISTING_URL_LOADER = 0;
    //passed Uri from CatalogActivity
    private Uri mCurrentUri;
    //Edit text to enter the books' name
    private EditText mBookNameEditText;
    //Edit text to enter the books' price
    private EditText mBookPriceEditText;
    //Edit text to enter the books' quantity
    private EditText mBookQuantityEditText;
    //Edit text to enter the books' supplier name
    private EditText mBookSupplierEditText;
    //Edit text to enter the books' supplier phone
    private EditText mBookSupplierPhoneEditText;
    //Button that increases the quantity
    private Button mAddButton;
    //Button that decreases the quantity
    private Button mSubtractButton;
    //Button that calls the supplier when pressed
    private Button mCallButton;
    //listens if a book entry has been changed
    private boolean mBookHasChanged = false;

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the mBookHasChanged boolean to true.
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentUri = intent.getData();

        if (mCurrentUri == null) {

            //sets the action bar if we add a book
            setTitle(R.string.editor_activity_title_new_book);

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            invalidateOptionsMenu();
        } else {
            //sets the action bar if we edit a book
            setTitle(R.string.editor_activity_title_edit_book);

            //initializing the loader
            getSupportLoaderManager().initLoader(EXISTING_URL_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mBookNameEditText = (EditText) findViewById(R.id.edit_book_name);
        mBookPriceEditText = (EditText) findViewById(R.id.edit_book_price);
        mBookQuantityEditText = (EditText) findViewById(R.id.edit_book_quantity);
        mBookSupplierEditText = (EditText) findViewById(R.id.supplier_name);
        mBookSupplierPhoneEditText = (EditText) findViewById(R.id.supplier_phone);
        mAddButton = (Button) findViewById(R.id.plus);
        mSubtractButton = (Button) findViewById(R.id.minus);
        mCallButton = (Button) findViewById(R.id.call_supplier);

        //Listens if "+" button is clicked
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incrementQuantity();
            }
        });

        //Listens if "-" button is clicked
        mSubtractButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decrementQuantity();
            }
        });

        //Listens if Call Supplier button is clicked
        mCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentUri != null) {
                    callSupplier();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.save_before_call), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBookNameEditText.setOnTouchListener(mTouchListener);
        mBookPriceEditText.setOnTouchListener(mTouchListener);
        mBookQuantityEditText.setOnTouchListener(mTouchListener);
        mBookSupplierEditText.setOnTouchListener(mTouchListener);
        mBookSupplierPhoneEditText.setOnTouchListener(mTouchListener);
        mAddButton.setOnTouchListener(mTouchListener);
        mSubtractButton.setOnTouchListener(mTouchListener);
    }

    //helper method that decreases the book's quantity
    private void decrementQuantity() {
        String quantityString = String.valueOf(mBookQuantityEditText.getText());
        int bookQuantity;

        if (TextUtils.isEmpty(quantityString)) {
            bookQuantity = 0;
        } else {
            bookQuantity = Integer.parseInt(quantityString);
        }

        if (bookQuantity > 0) {
            bookQuantity--;
        }

        mBookQuantityEditText.setText(String.valueOf(bookQuantity));
    }

    //helper method that increases the book's quantity
    private void incrementQuantity() {
        String quantityString = String.valueOf(mBookQuantityEditText.getText());
        int bookQuantity;

        if (TextUtils.isEmpty(quantityString)) {
            bookQuantity = 0;
        } else {
            bookQuantity = Integer.parseInt(quantityString);
        }

        if (bookQuantity < MAX_BOOK_QUANTITY) {
            bookQuantity++;
        }

        mBookQuantityEditText.setText(String.valueOf(bookQuantity));
    }

    //helper method that opens phone app to call supplier via intent
    private void callSupplier() {
        String supplerPhone = String.valueOf(mBookSupplierPhoneEditText.getText());

        if (!TextUtils.isEmpty(supplerPhone)) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + supplerPhone));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.call_supplier_failed), Toast.LENGTH_SHORT).show();
        }
    }

    //helper method which deletes a book from the database
    public void deleteBook() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_book_dialog_msg));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.delete_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mCurrentUri != null) {
                    int rowsDeleted = getContentResolver().delete(mCurrentUri, null, null);

                    if (rowsDeleted == 0) {
                        Toast.makeText(getApplicationContext(), getString(R.string.delete_book_failed), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.delete_book_successful), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        });

        builder.setNegativeButton(getString(R.string.delete_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked the "No" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        builder.show();
    }

    //helper method which saves a book in the database
    public void saveBook() {

        String nameOfBook = mBookNameEditText.getText().toString().trim();
        String priceOfBook = mBookPriceEditText.getText().toString().trim();
        String quantityOfBook = mBookQuantityEditText.getText().toString().trim();
        String supplierOfBook = mBookSupplierEditText.getText().toString().trim();
        String phoneOfSupplier = mBookSupplierPhoneEditText.getText().toString().trim();

        if (mCurrentUri == null && TextUtils.isEmpty(nameOfBook) && TextUtils.isEmpty(priceOfBook) && TextUtils.isEmpty(quantityOfBook)
                && TextUtils.isEmpty(supplierOfBook) && TextUtils.isEmpty(phoneOfSupplier)) {

            Toast.makeText(getApplicationContext(), getString(R.string.editor_empty_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(nameOfBook)) {
            Toast.makeText(getApplicationContext(), getString(R.string.editor_empty_book_name), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(priceOfBook)) {
            Toast.makeText(getApplicationContext(), getString(R.string.editor_empty_price), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(quantityOfBook)) {
            Toast.makeText(getApplicationContext(), getString(R.string.editor_empty_quantity), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(supplierOfBook)) {
            Toast.makeText(getApplicationContext(), getString(R.string.editor_empty_supplier), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(phoneOfSupplier)) {
            Toast.makeText(getApplicationContext(), getString(R.string.editor_empty_supplier_phone), Toast.LENGTH_SHORT).show();
            return;
        }


        double price = 0;

        if (!TextUtils.isEmpty(priceOfBook)) {
            price = Double.parseDouble(priceOfBook) * INTEGER_BOOK_PRICE;
        }

        int quantity = 0;

        if (!TextUtils.isEmpty(quantityOfBook)) {
            quantity = Integer.parseInt(quantityOfBook);
        }

        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_BOOK_NAME, nameOfBook);
        values.put(BookEntry.COLUMN_BOOK_PRICE, price);
        values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantity);
        values.put(BookEntry.COLUMN_BOOK_SUPPLIER_NAME, supplierOfBook);
        values.put(BookEntry.COLUMN_BOOK_SUPPLIER_PHONE, phoneOfSupplier);

        if (mCurrentUri != null) {
            int rowsUpdated = getContentResolver().update(mCurrentUri, values, null, null);

            if (rowsUpdated == 0) {
                Toast.makeText(getApplicationContext(), getString(R.string.updated_book_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.update_book_successful), Toast.LENGTH_SHORT).show();
            }
        } else {
            Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.save_book_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.save_book_successful), Toast.LENGTH_SHORT).show();
            }
        }

        //exits the activity
        finish();
    }

    @Override
    public void onBackPressed() {
        // If the book hasn't changed, continue with handling back button press
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        //hides the "delete" option if user is adding new book
        if (mCurrentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveBook();
                return true;
            case R.id.action_delete:
                //deletes book
                deleteBook();
                return true;
            case android.R.id.home:
                // If the book hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case EXISTING_URL_LOADER:
                String[] projection = {
                        BookEntry._ID,
                        BookEntry.COLUMN_BOOK_NAME,
                        BookEntry.COLUMN_BOOK_PRICE,
                        BookEntry.COLUMN_BOOK_QUANTITY,
                        BookEntry.COLUMN_BOOK_SUPPLIER_NAME,
                        BookEntry.COLUMN_BOOK_SUPPLIER_PHONE
                };
                return new CursorLoader(this, mCurrentUri, projection, null, null, null);

            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        cursor.moveToPosition(0);

        String currentName = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME));
        double currentPrice = cursor.getInt(cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE)) / EDITOR_BOOK_PRICE_DECIMAL_VALUE;
        int currentQuantity = cursor.getInt(cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY));
        String currentSupplier = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_NAME));
        String currentSupplierPhone = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_PHONE));

        mBookNameEditText.setText(currentName);
        mBookPriceEditText.setText(String.valueOf(currentPrice));
        mBookQuantityEditText.setText(String.valueOf(currentQuantity));
        mBookQuantityEditText.setGravity(Gravity.CENTER_HORIZONTAL);
        mBookSupplierEditText.setText(currentSupplier);
        mBookSupplierPhoneEditText.setText(currentSupplierPhone);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mBookNameEditText.getText().clear();
        mBookPriceEditText.getText().clear();
        mBookQuantityEditText.getText().clear();
        mBookSupplierEditText.getText().clear();
        mBookSupplierPhoneEditText.getText().clear();
    }
}
