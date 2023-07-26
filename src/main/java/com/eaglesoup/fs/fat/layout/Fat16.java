package com.eaglesoup.fs.fat.layout;

import com.eaglesoup.device.IDisk;
import com.eaglesoup.fs.UnixDirectory;
import com.eaglesoup.fs.fat.Layout;
import com.eaglesoup.util.ParseUtils;
import lombok.SneakyThrows;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.*;
import java.util.stream.Collectors;

public class Fat16 {
    private final IDisk disk;
    private int[] fat = new int[Layout.FAT_ENTRIES_COUNT];

    public final UnixDirectory rootDirectory;

    public Fat16(IDisk disk) {
        DirectoryEntity directoryEntity = new DirectoryEntity();
        directoryEntity.setAttributeByte(Layout.DIR_MARK);
        this.rootDirectory = new UnixDirectory(null, directoryEntity, "/",
                Layout.ROOT_DIRECTORY_REGION_START, 0);
        this.disk = disk;
        if (!loadRegion()) {
            format();
        }
        loadFat();
    }

    @SneakyThrows
    private boolean loadRegion() {
        ReservedRegion region = new ReservedRegion();
        byte[] data = this.disk.readSector(Layout.RESERVED_REGION_START);
        region.from(data);
        return region.getBootSectorSignature()[0] == new ReservedRegion().getBootSectorSignature()[0];
    }

    /**
     * =======FAT操作==========
     */
    private void loadFat() {
        ByteBuffer buffer = ByteBuffer.allocate(Layout.SECTORS_PER_FAT * Layout.SECTOR_SIZE);
        for (int i = 0; i < Layout.SECTORS_PER_FAT; i++) {
            byte[] data = disk.readSector(Layout.FAT_REGION_START + i);
            buffer.put(data);
        }
        buffer.rewind();
        ShortBuffer shortBuffer = buffer.asShortBuffer();
        for (int i = 0; i < Layout.FAT_ENTRIES_COUNT; i++) {
            fat[i] = shortBuffer.get() & 0xFFFF;
        }
    }

    private void flushFat() {
        ByteBuffer buffer = ByteBuffer.allocate(Layout.SECTORS_PER_FAT * Layout.SECTOR_SIZE);
        for (int j : fat) {
            short f = (short) (j & 0xFFFF);
            buffer.putShort(f);
        }
        buffer.rewind();
        for (int i = 0; i < Layout.SECTORS_PER_FAT; i++) {
            byte[] data = new byte[Layout.SECTOR_SIZE];
            buffer.get(data);
            this.disk.writeSector(Layout.FAT_REGION_START + i, data);
        }
    }

    private void resetDataFat() {
        byte[] clear = new byte[Layout.SECTOR_SIZE];
        for (int i = Layout.NUMBER_OF_LIST_CLUSTER_SIZE; i < Layout.FAT_ENTRIES_COUNT; i++) {
            for (int j = 0; j < Layout.SECTORS_PER_CLUSTER; j++) {
                int sectorIndex = i * Layout.SECTORS_PER_CLUSTER + j;
                disk.writeSector(sectorIndex, clear);
            }
        }
    }

    public void format() {
        disk.format();
        byte[] clear = new byte[Layout.SECTOR_SIZE];
        //数据区前的所有扇区重置为0
        for (int i = 0; i < Layout.DATA_REGION_START; i++) {
            this.disk.writeSector(i, clear);
        }
        //格式化"保留区"
        this.disk.writeSector(0, new ReservedRegion().getBytes());
        //格式化fat有值的"data区域"
        resetDataFat();
        fat = new int[Layout.FAT_ENTRIES_COUNT];
    }

    public void close() {
        flushFat();
    }

    /**
     * =======文件条目相关操作=======
     */
//    public List<UnixDirectory> listDirectoryEntry(UnixDirectory parent) {
//        if (parent == rootDirectory) {
//            return listDirectoryEntry(parent, Layout.ROOT_DIRECTORY_REGION_START, Layout.NUMBER_OF_POSSIBLE_ROOT_ENTRIES);
//        } else {
//            int clusterIndex = parent.getOriginal().getStartingCluster() * Layout.SECTORS_PER_CLUSTER;
//            return listDirectoryEntry(parent, clusterIndex, Layout.SECTORS_PER_CLUSTER);
//        }
//    }
    public List<UnixDirectory> listEntityExcludeFln(UnixDirectory parent) {
        List<UnixDirectory> directories = listDirectoryEntry(parent);
        for (int i = 0; i < directories.size(); ) {
            DirectoryEntity directoryEntity = directories.get(i).getOriginal();
            LfnEntity lfnEntity = new LfnEntity();
            lfnEntity.from(directoryEntity.getBytes());
            if (lfnEntity.isLastLfnEntity()) {

            } else {
                i++;
            }
        }
        return directories;
    }

    /**
     * 查询所有的entity(32个字节): 包括LFN类型的entity
     */
    public List<UnixDirectory> listDirectoryEntry(UnixDirectory parent) {
        int sectorIdx = parent == rootDirectory ? Layout.ROOT_DIRECTORY_REGION_START : parent.getOriginal().getStartingCluster() * Layout.SECTORS_PER_CLUSTER;
        int sectorsSize = parent == rootDirectory ? Layout.NUMBER_OF_POSSIBLE_ROOT_ENTRIES : Layout.SECTORS_PER_CLUSTER;
        List<UnixDirectory> directories = new ArrayList<>();
        for (int j = 0; j < sectorsSize; j++) {
            byte[] sectors = this.disk.readSector(sectorIdx + j);
            ByteBuffer buffer = ByteBuffer.wrap(sectors);
            for (int p = 0; p < sectors.length / Layout.DIRECTORY_ENTRY_SIZE; p++) {
                byte[] bytes = new byte[Layout.DIRECTORY_ENTRY_SIZE];
                buffer.get(bytes, 0, Layout.DIRECTORY_ENTRY_SIZE);
                DirectoryEntity entry = new DirectoryEntity();
                entry.from(bytes);
                String fullFileName = fullFileName(directories, entry);
                directories.add(new UnixDirectory(parent, entry, fullFileName, sectorIdx + j, p * Layout.DIRECTORY_ENTRY_SIZE));
            }
        }
        return new ArrayList<>(directories);
    }

    private String fullFileName(List<UnixDirectory> directories, DirectoryEntity directoryEntity) {
        //LFN entity
        if (directoryEntity.isLFN(directoryEntity.getAttributeByte())) {
            return "";
        }
        //directory entity
        if (!directoryEntity.isLongFileName()) {
            return ParseUtils.byte2Str(directoryEntity.getFileName());
        }
        //directory entity: longFileName
        StringBuilder longFileName = new StringBuilder();
        int prefixLfnIndex = directories.size() - 1;
        while (true) {
            //处理"长文件"逻辑
            UnixDirectory unixDirectory = directories.get(prefixLfnIndex);
            LfnEntity lfnEntity = unixDirectory.getOriginal().transferToLfnEntity();
            longFileName.append(ParseUtils.byte2Str(lfnEntity.getPart1()));
            longFileName.append(ParseUtils.byte2Str(lfnEntity.getPart2()));
            if (lfnEntity.isLastLfnEntity()) {
                break;
            }
            prefixLfnIndex--;
        }
        return longFileName.toString();
    }

    private Integer[] listSector(int clusterIdx) {
        List<Integer> sectors = new ArrayList<>();
        Integer[] clusters = listCluster(clusterIdx);
        for (Integer cluster : clusters) {
            sectors.add(cluster * Layout.SECTORS_PER_CLUSTER);
        }
        return sectors.toArray(new Integer[0]);
    }

    public Integer[] listCluster(int clusterIdx) {
        List<Integer> clusters = new ArrayList<>();
        int pos = clusterIdx;
        clusters.add(pos);
        while (true) {
            int curr = fat[pos];
            if (curr == 0x0000) {
                return new Integer[0];
            } else if (curr == 0x0001 || curr == 0x0002) {
                throw new IllegalStateException("fat cluster[" + clusterIdx + "]  not allowed");
            } else if (curr == 0xFFF7) {
                throw new IllegalStateException("fat cluster[" + clusterIdx + "]  bad sectors in cluster");
            } else if (curr == 0xFFF8) {
                break;
            } else {
                clusters.add(curr);
                pos = curr;
            }
        }
        return clusters.toArray(new Integer[0]);
    }

    public Integer findNextCluster(int clusterIdx) {
        int curr = fat[clusterIdx];
        if (curr == 0x0000) {
            return null;
        } else if (curr == 0x0001 || curr == 0x0002) {
            throw new IllegalStateException("fat cluster[" + clusterIdx + "]  not allowed");
        } else if (curr == 0xFFF7) {
            throw new IllegalStateException("fat cluster[" + clusterIdx + "]  bad sectors in cluster");
        } else if (curr == 0xFFF8) {
            return -1;
        } else {
            return curr;
        }
    }

    public Integer findNextFreeCluster(int clusterIdx) {
        int freePos = -1;
        for (int pos = Layout.FAT_FREE_START; pos < Layout.DATA_REGION_SIZE; pos++) {
            if (fat[pos] == 0x0000) {
                freePos = pos;
                break;
            }
        }
        if (freePos < 0) {
            throw new IllegalStateException("not found free cluster");
        }
        if (clusterIdx > 0) {
            fat[clusterIdx] = freePos;
        }
        fat[freePos] = 0xFFF8;
        flushFat();
        return freePos;
    }

    /**
     * 创建目录或者文件
     */
    public UnixDirectory createDirectory(UnixDirectory parent, String pathName, boolean isDir) {
        UnixDirectory directory = findFreeDirectory(parent, pathName);
        if (directory == null) {
            throw new IllegalStateException("directory max size " + Layout.NUMBER_OF_ROOT_ENTRIES_COUNT);
        }
        directory.setOriginal(buildDirectory(pathName, isDir));
        writeEntry(directory.getOriginal(), directory.getSectorIdx(), directory.getOffset());
        return directory;
    }

    /**
     * 创建目录项目
     */
    private DirectoryEntity buildDirectory(String pathName, boolean isDir) {
        DirectoryEntity entry = new DirectoryEntity();
        byte[] fileName = new byte[8];
        System.arraycopy(pathName.getBytes(), 0, fileName, 0, Math.min(pathName.length(), 8));
        entry.setFileName(fileName);
        //todo 设置文件后缀
//        entry.setFilenameExtension();
        entry.setCreationTimeStamp((int) (System.currentTimeMillis() / 1000));
        entry.setLastWriteTimeStamp((int) (System.currentTimeMillis() / 1000));
        entry.setStartingCluster((short) (findNextFreeCluster(Integer.MIN_VALUE) & 0XFFFF));
        entry.setAttributeByte(isDir ? Layout.DIR_MARK : Layout.FILE_MARK);
        return entry;
    }

    private UnixDirectory findFreeDirectory(UnixDirectory parent, String pathName) {
        List<UnixDirectory> subDirectors = listDirectoryEntry(parent);
        for (int i = 0; i < subDirectors.size(); i++) {
            //n个LfnEntity + 1个directoryEntity
            int needEntityCount = 1;
            if (pathName.length() > Layout.LONG_FILE_NAME_LENGTH) {
                needEntityCount = (int) Math.ceil((double) pathName.length() / Layout.LFN_FILE_LENGTH) + 1;
            }
            int hitCount = 0;
            for (int j = 0; j < needEntityCount; j++) {
                int pos = i + j;
                if (subDirectors.size() == pos) {
                    return null;
                }
                DirectoryEntity dirEntity = subDirectors.get(pos).getOriginal();
                if (dirEntity.isDirEntity(dirEntity.getAttributeByte()) && dirEntity.isEmptyDirEntity()) {
                    hitCount++;
                } else {
                    break;
                }
            }
            if (hitCount == needEntityCount) {
                return subDirectors.get(i);
            }
        }
        return null;
    }

    public void removeDirectory(UnixDirectory directory) {
        if (directory == rootDirectory) {
            throw new IllegalStateException("root directory not delete");
        }
        this.writeEntry(new DirectoryEntity(), directory.getSectorIdx(), directory.getOffset());
    }


    public void updateDirectory(UnixDirectory path) {
        this.writeEntry(path.getOriginal(), path.getSectorIdx(), path.getOffset());
    }

    private void writeEntry(DirectoryEntity directoryEntity, int sectorIdx, int offset) {
        ByteBuffer buffer = ByteBuffer.wrap(this.disk.readSector(sectorIdx));
        buffer.position(offset);
        buffer.put(directoryEntity.getBytes(), 0, Layout.DIRECTORY_ENTRY_SIZE);
        this.disk.writeSector(sectorIdx, buffer.array());
    }

    public void freeCluster(int clusterIdx) {
        int pos = clusterIdx;
        while (true) {
            Integer curr = findNextCluster(pos);
            if (curr > 0) {
                pos = curr;
                fat[curr] = 0x0000;
            } else {
                break;
            }
        }
        fat[clusterIdx] = 0xFFF8;
        flushFat();
    }
}
