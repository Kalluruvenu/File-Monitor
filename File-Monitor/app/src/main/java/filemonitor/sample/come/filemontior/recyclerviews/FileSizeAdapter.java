package filemonitor.sample.come.filemontior.recyclerviews;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import filemonitor.sample.come.filemontior.R;

public class FileSizeAdapter extends RecyclerView.Adapter<FileSizeAdapter.FileSizeViewHolder> {

    private ArrayList<File> mFileData;

    public FileSizeAdapter(ArrayList<File> fileData) {
        this.mFileData = fileData;
    }

    @Override
    public FileSizeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_row, parent, false);
        FileSizeViewHolder holder = new FileSizeViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(FileSizeViewHolder holder, int position) {
        if (mFileData != null && this.mFileData.size() > 0) {
            File eachData = this.mFileData.get(position);
            holder.txtViewFileName.setText(eachData.getName());
        }
    }

    @Override
    public int getItemCount() {
        return 10;
    }

    public class FileSizeViewHolder extends RecyclerView.ViewHolder {
        public TextView txtViewFileName;


        public FileSizeViewHolder(View itemView) {
            super(itemView);
            if (itemView != null) {
                txtViewFileName = (TextView) itemView.findViewById(R.id.txtName);
            }
        }
    }
}
