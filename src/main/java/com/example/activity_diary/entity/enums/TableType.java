package com.example.activity_diary.entity.enums;

import com.example.activity_diary.exception.types.BadRequestException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Locale;

@Getter
@RequiredArgsConstructor
public enum TableType {
    DAY_GOAL("day_goal"),
    DAY_TEMPLATE("day_template"),
    DIARY_ENTRY("diary_entry"),
    DIARY_ENTRY_GOAL("diary_entry_goal"),
    DIARY_ENTRY_TAG("diary_entry_tag"),
    DIARY_ENTRY_TEMPLATE("diary_entry_template"),
    ENTRY_METRIC("entry_metric"),
    ENTRY_METRIC_GOAL("entry_metric_goal"),
    ENTRY_METRIC_VALUE("entry_metric_value"),
    ENTRY_METRIC_VALUE_GOAL("entry_metric_value_goal"),
    ENTRY_TEMPLATE_METRIC("entry_template_metric"),
    ENTRY_TEMPLATE_METRIC_VALUE("entry_template_metric_value"),
    GENERAL_FOOD("general_food"),
    GLOBAL_SYNC_STATE("global_sync_state"),
    LOGIN_EVENT("login_event"),
    METRIC_NAME_UNIT_LINK("metric_name_unit_link"),
    METRIC_USAGE_AGG("metric_usage_agg"),
    REFRESH_TOKEN("refresh_token"),
    REGISTRATION_EVENT("registration_event"),
    TAG("tag"),
    TAG_CHART_TYPE_LINK("tag_chart_type_link"),
    TAG_USAGE_AGG("tag_usage_agg"),
    TEMPLATE_DAY_ITEM("template_day_item"),
    TEMPLATE_ENTRY_ITEM("template_entry_item"),
    USER_FOOD("user_food"),
    USER_SYNC_STATE("user_sync_state"),
    USER_TAG("user_tag"),
    VERIFICATION_CODES("verification_codes"),
    WEEK_GOAL("week_goal"),
    WEEK_TEMPLATE("week_template");

    private final String tableName;

    public static List<TableType> allValues() {
        return List.of(values());
    }

    @JsonValue
    public String getValue() {
        return tableName;
    }

    @JsonCreator
    public static TableType fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException("Table type is required");
        }

        String normalizedValue = value.trim().toLowerCase(Locale.ROOT);

        for (TableType tableType : values()) {
            if (tableType.tableName.equals(normalizedValue)) {
                return tableType;
            }
        }

        throw new BadRequestException("Unsupported table type: " + value);
    }
}
