package filemonitor.sample.come.filemontior;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FilesData {
    private static final String TAG = "FilesData";
    public ArrayList<File> fileDetails;
    private static final int MAX_SIZE = 10;
    FileComparator fileComparator;
    FileExtnComparator fileExtnComparator;
    private HashMap<String, ArrayList<File>> mHitMap;
    private ArrayList<FileMapData> fileMapData;

    public FilesData() {
        this.fileDetails = new ArrayList<>();
        this.fileComparator = new FileComparator();
        fileExtnComparator = new FileExtnComparator();
        this.mHitMap = new HashMap<>();
        fileMapData = new ArrayList<>();
    }


    public ArrayList<File> getSortedList() {
        return this.fileDetails;
    }


    public void addFile(File pFile) {
        if (pFile != null) {
            String extn = getFileExtn(pFile.getName());
            if (!TextUtils.isEmpty(extn)) {
                addToHitMap(extn, pFile);
//                Log.i(TAG, "addFile: To the List" + pFile.getName());
                this.fileDetails.add(pFile);
            }
        }
    }

    private void addToHitMap(String pExtn, File pFile) {
        ArrayList<File> files;
        if (this.mHitMap.containsKey(pExtn)) {
            files = this.mHitMap.get(pExtn);
            files.add(pFile);
        } else {
            files = new ArrayList<>();
            files.add(pFile);
            this.mHitMap.put(pExtn, files);
        }
    }

    private String getFileExtn(String pFileName) {
        if (!TextUtils.isEmpty(pFileName) && pFileName.contains(".")) {
            return pFileName.substring(pFileName.lastIndexOf('.') + 1, pFileName.length());
        }
        return null;
    }


    public ArrayList<FileMapData> getExtensionMapData() {
        if (this.fileMapData != null) {
            this.fileMapData.clear();
        }

        Set s = this.mHitMap.entrySet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String) entry.getKey();
            ArrayList<File> files = (ArrayList<File>) entry.getValue();
            FileMapData data = new FileMapData();
            data.extension = key;
            data.count = files.size();
            fileMapData.add(data);
        }//while
        Collections.sort(this.fileMapData, this.fileExtnComparator);
        return this.fileMapData;
    }


    public void addlist(ArrayList<File> list) {
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (!list.get(i).isDirectory()) {
                    addFile(list.get(i));
                }
            }
        }
    }


    public void sortList() {
        Collections.sort(this.fileDetails, this.fileComparator);
    }

    public class FileComparator implements Comparator<File> {

        @Override
        public int compare(File lhs, File rhs) {
            return (int) (rhs.length() - lhs.length());
        }
    }

    public class FileExtnComparator implements Comparator<FileMapData> {

        @Override
        public int compare(FileMapData lhs, FileMapData rhs) {
            return rhs.count - lhs.count;
        }
    }


}
