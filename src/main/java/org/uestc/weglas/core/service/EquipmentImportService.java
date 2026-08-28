package org.uestc.weglas.core.service;

import org.uestc.weglas.biz.dto.ImportResultDTO;
import org.springframework.web.multipart.MultipartFile;

public interface EquipmentImportService {
    ImportResultDTO importFromExcel(MultipartFile file);
}
