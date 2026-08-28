package org.uestc.weglas.core.service;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.uestc.weglas.base.dal.entity.EquipmentEntity;
import org.uestc.weglas.base.dal.entity.RoomEntity;
import org.uestc.weglas.base.dal.mapper.EquipmentMapper;
import org.uestc.weglas.base.dal.mapper.RoomMapper;
import org.uestc.weglas.biz.dto.ImportResultDTO;
import org.uestc.weglas.core.model.Room;
import org.uestc.weglas.core.util.IdGenerator;
import org.uestc.weglas.core.util.LocationParser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

@Service
public class EquipmentImportServiceImpl implements EquipmentImportService {

    private static final String SHEET_FULL = "完整表";

    private static final String[] EXPORT_HEADERS = {
            "编号", "名称", "品牌", "型号", "出厂号", "规格", "数量", "计量单位", "单价", "账面净值",
            "卡片状态", "现状", "安置地点", "所属部门", "楼宇名称", "保管人", "购置日期", "预计报废时间", "供货商", "生产厂家"
    };

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RoomService roomService;

    @Autowired
    private IdGenerator idGenerator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultDTO importFromExcel(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        int success = 0;
        int total = 0;
        Set<String> roomCodes = new HashSet<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = createWorkbook(file.getOriginalFilename(), inputStream)) {

            Sheet sheet = workbook.getSheet(SHEET_FULL);
            if (sheet == null) {
                sheet = workbook.getSheetAt(0);
            }

            Row headerRow = sheet.getRow(0);
            Map<String, Integer> headerIndex = buildHeaderIndex(headerRow);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) {
                    continue;
                }
                total++;
                try {
                    importRow(row, headerIndex, roomCodes);
                    success++;
                } catch (Exception e) {
                    String code = getCellString(row, headerIndex.get("编号"));
                    errors.add("第" + (i + 1) + "行" + (StringUtils.isNotBlank(code) ? ("(" + code + ")") : "") + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("读取 Excel 失败: " + e.getMessage(), e);
        }

        return ImportResultDTO.builder()
                .totalRows(total)
                .successCount(success)
                .failCount(total - success)
                .roomCount(roomCodes.size())
                .errors(errors.size() > 20 ? errors.subList(0, 20) : errors)
                .build();
    }

    @Override
    public byte[] exportToExcel() {
        List<EquipmentEntity> equipmentList = equipmentMapper.selectAll();
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(SHEET_FULL);
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                headerRow.createCell(i).setCellValue(EXPORT_HEADERS[i]);
            }

            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("yyyy-mm-dd"));

            int rowIndex = 1;
            for (EquipmentEntity entity : equipmentList) {
                writeExportRow(sheet.createRow(rowIndex++), entity, dateStyle);
            }

            for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出 Excel 失败: " + e.getMessage(), e);
        }
    }

    private void writeExportRow(Row row, EquipmentEntity entity, CellStyle dateStyle) {
        setCellString(row, 0, entity.getAssetCode());
        setCellString(row, 1, entity.getName());
        setCellString(row, 2, entity.getBrand());
        setCellString(row, 3, entity.getModel());
        setCellString(row, 4, entity.getSerialNo());
        setCellString(row, 5, entity.getSpec());
        setCellDecimal(row, 6, entity.getQuantity());
        setCellString(row, 7, entity.getUnit());
        setCellDecimal(row, 8, entity.getUnitPrice());
        setCellDecimal(row, 9, entity.getBookValue());
        setCellString(row, 10, entity.getCardStatus());
        setCellString(row, 11, entity.getUsageStatus());
        setCellString(row, 12, resolveLocationForExport(entity));
        setCellString(row, 13, entity.getDepartment());
        setCellString(row, 14, entity.getBuilding());
        setCellString(row, 15, entity.getCustodian());
        setCellDate(row, 16, entity.getPurchaseDate(), dateStyle);
        setCellDate(row, 17, entity.getScrapDate(), dateStyle);
        setCellString(row, 18, entity.getSupplier());
        setCellString(row, 19, entity.getManufacturer());
    }

    private String resolveLocationForExport(EquipmentEntity entity) {
        String location = StringUtils.trimToNull(entity.getLocationRaw());
        if (location == null && StringUtils.isNotBlank(entity.getRoomId())) {
            RoomEntity room = roomMapper.selectById(entity.getRoomId());
            if (room != null) {
                location = room.getRoomCode();
                if (StringUtils.isNotBlank(entity.getBuilding()) && location.matches("[AB]\\d{3}")) {
                    location = entity.getBuilding() + location;
                }
            }
        }
        String note = StringUtils.trimToNull(entity.getLocationNote());
        if (note != null && location != null && !location.contains(note)) {
            if (!note.startsWith("（") && !note.startsWith("(")) {
                note = "（" + note + "）";
            }
            location = location + note;
        } else if (note != null) {
            location = note;
        }
        return location;
    }

    private void setCellString(Row row, int columnIndex, String value) {
        if (value == null) {
            return;
        }
        row.createCell(columnIndex).setCellValue(value);
    }

    private void setCellDecimal(Row row, int columnIndex, BigDecimal value) {
        if (value == null) {
            return;
        }
        row.createCell(columnIndex).setCellValue(value.doubleValue());
    }

    private void setCellDate(Row row, int columnIndex, Date value, CellStyle dateStyle) {
        if (value == null) {
            return;
        }
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value);
        cell.setCellStyle(dateStyle);
    }

    private void importRow(Row row, Map<String, Integer> headerIndex, Set<String> roomCodes) {
        String assetCode = getCellString(row, headerIndex.get("编号"));
        if (StringUtils.isBlank(assetCode)) {
            throw new IllegalArgumentException("编号为空");
        }

        String locationRaw = getCellString(row, headerIndex.get("安置地点"));
        LocationParser.ParsedLocation parsed = LocationParser.parse(locationRaw);
        String building = getCellString(row, headerIndex.get("楼宇名称"));

        Room room = null;
        if (StringUtils.isNotBlank(parsed.getRoomCode())) {
            room = roomService.getOrCreateByRoomCode(parsed.getRoomCode(), building);
            roomCodes.add(parsed.getRoomCode());
        }

        Date now = new Date();
        EquipmentEntity existing = equipmentMapper.selectByAssetCode(assetCode.trim());
        EquipmentEntity entity = existing != null ? existing : new EquipmentEntity();

        if (existing == null) {
            entity.setId(idGenerator.generate(IdGenerator.EntityType.EQUIPMENT));
            entity.setCreatedAt(now);
            entity.setIsAbnormal(false);
        }

        entity.setAssetCode(assetCode.trim());
        entity.setName(getCellString(row, headerIndex.get("名称")));
        entity.setBrand(getCellString(row, headerIndex.get("品牌")));
        entity.setModel(getCellString(row, headerIndex.get("型号")));
        entity.setSerialNo(getCellString(row, headerIndex.get("出厂号")));
        entity.setSpec(getCellString(row, headerIndex.get("规格")));
        entity.setQuantity(parseDecimal(getCellString(row, headerIndex.get("数量"))));
        entity.setUnit(getCellString(row, headerIndex.get("计量单位")));
        entity.setUnitPrice(parseDecimal(getCellString(row, headerIndex.get("单价"))));
        entity.setBookValue(parseDecimal(getCellString(row, headerIndex.get("账面净值"))));
        entity.setCardStatus(getCellString(row, headerIndex.get("卡片状态")));
        entity.setUsageStatus(getCellString(row, headerIndex.get("现状")));
        entity.setRoomId(room != null ? room.getId() : null);
        entity.setLocationRaw(parsed.getRaw());
        entity.setLocationNote(parsed.getNote());
        entity.setDepartment(getCellString(row, headerIndex.get("所属部门")));
        entity.setBuilding(building);
        entity.setCustodian(getCellString(row, headerIndex.get("保管人")));
        entity.setPurchaseDate(parseDate(row, headerIndex.get("购置日期")));
        entity.setScrapDate(parseDate(row, headerIndex.get("预计报废时间")));
        entity.setSupplier(getCellString(row, headerIndex.get("供货商")));
        entity.setManufacturer(getCellString(row, headerIndex.get("生产厂家")));
        entity.setUpdatedAt(now);

        if (existing == null) {
            equipmentMapper.insert(entity);
        } else {
            equipmentMapper.updateById(entity);
        }
    }

    private Workbook createWorkbook(String filename, InputStream inputStream) throws Exception {
        if (filename != null && filename.toLowerCase().endsWith(".xlsx")) {
            return new XSSFWorkbook(inputStream);
        }
        return new HSSFWorkbook(inputStream);
    }

    private Map<String, Integer> buildHeaderIndex(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        if (headerRow == null) {
            return map;
        }
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String value = cell.getStringCellValue();
                if (StringUtils.isNotBlank(value)) {
                    map.put(value.trim(), i);
                }
            }
        }
        return map;
    }

    private boolean isEmptyRow(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && StringUtils.isNotBlank(getCellValue(cell))) {
                return false;
            }
        }
        return true;
    }

    private String getCellString(Row row, Integer index) {
        if (index == null || row == null) {
            return null;
        }
        Cell cell = row.getCell(index);
        return getCellValue(cell);
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return new java.text.SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
            }
            double val = cell.getNumericCellValue();
            if (val == Math.floor(val)) {
                return String.valueOf((long) val);
            }
            return String.valueOf(val);
        }
        if (cell.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        }
        if (cell.getCellType() == CellType.FORMULA) {
            try {
                return cell.getStringCellValue();
            } catch (Exception e) {
                return String.valueOf(cell.getNumericCellValue());
            }
        }
        cell.setCellType(CellType.STRING);
        String value = cell.getStringCellValue();
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private BigDecimal parseDecimal(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.replace(",", ""));
        } catch (Exception e) {
            return null;
        }
    }

    private Date parseDate(Row row, Integer index) {
        if (index == null || row == null) {
            return null;
        }
        Cell cell = row.getCell(index);
        if (cell == null) {
            return null;
        }
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue();
            }
            String text = getCellValue(cell);
            if (StringUtils.isBlank(text)) {
                return null;
            }
            return java.sql.Date.valueOf(text.substring(0, Math.min(10, text.length())));
        } catch (Exception e) {
            return null;
        }
    }
}
