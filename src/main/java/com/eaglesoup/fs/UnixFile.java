package com.eaglesoup.fs;

import com.eaglesoup.os.MosOs;

import java.util.Stack;

public final class UnixFile {
    private String path;
    private final transient int prefixLength;
    private IFileSystem fs = MosOs.fileSystem();

    public UnixFile(String path) {
        this.path = canonicalPath(path);
        this.prefixLength = prefixLength(path);
    }

    public UnixFile(UnixFile parent, String path) {
        if (parent != null) {
            if (path.startsWith("/")) {
                this.path = canonicalPath(path);
            } else {
                this.path = canonicalPath(parent.path + "/" + path);
            }
        } else {
            this.path = canonicalPath(path);
        }
        this.prefixLength = prefixLength(path);
    }

    public boolean isAbsolute() {
        return path.startsWith("/");
    }

    public String getAbstractPath() {
        if (isAbsolute()) return this.path;
        return canonicalPath("/" + this.path);
    }

    public String getName() {
        int index = path.lastIndexOf("/");
        if (index < prefixLength) return path.substring(prefixLength);
        return path.substring(index + 1);
    }

    public String getParent() {
        int index = path.lastIndexOf(fs.getDefaultSeparator());
        if (index < prefixLength) {
            if ((prefixLength > 0) && (path.length() > prefixLength))
                return path.substring(0, prefixLength);
            return fs.getDefaultRootPath();
        }
        return path.substring(0, index);
    }

    public boolean isDir() {
        return fs.isDir(this);
    }

    public boolean exist() {
        return fs.exist(this);
    }

    public boolean mkdir() {
        return fs.createDirectory(this);
    }

    public boolean delete() {
        if (exist()) {
            fs.delete(path);
            return true;
        } else {
            throw new IllegalStateException("delete: " + this.path + ": No such file or directory");
        }
    }


    public UnixFile[] listFiles() {
        if (isDir()) {
            UnixFile[] unixFiles = fs.listPaths(path);
            for (int i = 0; i < unixFiles.length; i++) {
                unixFiles[i] = new UnixFile(unixFiles[i].getAbstractPath());
            }
            return unixFiles;
        }
        throw new IllegalStateException("listPath: " + this.path + ": Not a directory");
    }

    public int length() {
        if (exist()) {
            return fs.length(this);
        } else {
            throw new IllegalStateException("length: " + this.path + ": No such file or directory");
        }
    }

    public long lastModified() {
        if (exist()) {
            return fs.lastModifiedTime(this) * 1000L;
        }
        throw new IllegalStateException("lastModified: " + this.path + ": No such file or directory");
    }

    private int prefixLength(String pathname) {
        if (pathname.isEmpty()) return 0;
        return (pathname.charAt(0) == '/') ? 1 : 0;
    }

    private String canonicalPath(String path) {
        String[] paths = path.split(fs.getDefaultSeparator());
        Stack<String> stack = new Stack<>();
        for (String p : paths) {
            if (p.equals(".") || p.equals("")) {
                continue;
            } else if (p.equals("..")) {
                if (!stack.empty()) {
                    stack.pop();
                }
            } else {
                stack.push(p);
            }
        }
        return fs.getDefaultRootPath() + String.join(fs.getDefaultSeparator(), stack);
    }
}
