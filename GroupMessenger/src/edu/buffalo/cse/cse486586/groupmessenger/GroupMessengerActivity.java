package edu.buffalo.cse.cse486586.groupmessenger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;

public class GroupMessengerActivity extends Activity 
{
	// HARDCODED SERVERPORT

	int SERVER_PORT = 10000;
	static final String[] REMOTE_PORT = {"11108", "11112", "11116", "11120", "11124"};
	int process_Count = REMOTE_PORT.length;

	// DATABASE COLUMNS

	static final String KEY_FIELD = "key";
	static final String VALUE_FIELD = "value";

	int countOfInsert = 0;
	int countMsgs = 0;
	
	TreeMap<Long , String> msgMap = new TreeMap<Long , String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_messenger);

		// VARIABLES

		ContentResolver contentResolver = getContentResolver();
		Uri uri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger.provider");



		// FIND THE PORT FOR THIS DEVICE - INDRANIL_DEB

		TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
		final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

		// TEXTVIEW

		TextView tv = (TextView) findViewById(R.id.textView1);

		// OPEN SERVER SOCKET AND WAIT FOR CONNECTION

		try 
		{
			ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
			new ServerTask(uri).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
		} 
		catch (IOException e) 
		{
			Log.e("ServerSocket", "Can't create a ServerSocket");
			return;
		}

		// TESTER PROGRAM

		tv.setMovementMethod(new ScrollingMovementMethod());
		findViewById(R.id.button1).setOnClickListener(new OnPTestClickListener(tv, getContentResolver()));

		// ONCLICK LISTENER FOR SEND BUTTON - INDRANIL_DEB

		final EditText editText = (EditText) findViewById(R.id.editText1);
		findViewById(R.id.button4).setOnClickListener(
				new OnSendButton(editText,
						tv, 
						contentResolver,
						myPort,
						uri));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
		return true;
	}

	// BUILD URI USING URIBUILDER

	private Uri buildUri(String scheme, String authority) 
	{
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.authority(authority);
		uriBuilder.scheme(scheme);
		return uriBuilder.build();
	}

	// SERVER TASK

	private class ServerTask extends AsyncTask<ServerSocket, String, Void> 
	{
		Uri uri;

		protected ServerTask(Uri uri)
		{
			this.uri = uri;
		}

		@Override
		protected Void doInBackground(ServerSocket... sockets) 
		{
			// RECEIVE THE MESSAGE FROM OTHER AVDS
			try
			{
				while (true)
				{        	
					ServerSocket serverSocket = sockets[0];
					Socket accept_socket = serverSocket.accept();

					InputStream sock_in = accept_socket.getInputStream();
					InputStreamReader sock_in_read = new InputStreamReader(sock_in);
					BufferedReader recv_buffer = new BufferedReader(sock_in_read);

					String msgToRecv = recv_buffer.readLine();
					
					publishProgress(msgToRecv);
				}
			}
			catch (IOException e)
			{
				Log.e("ServerSocket", "Accept Failed");
			}
			return null;
		}

		protected void onProgressUpdate(String...strings) 
		{
			// DO WITH THE MESSAGE WHAT NEEDS DONE

			String recvdMsg = strings[0].trim();
			
			String theMsg = recvdMsg.split("\\|")[0];
			Long timeStamp = Long.parseLong(recvdMsg.split("\\|")[1]);
			
						
			if (countMsgs < process_Count-1)
			{
				msgMap.put(timeStamp, theMsg);
				countMsgs++;
			}
			else
			{
				msgMap.put(timeStamp, theMsg);
				Set set = msgMap.entrySet();
				Iterator itr = set.iterator();
				while (itr.hasNext())
				{
					Map.Entry map = (Map.Entry) itr.next();
					String msgNow = map.getValue().toString();
					
					ContentValues keyValueToInsert = new ContentValues();

					keyValueToInsert.put(KEY_FIELD, Integer.toString(countOfInsert));
					keyValueToInsert.put(VALUE_FIELD, msgNow);
					
					TextView tv1 = (TextView) findViewById(R.id.textView1);
					tv1.append(msgNow + "  " + map.getKey().toString() + "\t\n");
					TextView tv2 = (TextView) findViewById(R.id.textView1);
					tv2.append("\n");
					// INSERT INTO DATABASE THE CONTENTVALUES
					
					getContentResolver().insert(uri , keyValueToInsert);
					
					countOfInsert++;
				}
				msgMap.clear();
				countMsgs = 0;
			}
			return;
		}
	}
}
