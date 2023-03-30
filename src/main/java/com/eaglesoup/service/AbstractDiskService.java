package com.eaglesoup.service;

import com.eaglesoup.core.CoreFAT16XDiskService;
import com.eaglesoup.core.enums.CreateFileSectorTypeEnum;
import com.eaglesoup.core.model.BootSectorStruct;
import com.eaglesoup.core.model.DirectoryEntityStruct;
import com.eaglesoup.dto.CreateFileSectorIndexDto;
import com.eaglesoup.exception.BusinessException;
import com.eaglesoup.util.FATUtil;
import com.eaglesoup.util.FileUtil;
import com.eaglesoup.util.SizeUtil;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractDiskService {
    /**
     * 创建文件时获取可用的sector索引位置
     */
    @SneakyThrows
    protected CreateFileSectorIndexDto getStartSectorIndex(String fileName, int startClusterIndex) {
        Pair<Integer, Integer> pair = calSectorIndexByClusterIndex(startClusterIndex);
        int sectorCount = pair.getKey();
        int startSectorIndex = pair.getValue();
        //todo 常少帅 判断根条目最大个数
        for (int i = 0; i < sectorCount; i++) {
            byte[] buffer = CoreFAT16XDiskService.getInstance().readSector(startSectorIndex + i);
            for (int j = 0; j < buffer.length; j += DirectoryEntityStruct.ENTITY_SIZE) {
                if (buffer[j] == 0) {
                    return CreateFileSectorIndexDto.builder()
                            .type(CreateFileSectorTypeEnum.AVAILABLE_SECTOR.getCode())
                            .sectorIndex(startSectorIndex + i)
                            .build();
                }
                DirectoryEntityStruct entityStruct = DirectoryEntityStruct.byteToObj(Arrays.copyOfRange(buffer, j, j + DirectoryEntityStruct.ENTITY_SIZE));
                String storeFileName = new String(entityStruct.getFilename()).trim();
                if (!FileUtil.isDir(entityStruct.getAttribute())) {
                    String storeFileExtension = new String(entityStruct.getFilenameExtension()).trim();
                    storeFileName += "." + storeFileExtension;
                }
                if (fileName.equals(storeFileName)) {
                    //文件重名
                    return CreateFileSectorIndexDto.builder()
                            .type(CreateFileSectorTypeEnum.FILE_NAME_EXISTS.getCode())
                            .sectorIndex(startSectorIndex + i)
                            .subDirClusterIndex((int) SizeUtil.byteArrayToLong(entityStruct.getStartingCluster(), Short.BYTES))
                            .build();
                }
            }
        }
        //根目录条目已满
        return CreateFileSectorIndexDto.builder()
                .type(CreateFileSectorTypeEnum.DISK_FULL.getCode())
                .build();
    }

    /**
     * 尝试创建文件
     */
    @SneakyThrows
    protected int tryCreateFile(File file, boolean isFile) {
        //校验文件格式


        String[] fileNameArr = file.getPath().split(File.separator);
        int startCluster = 8; //rootDirectory对应的
        int sectorIndex = 0;
        int lastIndex = fileNameArr.length - 1;
        for (int i = 1; i < fileNameArr.length; i++) {
            //获取startSectorIndex
            CreateFileSectorIndexDto createFileSectorIndexDto = getStartSectorIndex(fileNameArr[i], startCluster);
            sectorIndex = createFileSectorIndexDto.getSectorIndex();
            //判断
            if (CreateFileSectorTypeEnum.AVAILABLE_SECTOR.getCode() == createFileSectorIndexDto.getType()) {
                if (i == lastIndex) {
                    createFile(fileNameArr[i], createFileSectorIndexDto.getSectorIndex(), isFile);
                } else {
                    throw new BusinessException(String.format("%s 目录不存在", fileNameArr[i]));
                }
            } else if (CreateFileSectorTypeEnum.FILE_NAME_EXISTS.getCode() == createFileSectorIndexDto.getType()) {
                if (i == lastIndex) {
                    if (!isFile) {
                        throw new BusinessException(String.format("%s 目录已存在", fileNameArr[i]));
                    }
                } else {
                    startCluster = createFileSectorIndexDto.getSubDirClusterIndex();
                }
            } else {
                throw new BusinessException("创建目录失败!");
            }
        }
        return sectorIndex;
    }

    /**
     * 创建1个文件: dir or file
     */
    private void createFile(String fileFullName, int sectorIndex, boolean isFile) {
        DirectoryEntityStruct entityStruct = buildDirectoryEntityStruct(fileFullName, isFile);
        //根目录条目信息写入
        CoreFAT16XDiskService coreFAT16XDiskService = CoreFAT16XDiskService.getInstance();
        byte[] buffer = coreFAT16XDiskService.readSector(sectorIndex);

        //todo 这里要修改对应的fat吗
        for (int i = 0; i < buffer.length; i += DirectoryEntityStruct.ENTITY_SIZE) {
            if (buffer[i] == 0) {
                System.arraycopy(entityStruct.toByteArray(), 0, buffer, i, DirectoryEntityStruct.ENTITY_SIZE);
                coreFAT16XDiskService.writeSector(sectorIndex, buffer);
                break;
            }
        }

        //根目录申请的1个cluster写入
        byte[] newClusterBytes = new byte[BootSectorStruct.getInstance().getPerSectorBytes() * BootSectorStruct.getInstance().getPerClusterSectors()];
        long fileSize = new File(FileUtil.getBaseFile()).length();
        long sectorCount = fileSize / BootSectorStruct.getInstance().getPerSectorBytes();

        //更新fat信息
        long clusterCount = sectorCount / BootSectorStruct.getInstance().getPerClusterSectors();
        byte[] fatVal = {0x00, 0x00};
        writeFat(fatVal, (int) clusterCount);

        //写新的cluster内容
        coreFAT16XDiskService.writeSector((int) sectorCount, newClusterBytes);
    }

    /**
     * 获取一个cluster下所有的目录or文件
     */
    protected List<DirectoryEntityStruct> getFileList(int clusterIndex) {
        Pair<Integer, Integer> pair = calSectorIndexByClusterIndex(clusterIndex);
        int sectorCount = pair.getKey();
        int startSectorIndex = pair.getValue();

        List<DirectoryEntityStruct> entityStructList = new ArrayList<>();
        for (int i = 0; i < sectorCount; i++) {
            byte[] buffer = CoreFAT16XDiskService.getInstance().readSector(startSectorIndex + i);
            //1个扇区多少个条目: 512 / 32 = 16
            for (int j = 0; j < buffer.length; j += DirectoryEntityStruct.ENTITY_SIZE) {
                if (buffer[j] == 0) {
                    return entityStructList;
                }
                DirectoryEntityStruct entityStruct = DirectoryEntityStruct.byteToObj(Arrays.copyOfRange(buffer, j, j + DirectoryEntityStruct.ENTITY_SIZE));
                entityStructList.add(entityStruct);
            }
        }
        return entityStructList;
    }

    /**
     * 查看文件内容
     *
     * @return
     */
    protected byte[] catFile(int sectorIndex, File file) {
        DirectoryEntityStruct entityStruct = trySetDirectoryEntity(sectorIndex, file, null);
        int fileSize = entityStruct.getFileSize();
        byte[] result = new byte[fileSize];
        int clusterIndex = (int) SizeUtil.byteArrayToLong(entityStruct.getStartingCluster(), Short.BYTES);
        readFileContent(result, clusterIndex, 0);
        return result;
    }

    private void readFileContent(byte[] result, int clusterIndex, int resultIndex) {
        Pair<Integer, Integer> pair = calSectorIndexByClusterIndex(clusterIndex);
        int sectorCount = pair.getKey();
        int startSectorIndex = pair.getValue();
        for (int i = 0; i < sectorCount; i++) {
            byte[] buffer = CoreFAT16XDiskService.getInstance().readSector(startSectorIndex + i);
            for (int j = 0; j < buffer.length; j++) {
                if (buffer[j] == 0) return;
                result[resultIndex] = buffer[j];
                resultIndex++;
            }
        }
        byte[] fatBytes = readFat(clusterIndex);
        boolean isNextClusterIndex = FileUtil.isNextCluster(fatBytes);
        if (isNextClusterIndex) {
            readFileContent(result, (int) SizeUtil.byteArrayToLong(fatBytes, 4), resultIndex);
        }
    }

    private Pair<Integer, Integer> calSectorIndexByClusterIndex(int startClusterIndex) {
        int sectorCount;
        int startSectorIndex;
        if (startClusterIndex == 8) {
            //Root Directory
            sectorCount = FileUtil.rootDirSectorSize();
            startSectorIndex = FileUtil.bootSectorSize() + FileUtil.fatSectorSize();
        } else {
            sectorCount = BootSectorStruct.getInstance().getPerClusterSectors();
            startSectorIndex = startClusterIndex * sectorCount;
        }
        return Pair.of(sectorCount, startSectorIndex);
    }

    @SneakyThrows
    private DirectoryEntityStruct buildDirectoryEntityStruct(String fileFullName, boolean isFile) {
        //创建时间
        long millis = Calendar.getInstance().getTimeInMillis() / 1000;
        //上次访问日期
        //todo 每次访问的时候记得修改
        long lastAccessTime = millis / 24 * 60 * 60;
        //获取 startCluster 值
        int clusterCount = FileUtil.getClusterCount();

        //构建 DirectoryEntityStruct
        DirectoryEntityStruct directoryEntityStruct = new DirectoryEntityStruct();
        directoryEntityStruct.setCreationTimestamp((int) millis);
        directoryEntityStruct.setLastAccessDateStamp((short) lastAccessTime);
        directoryEntityStruct.setLastWriteTimestamp((int) millis);
        directoryEntityStruct.setStartingCluster(SizeUtil.longToByteArray(clusterCount, Short.BYTES));

        Pair<byte[], byte[]> pair = calFileNameAndExtension(fileFullName, isFile);
        System.arraycopy(pair.getKey(), 0, directoryEntityStruct.getFilename(), 0x00, pair.getKey().length);
        System.arraycopy(pair.getValue(), 0, directoryEntityStruct.getFilenameExtension(), 0x00, pair.getValue().length);
        if (!isFile) {
            byte attr = 1 << 4;
            directoryEntityStruct.setAttribute(attr);
        }
        //设置扩展名
        return directoryEntityStruct;
    }

    @SneakyThrows
    private Pair<byte[], byte[]> calFileNameAndExtension(String fileFullName, boolean isFile) {
        //todo 特殊字符校验
        String fileName = fileFullName;
        //DirectoryEntityStruct.filenameExtension属性
        byte[] fileExtensionBytes = new byte[3];
        if (isFile) {
            //todo 边界测试一下
            int extensionIndex = fileFullName.lastIndexOf(".");
            fileExtensionBytes = fileFullName.substring(extensionIndex + 1).getBytes(StandardCharsets.UTF_8);
            fileName = fileFullName.substring(0, extensionIndex);
            if (fileExtensionBytes.length > 3) {
                throw new BusinessException(String.format("%s扩展名超过3个字节", new String(fileExtensionBytes)));
            }
        }
        //2.文件名称
        byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
        if (fileNameBytes.length > 8) {
            throw new BusinessException(fileName + "超过8个字符长度");
        }
        return Pair.of(fileNameBytes, fileExtensionBytes);
    }

    protected void tryWriteFileContent(int sectorIndex, byte[] contentBytes, File file, boolean isAppend) throws BusinessException {
        //获取fat
        DirectoryEntityStruct entityStruct = trySetDirectoryEntity(sectorIndex, file, null);
        if (entityStruct == null) {
            throw new BusinessException("文件不存在");
        }
        int fileSize = entityStruct.getFileSize();
        int clusterIndex = (int) SizeUtil.byteArrayToLong(entityStruct.getStartingCluster(), Short.BYTES);
        if (isAppend) {
            //追加写
            writeFileContentAppend(clusterIndex, contentBytes);
            fileSize += contentBytes.length;
        } else {
            writeFileContentOverwrite(clusterIndex, contentBytes);
            fileSize = contentBytes.length;
        }
        //更新文件大小
        int finalFileSize = fileSize;
        trySetDirectoryEntity(sectorIndex, file, (DirectoryEntityStruct struct) -> struct.setFileSize(finalFileSize));
    }

    private void writeFileContentAppend(int clusterIndex, byte[] content) {
        int perClusterSectors = BootSectorStruct.getInstance().getPerClusterSectors();
        int sectorIndex = clusterIndex * perClusterSectors;
        byte[] restContent = content;

        boolean isEnd = false;
        for (int i = 0; i < perClusterSectors; i++) {
            byte[] buffer = CoreFAT16XDiskService.getInstance().readSector(sectorIndex);
            int j = 0;
            for (; j < buffer.length; j++) {
                if (buffer[j] == 0) {
                    break;
                }
            }
            if (j == buffer.length - 1) {
                //当前扇区无可用空间
                continue;
            }
            if (buffer.length - j >= restContent.length) {
                System.arraycopy(restContent, 0, buffer, j, restContent.length);
                restContent = new byte[0];
                isEnd = true;
            } else {
                System.arraycopy(restContent, 0, buffer, j, buffer.length - j);
                restContent = Arrays.copyOfRange(restContent, buffer.length - j, buffer.length);
            }
            //写入
            CoreFAT16XDiskService.getInstance().writeSector(sectorIndex, buffer);
            if (isEnd) {
                break;
            }
        }
        handleWriteFileRestContent(clusterIndex, restContent, true);
    }

    private void writeFileContentOverwrite(int clusterIndex, byte[] content) {
        int perClusterSectors = BootSectorStruct.getInstance().getPerClusterSectors();
        int perSectorBytes = BootSectorStruct.getInstance().getPerSectorBytes();
        int sectorIndex = clusterIndex * perClusterSectors;
        byte[] restContent = content;
        for (int i = 0; i < perClusterSectors; i++) {
            if (restContent.length == 0) {
                break;
            }
            byte[] buffer = new byte[perSectorBytes];
            int contentLen;
            if (restContent.length > perSectorBytes) {
                contentLen = perClusterSectors;
            } else {
                contentLen = restContent.length;
            }
            System.arraycopy(restContent, 0, buffer, 0, contentLen);
            //新写入的数据
            CoreFAT16XDiskService.getInstance().writeSector(sectorIndex, buffer);
            restContent = Arrays.copyOfRange(restContent, contentLen, restContent.length);
        }
        handleWriteFileRestContent(clusterIndex, restContent, false);
    }

    private void handleWriteFileRestContent(int clusterIndex, byte[] restContent, boolean isAppend) {
        int perClusterSectors = BootSectorStruct.getInstance().getPerClusterSectors();
        int perSectorBytes = BootSectorStruct.getInstance().getPerSectorBytes();
        //磁盘空间优化：优先写原先的地方
        int originClusterIndex = clusterIndex;
        boolean isBigFile;
        if (restContent.length == 0) {
            do {
                byte[] nextClusterIndexBytes = readFat(clusterIndex);
                isBigFile = FATUtil.isBigFile(nextClusterIndexBytes);
                if (isBigFile) {
                    clusterIndex = (int) SizeUtil.byteArrayToLong(nextClusterIndexBytes, Short.BYTES);
                    writeFat(SizeUtil.longToByteArray(0x0000, Short.BYTES), clusterIndex);
                }
            } while (isBigFile);
            writeFat(SizeUtil.longToByteArray(0xFFF8, Short.BYTES), originClusterIndex);
        } else {
            //新申请 or 原先的空间
            byte[] nextClusterIndexBytes = readFat(clusterIndex);
            isBigFile = FATUtil.isBigFile(nextClusterIndexBytes);
            if (isBigFile) {
                clusterIndex = (int) SizeUtil.byteArrayToLong(nextClusterIndexBytes, Short.BYTES);
                if (isAppend) {
                    writeFileContentAppend(clusterIndex, restContent);
                } else {
                    writeFileContentOverwrite(clusterIndex, restContent);
                }
            } else {
                clusterIndex = FileUtil.getClusterCount();
                CoreFAT16XDiskService.getInstance().writeSector(clusterIndex * perClusterSectors, new byte[perSectorBytes]);
                writeFat(SizeUtil.longToByteArray(0x0000, Short.BYTES), clusterIndex);
            }
            writeFat(SizeUtil.longToByteArray(clusterIndex, Short.BYTES), originClusterIndex);
        }
    }

    @SneakyThrows
    private DirectoryEntityStruct trySetDirectoryEntity(int sectorIndex, File file, Consumer<DirectoryEntityStruct> consumer) {
        byte[] buffer = CoreFAT16XDiskService.getInstance().readSector(sectorIndex);
        DirectoryEntityStruct entityStruct;
        for (int j = 0; j < buffer.length; j += DirectoryEntityStruct.ENTITY_SIZE) {
            entityStruct = DirectoryEntityStruct.byteToObj(Arrays.copyOfRange(buffer, j, j + DirectoryEntityStruct.ENTITY_SIZE));
            String storeFileName = new String(entityStruct.getFilename()).trim();
            //如果是文件类型
            if (!FileUtil.isDir(entityStruct.getAttribute())) {
                String storeFileExtension = new String(entityStruct.getFilenameExtension()).trim();
                storeFileName += "." + storeFileExtension;
            }
            if (file.getName().equals(storeFileName)) {
                if (consumer != null) {
                    consumer.accept(entityStruct);
                    byte[] entityBytes = entityStruct.toByteArray();
                    System.arraycopy(entityBytes, 0, buffer, j, entityBytes.length);
                    CoreFAT16XDiskService.getInstance().writeSector(sectorIndex, buffer);
                }
                return entityStruct;
            }
        }
        throw new BusinessException("文件不存在");
    }

    /******************** fat部分操作 start *********************/
    private int calFatSectorIndex(int clusterIndex) {
        //1fat共有256个扇区，位置：从1-255个扇区;
        //1个cluster需要占用fat的2个字节
        double i = (clusterIndex + 1) * 2 / 512;
        //第0个扇区是保留区的空间
        int sectorIndex = (int) Math.ceil(i) + 1;
        return sectorIndex;
    }

    private void writeFat(byte[] val, int clusterIndex) {
        int sectorIndex = calFatSectorIndex(clusterIndex);
        byte[] buffer = CoreFAT16XDiskService.getInstance().readSector(sectorIndex);

        int i = (clusterIndex) * 2 % 512;
        buffer[i] = val[0];
        buffer[i + 1] = val[1];

        CoreFAT16XDiskService.getInstance().writeSector(sectorIndex, buffer);
    }

    private byte[] readFat(int clusterIndex) {
        byte[] result = new byte[2];
        int sectorIndex = calFatSectorIndex(clusterIndex);
        byte[] buffer = CoreFAT16XDiskService.getInstance().readSector(sectorIndex);
        int i = (clusterIndex) * 2 % 512;
        result[0] = buffer[i];
        result[1] = buffer[i + 1];
        return result;
    }

    /******************** fat部分操作 end *********************/

    public File buildFile(String fileFullName) {
        if (!fileFullName.startsWith(File.separator)) {
            fileFullName = FileUtil.getCurrentPath() + File.separator + fileFullName;
        }
        return new File(fileFullName);
    }

}
