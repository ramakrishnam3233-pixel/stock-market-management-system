package com.stockmarket.analytics.util;

import com.stockmarket.analytics.dto.AnalyticsDtos.ChartSeriesPoint;
import com.stockmarket.analytics.dto.AnalyticsDtos.PerformanceChartResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Component
public class ChartRendererUtility {

    private static final Logger log = LoggerFactory.getLogger(ChartRendererUtility.class);

    public byte[] renderPngChart(PerformanceChartResponse chart, int width, int height) {
        log.info("Rendering PNG chart for {}:{} ({} points)", chart.exchangeCode(), chart.symbol(), chart.series().size());
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        g.setColor(new Color(30, 41, 59));
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.drawString(chart.symbol() + " (" + chart.exchangeCode() + ") - " + chart.chartType()
                + " | Change: " + chart.percentageChange() + "%", 24, 32);

        List<ChartSeriesPoint> pts = chart.series();
        if (pts.size() >= 2) {
            double min = pts.stream().mapToDouble(p -> p.price().doubleValue()).min().orElse(0.0);
            double max = pts.stream().mapToDouble(p -> p.price().doubleValue()).max().orElse(1.0);
            double span = Math.max(max - min, 0.0001);

            int padX = 50;
            int padTop = 60;
            int padBottom = 40;
            int plotW = width - 2 * padX;
            int plotH = height - padTop - padBottom;

            g.setColor(new Color(203, 213, 225));
            g.drawRect(padX, padTop, plotW, plotH);

            g.setColor(new Color(37, 99, 235));
            g.setStroke(new BasicStroke(2.5f));
            for (int i = 0; i < pts.size() - 1; i++) {
                int x1 = padX + (int) ((i / (double) (pts.size() - 1)) * plotW);
                int y1 = padTop + plotH - (int) (((pts.get(i).price().doubleValue() - min) / span) * plotH);
                int x2 = padX + (int) (((i + 1) / (double) (pts.size() - 1)) * plotW);
                int y2 = padTop + plotH - (int) (((pts.get(i + 1).price().doubleValue() - min) / span) * plotH);
                g.drawLine(x1, y1, x2, y2);
            }
        }

        g.dispose();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(img, "PNG", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to render PNG chart", e);
        }
    }
}
