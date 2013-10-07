package stericson.busybox.donate.fileexplorer;

import java.io.File;


public class FileEntry implements Comparable<FileEntry> {
    public String filePath = "";
    public File file = null;

    public FileEntry(String filePath) {
        this.filePath = filePath;
        this.file = new File(filePath);
    }

    public int compareTo(FileEntry another) {
        return file.getName().compareToIgnoreCase(another.file.getName());
    }
}
