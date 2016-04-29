package filemonitor.sample.come.filemontior.recyclerviews;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import filemonitor.sample.come.filemontior.FileMapData;
import filemonitor.sample.come.filemontior.R;

public class BigFilesAdapter extends RecyclerView.Adapter<BigFilesAdapter.FileSizeViewHolder> {


    private ArrayList<FileMapData> mData;

    public BigFilesAdapter(ArrayList<FileMapData> pData) {
        this.mData = pData;
    }

    @Override
    public FileSizeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_row, parent, false);
        FileSizeViewHolder holder = new FileSizeViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(FileSizeViewHolder holder, int position) {
        if (mData != null) {
            FileMapData eachData = this.mData.get(position);
            holder.txtViewFileName.setText(eachData.extension);
            holder.txtViewValue.setText("" + eachData.count);
        }

    }

    @Override
    public int getItemCount() {
        return 10;//this.mData.size();
    }

    public class FileSizeViewHolder extends RecyclerView.ViewHolder {
        public TextView txtViewFileName;
        public TextView txtViewValue;

        public FileSizeViewHolder(View itemView) {
            super(itemView);
            if (itemView != null) {
                txtViewFileName = (TextView) itemView.findViewById(R.id.txtName);
                txtViewValue = (TextView) itemView.findViewById(R.id.txtValue);
                txtViewValue.setVisibility(View.VISIBLE);
            }
        }
    }
}
