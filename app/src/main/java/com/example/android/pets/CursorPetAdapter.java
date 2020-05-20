package com.example.android.pets;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import data.PetsContract;
import data.PetsContract.PetsEntry;

public class CursorPetAdapter extends CursorAdapter {

    public CursorPetAdapter(Context context, Cursor c) {
        super(context, c,0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.petitem,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView name=(TextView) view.findViewById(R.id.petName);
        name.setText(cursor.getString(cursor.getColumnIndex(PetsEntry.PET_NAME)));

        TextView breed=(TextView) view.findViewById(R.id.petBreed);
        if(!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(PetsEntry.PET_BREED))))
            breed.setText(cursor.getString(cursor.getColumnIndex(PetsEntry.PET_BREED)));
        else
            breed.setText("Not Available");
    }
}
