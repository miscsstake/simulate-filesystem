package com.eaglesoup.service;

import com.eaglesoup.core.FAT16XDisk2;
import com.eaglesoup.core.IDisk2;
import com.eaglesoup.core.model.BootSectorStruct;
import com.eaglesoup.core.model.DirectoryEntityStruct;
import com.eaglesoup.core.model.FAT16XStruct;
import com.eaglesoup.dto.DirectoryEntityDto;
import com.eaglesoup.dto.FileModeDTO;
import com.eaglesoup.enums.FileModeEnum;
import com.eaglesoup.exception.BusinessException;
import com.eaglesoup.fs.IFileSystem2;
import com.eaglesoup.util.FATUtil;
import com.eaglesoup.util.SizeUtil;
import lombok.SneakyThrows;

import java.util.*;

/**
 * 具体的fat16文件系统层
 */
public class FAT16FileSystem implements IFileSystem2 {
    private static final IDisk2 IDISK = FAT16XDisk2.getInstance();
    private static final BootSectorStruct bootSectorStruct = BootSectorStruct.getInstance();
    private FileModeDTO fileModeDTO;

    //设备层类：CoreFAT16XDiskService
    @Override
    public FileModeDTO open(String fileName, FileModeEnum fileModeEnum) {
        this.fileModeDTO = FileModeDTO.builder()
                .filename(fileName)
                .fileMode(fileModeEnum == null ? null : fileModeEnum.getMode())
                .build();
        return this.fileModeDTO;
    }

    @Override
    public void changeFileMode(FileModeEnum fileModeEnum) {
        open(this.fileModeDTO.getFilename(), fileModeEnum);
    }

    @Override
    public boolean exist() {
        if ("/".equals(fileModeDTO.getFilename())) {
            return true;
        }
        DirectoryEntityDto dirEntityDto = intactFileLocationInfo(fileModeDTO.getFilename());
        return dirEntityDto.getDirectoryEntityStruct() != null;
    }

    @SneakyThrows
    @Override
    public byte[] read() {
        String filename = fileModeDTO.getFilename();
        DirectoryEntityDto dirEntityDto = intactFileLocationInfo(filename);
        if (dirEntityDto.getDirectoryEntityStruct() == null) {
            //文件不存在
            throw new BusinessException("文件不存在");
        }
        DirectoryEntityStruct struct = dirEntityDto.getDirectoryEntityStruct();
        if (FATUtil.isDir(struct.getAttribute())) {
            throw new BusinessException("这是个目录");
        }
        byte[] result = new byte[struct.getFileSize()];
        int clusterIndex = (int) SizeUtil.byteArrayToLong(struct.getStartingCluster(), Short.BYTES);
        readFileContent(clusterIndex, result, 0, struct.getFileSize());
        return result;
    }

    @SneakyThrows
    @Override
    public boolean write(byte[] bytes) {
        String filename = fileModeDTO.getFilename();
        String lastFilename = filename.substring(filename.lastIndexOf("/") + 1);
        DirectoryEntityDto dirEntityDto = intactFileLocationInfo(filename);
        int fileClusterIndex;
        int oldFileSize = 0;
        boolean isAppend = FileModeEnum.APPEND_MODE.getMode().equals(fileModeDTO.getFileMode());
        if (dirEntityDto.getDirectoryEntityStruct() == null) {
            //文件不存在
            fileClusterIndex = createFile(lastFilename, dirEntityDto, false, bytes.length);
        } else {
            DirectoryEntityStruct dirEntityStruct = dirEntityDto.getDirectoryEntityStruct();
            fileClusterIndex = (int) SizeUtil.byteArrayToLong(dirEntityStruct.getStartingCluster(), Short.BYTES);
            oldFileSize = dirEntityStruct.getFileSize();
            //修改文件大小
            long millis = Calendar.getInstance().getTimeInMillis() / 1000;
            dirEntityStruct.setFileSize(isAppend ? oldFileSize + bytes.length : bytes.length);
            dirEntityStruct.setLastWriteTimestamp((int) millis);
            System.arraycopy(dirEntityStruct.toByteArray(), 0, dirEntityDto.getBuffer(), dirEntityDto.getAvailableEntityIndex(), DirectoryEntityStruct.ENTITY_SIZE);
            writeBigSize(0, dirEntityDto.getAvailableSectorIndex(), dirEntityDto.getBuffer());
        }
        //追加模式
        if (FileModeEnum.APPEND_MODE.getMode().equals(fileModeDTO.getFileMode())) {
            //写入下一个cluster
            writeFileContent(fileClusterIndex, oldFileSize, bytes);
        } else if (FileModeEnum.WRITE_MODE.getMode().equals(fileModeDTO.getFileMode())) {
            //清空fat和"文件内容"
            resetFileContent(fileClusterIndex, oldFileSize);
            writeFileContent(fileClusterIndex, 0, bytes);
        }
        return true;
    }

    @SneakyThrows
    @Override
    public boolean mkdir() {
        String filename = fileModeDTO.getFilename();
        DirectoryEntityDto dirEntityDto = intactFileLocationInfo(filename);
        if (dirEntityDto.getDirectoryEntityStruct() == null) {
            //创建目录
            filename = filename.substring(filename.lastIndexOf("/") + 1);
            createFile(filename, dirEntityDto, true, 0);
        }
        return true;
    }

    @SneakyThrows
    @Override
    public List<DirectoryEntityStruct> listFiles() {
        List<DirectoryEntityStruct> result = new ArrayList<>();
        int startSectorIndex;
        int sectorCount;
        if ("/".equals(fileModeDTO.getFilename())) {
            startSectorIndex = FATUtil.rootDirectorySectorIndex();
            sectorCount = FATUtil.rootDirectorySize() / bootSectorStruct.getPerSectorBytes();
        } else {
            DirectoryEntityDto dirEntityDto = intactFileLocationInfo(fileModeDTO.getFilename());
            if (dirEntityDto.getDirectoryEntityStruct() == null) {
                return result;
            }
            byte[] clusterBytes = dirEntityDto.getDirectoryEntityStruct().getStartingCluster();
            sectorCount = bootSectorStruct.getPerClusterSectors();
            startSectorIndex = (int) SizeUtil.byteArrayToLong(clusterBytes, Short.BYTES) * sectorCount;
        }
        for (int i = 0; i < sectorCount; i++) {
            byte[] buffer = IDISK.readSector(startSectorIndex + i);
            for (int j = 0; j < buffer.length; j += DirectoryEntityStruct.ENTITY_SIZE) {
                if (buffer[j] != 0) {
                    DirectoryEntityStruct entityStruct = DirectoryEntityStruct.byteToObj(Arrays.copyOfRange(buffer, j, j + DirectoryEntityStruct.ENTITY_SIZE));
                    result.add(entityStruct);
                }
            }
        }
        return result;
    }


    @Override
    public void close() {
    }

    @SneakyThrows
    @Override
    public boolean rm() {
        String filename = fileModeDTO.getFilename();
        if ("/".equals(filename)) {
            return true;
        }
        DirectoryEntityDto dirEntityDto = intactFileLocationInfo(filename);
        DirectoryEntityStruct struct = dirEntityDto.getDirectoryEntityStruct();
        if (struct == null) {
            throw new BusinessException("文件不存在");
        }
        int clusterIndex = (int) SizeUtil.byteArrayToLong(struct.getStartingCluster(), Short.BYTES);
        if (!FATUtil.isDir(struct.getAttribute())) {
            //文件部分
            //文件目录项情况
            resetFileDirEntity(dirEntityDto);
            //fat清空&文件清空
            resetFileContent(clusterIndex, struct.getFileSize());
        } else {
            //目录部分
            if (!listFiles().isEmpty()) {
                throw new BusinessException("目录不为空,禁止删除!");
            }
            //文件目录项情况
            resetFileDirEntity(dirEntityDto);
            //fat清空
            resetFat(clusterIndex);
        }
        return true;
    }

    @SneakyThrows
    @Override
    public void format() {
        byte[] boot = bootSectorStruct.format();
        //fat共占有8个簇，8*64 = 512个扇区
        byte[] fat = FAT16XStruct.getInstance().format();
        //directory共有63个扇区
        byte[] directory = DirectoryEntityStruct.format();

        //创建2g大小的文件
        writeBigSize(IDISK.diskSize(), 0, null);
        writeBigSize(0, 0, boot);
        writeBigSize(0, FATUtil.fatSectorIndex(), fat);
        writeBigSize(0, FATUtil.rootDirSectorIndex(), directory);
    }

    @SneakyThrows
    public DirectoryEntityDto intactFileLocationInfo(String filename) {
        // "/tmp/aa" -> ["", "/tmp", "aa"]
        String[] fileNameArr = filename.split("/");
        int startSectorIndex = FATUtil.rootDirectorySectorIndex();
        int sectorCount = FATUtil.rootDirectorySize() / bootSectorStruct.getPerSectorBytes();
        for (int i = 0; i < fileNameArr.length; i++) {
            if (fileNameArr[i].isEmpty()) {
                continue;
            }
            DirectoryEntityDto dirEntityDto = fileLocationInfo(startSectorIndex, sectorCount, fileNameArr[i]);
            //最后一个
            if (i == fileNameArr.length - 1) {
                return dirEntityDto;
            } else {
                if (dirEntityDto.getDirectoryEntityStruct() == null) {
                    throw new BusinessException(BusinessException.FILE_NO_EXIST, "目录不存在");
                } else {
                    sectorCount = bootSectorStruct.getPerClusterSectors();
                    long startClusterIndex = SizeUtil.byteArrayToLong(dirEntityDto.getDirectoryEntityStruct().getStartingCluster(), Short.BYTES);
                    startSectorIndex = (int) startClusterIndex * sectorCount;
                }
            }
        }
        return null;
    }

    @SneakyThrows
    private DirectoryEntityDto fileLocationInfo(int startSectorIndex, int sectorCount, String fileName) {
        DirectoryEntityDto directoryEntityDto = DirectoryEntityDto.builder().build();
        for (int i = 0; i < sectorCount; i++) {
            byte[] buffer = IDISK.readSector(startSectorIndex + i);
            for (int j = 0; j < buffer.length; j += DirectoryEntityStruct.ENTITY_SIZE) {
                if (buffer[j] == 0) {
                    if (directoryEntityDto.getAvailableSectorIndex() == 0) {
                        directoryEntityDto.setBuffer(buffer);
                        directoryEntityDto.setAvailableSectorIndex(startSectorIndex + i);
                        directoryEntityDto.setAvailableEntityIndex(j);
                    }
                } else {
                    DirectoryEntityStruct entityStruct = DirectoryEntityStruct.byteToObj(Arrays.copyOfRange(buffer, j, j + DirectoryEntityStruct.ENTITY_SIZE));
                    String storeFilename = new String(entityStruct.getFilename()).trim();
                    if (fileName.equals(storeFilename)) {
                        directoryEntityDto.setDirectoryEntityStruct(entityStruct);
                        directoryEntityDto.setBuffer(buffer);
                        directoryEntityDto.setAvailableSectorIndex(startSectorIndex + i);
                        directoryEntityDto.setAvailableEntityIndex(j);
                        return directoryEntityDto;
                    }
                }
            }
        }
        return directoryEntityDto;
    }


    /**
     * 创建文件 or 目录
     */
    @SneakyThrows
    private int createFile(String filename, DirectoryEntityDto dirEntityDto, boolean isDir, int fileSize) {
        int newClusterIndex = createNextClusterIndex();
        //修改"文件目录项"
        byte[] buffer = dirEntityDto.getBuffer();
        DirectoryEntityStruct createDirEntity = FATUtil.buildDirectoryEntityStruct(filename, isDir, newClusterIndex, fileSize);
        System.arraycopy(createDirEntity.toByteArray(), 0, buffer, dirEntityDto.getAvailableEntityIndex(), DirectoryEntityStruct.ENTITY_SIZE);
        writeBigSize(0, dirEntityDto.getAvailableSectorIndex(), buffer);
        //修改fat
        writeFat(newClusterIndex, FATUtil.initFatClusterStatus());
        return newClusterIndex;
    }

    @SneakyThrows
    private int createNextClusterIndex() {
        int fatSectorIndex = FATUtil.fatSectorIndex();
        int fatSectorCount = FATUtil.fatSize() / bootSectorStruct.getPerSectorBytes();
        int clusterIndex = 0;
        for (int i = 0; i < fatSectorCount; i++) {
            byte[] buffer = IDISK.readSector(fatSectorIndex + i);
            for (int j = 0; j < buffer.length; j += FAT16XStruct.PER_FAT_SIZE) {
                if (buffer[j] == 0) {
                    return clusterIndex;
                }
                clusterIndex++;
            }
        }
        return clusterIndex;
    }


    @SneakyThrows
    private void writeFat(int clusterIndex, byte[] clusterStatusBytes) {
        int fatSectorIndex = FATUtil.fatSectorIndex() + (clusterIndex * 2) / bootSectorStruct.getPerSectorBytes();
        byte[] buffer = IDISK.readSector(fatSectorIndex);
        System.arraycopy(clusterStatusBytes, 0, buffer, clusterIndex * 2, clusterStatusBytes.length);
        writeBigSize(0, fatSectorIndex, buffer);
    }

    @SneakyThrows
    private short readFat(int clusterIndex) {
        int fatSectorIndex = FATUtil.fatSectorIndex() + (clusterIndex * 2) / bootSectorStruct.getPerSectorBytes();
        byte[] buffer = IDISK.readSector(fatSectorIndex);
        byte[] cluster = Arrays.copyOfRange(buffer, clusterIndex * 2, (clusterIndex + 1) * 2);
        return (short) SizeUtil.byteArrayToLong(cluster, Short.BYTES);
    }

    @SneakyThrows
    private void writeBigSize(long size, int sectorIndex, byte[] buffer) {
        if (buffer != null) {
            IDISK.writeSector(sectorIndex, buffer);
            return;
        }
        int clusterBytesSize = bootSectorStruct.getPerClusterSectors() * bootSectorStruct.getPerSectorBytes();
        while (size > 0) {
            //每次写入一个cluster
            long writeSize = size < clusterBytesSize ? size : clusterBytesSize;
            IDISK.writeSector(sectorIndex, new byte[(int) writeSize]);

            size -= clusterBytesSize;
            sectorIndex += bootSectorStruct.getPerClusterSectors();
        }
    }

    /**
     * 追加写入文件内容
     */
    /**
     * @param clusterIndex: 文件起始
     * @param oldFileSize:  原文件大小
     * @param bytes:        新写入的文件大小
     */
    @SneakyThrows
    private void writeFileContent(int clusterIndex, int oldFileSize, byte[] bytes) {
        if (bytes.length == 0) return;

        while (oldFileSize > FATUtil.clusterSize()) {
            oldFileSize -= FATUtil.clusterSize();
            clusterIndex = readFat(clusterIndex);
        }
        //写入当前cluster
        for (int i = 0; i < bootSectorStruct.getPerClusterSectors(); i++) {
            if (oldFileSize >= bootSectorStruct.getPerSectorBytes()) {
                oldFileSize -= bootSectorStruct.getPerSectorBytes();
                continue;
            }
            //写入当前sector
            int sectorIndex = clusterIndex * bootSectorStruct.getPerClusterSectors() + i;
            int usableSpace = bootSectorStruct.getPerSectorBytes() - oldFileSize;
            int copyLength = Math.min(bytes.length, usableSpace);

            byte[] content = Arrays.copyOfRange(bytes, 0, copyLength);
            byte[] buffer = IDISK.readSector(sectorIndex);
            System.arraycopy(content, 0, buffer, oldFileSize, copyLength);
            writeBigSize(0, sectorIndex, buffer);

            //修改oldFileSize大小
            bytes = Arrays.copyOfRange(bytes, copyLength, bytes.length);
            if (bytes.length == 0) {
                return;
            }
            oldFileSize = 0;
        }

        int nextClusterIndex = createNextClusterIndex();
        writeFat(clusterIndex, SizeUtil.longToByteArray(nextClusterIndex, Short.BYTES));
        writeFat(nextClusterIndex, FATUtil.initFatClusterStatus());
        writeFileContent(nextClusterIndex, 0, bytes);
    }

    /**
     * 清空文件：
     * 1.文件内容清空;
     * 2.fat[cluster]清空
     */
    @SneakyThrows
    public void resetFileContent(int clusterIndex, int oldFileSize) {
        int nextClusterIndex = readFat(clusterIndex);
        resetFat(clusterIndex);
        for (int i = 0; i < bootSectorStruct.getPerClusterSectors(); i++) {
            if (oldFileSize <= 0) {
                return;
            }
            int sectorIndex = clusterIndex * bootSectorStruct.getPerClusterSectors() + i;
            writeBigSize(0, sectorIndex, new byte[bootSectorStruct.getPerSectorBytes()]);
            oldFileSize -= bootSectorStruct.getPerSectorBytes();
        }
        resetFileContent(nextClusterIndex, oldFileSize);
    }

    /**
     * 清空fat
     */
    public void resetFat(int clusterIndex) {
        writeFat(clusterIndex, FATUtil.resetFatClusterStatus());
    }

    /**
     * 清空文件目录项
     */
    public void resetFileDirEntity(DirectoryEntityDto dirEntityDto) {
        byte[] buffer = dirEntityDto.getBuffer();
        int structSize = DirectoryEntityStruct.ENTITY_SIZE;
        System.arraycopy(new byte[structSize], 0, buffer, dirEntityDto.getAvailableEntityIndex(), structSize);
        writeBigSize(0, dirEntityDto.getAvailableSectorIndex(), buffer);
    }

    @SneakyThrows
    private void readFileContent(int clusterIndex, byte[] result, int start, int end) {
        int sectorCount = bootSectorStruct.getPerClusterSectors();
        for (int i = 0; i < sectorCount; i++) {
            if (end <= 0) return;
            int sectorIndex = clusterIndex * sectorCount;
            byte[] buffer = IDISK.readSector(sectorIndex + i);
            int len = Math.min(buffer.length, end);
            byte[] copyContent = Arrays.copyOfRange(buffer, 0, len);
            System.arraycopy(copyContent, 0, result, start, len);

            start += len;
            end -= len;
        }
        if (end > 0) {
            int nextClusterIndex = readFat(clusterIndex);
            readFileContent(nextClusterIndex, result, start, end);
        }
    }
}
