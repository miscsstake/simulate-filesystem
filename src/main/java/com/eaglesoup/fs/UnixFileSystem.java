package com.eaglesoup.fs;

import com.eaglesoup.device.IDisk;
import com.eaglesoup.fs.fat.*;
import com.eaglesoup.fs.fat.layout.DirectoryEntity;
import com.eaglesoup.fs.fat.layout.Fat16;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class UnixFileSystem implements IFileSystem {
    private static final String DEFAULT_ROOT_PATH = "/";
    private static final String DEFAULT_SEPARATOR = "/";
    private static final UnixFileSystem fs = new UnixFileSystem();
    private IDisk disk;
    private Fat16 fat16;
    private UnixDirectory root;
    private final AtomicInteger FD_GEN = new AtomicInteger(1);

    private final Map<Integer, UnixFat16FileDescriptor> OPEN_LIST = new HashMap<>();

    public static IFileSystem getDefaultFileSystem() {
        return fs;
    }

    /**
     * ========磁盘操作==========
     */
    @Override
    public void mount(IDisk disk) {
        this.fat16 = new Fat16(disk);
        this.disk = disk;
        this.root = fat16.rootDirectory;
    }

    @Override
    public void umount() {
        fat16.close();
        disk.close();
    }

    @Override
    public void format() {
        fat16.format();
    }

    @Override
    public String getDefaultRootPath() {
        return DEFAULT_ROOT_PATH;
    }

    @Override
    public String getDefaultSeparator() {
        return DEFAULT_SEPARATOR;
    }

    public String[] normalize(String abstractPath) {
        List<String> result = new ArrayList<>();
        String[] paths = abstractPath.split(DEFAULT_SEPARATOR);
        for (String name : paths) {
            if (!name.isEmpty()) {
                result.add(name);
            }
        }
        return result.toArray(new String[0]);
    }

    @Override
    public void delete(String abstractPath) {
        UnixDirectory directory = getPath(abstractPath);
        if (directory != root) {
            if (directory.isFile()) {
                fat16.freeCluster(directory.getOriginal().getStartingCluster());
            } else if (directory.isDir()) {
                if (listPaths(directory).length > 0) {
                    throw new IllegalStateException("delete: " + abstractPath + ": Not Empty");
                }
            } else {
                throw new IllegalStateException("delete: " + abstractPath + " file type not support");
            }
            fat16.removeDirectory(directory);
        }
    }

    @Override
    public UnixFile[] listPaths(String path) {
        if (path.equals(DEFAULT_ROOT_PATH)) {
            return listPaths(root);
        }
        return listPaths(normalize(path));
    }


    public UnixFile[] listPaths(String[] paths) {
        if (paths.length == 1 && paths[0].equals(DEFAULT_ROOT_PATH)) {
            return listPaths(root);
        }
        return listPaths(getPath(paths));
    }

    public UnixFile[] listPaths(UnixDirectory path) {
        List<UnixDirectory> directories = fat16.listDirectoryEntry(path);
        List<UnixFile> unixPaths = new ArrayList<>();
        for (UnixDirectory directory : directories) {
            DirectoryEntity directoryEntity = directory.getOriginal();
            if (directory.getOriginal().getCreationTimeStamp() != 0) {
                unixPaths.add(new UnixFile(directory.getAbstractPath()));
            }
        }
        return unixPaths.toArray(new UnixFile[0]);
    }

    @Override
    public synchronized boolean createDirectory(UnixFile file) {
        UnixDirectory parent = getPath(file.getParent());
        if (!parent.exist()) {
            throw new IllegalStateException("mkdir: " + file.getAbstractPath() + ": No such file or directory");
        }
        if (exist(file)) {
            throw new IllegalStateException("mkdir: " + file.getAbstractPath() + ": directory is exist");
        }
        UnixDirectory directory = fat16.createDirectory(parent, file.getName(), true);
        if (directory != null) {
            return true;
        }
        return false;
    }

    @Override
    public int length(UnixFile file) {
        UnixDirectory directory = getPath(file.getAbstractPath());
        if (directory.exist()) {
            return directory.getOriginal().getFileSize();
        }
        return 0;
    }

    public UnixDirectory getPath(String path) {
        if (path.equals(DEFAULT_ROOT_PATH)) {
            return root;
        }
        return this.getPath(normalize(path));
    }

    @Override
    public boolean exist(UnixFile file) {
        UnixDirectory directory = getPath(file.getAbstractPath());
        return directory.exist();
    }

    @Override
    public boolean isDir(UnixFile file) {
        UnixDirectory directory = getPath(file.getAbstractPath());
        return directory.isDir();
    }

    @Override
    public long lastModifiedTime(UnixFile file) {
        UnixDirectory directory = getPath(file.getAbstractPath());
        if (directory.exist()) {
            return directory.getOriginal().getLastWriteTimeStamp();
        }
        return 0;
    }

    public UnixDirectory getPath(String[] paths) {
        if (paths == null || paths.length == 0) {
            return root;
        }
        if (paths.length == 1 && paths[0].equals(DEFAULT_ROOT_PATH)) {
            return root;
        }
        UnixDirectory parent = root;
        for (String path : paths) {
            boolean find = false;
            if (parent.exist()) {
                List<UnixDirectory> directories = fat16.listDirectoryEntry(parent);
                for (UnixDirectory directory : directories) {
                    if (directory.getPathName().equals(path)) {
                        parent = new UnixDirectory(parent, directory);
                        find = true;
                        break;
                    }
                }
            }
            if (!find) {
                parent = new UnixDirectory(parent, null, path, -1, -1);
            }
        }
        return parent;
    }

    @Override
    public UnixFileDescriptor open(String path) {
        return this.open(path, READ);
    }

    @Override
    public UnixFileDescriptor open(String path, int mode) {
        UnixDirectory entry = getPath(path);
        if (entry.exist()) {
            if (entry.isDir()) {
                throw new IllegalStateException("mkdir: " + path + ": No such file or directory");
            }
        } else {
            entry = fat16.createDirectory(entry.getParent(), entry.getPathName(), false);
        }

        int clusterIdx = entry.getOriginal().getStartingCluster();
        int sectorIdx = 0;
        int pos = 0;
        if (mode == APPEND) {
            Integer[] clusters = fat16.listCluster(clusterIdx);
            clusterIdx = clusters[clusters.length - 1];
            int offset = entry.getOriginal().getFileSize() % (Layout.SECTOR_SIZE * Layout.SECTORS_PER_CLUSTER);
            sectorIdx = offset / Layout.SECTOR_SIZE;
            pos = offset % Layout.SECTOR_SIZE;
            if (entry.getOriginal().getFileSize() == clusters.length * (Layout.SECTOR_SIZE * Layout.SECTORS_PER_CLUSTER)) {
                clusterIdx = fat16.findNextFreeCluster(clusterIdx);
            }
        } else if (mode == WRITE) {
            fat16.freeCluster(clusterIdx);
            entry.getOriginal().setFileSize(0);
            fat16.updateDirectory(entry);
        } else if (mode == READ) {

        }
        int size = entry.getOriginal().getFileSize();
        int fd = FD_GEN.getAndDecrement();
        UnixFat16FileDescriptor fat16Fd = new UnixFat16FileDescriptor(fd, entry,
                new UnixFat16FileInputStream(disk, fat16, clusterIdx, sectorIdx, pos, size),
                new UnixFat16FileOutputStream(disk, fat16, entry, clusterIdx, sectorIdx, pos, size, mode));
        OPEN_LIST.put(fd, fat16Fd);

        return new UnixFileDescriptor(fd);
    }

    @Override
    public UnixFileDescriptor open(UnixFile file) {
        return open(file, IFileSystem.READ);
    }

    @Override
    public UnixFileDescriptor open(UnixFile file, int mode) {
        return this.open(file.getAbstractPath(), mode);
    }

    @Override
    public int read(UnixFileDescriptor fd) {
        UnixFat16FileDescriptor fileDescriptor = OPEN_LIST.get(fd.getFd());
        return fileDescriptor.getInputStream().read();
    }

    @Override
    public void write(UnixFileDescriptor fd, int b) {
        UnixFat16FileDescriptor fileDescriptor = OPEN_LIST.get(fd.getFd());
        fileDescriptor.getOutputStream().write(b);
    }

    @Override
    public void flush(UnixFileDescriptor fd) {
        UnixFat16FileDescriptor fileDescriptor = OPEN_LIST.get(fd.getFd());
        fileDescriptor.getOutputStream().flush();
    }

    @Override
    public void close(UnixFileDescriptor fd) {
        UnixFat16FileDescriptor fileDescriptor = OPEN_LIST.get(fd.getFd());
        fileDescriptor.getOutputStream().close();
        fileDescriptor.getInputStream().close();
        OPEN_LIST.remove(fd.getFd());
    }
}
