package de.cokechat.rendering;
import net.minecraft.client.gui.GuiGraphicsExtractor;
public final class RoundedRenderer {
    private static final int[][] INSETS = new int[33][];
    static { for (int r = 0; r <= 32; r++) { INSETS[r] = new int[r]; for (int y = 0; y < r; y++) { double d = r - y - .5; INSETS[r][y] = (int)Math.ceil(r - Math.sqrt(r * r - d * d) - .5); } } }
    public static int color(int rgb, double opacity) { return ((int)(Math.max(0, Math.min(1, opacity)) * 255) << 24) | (rgb & 0xFFFFFF); }
    public static void fill(GuiGraphicsExtractor g, int x, int y, int width, int height, int radius, int color) { band(g, x, y, width, height, y, y + height, radius, color); }
    /** Each row is drawn once, so transparent edges never overlap. */
    public static void band(GuiGraphicsExtractor g, int x, int y, int width, int height, int from, int to, int radius, int color) {
        if (width <= 0 || height <= 0 || (color >>> 24) == 0) return;
        int r = Math.max(0, Math.min(32, Math.min(radius, Math.min(width, height) / 2)));
        int start = Math.max(y, from), end = Math.min(y + height, to);
        for (int row = start; row < end;) {
            int distance = Math.min(row - y, y + height - 1 - row);
            int inset = distance < r ? INSETS[r][distance] : 0;
            int next = distance >= r ? Math.min(end, y + height - r) : row + 1;
            if (next <= row) next = row + 1;
            g.fill(x + inset, row, x + width - inset, next, color); row = next;
        }
    }
}
