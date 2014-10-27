package edu.buffalo.cse.cse486586.groupmessenger;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class OnSendButton implements OnClickListener 
{
	// HARDCODE PORTS

	static final String[] REMOTE_PORT = {"11108", "11112", "11116", "11120", "11124"};
		
	// FROM MAIN VALUES

	EditText editText;
	ContentResolver contentResolver;
	TextView tv;
	String myPort;
	Uri uri;
	
	// CONSTRUCTOR

	public OnSendButton(EditText editText, TextView tv, ContentResolver contentResolver, String myPort, Uri uri) 
	{
		this.editText = editText;
		this.tv = tv;
		this.myPort = myPort;
		this.contentResolver = contentResolver;
		this.uri = uri;
	}

	// ONCLICKLISTENER METHOD

	public void onClick(View v) 
	{
		String enteredMsg = editText.getText().toString();
		editText.setText("");
		//tv.setText(enteredMsg);
		//tv.setMovementMethod(new ScrollingMovementMethod());
		
		new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, enteredMsg, myPort);
	}

	// CLIENT TASK CLASS
	
	private class ClientTask extends AsyncTask<String, Void, Void> 
	{
		@Override
		protected Void doInBackground(String... msgs) 
		{
			String timeStamp = String.valueOf(System.currentTimeMillis());
			for (int i = 0 ; i < 5 ; i++)
			{
				// SENDING THE MESSAGE TO ALL 5 DEVICES
				try
				{				
					Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
											   Integer.parseInt(REMOTE_PORT[i]));
					
					String msgToSend = msgs[0] + "|" + timeStamp;
						
					OutputStream sock_out = socket.getOutputStream();
					OutputStreamWriter sock_out_write = new OutputStreamWriter(sock_out);
					BufferedWriter w_buffer = new BufferedWriter(sock_out_write);
	
					w_buffer.write(msgToSend);
					w_buffer.flush();
					
					socket.close();
				}
				catch (UnknownHostException e) 
				{
					Log.e("ClientTask", "UnknownHostException");
				} 
				catch (IOException e) 
				{
					Log.e("ClientTask", "socket IOException");
				}
			} 
			
			return null;
		}
	}
}
