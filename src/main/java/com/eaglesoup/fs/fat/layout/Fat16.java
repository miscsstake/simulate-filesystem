package com.eaglesoup.fs.fat.layout;

import com.eaglesoup.device.IDisk;
import com.eaglesoup.fs.UnixDirectory;
import com.eaglesoup.fs.fat.Layout;
import com.eaglesoup.util.ParseUtils;
import lombok.SneakyThrows;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.*;

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
            Integer[] clusters = listCluster(i);
            for (Integer clusterIdx : clusters) {
                int sectorIdx = clusterIdx * Layout.SECTORS_PER_CLUSTER;
                for (int j = 0; j < Layout.SECTORS_PER_CLUSTER; j++) {
                    this.disk.writeSector(sectorIdx + j, clear);
                }
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
        List<UnixDirectory> unixDirectoryList = findFreeDirectory(parent, pathName);
        if (unixDirectoryList.isEmpty()) {
            throw new IllegalStateException("directory max size " + Layout.NUMBER_OF_ROOT_ENTRIES_COUNT);
        }
        int unixDirectoryCount = unixDirectoryList.size();
        UnixDirectory dirEntityUnixDirectory = unixDirectoryList.get(unixDirectoryCount - 1);
        //倒序存放信息
        for (int i = 0; i < unixDirectoryCount; i++) {
            int pos = unixDirectoryCount - i - 1;
            if (i == 0) {
                //存放directoryEntity
                dirEntityUnixDirectory.setPathName(pathName);
                dirEntityUnixDirectory.setOriginal(buildDirectory(pathName, isDir));
                writeEntry(dirEntityUnixDirectory.getOriginal().getBytes(), dirEntityUnixDirectory.getSectorIdx(), dirEntityUnixDirectory.getOffset());
            } else {
                String lfnParts = pathName.substring((i - 1) * Layout.LFN_FILE_LENGTH, Math.min(i * Layout.LFN_FILE_LENGTH, pathName.length()));
                boolean isLastLfnNumber = i == unixDirectoryCount - 1;
                LfnEntity lfnEntity = buildLfnEntity(lfnParts, (byte) i, isLastLfnNumber);
                UnixDirectory lfnEntityUnixDirectory = unixDirectoryList.get(pos);
                writeEntry(lfnEntity.getBytes(), lfnEntityUnixDirectory.getSectorIdx(), lfnEntityUnixDirectory.getOffset());
            }
        }
        return dirEntityUnixDirectory;
    }

    /**
     * 创建目录项目
     */
    private DirectoryEntity buildDirectory(String pathName, boolean isDir) {
        DirectoryEntity entry = new DirectoryEntity();
        byte[] fileName = new byte[8];
        if (pathName.length() <= Layout.LONG_FILE_NAME_LENGTH) {
            System.arraycopy(pathName.getBytes(), 0, fileName, 0, pathName.length());
        }
        entry.setFileName(fileName);
        entry.setCreationTimeStamp((int) (System.currentTimeMillis() / 1000));
        entry.setLastWriteTimeStamp((int) (System.currentTimeMillis() / 1000));
        entry.setStartingCluster((short) (findNextFreeCluster(Integer.MIN_VALUE) & 0XFFFF));
        entry.setAttributeByte(isDir ? Layout.DIR_MARK : Layout.FILE_MARK);
        return entry;
    }

    /**
     * 创建长文件
     */
    private LfnEntity buildLfnEntity(String pathName, byte lfnNumber, boolean isLastLfn) {
        byte[] pathNameBytes = pathName.getBytes();
        LfnEntity lfnEntity = new LfnEntity();
        lfnEntity.setOriginField((byte) ((isLastLfn ? Layout.LFN_LAST_NUMBER : 0) + lfnNumber));
        lfnEntity.setPart1(Arrays.copyOfRange(pathNameBytes, 0, lfnEntity.getPart1().length));
        lfnEntity.setAttributeByte(Layout.LFN_MARK);
        lfnEntity.setPart2(Arrays.copyOfRange(pathNameBytes, lfnEntity.getPart1().length, lfnEntity.getPart2().length));
        return lfnEntity;
    }

    /**
     * 查找可用来创建新文件的索引位置：如果是长文件支持跨sector
     */
    private List<UnixDirectory> findFreeDirectory(UnixDirectory parent, String pathName) {
        List<UnixDirectory> subDirectors = listDirectoryEntry(parent);
        //n个LfnEntity + 1个directoryEntity
        int needEntityCount;
        if (pathName.length() > Layout.LONG_FILE_NAME_LENGTH) {
            needEntityCount = (int) Math.ceil((double) pathName.length() / Layout.LFN_FILE_LENGTH) + 1;
        } else {
            needEntityCount = 1;
        }
        for (int i = 0; i < subDirectors.size(); i++) {
            List<UnixDirectory> freeDirectoryList = new ArrayList<>();
            for (int j = 0; j < needEntityCount; j++) {
                int pos = i + j;
                if (subDirectors.size() == pos) {
                    return new ArrayList<>();
                }
                DirectoryEntity dirEntity = subDirectors.get(pos).getOriginal();
                if (dirEntity.isDirEntity(dirEntity.getAttributeByte()) && dirEntity.isEmptyDirEntity()) {
                    freeDirectoryList.add(subDirectors.get(pos));
                } else {
                    break;
                }
            }
            if (freeDirectoryList.size() == needEntityCount) {
                return freeDirectoryList;
            }
        }
        return new ArrayList<>();
    }

    public void removeDirectory(UnixDirectory directory) {
        if (directory == rootDirectory) {
            throw new IllegalStateException("root directory not delete");
        }
        int sectorIdx = directory.getSectorIdx();
        int offset = directory.getOffset();
        if (directory.getOriginal().isLongFileName()) {
            this.writeDirEntry(new DirectoryEntity(), sectorIdx, offset);
            byte[] bytes = disk.readSector(sectorIdx);
            while (true) {
                offset -= Layout.DIRECTORY_ENTRY_SIZE;
                if (offset <= 0) {
                    offset = Layout.SECTOR_SIZE / Layout.DIRECTORY_ENTRY_SIZE;
                    sectorIdx--;
                    bytes = disk.readSector(sectorIdx);
                }
                LfnEntity lfnEntity = new LfnEntity();
                lfnEntity.from(Arrays.copyOfRange(bytes, offset, offset + Layout.DIRECTORY_ENTRY_SIZE));
                this.writeDirEntry(new DirectoryEntity(), sectorIdx, offset);
                if (!lfnEntity.isLFN(lfnEntity.getAttributeByte())) {
                    throw new IllegalStateException("filesystem data is exception");
                }
                if (lfnEntity.isLastLfnEntity()) {
                    break;
                }
            }
        } else {
            this.writeDirEntry(new DirectoryEntity(), sectorIdx, offset);
        }
    }


    public void updateDirectory(UnixDirectory path) {
        this.writeDirEntry(path.getOriginal(), path.getSectorIdx(), path.getOffset());
    }

    private void writeDirEntry(DirectoryEntity directoryEntity, int sectorIdx, int offset) {
        writeEntry(directoryEntity.getBytes(), sectorIdx, offset);
    }

    private void writeEntry(byte[] bytes, int sectorIdx, int offset) {
        ByteBuffer buffer = ByteBuffer.wrap(this.disk.readSector(sectorIdx));
        buffer.position(offset);
        buffer.put(bytes, 0, Layout.DIRECTORY_ENTRY_SIZE);
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
