package com.example.app.service;

import com.example.app.config.AppProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class StorageUsageChecker {

    private final AppProperties props;

    public StorageUsageChecker(AppProperties props) {
        this.props = props;
    }

    public int getUsagePercent() {
        try {
            long used = Files.walk(Path.of(props.getAttachment().getBaseDir()))
                    .filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        try { return Files.size(p); }
                        catch (Exception e) { return 0L; }
                    })
                    .sum();

            long total = props.getStorage().getTotalBytes();
            if (total == 0) return 0;
            return (int) ((used * 100) / total);
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean isOverWarn() {
        return getUsagePercent() >= props.getStorage().getWarnThresholdPercent();
    }

    public boolean isOverDelete() {
        return getUsagePercent() >= props.getStorage().getDeleteThresholdPercent();
    }
}
