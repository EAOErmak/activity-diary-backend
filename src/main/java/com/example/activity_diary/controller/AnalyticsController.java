//package com.example.activity_diary.controller;
//
//import com.example.activity_diary.dto.analytics.ChartResponseDto;
//import com.example.activity_diary.security.LightUserDetails;
//import com.example.activity_diary.service.analytics.AnalyticsService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Profile;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//
//@RestController
//@RequestMapping("/api/analytics")
//@RequiredArgsConstructor
//@PreAuthorize("hasRole('PREMIUM')")
//@Profile("analytics")
//public class AnalyticsController {
//
//    private final AnalyticsService analyticsService;
//
//    @GetMapping("/time/category/{categoryId}")
//    public ChartResponseDto getByTimeByCategory(
//            @AuthenticationPrincipal LightUserDetails ud,
//            @PathVariable Long categoryId,
//            @RequestParam Long unitId,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
//            LocalDateTime from,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
//            LocalDateTime to
//    ) {
//        return analyticsService.buildByTimeByCategory(
//                ud.getId(),
//                categoryId,
//                unitId,
//                from,
//                to
//        );
//    }
//
//    @GetMapping("/time/sub-category/{subCategoryId}")
//    public ChartResponseDto getByTimeBySubCategory(
//            @AuthenticationPrincipal LightUserDetails ud,
//            @PathVariable Long subCategoryId,
//            @RequestParam Long unitId,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
//            LocalDateTime from,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
//            LocalDateTime to
//    ) {
//        return analyticsService.buildByTimeBySubCategory(
//                ud.getId(),
//                subCategoryId,
//                unitId,
//                from,
//                to
//        );
//    }
//
//    @GetMapping("/sequence/category/{categoryId}")
//    public ChartResponseDto getBySequenceByCategory(
//            @AuthenticationPrincipal LightUserDetails ud,
//            @PathVariable Long categoryId,
//            @RequestParam Long unitId,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
//            LocalDateTime from,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
//            LocalDateTime to
//    ) {
//        return analyticsService.buildBySequenceByCategory(
//                ud.getId(),
//                categoryId,
//                unitId,
//                from,
//                to
//        );
//    }
//
//    @GetMapping("/sequence/sub-category/{subCategoryId}")
//    public ChartResponseDto getBySequenceBySubCategory(
//            @AuthenticationPrincipal LightUserDetails ud,
//            @PathVariable Long subCategoryId,
//            @RequestParam Long unitId,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
//            LocalDateTime from,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
//            LocalDateTime to
//    ) {
//        return analyticsService.buildBySequenceBySubCategory(
//                ud.getId(),
//                subCategoryId,
//                unitId,
//                from,
//                to
//        );
//    }
//}

