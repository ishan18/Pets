package data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import data.PetsContract.PetsEntry;

public class ShelterDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME="Shelters.db";
    private static final int DATABASE_VERSION=1;

    public ShelterDB(@Nullable Context context) {
        super(context, DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL(PetsEntry.TABLE_DELETE);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PetsEntry.TABLE_CREATE);
    }
}
