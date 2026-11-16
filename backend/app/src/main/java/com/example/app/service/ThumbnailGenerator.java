package com.example.app.service;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Optional;

@Component
public class ThumbnailGenerator {

    private static final int MAX_SIZE = 200;

    public Optional<byte[]> generate(byte[] originalBytes) {
        try {
            BufferedImage original =
                    ImageIO.read(new ByteArrayInputStream(originalBytes));
            if (original == null) {
                return Optional.empty();
            }

            int width = original.getWidth();
            int height = original.getHeight();

            float scale = Math.min(
                    (float) MAX_SIZE / width,
                    (float) MAX_SIZE / height
            );

            int newWidth = Math.round(width * scale);
            int newHeight = Math.round(height * scale);

            BufferedImage resized =
                    new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resized.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(original, 0, 0, newWidth, newHeight, null);
            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resized, "jpeg", baos);
            return Optional.of(baos.toByteArray());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
