package org.uestc.weglas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.uestc.weglas.base.util.BaseResult;
import org.uestc.weglas.base.util.exception.AssertUtil;
import org.uestc.weglas.base.util.template.AbstractBizCallback;
import org.uestc.weglas.base.util.template.BizTemplate;
import org.uestc.weglas.biz.dto.ImportResultDTO;
import org.uestc.weglas.core.service.EquipmentImportService;

@RestController
public class ImportController {

    @Autowired
    private EquipmentImportService equipmentImportService;

    @PostMapping("/admin/import.json")
    public BaseResult<ImportResultDTO> importExcel(@RequestParam("file") MultipartFile file) {
        return BizTemplate.execute(new AbstractBizCallback<ImportResultDTO>() {
            @Override
            public void checkParameter() {
                AssertUtil.notNull(file, "请上传 Excel 文件");
                AssertUtil.isTrue(!file.isEmpty(), "文件不能为空");
            }

            @Override
            public void execute(BaseResult<ImportResultDTO> result) {
                result.setData(equipmentImportService.importFromExcel(file));
            }
        });
    }
}
