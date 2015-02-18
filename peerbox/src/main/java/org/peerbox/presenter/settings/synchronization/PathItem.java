package org.peerbox.presenter.settings.synchronization;

import java.nio.file.Path;

public class PathItem {
    private Path path;
    public PathItem(Path path) {
        this.path = path;
    }
    public Path getPath() {
        return path;
    }
    @Override
    public String toString() {
    	return "";
//        if (path.getFileName() == null) {
//            return path.toString();
//        } else {
//            return path.getFileName().toString(); // showing file name on the TreeView
//        }
    }        
}