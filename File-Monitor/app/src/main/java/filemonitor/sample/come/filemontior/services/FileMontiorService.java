package filemonitor.sample.come.filemontior.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import filemonitor.sample.come.filemontior.FileMapData;
import filemonitor.sample.come.filemontior.FilesData;

public class FileMontiorService extends Service {

    private static final String TAG = "FileMontiorService";
    private static boolean isReadServiceStarted = false;

    private static FilesData filesData;

    private final IBinder mBinder = new LocalBinder();

    private final Messenger messenger = new Messenger(new ServiceHandler());

    public static final int SCAN_FILES = 1;
    public static final int STOP_SCAN = 2;
    public static final int SCAN_COMPLETED = 3;

    ArrayList<Messenger> mClients = new ArrayList<Messenger>();

    @Override
    public void onCreate() {
        super.onCreate();
        if (filesData == null) {
            filesData = new FilesData();
        }
    }

    public static boolean isServiceRunning() {
        return isReadServiceStarted;
    }


    public static FilesData getFilesData() {
        return filesData;
    }


    public void readFiles() {
        String path = Environment.getExternalStorageDirectory().toString();
        File f = new File(path);
        File file[] = Environment.getExternalStorageDirectory().listFiles();
        if (file == null) return;
        for (int i = 0; i < file.length; i++) {
            File eachFile = file[i];
            readSubElements(eachFile.toString());
        }

        this.filesData.sortList();

        List<File> topList = filesData.getSortedList();

        for (int i = 0; i < topList.size(); i++) {
            Log.i("PrintFiles", topList.get(i).getName() + " - " + topList.get(i).length());
        }


        ArrayList<FileMapData> mapList = filesData.getExtensionMapData();
        for (int i = 0; i < mapList.size(); i++) {
            Log.i("PrintFiles", "readFiles: " + mapList.get(i).extension + " Count : " + mapList.get(i).count);
        }

    }

    public ArrayList<File> readSubElements(String directoryName) {
        File directory = new File(directoryName);
        ArrayList<File> resultList = new ArrayList<>();
        // get all the files from a directory
        File[] fList = directory.listFiles();
        if (fList != null && fList.length > 0) {
            resultList.addAll(Arrays.asList(fList));
            for (File file : fList) {
                if (file.isFile()) {
                    this.filesData.addFile(file);
                } else if (file.isDirectory()) {
                    resultList.addAll(readSubElements(file.getAbsolutePath()));
                }
            }
        } else {
            System.out.println("Nothing in : " + directory.getName());
        }
        return resultList;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }


    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: called");
        super.onDestroy();
        isReadServiceStarted = false;
    }

    public class LocalBinder extends Binder {
        public FileMontiorService getServiceInstance() {
            return FileMontiorService.this;
        }
    }


    public class ServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage: Message Wht : " + msg.what);
            switch (msg.what) {
                case FileMontiorService.SCAN_FILES:
                    mClients.add(msg.replyTo);
                    if (isReadServiceStarted == false) {
                        isReadServiceStarted = true;
                        ScanTask scanTask = new ScanTask();
                        scanTask.execute();
                        isReadServiceStarted = false;
                    }

                    break;
                case FileMontiorService.STOP_SCAN:
                    stopSelf();
                    break;
            }
            super.handleMessage(msg);
        }
    }


    private void sendMessageToSender() {
        Log.i(TAG, "sendMessageToSender: Number of clients :  " + mClients.size());
        for (int i = 0; i < mClients.size(); i++) {
            Messenger eachClient = mClients.get(i);
            Message msg = Message.obtain(null, FileMontiorService.SCAN_COMPLETED);
            try {
                eachClient.send(msg);
            } catch (RemoteException e) {
                if (eachClient != null) {
                    mClients.remove(i);
                }
            }
        }
        isReadServiceStarted = false;
    }

    private class ScanTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            readFiles();
            sendMessageToSender();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            sendMessageToSender();
        }
    }


}
