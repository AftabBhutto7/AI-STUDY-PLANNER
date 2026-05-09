import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class IconUtils {

    public static Icon get(String type, int size, Color color) {
        return new CustomIcon(type, size, color);
    }

    private static class CustomIcon implements Icon {
        private String type;
        private int size;
        private Color color;

        public CustomIcon(String type, int size, Color color) {
            this.type = type;
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.translate(x, y);

            float s = size;
            float stroke = Math.max(1.5f, s / 12f);
            g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            float pad = s * 0.2f;
            float w = s - 2 * pad;

            switch(type.toLowerCase()) {
                case "book":
                    g2.draw(new Rectangle2D.Float(pad, pad + w*0.1f, w*0.5f, w*0.8f));
                    g2.draw(new Rectangle2D.Float(pad + w*0.5f, pad + w*0.1f, w*0.5f, w*0.8f));
                    g2.draw(new Line2D.Float(pad + w*0.5f, pad + w*0.1f, pad + w*0.5f, pad + w*0.9f));
                    break;
                case "dashboard":
                    g2.draw(new Rectangle2D.Float(pad, pad, w*0.4f, w*0.4f));
                    g2.draw(new Rectangle2D.Float(pad + w*0.6f, pad, w*0.4f, w*0.6f));
                    g2.draw(new Rectangle2D.Float(pad, pad + w*0.6f, w*0.4f, w*0.4f));
                    g2.draw(new Rectangle2D.Float(pad + w*0.6f, pad + w*0.8f, w*0.4f, w*0.2f));
                    break;
                case "tasks":
                    g2.draw(new Rectangle2D.Float(pad, pad, w, w));
                    g2.draw(new Line2D.Float(pad + w*0.2f, pad + w*0.3f, pad + w*0.8f, pad + w*0.3f));
                    g2.draw(new Line2D.Float(pad + w*0.2f, pad + w*0.6f, pad + w*0.8f, pad + w*0.6f));
                    break;
                case "schedule":
                    g2.draw(new Rectangle2D.Float(pad, pad + w*0.2f, w, w*0.8f));
                    g2.draw(new Line2D.Float(pad + w*0.2f, pad, pad + w*0.2f, pad + w*0.4f));
                    g2.draw(new Line2D.Float(pad + w*0.8f, pad, pad + w*0.8f, pad + w*0.4f));
                    g2.draw(new Line2D.Float(pad, pad + w*0.5f, pad + w, pad + w*0.5f));
                    break;
                case "progress":
                    g2.draw(new Line2D.Float(pad, pad + w, pad + w, pad + w)); // x-axis
                    g2.draw(new Line2D.Float(pad, pad, pad, pad + w)); // y-axis
                    g2.draw(new Rectangle2D.Float(pad + w*0.2f, pad + w*0.5f, w*0.2f, w*0.5f));
                    g2.draw(new Rectangle2D.Float(pad + w*0.6f, pad + w*0.2f, w*0.2f, w*0.8f));
                    break;
                case "logout":
                    g2.draw(new Rectangle2D.Float(pad, pad, w*0.6f, w));
                    g2.draw(new Line2D.Float(pad + w*0.4f, pad + w*0.5f, pad + w, pad + w*0.5f));
                    g2.draw(new Line2D.Float(pad + w*0.8f, pad + w*0.3f, pad + w, pad + w*0.5f));
                    g2.draw(new Line2D.Float(pad + w*0.8f, pad + w*0.7f, pad + w, pad + w*0.5f));
                    break;
                case "add":
                    g2.draw(new Line2D.Float(pad + w*0.5f, pad, pad + w*0.5f, pad + w));
                    g2.draw(new Line2D.Float(pad, pad + w*0.5f, pad + w, pad + w*0.5f));
                    break;
                case "edit":
                    Path2D pencil = new Path2D.Float();
                    pencil.moveTo(pad, pad + w);
                    pencil.lineTo(pad + w*0.2f, pad + w);
                    pencil.lineTo(pad + w, pad + w*0.2f);
                    pencil.lineTo(pad + w*0.8f, pad);
                    pencil.lineTo(pad, pad + w*0.8f);
                    pencil.closePath();
                    g2.draw(pencil);
                    break;
                case "done":
                    Path2D check = new Path2D.Float();
                    check.moveTo(pad, pad + w*0.5f);
                    check.lineTo(pad + w*0.3f, pad + w*0.8f);
                    check.lineTo(pad + w, pad + w*0.2f);
                    g2.draw(check);
                    break;
                case "delete":
                    g2.draw(new Rectangle2D.Float(pad + w*0.2f, pad + w*0.3f, w*0.6f, w*0.7f));
                    g2.draw(new Line2D.Float(pad, pad + w*0.3f, pad + w, pad + w*0.3f));
                    g2.draw(new Line2D.Float(pad + w*0.4f, pad, pad + w*0.6f, pad));
                    break;
                default:
                    g2.draw(new Ellipse2D.Float(pad, pad, w, w));
            }
            g2.dispose();
        }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }
    }
}
