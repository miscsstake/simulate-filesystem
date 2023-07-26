package com.eaglesoup.fs.fat.layout;

import com.eaglesoup.fs.fat.Layout;
import lombok.Data;

import java.nio.ByteBuffer;

@Data
public class ReservedRegion {
    private byte[] jmpCode = new byte[]{(byte) 0xEB, 0x3C, (byte) 0x90};
    private byte[] oemName = new byte[]{'m', 'o', 's', '-', 'z', 'f', 'y', 0};
    private short sectorSize = Layout.SECTOR_SIZE;
    private byte sectorsPerCluster = Layout.SECTORS_PER_CLUSTER;
    private short reservedSectors = Layout.RESERVED_SECTORS;
    private byte numberOfFatCopies = Layout.NUMBER_OF_FAT_COPIES;
    private short numberOfPossibleRootEntries = Layout.NUMBER_OF_ROOT_ENTRIES_COUNT;
    private short smallNumberOfSectors = 0;
    private byte mediaDescriptor = (byte) 0xF8;
    private short sectorsPerFat = Layout.SECTORS_PER_FAT;
    private short sectorsPerTrack = 0;
    private short numberOfHeads = 0;
    private int hiddenSectors = 0;
    private int largeNumberOfSectors = 4194240;
    private byte driveNumber = 0;
    private byte reserved = 0;
    private byte extendedBootSignature = 0;
    private int volumeSerialNumber = 0;
    private byte[] volumeLabe = new byte[11];
    private byte[] fileSystemType = new byte[]{'F', 'A', 'T', '1', '6', 'X', 0, 0};
    private byte[] bootstrapCode = new byte[448];
    private byte[] bootSectorSignature = new byte[]{(byte) 0xAA, (byte) 0x55};

    public void from(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.get(this.jmpCode, 0, 3);
        buffer.get(this.oemName, 0, 8);
        this.sectorSize = buffer.getShort();
        this.sectorsPerCluster = buffer.get();
        this.reservedSectors = buffer.getShort();
        this.numberOfFatCopies = buffer.get();
        this.numberOfPossibleRootEntries = buffer.getShort();
        this.smallNumberOfSectors = buffer.getShort();
        this.mediaDescriptor = buffer.get();
        this.sectorsPerFat = buffer.getShort();
        this.sectorsPerTrack = buffer.getShort();
        this.numberOfHeads = buffer.getShort();
        this.hiddenSectors = buffer.getInt();
        this.largeNumberOfSectors = buffer.getInt();
        this.driveNumber = buffer.get();
        this.reserved = buffer.get();
        this.extendedBootSignature = buffer.get();
        this.volumeSerialNumber = buffer.getInt();
        buffer.get(volumeLabe, 0, 11);
        buffer.get(fileSystemType, 0, 8);
        buffer.get(bootstrapCode, 0, 448);
        buffer.get(bootSectorSignature, 0, 2);
    }

    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(Layout.SECTOR_SIZE);
        buffer.put(jmpCode);
        buffer.put(oemName);
        buffer.putShort(sectorSize);
        buffer.put(sectorsPerCluster);
        buffer.putShort(reservedSectors);
        buffer.put(numberOfFatCopies);
        buffer.putShort(numberOfPossibleRootEntries);
        buffer.putShort(smallNumberOfSectors);
        buffer.put(mediaDescriptor);
        buffer.putShort(sectorsPerFat);
        buffer.putShort(sectorsPerTrack);
        buffer.putShort(numberOfHeads);
        buffer.putInt(hiddenSectors);
        buffer.putInt(largeNumberOfSectors);
        buffer.put(driveNumber);
        buffer.put(reserved);
        buffer.put(extendedBootSignature);
        buffer.putInt(volumeSerialNumber);
        buffer.put(volumeLabe);
        buffer.put(fileSystemType);
        buffer.put(bootstrapCode);
        buffer.put(bootSectorSignature);
        buffer.rewind();
        byte[] bytes = new byte[Layout.SECTOR_SIZE];
        buffer.get(bytes, 0, buffer.capacity());
        return bytes;
    }
}
