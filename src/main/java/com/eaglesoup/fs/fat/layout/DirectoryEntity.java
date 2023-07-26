package com.eaglesoup.fs.fat.layout;

import com.eaglesoup.fs.fat.Layout;
import com.eaglesoup.util.ParseUtils;
import lombok.Data;

import java.nio.ByteBuffer;

@Data
public class DirectoryEntity implements IEntity {
    private byte[] fileName = new byte[8];
    private byte[] filenameExtension = new byte[3];
    private byte attributeByte = 0b00000000;
    private byte reservedForWindowsNT = 0;
    private byte creation = 0;
    private int creationTimeStamp = 0;
    private short lastAccessDateStamp = 0;
    private short reservedForFAT32 = 0;
    private int lastWriteTimeStamp = 0;
    private short startingCluster = 0;
    private int fileSize = 0;

    public void from(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.get(fileName, 0, 8);
        buffer.get(filenameExtension, 0, 3);
        this.attributeByte = buffer.get();
        this.reservedForWindowsNT = buffer.get();
        this.creation = buffer.get();
        this.creationTimeStamp = buffer.getInt();
        this.lastAccessDateStamp = buffer.getShort();
        this.reservedForFAT32 = buffer.getShort();
        this.lastWriteTimeStamp = buffer.getInt();
        this.startingCluster = buffer.getShort();
        this.fileSize = buffer.getInt();
    }

    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(Layout.DIRECTORY_ENTRY_SIZE);
        buffer.put(fileName);
        buffer.put(filenameExtension);
        buffer.put(attributeByte);
        buffer.put(reservedForWindowsNT);
        buffer.put(creation);
        buffer.putInt(creationTimeStamp);
        buffer.putShort(lastAccessDateStamp);
        buffer.putShort(reservedForFAT32);
        buffer.putInt(lastWriteTimeStamp);
        buffer.putShort(startingCluster);
        buffer.putInt(fileSize);
        return buffer.array();
    }

    public LfnEntity transferToLfnEntity() {
        LfnEntity lfnEntity = new LfnEntity();
        lfnEntity.from(getBytes());
        return lfnEntity;
    }

    public boolean isLongFileName() {
        return !isEmptyDirEntity() && "".equals(ParseUtils.byte2Str(this.getFileName()));
    }

    public boolean isEmptyDirEntity() {
        return this.creationTimeStamp == 0;
    }
}
