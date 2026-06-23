package com.example.project_aichat.controller;

import com.example.project_aichat.dto.response.ApiResponse;
import com.example.project_aichat.dto.response.PageResponse;
import com.example.project_aichat.entity.DailyRecord;
import com.example.project_aichat.service.DailyRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class DailyRecordController {

    private final DailyRecordService dailyRecordService;

    @PostMapping
    public ApiResponse<DailyRecord> addRecord(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        String recordType = (String) request.get("recordType");
        Long targetId = Long.parseLong(request.get("targetId").toString());
        String targetName = (String) request.get("targetName");
        Integer calories = request.get("calories") != null ? Integer.parseInt(request.get("calories").toString()) : null;

        DailyRecord record = dailyRecordService.addRecord(userId, recordType, targetId, targetName, calories);
        return ApiResponse.success(record);
    }

    @GetMapping
    public ApiResponse<PageResponse<DailyRecord>> getUserRecords(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String recordType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        PageResponse<DailyRecord> records = dailyRecordService.getUserRecords(userId, startDate, endDate, recordType, page, size);
        return ApiResponse.success(records);
    }

    @GetMapping("/today")
    public ApiResponse<Map<String, Object>> getTodaySummary(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        Map<String, Object> summary = dailyRecordService.getTodaySummary(userId);
        return ApiResponse.success(summary);
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> getDateSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        Map<String, Object> summary = dailyRecordService.getDateSummary(userId, date);
        return ApiResponse.success(summary);
    }

    @GetMapping("/weekly")
    public ApiResponse<List<Map<String, Object>>> getWeeklyStats(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        List<Map<String, Object>> stats = dailyRecordService.getWeeklyStats(userId);
        return ApiResponse.success(stats);
    }

    @GetMapping("/categories")
    public ApiResponse<List<Map<String, Object>>> getCategoryStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        List<Map<String, Object>> stats = dailyRecordService.getCategoryStats(userId, startDate, endDate);
        return ApiResponse.success(stats);
    }
}
