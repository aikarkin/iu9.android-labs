package iu9.bmstu.ru.lab8;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class FilesAdapter extends ArrayAdapter<FilesAdapter.FileItem> {
    FilesAdapter(@NonNull Context context, @NonNull List<FileItem> fileList) {
        super(context, R.layout.file_item_layout, fileList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        FileItem file = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.file_item_layout, parent, false);
        }

        if(file == null) {
            return convertView;
        }

        ImageView imgFileType = convertView.findViewById(R.id.fileTypeIcon);
        TextView tvFileName = convertView.findViewById(R.id.tvFileName);
        TextView tvFileType = convertView.findViewById(R.id.tvFileType);

        imgFileType.setImageResource(file.file.isDirectory() ? R.drawable.ic_folder_dark_64dp : R.drawable.ic_insert_drive_file_dark_64dp);
        tvFileName.setText(file.getFileName());
        tvFileType.setText(file.getFile().isDirectory() ? R.string.file_type_dir : R.string.file_type_file);

        return convertView;
    }

    public static class FileItem {
        private String fileName;
        private File file;

        public FileItem(File file) {
            this.file = file;
            this.fileName = file.getName();
        }

        String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public File getFile() {
            return file;
        }
    }
}
