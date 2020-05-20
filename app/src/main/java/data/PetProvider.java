package data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import data.PetsContract.PetsEntry;

public class PetProvider extends ContentProvider {
    ShelterDB shelterDB=null;
    @Override
    public boolean onCreate() {
        shelterDB=new ShelterDB(getContext());
        return true;
    }

    public static UriMatcher sUriMatcher=new UriMatcher(UriMatcher.NO_MATCH);
    private final static int PETS=100;
    private final static int PETS_ID=101;
    static {
        sUriMatcher.addURI(PetsContract.CONTENT_AUTHORITY, PetsEntry.TABLE_NAME,PETS);
        sUriMatcher.addURI(PetsContract.CONTENT_AUTHORITY,PetsEntry.TABLE_NAME+"/#",PETS_ID);
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db=shelterDB.getReadableDatabase();

        Cursor cursor=null;
        int match=sUriMatcher.match(uri);
        switch (match){
            case PETS:
                cursor=db.query(PetsEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case PETS_ID:
                selection=PetsEntry.PET_ID+"=?";
                selectionArgs=new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor=db.query(PetsEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query Uri"+uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match=sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return PetsEntry.CONTENT_LIST_TYPE;
            case PETS_ID:
                return PetsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Error matching uri");
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int match=sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, values);
            default:
                throw new IllegalArgumentException("Cannot match Uri " + uri);
        }
    }

    private Uri insertPet(Uri uri, ContentValues values) {
        SQLiteDatabase db=shelterDB.getWritableDatabase();
        long id=db.insert(PetsEntry.TABLE_NAME,null,values);
        if(id!=-1){
            getContext().getContentResolver().notifyChange(uri,null);
            return ContentUris.withAppendedId(uri,id);
        }
        else
            return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match=sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return deletePet(uri,selection,selectionArgs);
            case PETS_ID:
                selection=PetsEntry.PET_ID+"=?";
                selectionArgs=new String[]{String.valueOf(ContentUris.parseId(uri))};
                return deletePet(uri,selection,selectionArgs);
            default:
                throw new IllegalArgumentException("Error in uri match "+uri);
        }
    }

    private int deletePet(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db=shelterDB.getWritableDatabase();
        int id=db.delete(PetsEntry.TABLE_NAME,selection,selectionArgs);
        if(id!=0)
            getContext().getContentResolver().notifyChange(uri,null);
        return id;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match=sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return updatePet(uri,values,selection,selectionArgs);
            case PETS_ID:
                selection=PetsEntry.PET_ID+"=?";
                selectionArgs=new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri,values,selection,selectionArgs);
            default:
                throw new IllegalArgumentException("Uri Not matched"+uri);
        }
    }

    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db=shelterDB.getWritableDatabase();

        if(values.containsKey(PetsEntry.PET_NAME))
        {
            String name=values.getAsString(PetsEntry.PET_NAME);
            if(name==null)
                throw new IllegalArgumentException("Name not found");
        }
        if(values.containsKey(PetsEntry.PET_GENDER))
        {
            Integer gender=values.getAsInteger(PetsEntry.PET_GENDER);
            if(gender==null || !(gender==PetsEntry.GENDER_FEMALE || gender==PetsEntry.GENDER_MALE || gender==PetsEntry.GENDER_UNKNOWN))
                throw new IllegalArgumentException("Invalid Gender");
        }
        if(values.containsKey(PetsEntry.PET_WEIGHT))
        {
            Integer weight=values.getAsInteger(PetsEntry.PET_WEIGHT);
            if(weight!=null && weight<0)
                throw new IllegalArgumentException("Invalid Weight");
        }

        if(values.size()==0)
                return 0;
        int id=db.update(PetsEntry.TABLE_NAME,values,selection,selectionArgs);
        if(id!=0)
            getContext().getContentResolver().notifyChange(uri,null);
        return id;
    }

}
