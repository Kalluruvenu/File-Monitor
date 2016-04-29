package filemonitor.sample.come.filemontior;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.util.ArrayList;

import filemonitor.sample.come.filemontior.recyclerviews.BigFilesAdapter;
import filemonitor.sample.come.filemontior.recyclerviews.FileSizeAdapter;
import filemonitor.sample.come.filemontior.services.FileMontiorService;

public class MainActivity extends AppCompatActivity {


    private ViewPager mViewPager;
    private GoogleApiClient client;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private int instanceNum;
    private Toolbar toolbar;
    private Button shareButton;
    private TextView txtViewNoData;

    public static boolean isFileScanCompleted = false;

    FileMontiorService fileMontiorService;
    private ProgressDialog mProgressDialog;

    Messenger messenger = new Messenger(new IncomingHandler());

    Messenger mService;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            sendMessage(FileMontiorService.SCAN_FILES);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    public void shareData(View view) {
        if (!isFileScanCompleted) {
            Toast.makeText(MainActivity.this, "No files scanned, Run scan once", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            if (isFilesDataAvailable()) {
                intent.putExtra(Intent.EXTRA_INTENT, "Some information");
            }else{
                intent.putExtra(Intent.EXTRA_INTENT, "No Files present");
            }
            intent.setType("text/plain");
            startActivity(Intent.createChooser(intent, "Select"));
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setTitle("File chooser");
    }

    private void setToolBarTitle(String title) {
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_start_scan) {
            startScan();
        } else if (id == R.id.action_stop_scan) {
            stopScan();
        }

        return super.onOptionsItemSelected(item);
    }

    private void startScan() {
        Log.i("MainActivity", "startScan: " + FileMontiorService.isServiceRunning());
        if (!FileMontiorService.isServiceRunning()) {
            Intent intent = new Intent(MainActivity.this, FileMontiorService.class);
            boolean retValue = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            if (retValue == true) {
                sendMessage(FileMontiorService.SCAN_FILES);
            }
            Log.i("MainActivity", "startScan: retVvalue " + retValue);
        } else {
            Toast.makeText(MainActivity.this, "Scan in progress", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopScan() {
        Intent intent = new Intent(MainActivity.this, FileMontiorService.class);
//        unbindService(mConnection);
        stopService(intent);
        dismissDialog();
        FilesData fileData = FileMontiorService.getFilesData();
    }

    private void sendMessage(int messagetype) {
        Message message = Message.obtain(null, messagetype);
        message.replyTo = messenger;
        try {
            if (mService != null) {
                showProgressDialog("Please wait...");
                mService.send(message);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    public void showToast(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    public class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int state = msg.arg1;
            Log.i("MainActivity", "handleMessage: Handle completed : " + state);
            showToast("Scan completed");
            isFileScanCompleted = true;
            stopScan();
            initRecyclerView(0);
        }
    }

    public void startFileScan(View view) {
        startScan();
    }

    public void showBiggestFiles(View view) {
        initRecyclerView(0);
    }


    public void showFrequentFiles(View view) {
        initRecyclerView(1);
    }


    @Override
    public void onBackPressed() {
        stopScan();
        super.onBackPressed();
    }

    protected void showProgressDialog(String pMessage) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this, R.style.Custom_Dark_Dialog);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.setMessage(pMessage);
        mProgressDialog.show();
    }

    protected void dismissDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public void initRecyclerView(int value) {
        Log.i("Recycler view", "initRecyclerView: instanceNum : " + instanceNum);
        mRecyclerView = (RecyclerView) findViewById(R.id.recylerViewSizes);
        FilesData filesData = FileMontiorService.getFilesData();

        txtViewNoData = (TextView) findViewById(R.id.txtViewNoData);
        /*There is no valid data to show*/
        if (isFilesDataAvailable()){
            txtViewNoData.setVisibility(View.VISIBLE);
            txtViewNoData.setText(getString(R.string.strNoFiles));
            mRecyclerView.setVisibility(View.GONE);
            return;
        }


        /*There is some data to show... Render the Recycler Views*/
        txtViewNoData.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        if (filesData != null) {
            ArrayList<File> sortedlist = filesData.getSortedList();
            ArrayList<FileMapData> mapdata = filesData.getExtensionMapData();

            mRecyclerView.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mAdapter = null;
            if (value == 0) {
                mAdapter = new FileSizeAdapter(sortedlist);
                setToolBarTitle("Biggest Files");
            } else {
                mAdapter = new BigFilesAdapter(mapdata);
                setToolBarTitle("Frequent Files");
            }
            mRecyclerView.setAdapter(mAdapter);
        }
    }


    private boolean isFilesDataAvailable() {
        FilesData filesData = FileMontiorService.getFilesData();

        /*if there is valid data and valid files in the data.*/

        return filesData == null || (filesData.fileDetails.size() <= 0);
    }


}
