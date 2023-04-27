package com.eaglesoup.service;

import com.eaglesoup.dto.FileModeDTO;

import java.io.File;

public class FileService {
    private CustomerDiskService customerDiskService;

    public FileService() {
        customerDiskService = CustomerDiskService.getInstance();
    }

    public boolean mkdir(String dirname) {
        return false;
    }

    public FileModeDTO open(String filename, String mode) {
        File file = new File("/tmp/a.txt");
        return null;
    }

    public boolean format() {
        customerDiskService.format();
        return true;
    }
}
