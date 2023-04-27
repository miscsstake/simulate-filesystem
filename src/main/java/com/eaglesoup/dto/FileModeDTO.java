package com.eaglesoup.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileModeDTO {
    private String filename;
    private String fileMode;
}
