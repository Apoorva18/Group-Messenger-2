package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.impl.SocketHttpClientConnection;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.io.ObjectOutputStream;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";
    static final int SERVER_PORT = 10000;
    static int k =0;
    int Pseqno = -1;
    int idseqno =1;
    int Aseqno = -1;
    int failedPort =6;
    int fp=-1;
    int count =0;
    // static int max;
    // static ArrayList<String> remotePort = new ArrayList<String>();
    //String[] remotePort = new String[5];


    PriorityQueue<ProposedSequence> priorityQueue = new PriorityQueue<ProposedSequence>(10, new ProposedSequenceComparator());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));


        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             *
             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            /*
             * Log is a good way to debug your code. LogCat prints out all the messages that
             * Log class writes.
             *
             * Please read http://developer.android.com/tools/debugging/debugging-projects.html
             * and http://developer.android.com/tools/debugging/debugging-log.html
             * for more information on debugging.
             */
            Log.e(TAG, "Can't create a ServerSocket");
            return;}

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        final TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        final EditText editText = (EditText) findViewById(R.id.editText1);
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));



        //findViewById(R.id.button4).setOnClickListener();

        //final EditText editText = (EditText) findViewById(R.id.edit_text);

        //
        //* Register an OnKeyListener for the input box. OnKeyListener is an event handler that
        //* processes each key event. The purpose of the following code is to detect an enter key
        //* press event, and create a client thread so that the client thread can send the string
        //* in the input box over the network.
        //
        Button send = (Button)findViewById(R.id.button4);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                /*
                 * If the key is pressed (i.e., KeyEvent.ACTION_DOWN) and it is an enter key
                 * (i.e., KeyEvent.KEYCODE_ENTER), then we display the string. Then we create
                 * an AsyncTask that sends the string to the remote AVD.
                 */
                String msg = editText.getText().toString() + "\n";
                editText.setText(""); // This is one way to reset the input box.
                //TextView localTextView = (TextView) findViewById(R.id.local_text_display);
                tv.append("\t" + msg); // This is one way to display a string.
                //TextView remoteTextView = (TextView) findViewById(R.id.remote_text_display);
                //remoteTextView.append("\n");

                /*
                 * Note that the following AsyncTask uses AsyncTask.SERIAL_EXECUTOR, not
                 * AsyncTask.THREAD_POOL_EXECUTOR as the above ServerTask does. To understand
                 * the difference, please take a look at
                 * http://developer.android.com/reference/android/os/AsyncTask.html
                 */
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);


            }

        });
        //remotePort.add(REMOTE_PORT0);
        // remotePort.add(REMOTE_PORT1);
        // remotePort.add(REMOTE_PORT2);
        //  remotePort.add(REMOTE_PORT3);
        // remotePort.add(REMOTE_PORT4);
    }


    /***
     * ServerTask is an AsyncTask that should handle incoming messages. It is created by
     * ServerTask.executeOnExecutor() call in SimpleMessengerActivity.
     *
     * Please make sure you understand how AsyncTask works by reading
     * http://developer.android.com/reference/android/os/AsyncTask.html
     *
     * @author stevko
     *
     */

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            final ServerSocket serverSocket = sockets[0];
            // Log.e("Server socket",serverSocket.toString());


            try {
                while(true){
                    Socket sc = serverSocket.accept();
                    //Log.e(TAG,"SERVER TASK");

                    InputStream inl = sc.getInputStream();
                    ObjectInputStream objectInputStream = new ObjectInputStream(inl);
                    Object object = objectInputStream.readObject();

                    if(object instanceof InitialMessage){
                        InitialMessage initialMessage = (InitialMessage)object;

                        String id =  initialMessage.getId();
                        String msg = initialMessage.getMsg();

                        //Pseqno = generateseqno();
                        int p =generateseqno();
                        //Log.e(TAG,"Msg received");
                        ProposedSequence proposedSequence = new ProposedSequence(id,msg,p);
                       // Priority priorityQueue2 = new Priority();

//                        priorityQueue2.addPriority(proposedSequence,priorityQueue);
                            priorityQueue.add(proposedSequence);
                        //Log.e(TAG,"ADDING TO QUEUE");

                        OutputStream outputStream = sc.getOutputStream();
                        ObjectOutputStream out = new ObjectOutputStream(outputStream);
                        // Log.e(TAG,"sending seqno");
                        out.writeObject(proposedSequence);

                    }


                    if(object instanceof ProposedSequence) {
                        ProposedSequence finalmsg = (ProposedSequence) object;
                        Aseqno = finalmsg.seqno;
                        finalmsg.deliverable = TRUE;
                        Log.e("ASEqno is", "" + Aseqno);
                       // Priority pq = new Priority();
                        changePriority(finalmsg);
                        // Log.e(TAG,"Got object");
                       // pq.changePriority(finalmsg, priorityQueue);
                         /*

                        ProposedSequence priority = pq.gethead();
                        if(priority == null){
                            Log.e(TAG," 000000");
                        }
                        if(priority!=null){
                            Log.e(TAG,"GOING INSIDE PRIORITY");
                        while (priority.seqno < finalmsg.seqno) {
                            publishProgress(priority.getMsg());
                            pq.removehead();
                            priority = pq.gethead();
                            if(priority==null)
                                break;

                            }}*/
                         String[] remotePort = new String[5];
                        remotePort[0] = REMOTE_PORT0;
                        remotePort[1] = REMOTE_PORT1;
                        remotePort[2] = REMOTE_PORT2;
                         remotePort[3] = REMOTE_PORT3;
                         remotePort[4] = REMOTE_PORT4;
                if(failedPort>5){
                        ProposedSequence ps =priorityQueue.peek();
                        while(priorityQueue.size()!=0 &&ps.deliverable==TRUE){
                            publishProgress((ps.getMsg()));
                            priorityQueue.poll();
                            ps = priorityQueue.peek();
                        }}
                        else
                {   ProposedSequence ps =priorityQueue.peek();
                    while(priorityQueue.size()!=0)
                    {
                        if(ps.deliverable==TRUE)
                        {
                            publishProgress((ps.getMsg()));
                            priorityQueue.poll();
                            ps = priorityQueue.peek();
                        }
                        else if(remotePort[failedPort].equals(ps.getId().split(":")[0])){
                            priorityQueue.poll();
                            ps= priorityQueue.peek();
                            Log.e("REMOVED",remotePort[failedPort]);
                        }
                        else
                            break;
                    }
                }

//                        ProposedSequence ps  = pq.gethead(priorityQueue);
  //                      while (pq.getsize(priorityQueue) != 0 &&ps.deliverable==TRUE) {
//
  //                          publishProgress(ps.getMsg());
    //                        pq.removehead(priorityQueue);
      //                      ps = pq.gethead(priorityQueue);
        //                }
                        /*    }
                            else if(failedPort<5)
                            {
                                if(remotePort[failedPort].equals(ps.getId().split(":")[0])){
                                    pq.removehead(priorityQueue);
                                    Log.e("WORKED", remotePort[failedPort]);
                                  //  ps = pq.gethead(priorityQueue);
                            }

                            }
                            else
                                break;
                        }*/


/*}
                            if(pq.getsize(priorityQueue)==25)
                            {   pq.printq(priorityQueue);
                                while(pq.gethead(priorityQueue)!=null){
                                    publishProgress(pq.gethead(priorityQueue).getMsg());
                                    pq.removehead(priorityQueue);
                                }
                            }


*/


                    }    //publishProgress(br.readLine());

                }



            }catch(ClassNotFoundException e){
                Log.e(TAG,"CLASS NOT FOUND");
            }catch(IOException e){


            }







            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */


            return null;
        }

        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            //TextView remoteTextView = (TextView) findViewById(R.id.remote_text_display);
            //remoteTextView.append(strReceived + "\t\n");
            //TextView localTextView = (TextView) findViewById(R.id.local_text_display);
            //localTextView.append("\n");

            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             *
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */

            /**
             * buildUri() demonstrates how to build a URI for a ContentProvider.
             *
             * @param scheme
             * @param authority
             * @return the URI
             */


            final ContentResolver mContentResolver = getContentResolver();

            final Uri mUri;
            final ContentValues mContentValues= new ContentValues();

            mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");

            //String filename = "SimpleMessengerOutput";
            mContentValues.put("key",Integer.toString(k));
            mContentValues.put("value",strReceived);
            mContentResolver.insert(mUri,mContentValues);
            k = k + 1;

            //String  = strReceived + "\n";

            //FileOutputStream outputStream;

            //try {
            //  outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            //outputStream.write(string.getBytes());
            //outputStream.close();
            //} catch (Exception e) {
            //   Log.e(TAG, "File write failed");
            //}


        }
    }

    /***
     * ClientTask is an AsyncTask that should send a string over the network.
     * It is created by ClientTask.executeOnExecutor() call whenever OnKeyListener.onKey() detects
     * an enter key press event.
     *
     * @author stevko
     *
     */

    private class ClientTask extends AsyncTask<String, Void, Void> {
        int i;
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String myPort = msgs[1];
                ArrayList<Integer> values = new ArrayList<Integer>();
                String id2 ="";
                String msg2 = "";
                int Pseq = 0;

                String[] remotePort = new String[5];
                remotePort[0] = REMOTE_PORT0;
                remotePort[1] = REMOTE_PORT1;
                remotePort[2] = REMOTE_PORT2;
                remotePort[3] = REMOTE_PORT3;
                remotePort[4] = REMOTE_PORT4;

                for ( i= 0;i<5;i++) {
if(failedPort!=i) {
    try {
        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                Integer.parseInt(remotePort[i]));
        socket.setSoTimeout(1500);

        String msgToSend = msgs[0];
        String id = myPort + ":" + idseqno;
        // idseqno += 1;
        InitialMessage initialMessage = new InitialMessage(id, msgToSend);
        OutputStream outputStream = socket.getOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(outputStream);
        out.writeObject(initialMessage);
        // Log.e(TAG,"sending msg CLIENT");

        InputStream inputStream = socket.getInputStream();

        ObjectInputStream oinput = new ObjectInputStream(inputStream);

        Object obj = oinput.readObject();


        //Log.e(TAG,"RECEIVED SEQ");
        ProposedSequence proposedSequence = (ProposedSequence) obj;
        id2 = proposedSequence.id;
        msg2 = proposedSequence.msg;
        Pseq = proposedSequence.getSeqno();

        //Log.e("id",id2);
        // Log.e("msg",msg2);
        values.add(Pseq);

    } catch (IOException e) {
        if(count==0) {
            failedPort = i;
            Log.e(TAG, "IOFAIL" + remotePort[failedPort]);
            count++;
        }
    }




}  }

                idseqno += 1;
                //for (int i = 0; i < 5; i++) {
                //  Log.e(TAG, "values are:" + values.get(i));
                //}
                int maxi = Collections.max(values);

                Log.e(TAG, "MAX VALUE" + maxi);
                values.clear();
                // Aseqno =max;
                // Log.e("id2",id2);
                // Log.e("msg2",msg2);
                ProposedSequence ps = new ProposedSequence(id2, msg2, maxi);

                for (int i = 0; i < 5; i++) {

if(failedPort!=i) {
    try{
    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
            Integer.parseInt(remotePort[i]));
    socket.setSoTimeout(1500);
    OutputStream outputStream = socket.getOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(outputStream);
    out.writeObject(ps);
    //   Log.e(TAG,"sending msg with AMax");
    // socket.close();

}catch(IOException e) {
        if (count == 0) {
            failedPort = i;
            Log.e(TAG, "2nd fail" + remotePort[failedPort]);
            count++;
        }
    }
}
                }
            }
            catch(ClassNotFoundException e){
                // Log.e(TAG,e.getMessage());
            }

            return null;
        }
    }





    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }
    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }
    public int generateseqno() {



/*
        if (Pseqno < Aseqno) {
            Pseqno = Aseqno+1;
        }
        else{
            Pseqno = Pseqno + 1;

        }*/

        Pseqno = Math.max(Pseqno,Aseqno)+1;

        // String s = Integer.toString(Pseqno);
        Log.e(TAG,""+Pseqno);
        return Pseqno;
    }
    public void changePriority(ProposedSequence obj){
        String id = obj.getId();
        Iterator itr = priorityQueue.iterator();
        while(itr.hasNext()){
            ProposedSequence qk = (ProposedSequence) itr.next();
            //Log.e("TAG is the size",""+qk.getId());
            //Log.e("TAG is the id",""+id);
            //Log.e("TAG is the size",""+qk.getId());
            if(qk.getId().equals(id))
            {  // Log.e("TAG is the size",""+qk.getId());
                priorityQueue.remove(qk);
                priorityQueue.add(obj);

            }


        }

    }
}
