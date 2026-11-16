package com.example.app.service;

import com.example.app.config.AppProperties;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Component
public class LocalFileStorage {

    private final AppProperties props;
    private static final Logger appLog = LoggerFactory.getLogger("APP_LOG");

    public LocalFileStorage(AppProperties props) {
        this.props = props;
    }

    public String save(byte[] data, String storedFileName) {
        try {
            Path dir = Path.of(props.getAttachment().getBaseDir());
            Files.createDirectories(dir);
            Path file = dir.resolve(storedFileName);
            Files.write(file, data, StandardOpenOption.CREATE_NEW);
            return "/files/download/" + storedFileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file", e);
        }
    }

    public String saveThumbnail(byte[] data, String storedFileName) {
        try {
            Path dir = Path.of(props.getAttachment().getBaseDir());
            Files.createDirectories(dir);
            Path file = dir.resolve(storedFileName);
            Files.write(file, data, StandardOpenOption.CREATE_NEW);
            return "/thumbnails/" + storedFileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save thumbnail", e);
        }
    }

    public Resource get(String storedFileName) {
        try {
            Path file = Path.of(props.getAttachment().getBaseDir()).resolve(storedFileName);
            return new UrlResource(file.toUri());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file", e);
        }
    }

    public Resource getThumbnail(String storedFileName) {
        try {
            Path file = Path.of(props.getAttachment().getBaseDir()).resolve(storedFileName);
            return new UrlResource(file.toUri());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load thumbnail", e);
        }
    }


    public void delete(String storedFileName) {
        try {
            Path file = Path.of(props.getAttachment().getBaseDir()).resolve(storedFileName);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // ログのみ
        }
    }

    public void deleteThumbnail(String storedFileName) {
        try {
            Path file = Path.of(props.getAttachment().getBaseDir()).resolve(storedFileName);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // ログのみ
        }
    }
}
