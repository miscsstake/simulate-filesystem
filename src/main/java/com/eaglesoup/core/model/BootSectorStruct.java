package com.eaglesoup.core.model;

import com.eaglesoup.util.SizeUtil;
import lombok.Data;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Data
public class BootSectorStruct {
    private byte[] jumpCode = {(byte) 0xEB, (byte) 0x3C, (byte) 0x90};

    //格式化系统名称：默认为 mos-[4个字节以内的作者标识，不足补0]
    private byte[] oemName = new byte[8];

    //每个sector字节数
    private short perSectorBytes = 512;

    //每个 cluster 包含的 sector 数量，默认为 64
    private byte perClusterSectors = 64;

    //「保留区」使用几个 sector，因为保留扇区里总是保存着 Boot sector，默认为1
    private short reservedSectors = 1;

    //系统中使用的 FAT 副本有几份，默认为 2
    private byte fatCopiesCount = 2;

    //根目录下支持的条目数量，我们定为 63 个 sectors，换算为条目数量为 1008。
    // 这 63 个 sectors 和 boot sector 正好组成一个完整的 cluster，而 FAT 区域是几个完整 cluster，这样，整个非数据区域，正好占满若干个 clusters。
    private short rootEntitiesCount = 1008;

    private short sectorsSmallCount = 0;

    //分区所使用的存储介质是什么，我们用的磁盘，默认为值 0xF8
    private byte mediaDescriptor = (byte) 0xF8;

    //每个 FAT 副本包含的 sector 数量
    //每个 FAT 包含65536个簇状态描述，每个簇状态描述 2 bytes，而每个sector使用512 bytes，所以这里的默认值为:65536 * 2 / 512 = 256
    private short perFatSectorsCount = 256;

    //每个磁道上的扇区数量
    private short perTrackSectorsCount = 0;

    //有多少个磁头
    private short headsCount = 0;

    //如果当前分区前边还有分区，那么前面的分区占用的 sector 的数量
    private int hiddenSectorsCount = 0;

    //分区中保存的 sector 总数量，如果一个存储截止大小大于32Mb时有效，默认值 4194240
    private int sectorsLargeCount = 4194240;

    //驱动器编号
    private byte driveIndex = 0;

    //系统特定字段，我们不用，默认 0
    private byte reserved = 0;
    //系统特定字段，我们不用，默认 0
    private byte extendedBootSignature = 0;
    //系统特定字段，我们不用，默认 0
    private int volumeSerialNumber = 0;
    //系统特定字段，我们不用，默认 0
    private byte[] volumeLabel = new byte[11];

    //文件系统的名称，默认为 FAT16X
    private byte[] fileSystemType = new byte[8];

    //系统启动代码，默认全设为0
    private byte[] bootstrapCode = new byte[448];

    //签名字段
    private byte[] bootSectorSignature = {(byte) 0xAA, (byte) 0x55};

    //饿汉模式创建单例
    private static final BootSectorStruct instance = new BootSectorStruct();

    public static BootSectorStruct getInstance() {
        return instance;
    }

    private BootSectorStruct() {
    }

    public byte[] format() {
        byte[] result = new byte[perSectorBytes];
        //jumpCode属性
        System.arraycopy(jumpCode, 0, result, 0x0000, 3);
        //oemName 属性
        byte[] tmpOemName = "mos-".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(tmpOemName, 0, oemName, 0, tmpOemName.length);
        System.arraycopy(oemName, 0, result, 0x0003, 8);
        //perSectorBytes
        System.arraycopy(SizeUtil.longToByteArray(perSectorBytes, Short.BYTES), 0, result, 0x000B, Short.BYTES);
        //perClusterSectors
        System.arraycopy(SizeUtil.longToByteArray(perClusterSectors, Byte.BYTES), 0, result, 0x000D, Byte.BYTES);
        //reservedSectors
        System.arraycopy(SizeUtil.longToByteArray(reservedSectors, Short.BYTES), 0, result, 0x000E, Short.BYTES);
        //fatCopiesCount
        System.arraycopy(SizeUtil.longToByteArray(fatCopiesCount, Byte.BYTES), 0, result, 0x0010, Byte.BYTES);
        //rootEntitiesCount
        System.arraycopy(SizeUtil.longToByteArray(rootEntitiesCount, Short.BYTES), 0, result, 0x0011, Short.BYTES);
        //sectorsSmallCount
        System.arraycopy(SizeUtil.longToByteArray(sectorsSmallCount, Short.BYTES), 0, result, 0x0013, Short.BYTES);
        System.arraycopy(SizeUtil.longToByteArray(mediaDescriptor, Byte.BYTES), 0, result, 0x0015, Byte.BYTES);
        System.arraycopy(SizeUtil.longToByteArray(perFatSectorsCount, Short.BYTES), 0, result, 0x0016, Short.BYTES);
        System.arraycopy(SizeUtil.longToByteArray(perTrackSectorsCount, Short.BYTES), 0, result, 0x0018, Short.BYTES);
        System.arraycopy(SizeUtil.longToByteArray(headsCount, Short.BYTES), 0, result, 0x001A, Short.BYTES);
        System.arraycopy(SizeUtil.longToByteArray(hiddenSectorsCount, Integer.BYTES), 0, result, 0x001C, Integer.BYTES);
        System.arraycopy(SizeUtil.longToByteArray(sectorsLargeCount, Integer.BYTES), 0, result, 0x0020, Integer.BYTES);
        System.arraycopy(SizeUtil.longToByteArray(driveIndex, Byte.BYTES), 0, result, 0x0024, Byte.BYTES);
        System.arraycopy(SizeUtil.longToByteArray(reserved, Byte.BYTES), 0, result, 0x0025, Byte.BYTES);
        System.arraycopy(SizeUtil.longToByteArray(extendedBootSignature, Byte.BYTES), 0, result, 0x0026, Byte.BYTES);
        System.arraycopy(SizeUtil.longToByteArray(volumeSerialNumber, Integer.BYTES), 0, result, 0x0027, Integer.BYTES);
        //volumeLabel:new byte[11];
        System.arraycopy(volumeLabel, 0, result, 0x002B, 11);
        //fileSystemType
        byte[] tmpFileSystemType = "FAT16X".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(tmpFileSystemType, 0, fileSystemType, 0, tmpFileSystemType.length);
        //fileSystemType
        System.arraycopy(fileSystemType, 0, result, 0x0036, 8);
        //bootstrapCode
        System.arraycopy(bootstrapCode, 0, result, 0x003E, 448);
        //bootSectorSignature
        System.arraycopy(bootSectorSignature, 0, result, 0x01FE, 2);
        return result;
    }
}
