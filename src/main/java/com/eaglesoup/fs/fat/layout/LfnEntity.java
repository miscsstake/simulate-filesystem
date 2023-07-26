package com.eaglesoup.fs.fat.layout;

import com.eaglesoup.fs.fat.Layout;
import lombok.Data;

import java.nio.ByteBuffer;

/**
 * 长文件类：
 * <a href="https://github.com/cooder-org/mos/blob/main/docs/vfat-long-file-names-spec.md#attr-byte">长文件结构文档</a>
 */
@Data
public class LfnEntity implements IEntity {
    //在LFN字符串中，当前的LFN Entry是第几个
    private byte originField;

    private byte[] part1 = new byte[10];

    /**
     * 当一个LFN Entry是最后一个时，Last LFN位（第6位）被置为1
     */
    private byte attributeByte = 0b00000000;

    private byte[] part2 = new byte[20];

    public void from(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        this.originField = buffer.get();
        buffer.get(part1, 0, 10);
        this.attributeByte = buffer.get();
        buffer.get(part2, 0, 20);
    }

    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(Layout.DIRECTORY_ENTRY_SIZE);
        buffer.put(this.originField);
        buffer.put(this.part1);
        buffer.put(this.attributeByte);
        buffer.put(this.part2);
        return buffer.array();
    }

    /**
     * 判断是Last LFN
     */
    public boolean isLastLfnEntity() {
        return (Layout.LFN_LAST_NUMBER & this.originField) != 0;
    }

    public int lfnNumber() {
        return this.originField & 0x3F;
    }
}
