package com.eaglesoup.core;

import com.eaglesoup.exception.BusinessException;
import com.eaglesoup.util.FileUtil;

import java.io.*;
import java.util.Arrays;

public class CoreFAT16XDiskService implements IDisk {

    private static final CoreFAT16XDiskService instance = new CoreFAT16XDiskService();

    private CoreFAT16XDiskService() {
    }

    public static CoreFAT16XDiskService getInstance() {
        return instance;
    }

    @Override
    public byte[] readSector(int sectorIdx) {
        byte[] buf = new byte[sectorSize()];//缓冲区512字节
        try (
                FileInputStream fileInputStream = new FileInputStream(FileUtil.getBaseFile());
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)
        ) {

            bufferedInputStream.skip(sectorSize() * sectorIdx);
            //循环结束，不会再读了
            bufferedInputStream.read(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buf;
    }

    @Override
    public void writeSector(int sectorIdx, byte[] sectorData) {
        checkSectorData(sectorData.length);
        //第一个扇区不允许修改
        long fileSize = new File(FileUtil.getBaseFile()).length();
        if (sectorIdx == 0 || (long) sectorIdx * sectorSize() >= fileSize) {
            //直接写入内容
            for (int i = 0; i < sectorData.length / sectorSize(); i++) {
                byte[] partContent = Arrays.copyOfRange(sectorData, i * sectorSize(), (i + 1) * sectorSize());
                sectorIdx += i;
                directWrite(partContent, sectorIdx != 0);
            }
        } else {
            //修改内容
            byte[] fileContentBytes = replaceSectorContent(sectorIdx, sectorData);
            int sectorCount = fileContentBytes.length / sectorSize();
            for (int i = 0; i < sectorCount; i++) {
                byte[] partContent = Arrays.copyOfRange(fileContentBytes, i * sectorSize(), (i + 1) * sectorSize());
                directWrite(partContent, i != 0);
            }
        }
    }

    private void directWrite(byte[] sectorData, boolean append) {
        try (
                FileOutputStream fos = new FileOutputStream(FileUtil.getBaseFile(), append);
                BufferedOutputStream bos = new BufferedOutputStream(fos)
        ) {
            bos.write(sectorData);
            bos.flush();//强制将没有装满 bos 的写出一次
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkSectorData(int size) {
        try {
            if (size % sectorSize() != 0) {
                throw new BusinessException(String.format("修改内容的字节大小为:%s，不是 %s 的倍数", size, sectorSize()));
            }
        } catch (BusinessException e) {
            e.printStackTrace();
        }
    }

    private byte[] replaceSectorContent(int sectorIdx, byte[] sectorData) {
        long fileSize = new File(FileUtil.getBaseFile()).length();
        byte[] result = new byte[(int) fileSize];
        try (FileInputStream fis = new FileInputStream(FileUtil.getBaseFile());
             BufferedInputStream bis = new BufferedInputStream(fis)
        ) {
            byte[] buffer = new byte[sectorSize()];
            int i = 0;
            while (bis.read(buffer) != -1) {
                if (sectorIdx == i) {
                    System.arraycopy(sectorData, 0, result, i * sectorSize(), sectorSize());
                } else {
                    System.arraycopy(buffer, 0, result, i * sectorSize(), sectorSize());
                }
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


}
