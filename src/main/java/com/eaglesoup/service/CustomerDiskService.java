package com.eaglesoup.service;

import com.eaglesoup.core.CoreFAT16XDiskService;
import com.eaglesoup.enums.CreateFileSectorTypeEnum;
import com.eaglesoup.core.model.BootSectorStruct;
import com.eaglesoup.core.model.DirectoryEntityStruct;
import com.eaglesoup.core.model.FAT16XStruct;
import com.eaglesoup.dto.CreateFileSectorIndexDto;
import com.eaglesoup.exception.BusinessException;
import com.eaglesoup.util.FileUtil;
import lombok.SneakyThrows;

import java.io.File;
import java.util.List;

public class CustomerDiskService extends AbstractDiskService {
    private static final CustomerDiskService instance = new CustomerDiskService();

    private CustomerDiskService() {
    }

    public static CustomerDiskService getInstance() {
        return instance;
    }

    public String format() {
        byte[] boot = BootSectorStruct.getInstance().format();
        //fat共占有8个簇，8*64 = 512个扇区
        byte[] fat = FAT16XStruct.getInstance().format();
        //directory共有63个扇区
        byte[] directory = DirectoryEntityStruct.format();

        //组装
        byte[] result = new byte[boot.length + fat.length + directory.length];
        System.arraycopy(boot, 0, result, 0, boot.length);
        System.arraycopy(fat, 0, result, boot.length, fat.length);
        System.arraycopy(directory, 0, result, boot.length + fat.length, directory.length);
        CoreFAT16XDiskService.getInstance().writeSector(0, result);

        //设置信息
        FileUtil.setCurrentPath("/");
        return "format success!";
    }

    /**
     * 创建目录
     */
    @SneakyThrows
    public void mkdir(String fileFullName) {
        tryCreateFile(buildFile(fileFullName), false);
    }

    @SneakyThrows
    public void touch(String fileFullName) {
        tryCreateFile(buildFile(fileFullName), true);
    }

    @SneakyThrows
    public List<DirectoryEntityStruct> ls(String fileFullName) {
        File file = buildFile(fileFullName);
        String[] fileNameArr = file.getPath().split(File.separator);
        int startCluster = 8; //rootDirectory对应的
        for (int i = 1; i < fileNameArr.length; i++) {
            //获取startSectorIndex
            CreateFileSectorIndexDto createFileSectorIndexDto = getStartSectorIndex(fileNameArr[i], startCluster);
            if (CreateFileSectorTypeEnum.FILE_NAME_EXISTS.getCode() != createFileSectorIndexDto.getType()) {
                throw new BusinessException("当前目录不存在");
            }
            startCluster = createFileSectorIndexDto.getSubDirClusterIndex();
        }
        return getFileList(startCluster);
    }

    public void cd(String pathName) throws BusinessException {
        if (pathName.startsWith("..")) {
            pathName = FileUtil.getCurrentPath() == "/" ? "/" : new File(FileUtil.getCurrentPath()).getParent();
        }
        File file = buildFile(pathName);
        String[] fileNameArr = file.getPath().split(File.separator);
        int startCluster = 8; //rootDirectory对应的
        String errMsg = "";
        for (int i = 1; i < fileNameArr.length; i++) {
            errMsg += File.separator + fileNameArr[i];
            //获取startSectorIndex
            CreateFileSectorIndexDto createFileSectorIndexDto = getStartSectorIndex(fileNameArr[i], startCluster);
            if (CreateFileSectorTypeEnum.FILE_NAME_EXISTS.getCode() == createFileSectorIndexDto.getType()) {
                startCluster = createFileSectorIndexDto.getSubDirClusterIndex();
            } else {
                throw new BusinessException("目录不存在:" + errMsg);
            }
        }
        FileUtil.setCurrentPath(file.getPath());
    }

    public String pwd() {
        return FileUtil.getCurrentPath();
    }

    public void echo(byte[] content, String fileFullName, boolean isAppend) throws BusinessException {
        File file = buildFile(fileFullName);
        int sectorIndex = tryCreateFile(file, true);
        tryWriteFileContent(sectorIndex, content, file, isAppend);
    }

    public byte[] cat(String fileFullName) throws BusinessException {
        File file = buildFile(fileFullName);
        String[] fileNameArr = file.getPath().split(File.separator);
        int startCluster = 8; //rootDirectory对应的
        int sectorIndex = 0;
        for (int i = 1; i < fileNameArr.length; i++) {
            //获取startSectorIndex
            CreateFileSectorIndexDto createFileSectorIndexDto = getStartSectorIndex(fileNameArr[i], startCluster);
            sectorIndex = createFileSectorIndexDto.getSectorIndex();
            if (CreateFileSectorTypeEnum.FILE_NAME_EXISTS.getCode() != createFileSectorIndexDto.getType()) {
                throw new BusinessException("文件不存在");
            } else {
                startCluster = createFileSectorIndexDto.getSubDirClusterIndex();
            }
        }
        return catFile(sectorIndex, file);
    }
}
