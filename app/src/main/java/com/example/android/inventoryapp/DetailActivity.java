package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ItemContract.ItemEntry;

/**
 * Created by Timo on 23.07.2017.
 */

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "Main";

    private static final int EXISTING_ITEM_LOADER = 0;

    private Uri mCurrentItemUri;

    /** Edit view variables */
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mSupplierEditText;

    private ImageView mPictureDetail;

    private ImageButton mIncrease;
    private ImageButton mDecrease;

    private Uri mImageUri;

    private boolean mItemHasChanged = false;

    private final int SELECT_PICTURE = 1;
    private ImageView imageView;

    /**
     * OnTouchListener that listens for any user touches
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        if (mCurrentItemUri == null) {
            // This is a new item --> add item
            setTitle(getString(R.string.detail_new_item));
        } else {
            // Existing item --> edit item
            setTitle(getString(R.string.detail_update_item));

            // read items
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        // Find all relevant views
        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_item_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_item_quantity);
        mSupplierEditText = (EditText) findViewById(R.id.edit_item_supplier);

        mPictureDetail = (ImageView) findViewById(R.id.pictureDetail);

        mIncrease = (ImageButton) findViewById(R.id.increase_quantity);
        mDecrease = (ImageButton) findViewById(R.id.decrease_quantity);

        // Setup OnTouchListeners on all the input fields
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);

        // set onClick listener for select picture
        ImageButton pickImage = (ImageButton) findViewById(R.id.edit_picture);
        pickImage.setOnTouchListener(mTouchListener);
        pickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent picturePickerIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                picturePickerIntent.setType("image/*");
                startActivityForResult(picturePickerIntent, SELECT_PICTURE);
            }
        });


        Button order = (Button) findViewById(R.id.order);
        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String product = mNameEditText.getText().toString().trim();
                String supplier = mSupplierEditText.getText().toString().trim();
                String supplierAdress = "order@" + supplier + ".com";

                Intent send = new Intent(Intent.ACTION_SEND);
                send.setType("plain/text");
                send.putExtra(Intent.EXTRA_EMAIL, new String[] { supplierAdress });
                send.putExtra(Intent.EXTRA_SUBJECT, "Order");
                send.putExtra(Intent.EXTRA_TEXT, "Dear " + supplier + ", we'd like to order 1 " + product + ".");

                startActivity(Intent.createChooser(send, ""));
            }
        });


        //add listeners and call helpers for quantity
        mIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quantityAdd();
            }
        });
        mDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quantityRemove();
            }
        });
    }


    //get image Uri from picturePickerIntent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case SELECT_PICTURE:
                if(resultCode == RESULT_OK){
                    mImageUri = imageReturnedIntent.getData();
                    mPictureDetail.setImageURI(mImageUri);
                }
        }
    }

    private boolean saveItem() {
        if (isEmpty(mNameEditText)) {
            Toast.makeText(this, getString(R.string.item_needs) + " " + getString(R.string.name),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if (isEmpty(mPriceEditText)) {
            Toast.makeText(this, getString(R.string.item_needs) + " " + getString(R.string.price),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if (isEmpty(mQuantityEditText)) {
            Toast.makeText(this, getString(R.string.item_needs) + " " + getString(R.string.quantity),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if (isEmpty(mSupplierEditText)) {
            Toast.makeText(this, getString(R.string.item_needs) + " " + getString(R.string.supplier),
                    Toast.LENGTH_SHORT).show();
        }
        if (mImageUri == null) {
            Toast.makeText(this, getString(R.string.item_needs) + " " + getString(R.string.picture),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        // Read from input fields
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String pictureString = mImageUri.toString();

        // Check if this is supposed to be a new item and all editor fields are empty --> return
        if (mCurrentItemUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(supplierString) && TextUtils.isEmpty(pictureString)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, nameString);
        values.put(ItemEntry.COLUMN_ITEM_PRICE, priceString);
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER, supplierString);
        values.put(ItemEntry.COLUMN_ITEM_PICTURE, pictureString);

        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);


        if (mCurrentItemUri == null) {
            // This is a NEW item
            Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.detail_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.detail_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // This is an EXISTING item
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            Log.v(TAG, "rowsAffected: " + rowsAffected);

            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.detail_update_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.detail_update_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // projection for all data from table
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_SUPPLIER,
                ItemEntry.COLUMN_ITEM_PICTURE };

        return new CursorLoader(this,mCurrentItemUri,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // return if cursor is null or less than 1 row
        if (data == null || data.getCount() < 1) {
            return;
        }

        // Proceed to first row and read data
        if (data.moveToFirst()) {
            int nameColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            int priceColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
            int quantityColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
            int supplierColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER);
            int pictureColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_PICTURE);

            // Extract out the value from the Cursor for the given column index
            String name = data.getString(nameColumnIndex);
            String breed = data.getString(priceColumnIndex);
            int quantity = data.getInt(quantityColumnIndex);
            String supplier = data.getString(supplierColumnIndex);
            String picture = data.getString(pictureColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mPriceEditText.setText(breed);
            mQuantityEditText.setText(Integer.toString(quantity));
            mSupplierEditText.setText(supplier);

            mImageUri = Uri.parse(picture);
            mPictureDetail.setImageURI(mImageUri);

         }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierEditText.setText("");
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
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                if (saveItem())
                {
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case R.id.home:

                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                            }
                        };


                //showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Create AlertDialog
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Create ConfirmationDialog
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        if (mCurrentItemUri != null) {
            // Call the ContentResolver to delete the item at the given content URI.
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    public void quantityAdd() {

        String quantity = mQuantityEditText.getText().toString().trim();
        int add = 0;
        if (!TextUtils.isEmpty(quantity)) {
            add = Integer.parseInt(quantity);
        }

        add++;
        String quantityNew = String.valueOf(add);
        mQuantityEditText.setText(quantityNew);

    }

    public void quantityRemove() {

        String quantity = mQuantityEditText.getText().toString().trim();
        int remove = 0;
        if (TextUtils.isEmpty(quantity)) {
            return;
        }
        else{
            remove = Integer.parseInt(quantity);
        }
        if (remove > 0){
            remove--;
        }
        else {
            return;
        }

        String quantityNew = String.valueOf(remove);
        mQuantityEditText.setText(quantityNew);

    }

    // helper to check empty EditText
    private boolean isEmpty(EditText text) {
        if (text.getText().toString().trim().length() > 0)
            return false;

        return true;
    }


}
