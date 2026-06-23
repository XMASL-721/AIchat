package com.example.project_aichat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.project_aichat.dto.response.PageResponse;
import com.example.project_aichat.entity.DailyRecord;
import com.example.project_aichat.mapper.DailyRecordMapper;
import com.example.project_aichat.service.DailyRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DailyRecordServiceImpl implements DailyRecordService {

    private final DailyRecordMapper dailyRecordMapper;

    @Override
    public DailyRecord addRecord(Long userId, String recordType, Long targetId, String targetName, Integer calories) {
        DailyRecord record = new DailyRecord();
        record.setUserId(userId);
        record.setRecordDate(LocalDate.now());
        record.setRecordType(recordType);
        record.setTargetId(targetId);
        record.setTargetName(targetName);
        record.setCalories(calories != null ? calories : 0);
        dailyRecordMapper.insert(record);
        return record;
    }

    @Override
    public PageResponse<DailyRecord> getUserRecords(Long userId, LocalDate startDate, LocalDate endDate, String recordType, int page, int size) {
        Page<DailyRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<DailyRecord> wrapper = new LambdaQueryWrapper<DailyRecord>()
                .eq(DailyRecord::getUserId, userId)
                .ge(startDate != null, DailyRecord::getRecordDate, startDate)
                .le(endDate != null, DailyRecord::getRecordDate, endDate)
                .eq(recordType != null && !recordType.isEmpty(), DailyRecord::getRecordType, recordType)
                .orderByDesc(DailyRecord::getRecordDate)
                .orderByDesc(DailyRecord::getCreatedAt);
        Page<DailyRecord> result = dailyRecordMapper.selectPage(pageParam, wrapper);
        return PageResponse.of(result);
    }

    @Override
    public Map<String, Object> getTodaySummary(Long userId) {
        return getDateSummary(userId, LocalDate.now());
    }

    @Override
    public Map<String, Object> getDateSummary(Long userId, LocalDate date) {
        LambdaQueryWrapper<DailyRecord> wrapper = new LambdaQueryWrapper<DailyRecord>()
                .eq(DailyRecord::getUserId, userId)
                .eq(DailyRecord::getRecordDate, date);

        List<DailyRecord> records = dailyRecordMapper.selectList(wrapper);

        int mealCalories = records.stream()
                .filter(r -> "meal".equals(r.getRecordType()))
                .mapToInt(r -> r.getCalories() != null ? r.getCalories() : 0)
                .sum();

        int workoutCalories = records.stream()
                .filter(r -> "workout".equals(r.getRecordType()))
                .mapToInt(r -> r.getCalories() != null ? r.getCalories() : 0)
                .sum();

        long mealCount = records.stream()
                .filter(r -> "meal".equals(r.getRecordType()))
                .count();

        long workoutCount = records.stream()
                .filter(r -> "workout".equals(r.getRecordType()))
                .count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("date", date);
        summary.put("mealCalories", mealCalories);
        summary.put("workoutCalories", workoutCalories);
        summary.put("netCalories", mealCalories - workoutCalories);
        summary.put("mealCount", mealCount);
        summary.put("workoutCount", workoutCount);
        summary.put("totalRecords", records.size());

        return summary;
    }

    @Override
    public List<Map<String, Object>> getWeeklyStats(Long userId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6); // 最近7天

        LambdaQueryWrapper<DailyRecord> wrapper = new LambdaQueryWrapper<DailyRecord>()
                .eq(DailyRecord::getUserId, userId)
                .ge(DailyRecord::getRecordDate, startDate)
                .le(DailyRecord::getRecordDate, endDate);

        List<DailyRecord> records = dailyRecordMapper.selectList(wrapper);

        // 按日期分组统计
        Map<LocalDate, Map<String, Object>> dailyStats = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date);
            dayData.put("mealCalories", 0);
            dayData.put("workoutCalories", 0);
            dailyStats.put(date, dayData);
        }

        for (DailyRecord record : records) {
            Map<String, Object> dayData = dailyStats.get(record.getRecordDate());
            if (dayData != null) {
                if ("meal".equals(record.getRecordType())) {
                    dayData.put("mealCalories", (int) dayData.get("mealCalories") + (record.getCalories() != null ? record.getCalories() : 0));
                } else if ("workout".equals(record.getRecordType())) {
                    dayData.put("workoutCalories", (int) dayData.get("workoutCalories") + (record.getCalories() != null ? record.getCalories() : 0));
                }
            }
        }

        // 转换为列表并按日期排序
        List<Map<String, Object>> result = new ArrayList<>(dailyStats.values());
        result.sort((a, b) -> ((LocalDate) a.get("date")).compareTo((LocalDate) b.get("date")));

        return result;
    }

    @Override
    public List<Map<String, Object>> getCategoryStats(Long userId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<DailyRecord> wrapper = new LambdaQueryWrapper<DailyRecord>()
                .eq(DailyRecord::getUserId, userId)
                .eq(DailyRecord::getRecordType, "workout")
                .ge(startDate != null, DailyRecord::getRecordDate, startDate)
                .le(endDate != null, DailyRecord::getRecordDate, endDate);

        List<DailyRecord> records = dailyRecordMapper.selectList(wrapper);

        // 按训练名称分组统计卡路里
        Map<String, Integer> categoryMap = new HashMap<>();
        for (DailyRecord record : records) {
            String name = record.getTargetName() != null ? record.getTargetName() : "未知训练";
            categoryMap.put(name, categoryMap.getOrDefault(name, 0) + (record.getCalories() != null ? record.getCalories() : 0));
        }

        // 转换为列表
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : categoryMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            result.add(item);
        }

        // 按卡路里降序排序
        result.sort((a, b) -> (int) b.get("value") - (int) a.get("value"));

        return result;
    }
}
