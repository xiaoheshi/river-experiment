package com.river.experiment.core.chart;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Loads fonts that can correctly render Simplified Chinese characters across platforms.
 * <p>
 * Preferred order:
 * <ol>
 *     <li>Embedded Noto Sans SC font (bundled in resources).</li>
 *     <li>Available system fonts known to support Chinese.</li>
 *     <li>JRE default sans-serif font (fallback, may cause missing glyphs).</li>
 * </ol>
 */
final class ChartFontProvider {

    private static final float BASE_SIZE = 14f;
    private static final Font BASE_FONT = loadBaseFont();

    private ChartFontProvider() {
    }

    static Font titleFont() {
        return BASE_FONT.deriveFont(Font.BOLD, 20f);
    }

    static Font axisTitleFont() {
        return BASE_FONT.deriveFont(Font.BOLD, 16f);
    }

    static Font axisLabelFont() {
        return BASE_FONT.deriveFont(Font.PLAIN, 12f);
    }

    static Font legendFont() {
        return BASE_FONT.deriveFont(Font.PLAIN, 12f);
    }

    static Font annotationFont() {
        return BASE_FONT.deriveFont(Font.PLAIN, 11f);
    }

    static Font baseFont() {
        return BASE_FONT.deriveFont(Font.PLAIN, BASE_SIZE);
    }

    private static Font loadBaseFont() {
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();

        Font resourceFont = loadEmbeddedFont(environment);
        if (resourceFont != null) {
            return resourceFont.deriveFont(Font.PLAIN, BASE_SIZE);
        }

        Font systemFont = findSystemFont(environment);
        if (systemFont != null) {
            return systemFont;
        }

        return new Font(Font.SANS_SERIF, Font.PLAIN, Math.round(BASE_SIZE));
    }

    private static Font loadEmbeddedFont(GraphicsEnvironment environment) {
        try (InputStream stream = ChartFontProvider.class.getResourceAsStream("/fonts/NotoSansSC-Regular.otf")) {
            if (stream == null) {
                return null;
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, stream);
            environment.registerFont(font);
            return font;
        } catch (FontFormatException | IOException e) {
            return null;
        }
    }

    private static Font findSystemFont(GraphicsEnvironment environment) {
        var availableMap = Stream.of(environment.getAvailableFontFamilyNames(Locale.CHINA))
                .collect(Collectors.toMap(name -> name.toLowerCase(Locale.ROOT), name -> name, (a, b) -> a));

        List<String> candidates = List.of(
                "noto sans sc",
                "noto sans cjk sc",
                "pingfang sc",
                "microsoft yahei",
                "heiti sc",
                "wqy micro hei",
                "wenquanyi micro hei",
                "sarasa gothic sc",
                "simhei",
                "simsun"
        );

        for (String candidate : candidates) {
            String matched = availableMap.get(candidate);
            if (matched != null) {
                return new Font(matched, Font.PLAIN, Math.round(BASE_SIZE));
            }
        }
        return null;
    }
}
