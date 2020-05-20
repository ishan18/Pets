/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.support.annotation.IntRange;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import data.PetsContract;
import data.PetsContract.PetsEntry;
import data.ShelterDB;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;
    Uri uri=null;
    ArrayAdapter genderSpinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        uri=getIntent().getData();

        if(uri==null){
            setTitle("Add a Pet");
        }
        else{
            setTitle("Edit Pet");
            getLoaderManager().initLoader(0,null,this);
        }
        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetsEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetsEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetsEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        if(uri==null){
            MenuItem delete=menu.findItem(R.id.action_delete);
            delete.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                if(uri==null)
                    insertPet();
                else
                    updatePet();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
//                NavUtils.navigateUpFromSameTask(this);
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

    private void deletePet() {
        if(getContentResolver().delete(uri,null,null)==0)
            Toast.makeText(EditorActivity.this,"Error in Deleting Pet",Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(EditorActivity.this,"Pet Deleted Successfully",Toast.LENGTH_SHORT).show();
        finish();
    }

    private void updatePet() {
        ContentValues cv=new ContentValues();
        cv.put(PetsEntry.PET_NAME,mNameEditText.getText().toString().trim());
        cv.put(PetsEntry.PET_BREED,mBreedEditText.getText().toString().trim());
        cv.put(PetsEntry.PET_GENDER,mGender);
        if(TextUtils.isEmpty(mWeightEditText.getText().toString()))
        {
            Toast.makeText(EditorActivity.this,"Error in Updating Pet",Toast.LENGTH_SHORT).show();
            return;
        }
        cv.put(PetsEntry.PET_WEIGHT,Integer.parseInt(mWeightEditText.getText().toString()));

        String name=cv.getAsString(PetsEntry.PET_NAME);
        if(TextUtils.isEmpty(name)){
//            throw new IllegalArgumentException("Name not found");
            Toast.makeText(EditorActivity.this,"Error in Updating Pet",Toast.LENGTH_SHORT).show();
            return;
        }
        Integer gender=cv.getAsInteger(PetsEntry.PET_GENDER);
        if(gender==null || !(gender==PetsEntry.GENDER_FEMALE || gender==PetsEntry.GENDER_MALE || gender==PetsEntry.GENDER_UNKNOWN))
        {
//            throw new IllegalArgumentException("Name not found");
            Toast.makeText(EditorActivity.this,"Error in Updating Pet",Toast.LENGTH_SHORT).show();
            return;
        }
        Integer weight=cv.getAsInteger(PetsEntry.PET_WEIGHT);
        if(weight!=null && weight<0)
        {
//            throw new IllegalArgumentException("Name not found");
            Toast.makeText(EditorActivity.this,"Error in Updating Pet",Toast.LENGTH_SHORT).show();
            return;
        }

        if(getContentResolver().update(uri,cv,null,null)==0)
            Toast.makeText(EditorActivity.this,"Error in Updating Pet",Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(EditorActivity.this,"Updated Successfully",Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure You want to delete this Pet?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
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
    public void onBackPressed() {
        super.onBackPressed();
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
        builder.setMessage("Discard Changes?");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep editing", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void insertPet(){
        ContentValues cv=new ContentValues();
        cv.put(PetsEntry.PET_NAME,mNameEditText.getText().toString().trim());
        cv.put(PetsEntry.PET_BREED,mBreedEditText.getText().toString().trim());
        cv.put(PetsEntry.PET_GENDER,mGender);
        if(TextUtils.isEmpty(mWeightEditText.getText().toString()))
        {
            Toast.makeText(EditorActivity.this,"Error in Saving Pet",Toast.LENGTH_SHORT).show();
            return;
        }
        cv.put(PetsEntry.PET_WEIGHT,Integer.parseInt(mWeightEditText.getText().toString()));

        String name=cv.getAsString(PetsEntry.PET_NAME);
        if(TextUtils.isEmpty(name)){
            Toast.makeText(EditorActivity.this,"Error in Saving Pet",Toast.LENGTH_SHORT).show();
            return;
        }
        Integer gender=cv.getAsInteger(PetsEntry.PET_GENDER);
        if(gender==null || !(gender==PetsEntry.GENDER_FEMALE || gender==PetsEntry.GENDER_MALE || gender==PetsEntry.GENDER_UNKNOWN)){
            Toast.makeText(EditorActivity.this,"Error in Saving Pet",Toast.LENGTH_SHORT).show();
            return;
        }
        Integer weight=cv.getAsInteger(PetsEntry.PET_WEIGHT);
        if(weight!=null && weight<0){
            Toast.makeText(EditorActivity.this,"Error in Saving Pet",Toast.LENGTH_SHORT).show();
            return;
        }

        if(getContentResolver().insert(PetsContract.CONTENT_URI,cv)==null)
            Toast.makeText(EditorActivity.this,"Error in Saving Pet",Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(EditorActivity.this,"Saved Successfully",Toast.LENGTH_SHORT).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,uri,null,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(!data.moveToFirst())
            return;
        String name=data.getString(data.getColumnIndex(PetsEntry.PET_NAME));
        String breed=data.getString(data.getColumnIndex(PetsEntry.PET_BREED));
        int gender=data.getInt(data.getColumnIndex(PetsEntry.PET_GENDER));
        String weight=data.getString(data.getColumnIndex(PetsEntry.PET_WEIGHT));

        if(!name.isEmpty())
                mNameEditText.setText(name);
        if(!breed.isEmpty())
                mBreedEditText.setText(breed);
        if(!weight.isEmpty())
                mWeightEditText.setText(weight);
        int pos;
        if(gender==0)
                pos=genderSpinnerAdapter.getPosition("Unknown");
        else if(gender==1)
                pos=genderSpinnerAdapter.getPosition("Male");
        else
                pos=genderSpinnerAdapter.getPosition("Female");
        mGenderSpinner.setSelection(pos);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}