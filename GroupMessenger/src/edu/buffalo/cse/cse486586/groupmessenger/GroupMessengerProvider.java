package edu.buffalo.cse.cse486586.groupmessenger;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class GroupMessengerProvider extends ContentProvider
{
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) 
	{
		return 0;
	}

	@Override
	public String getType(Uri uri) 
	{
		return uri.toString();
	}

	// INSERT INTO DATABASE

	public Uri insert(Uri uri, ContentValues values) 
	{
		// STORE VALUES TO DATABASE
		DataBase mydb = new DataBase(getContext() , uri.toString());
		SQLiteDatabase database = mydb.getWritableDatabase();
		String key = (String) values.get("key");

		int check = database.update("["+uri.toString()+"]",
									values, 
									"key = '"+ key.toString() +"'", 
									null);
		if (check == 0)
		{
			database.insert("[" +uri.toString()+"]" , null ,  values);
		}
		database.close();
		Log.v("insert", values.toString());
		return uri;
	}

	// ONCREATE
	
	public boolean onCreate() 
	{
		return false;
	}

	// QUERY THE CURSOR FROM DATABASE
	
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) 
	{
		DataBase mydb = new DataBase(getContext() , uri.toString());
		SQLiteDatabase database = mydb.getWritableDatabase();
		Cursor query = database.rawQuery(
										"SELECT * " +
										"FROM [" +uri.toString()+"] " +
										"WHERE key = '" + selection +"'", null); 
		Log.v("query", selection);
		return query;
	}

	
	
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) 
	{
		return 0;
	}
}
