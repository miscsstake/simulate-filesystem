package com.eaglesoup.fs;

import com.eaglesoup.enums.DirectoryEntityAttrTypeEnum;
import com.eaglesoup.fs.fat.layout.DirectoryEntity;
import lombok.Data;

@Data
public class UnixDirectory {
    UnixDirectory parent;
    /**
     * LfnEntity时永远为空
     */
    private String pathName;
    private int sectorIdx;
    private int offset;

    /**
     * @see com.eaglesoup.fs.fat.layout.LfnEntity
     */
    private DirectoryEntity original;

    public UnixDirectory(UnixDirectory parent, DirectoryEntity original, String pathName, int sectorIdx, int offset) {
        this.parent = parent;
        this.original = original;
        this.pathName = pathName;
        this.sectorIdx = sectorIdx;
        this.offset = offset;
    }

    public UnixDirectory(UnixDirectory parent, UnixDirectory sub) {
        this(parent, sub.getOriginal(), sub.getPathName(), sub.getSectorIdx(), sub.getOffset());
    }

    public byte getAttributeByte() {
        return original.getAttributeByte();
    }

    public String getPathName() {
        return this.pathName;
    }

    public boolean exist() {
        return this.original != null;
    }

    /**
     * 是否是目录
     */
    public boolean isDir() {
        return DirectoryEntityAttrTypeEnum.DIR == DirectoryEntityAttrTypeEnum.valueOf(getAttributeByte());
    }

    /**
     * 是否是文件
     */
    public boolean isFile() {
        return DirectoryEntityAttrTypeEnum.FILE == DirectoryEntityAttrTypeEnum.valueOf(getAttributeByte());
    }

    /**
     * 是否是长文件(long file name)
     */
    public boolean isFLN() {
        return DirectoryEntityAttrTypeEnum.LFN == DirectoryEntityAttrTypeEnum.valueOf(getAttributeByte());
    }

    public String getAbstractPath() {
        if (parent == null) {
            return "/";
        } else {
            return parent.getAbstractPath() + "/" + getPathName();
        }
    }
}
