package edu.buffalo.cse.cse486586.groupmessenger2;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.Selection;
import android.util.Log;
import android.content.ContentResolver;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static android.content.Context.*;


/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 *
 * Please read:
 *
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 *
 * before you start to get yourself familiarized with ContentProvider.
 *
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 *
 * @author stevko
 *
 */
public class GroupMessengerProvider extends ContentProvider {
    public static int k =0;
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /*
         * TODO: You need to implement this method. Note that values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         *
         * For actual storage, you can use any option. If you know how to use SQL, then you can use
         * SQLite. But this is not a requirement. You can use other storage options, such as the
         * internal storage option that we used in PA1. If you want to use that option, please
         * take a look at the code for PA1.
         */
        ContentValues newvalue =  new ContentValues();
        String keyValue = values.getAsString("key");
        String FileName = keyValue ;
        String msg = values.getAsString("value");
        FileOutputStream outputStream;
        outputStream = null;
        try{
            outputStream = getContext().openFileOutput(FileName,MODE_PRIVATE);
            outputStream.write(msg.getBytes());
            outputStream.close();

        } catch(FileNotFoundException e)
        {
            Log.d("TAG","FIle not found");
        }catch(IOException e)
        {
            Log.d("Tag","IOexception");
        }

        return uri;
    }

    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.
        SQLiteOpenHelper helper;



        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         *
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the formatting might still be incorrect.
         *
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         */

        FileInputStream newFile = null;
        MatrixCursor matrixCursor = null;
        try {
            newFile = getContext().openFileInput(selection );
            InputStreamReader inputStreamReader = new InputStreamReader(newFile);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            matrixCursor = new MatrixCursor(new String[]{"key", "value"});


            matrixCursor.addRow(new String[]{selection, bufferedReader.readLine()});

            inputStreamReader.close();
        } catch (FileNotFoundException e) {
            Log.d("Tag", "File notfound");

        } catch (IOException e) {
            Log.d("Tag", "IO exception");
        }
        Log.v("query", selection);
        return matrixCursor;
    }
}
