package dk.easv.seaticketsystem.GUI.Util;

// Java Imports
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
//Other Imports
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

// Klasse til at sætte logo ind
public class LogoUtil {

    private static final String SVG_RESOURCE = "/dk/easv/seaticketsystem/images/sea-logo.svg";

    private static final double VIEWBOX_W = 300.0;
    private static final double VIEWBOX_H = 45.0;

    /**
     * @param targetWidth  desired display width in pixels
     * @param fill         colour to paint all paths (e.g. Color.WHITE)
     * @return             a Pane containing the logo, or a fallback text label
     */
    public static Pane create(double targetWidth, Color fill) {
        double scale = targetWidth / VIEWBOX_W;
        double targetHeight = VIEWBOX_H * scale;

        Pane pane = new Pane();
        pane.setPrefSize(targetWidth, targetHeight);
        pane.setMaxSize(targetWidth, targetHeight);
        pane.setMinSize(targetWidth, targetHeight);

        try (InputStream is = LogoUtil.class.getResourceAsStream(SVG_RESOURCE)) {
            if (is == null) return pane;

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);

            NodeList pathNodes = doc.getElementsByTagName("path");
            String fillHex = toHex(fill);

            for (int i = 0; i < pathNodes.getLength(); i++) {
                org.w3c.dom.Node node = pathNodes.item(i);
                if (node.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) continue;
                String d = ((org.w3c.dom.Element) node).getAttribute("d");
                if (d == null || d.isBlank()) continue;

                SVGPath svgPath = new SVGPath();
                svgPath.setContent(d);
                svgPath.setFill(Color.web(fillHex));
                svgPath.getTransforms().add(new Scale(scale, scale, 0, 0));
                pane.getChildren().add(svgPath);
            }
        } catch (Exception e)
        {
            // CSS Håndtere at den er tom.
        }
        return pane;
    }

    private static String toHex(Color c)
    {
        return String.format("#%02X%02X%02X",
                (int) (c.getRed()   * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue()  * 255));
    }
}
