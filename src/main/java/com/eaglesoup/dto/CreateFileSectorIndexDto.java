package com.eaglesoup.dto;

import com.eaglesoup.enums.CreateFileSectorTypeEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateFileSectorIndexDto {
    /**
     * @see CreateFileSectorTypeEnum
     */
    private int type;
    private int sectorIndex;
    private int subDirClusterIndex;
}
