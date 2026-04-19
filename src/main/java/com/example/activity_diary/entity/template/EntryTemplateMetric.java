package com.example.activity_diary.entity.template;

import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "entry_template_metric",
        indexes = {
                @Index(name = "idx_tpl_metric_tpl", columnList = "template_id"),
                @Index(name = "idx_tpl_metric_type", columnList = "metric_type_id")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class EntryTemplateMetric extends BaseEntity {

    /** Рљ РєР°РєРѕРјСѓ DiaryEntryTemplate РїСЂРёРЅР°РґР»РµР¶РёС‚ РјРµС‚СЂРёРєР°-С€Р°Р±Р»РѕРЅ */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    @JsonIgnore
    private DiaryEntryTemplate template;

    /** РўРёРї РјРµС‚СЂРёРєРё (С€Р°РіРё, РєР°Р»РѕСЂРёРё, РІРѕРґР° Рё С‚.Рї.) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metric_type_id", nullable = false)
    private DictionaryItem metricType;

    /**
     * Р—РЅР°С‡РµРЅРёСЏ РјРµС‚СЂРёРєРё РІ СЂР°Р·РЅС‹С… РµРґРёРЅРёС†Р°С… (unit).
     * РџСЂРёРјРµСЂ: metricType=Р’РѕРґР°, unit=РјР», value=2000
     */
    @OneToMany(
            mappedBy = "templateMetric",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<EntryTemplateMetricValue> values = new HashSet<>();

    /* ---------- FACTORY ---------- */

    public static EntryTemplateMetric create(
            DiaryEntryTemplate template,
            DictionaryItem metricType
    ) {
        if (template == null) throw new IllegalArgumentException("Template is required");
        if (metricType == null) throw new IllegalArgumentException("Metric type is required");

        EntryTemplateMetric metric = new EntryTemplateMetric();
        metric.metricType = metricType;
        metric.attachTo(template);
        return metric;
    }

    /* ---------- DOMAIN LOGIC ---------- */

    /**
     * Р”РѕР±Р°РІР»СЏРµС‚ unit->value РІРЅСѓС‚СЂСЊ РјРµС‚СЂРёРєРё-С€Р°Р±Р»РѕРЅР°.
     * Р’Р°Р¶РЅРѕ: unit СѓРЅРёРєР°Р»РµРЅ РІ СЂР°РјРєР°С… РѕРґРЅРѕР№ РјРµС‚СЂРёРєРё (РґРѕРї. constraint РЅР° С‚Р°Р±Р»РёС†Рµ values).
     */
    public void addValue(DictionaryItem unit, Integer value) {
        if (unit == null)
            throw new IllegalArgumentException("Unit is required");

        if (value == null || value <= 0)
            throw new IllegalArgumentException("Value must be positive");

        boolean exists = values.stream()
                .anyMatch(v -> v.getUnit().equals(unit));

        if (exists)
            throw new IllegalStateException("Unit already exists for this template metric");

        EntryTemplateMetricValue metricValue = EntryTemplateMetricValue.create(this, unit, value);
        values.add(metricValue);
    }

    public void removeValue(DictionaryItem unit) {
        if (unit == null) return;
        values.removeIf(v -> unit.equals(v.getUnit()));
    }

    public void clearValues() {
        values.clear();
    }

    public void changeMetricType(DictionaryItem newType) {
        if (newType == null)
            throw new IllegalArgumentException("Metric type is required");
        this.metricType = newType;
    }

    /** РџСЂРёРєСЂРµРїР»СЏРµС‚ РјРµС‚СЂРёРєСѓ Рє С€Р°Р±Р»РѕРЅСѓ (РёСЃРїРѕР»СЊР·СѓРµС‚СЃСЏ С„Р°Р±СЂРёРєРѕР№ Рё DiaryEntryTemplate.addMetric) */
    void attachTo(DiaryEntryTemplate template) {
        this.template = template;
    }

    /** РћС‚РІСЏР·С‹РІР°РµС‚ РјРµС‚СЂРёРєСѓ РѕС‚ С€Р°Р±Р»РѕРЅР° (РёСЃРїРѕР»СЊР·СѓРµС‚СЃСЏ DiaryEntryTemplate.removeMetric) */
    void detach() {
        this.template = null;
    }
}
