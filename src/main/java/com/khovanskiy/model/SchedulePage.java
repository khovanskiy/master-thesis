package com.khovanskiy.model;

import com.khovanskiy.util.Timeline;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author victor
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SchedulePage extends Model<SchedulePage> {
    /**
     * Расписание прибытий
     */
    protected Timeline<StopLight> arrivalTimeline = new Timeline<>();
    /**
     * Расписание отправлений
     */
    protected Timeline<StopLight> departureTimeline = new Timeline<>();

    @Data
    @AllArgsConstructor
    public static final class Id implements Ref<SchedulePage> {
        String id1;
        long id2;
    }
}
