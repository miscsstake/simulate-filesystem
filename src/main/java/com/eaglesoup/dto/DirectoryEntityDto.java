package com.eaglesoup.dto;

import com.eaglesoup.core.model.DirectoryEntityStruct;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DirectoryEntityDto {
    /**
     * 存在的文件(file or dir)信息
     */
    private DirectoryEntityStruct directoryEntityStruct;

    /**
     * 可用的扇区
     */
    private int availableSectorIndex;

    /**
     * 文件 or dir可用的"条目( directory entry structure)"索引
     */
    private int availableEntityIndex;

    /**
     * 可用扇区的buffer信息
     */
    private byte[] buffer;
}
