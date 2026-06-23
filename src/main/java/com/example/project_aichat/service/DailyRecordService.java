package com.example.project_aichat.service;

import com.example.project_aichat.dto.response.PageResponse;
import com.example.project_aichat.entity.DailyRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DailyRecordService {
    DailyRecord addRecord(Long userId, String recordType, Long targetId, String targetName, Integer calories);
    PageResponse<DailyRecord> getUserRecords(Long userId, LocalDate startDate, LocalDate endDate, String recordType, int page, int size);
    Map<String, Object> getTodaySummary(Long userId);
    Map<String, Object> getDateSummary(Long userId, LocalDate date);
    List<Map<String, Object>> getWeeklyStats(Long userId);
    List<Map<String, Object>> getCategoryStats(Long userId, LocalDate startDate, LocalDate endDate);
}
