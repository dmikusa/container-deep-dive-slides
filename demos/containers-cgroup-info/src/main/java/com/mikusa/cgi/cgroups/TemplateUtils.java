package com.mikusa.cgi.cgroups;

import java.util.List;

public class TemplateUtils {
    private TemplateUtils() {}

    public static String cgroupResultToListString(List<CGroupV2InfoService.CgroupResult> cgroupResult) {
        if (cgroupResult == null || cgroupResult.isEmpty()) {
            return "[]";
        }

        return String.join(
                ", ",
                cgroupResult.stream()
                        .map(c -> (c.max()) ? "max" : c.value().get().toString())
                        .toList());
    }

    public static String cgroupResultToListBytes(List<CGroupV2InfoService.CgroupResult> cgroupResult) {
        if (cgroupResult == null || cgroupResult.isEmpty()) {
            return "[]";
        }

        return String.join(
                ", ",
                cgroupResult.stream()
                        .map(c -> {
                            if (c.max()) {
                                return "max";
                            }
                            try {
                                long bytes = Long.parseLong(c.value().get().toString());
                                double megabytes = bytes / 1048576.0;
                                return String.format("%.2f MB (%d bytes)", megabytes, bytes);
                            } catch (NumberFormatException e) {
                                return c.value().get().toString();
                            }
                        })
                        .toList());
    }
}
