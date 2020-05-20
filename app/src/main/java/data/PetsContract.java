package data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.PublicKey;

public final class PetsContract {

    public static final String CONTENT_AUTHORITY="com.example.android.pets";
    public static final Uri BASE_CONTENT_URI=Uri.parse("content://"+CONTENT_AUTHORITY);
    public static final Uri CONTENT_URI=Uri.withAppendedPath(BASE_CONTENT_URI,PetsEntry.TABLE_NAME);

    public final static class PetsEntry implements BaseColumns {
        public static final String TABLE_NAME="Pets";
        public static final String PET_ID=BaseColumns._ID;
        public static final String PET_NAME="Name";
        public static final String PET_BREED="Breed";
        public static final String PET_GENDER="Gender";
        public static final String PET_WEIGHT="Weight";

        public static final int GENDER_MALE=1;
        public static final int GENDER_FEMALE=2;
        public static final int GENDER_UNKNOWN=0;

        public static final String TABLE_CREATE="CREATE TABLE "+TABLE_NAME+" ("+PET_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
                +PET_NAME+" TEXT NOT NULL,"+PET_BREED+" TEXT,"+PET_GENDER+" INTEGER NOT NULL DEFAULT 0,"+PET_WEIGHT+
                " INTEGER NOT NULL);";
        public static final String TABLE_DELETE="DROP TABLE IF EXISTS "+TABLE_NAME+";";

        public static final String CONTENT_LIST_TYPE= ContentResolver.CURSOR_DIR_BASE_TYPE+"/"+CONTENT_AUTHORITY+"/"+TABLE_NAME;
        public static final String CONTENT_ITEM_TYPE= ContentResolver.CURSOR_ITEM_BASE_TYPE+"/"+CONTENT_AUTHORITY+"/"+TABLE_NAME+"/#";
    }
}
