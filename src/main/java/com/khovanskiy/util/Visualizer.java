package com.khovanskiy.util;

import com.khovanskiy.model.*;
import com.khovanskiy.service.Repository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * victor
 */
@Slf4j
public class Visualizer {
    /**
     * Визуализация построенной модели.
     * Для рендера используется утилита Graphviz и следующая команда:
     * $ dot -Gsplines=true -Goverlap=false -Tpdf /tmp/model.dot -o /tmp/model.pdf
     *
     * @param filename
     */
    public static void visualize(Repository repository, String filename) {
        log.info("Визуализация модели...");
        try (PrintWriter writer = new PrintWriter(filename)) {
            writer.println("digraph Model {");
            writer.println("\trankdir = LR;");
            Map<Ref<? extends Point>, List<Stop>> points = new HashMap<>();
            List<TrainRun> runs = repository.findAll(TrainRun.class);
            runs.forEach(run -> {
                for (int i = 0; i < run.getWaypoints().size(); ++i) {
                    Waypoint waypoint = run.getWaypoints().get(i);
                    List<Stop> list = points.get(waypoint.getPoint());
                    if (list == null) {
                        list = new ArrayList<>();
                        points.put(waypoint.getPoint(), list);
                    }
                    list.add(new Stop(waypoint, i, run.getId()));
                }
            });
            int clusterId = 0;
            int runId = 0;
            Map<Ref<TrainRun>, Integer> runToId = new HashMap<>();
            //List<TrainRun.Id> refs = new ArrayList<>();
            //refs.add(new TrainRun.Id("TrainRun0"));
            //refs.add(new TrainRun.Id("TrainRun56"));
            //refs.add(new TrainRun.Id("TrainRun412"));
            List<TrainRun> selectedRuns = runs;//.stream().filter(trainRun -> refs.contains(trainRun.getId())).collect(Collectors.toList());
            Set<Waypoint> selectedWaypoints =  selectedRuns.stream().flatMap(trainRun -> trainRun.getWaypoints().stream()).collect(Collectors.toSet());
            Set<Ref<? extends Point>> selectedPoints = selectedRuns.stream().flatMap(trainRun -> trainRun.getWaypoints().stream()).map(w -> w.getPoint()).collect(Collectors.toSet());
            for (Map.Entry<Ref<? extends Point>, List<Stop>> entry : points.entrySet()) {
                if (!selectedPoints.contains(entry.getKey())) {
                    continue;
                }
                writer.println("\tsubgraph cluster_" + clusterId + " {");
                writer.println("\tlabel = \"" + entry.getKey() + "\";");
                writer.println("\t\tcolor=gray;");
                List<Stop> stops = new ArrayList<>(entry.getValue());
                Collections.sort(stops, (o1, o2) -> o1.getW().getDeparture().compareTo(o2.getW().getDeparture()));
                List<Stop> selectedStops = stops.stream().filter(stop -> selectedWaypoints.contains(stop.getW())).collect(Collectors.toList());
                for (Stop stop : selectedStops) {
                    Integer curRunId = runToId.get(stop.getRef());
                    if (curRunId == null) {
                        curRunId = runId;
                        ++runId;
                        runToId.put(stop.getRef(), curRunId);
                    }
                    writer.println("\t\tv_" + curRunId + "_" + stop.getNumber() + " [label=\"#" + stop.getNumber() + " " + stop.getW().getDeparture() + "\"];");
                }
                for (int i = 0; i < selectedStops.size(); ++i) {
                    Stop stop = selectedStops.get(i);
                    if (i == 0) {
                        writer.print("\t\t");
                    }
                    writer.print("v_" + runToId.get(stop.getRef()) + "_" + stop.getNumber());
                    if (i == selectedStops.size() - 1) {
                        writer.println(" [color=gray];");
                    } else {
                        writer.print(" -> ");
                    }
                }
                writer.println("\t}");
                ++clusterId;
            }
            selectedRuns.forEach(run -> {
                for (int i = 0; i < run.getWaypoints().size() - 1; ++i) {
                    writer.println("v_" + runToId.get(run.getId()) + "_" + i + " -> " + "v_" + runToId.get(run.getId()) + "_" + (i + 1) + " [label=\"" + run.getId() + "\", color=\"" + (colors[runToId.get(run.getId()) % colors.length]) + "\"];");
                }
            });
            writer.println("}");
        } catch (IOException e) {
            log.error("Ошибка при визуализации", e);
        }
    }

    private static final String[] colors = new String[]{"red", "darkorange", "blue", "green", "cyan3", "aquamarine3", "darkorchid", "navyblue"};

    @Data
    private static class Stop {
        final Waypoint w;
        final int number;
        final Ref<TrainRun> ref;
    }
}