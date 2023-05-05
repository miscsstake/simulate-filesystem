package com.eaglesoup.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileModeDTO {
    /**
     * 文件名称
     */
    private String filename;
    /**
     * @see com.eaglesoup.enums.FileModeEnum
     * 文件模式
     */
    private String fileMode;
}
