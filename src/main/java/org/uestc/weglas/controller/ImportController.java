package org.uestc.weglas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.uestc.weglas.base.util.BaseResult;
import org.uestc.weglas.base.util.exception.AssertUtil;
import org.uestc.weglas.biz.dto.ImportResultDTO;
import org.uestc.weglas.controller.support.ControllerBizRunner;
import org.uestc.weglas.core.service.EquipmentImportService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
public class ImportController {

    @Autowired
    private EquipmentImportService equipmentImportService;

    @PostMapping("/admin/import.json")
    public BaseResult<ImportResultDTO> importExcel(@RequestParam("file") MultipartFile file) {
        final EquipmentImportService svc = equipmentImportService;
        return ControllerBizRunner.runWithCheck(
                () -> {
                    AssertUtil.notNull(file, "请上传 Excel 文件");
                    AssertUtil.isTrue(!file.isEmpty(), "文件不能为空");
                },
                result -> result.setData(svc.importFromExcel(file)));
    }

    @GetMapping("/admin/export.json")
    public void exportExcel(HttpServletResponse response) throws IOException {
        byte[] data = equipmentImportService.exportToExcel();
        String filename = "equipment-export-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.setContentLength(data.length);
        response.getOutputStream().write(data);
        response.flushBuffer();
    }
}
