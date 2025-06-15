package com.pe.fisi.sw.cooperApp.files.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface FileUploader {
    String uploadFiles(List<File> files, String cuentaUid) throws IOException;
}
