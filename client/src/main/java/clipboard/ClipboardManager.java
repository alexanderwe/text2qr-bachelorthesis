package clipboard;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.input.Clipboard;

/**
 * Created by alexanderweiss
 * Class for representing a manager for the system clipboard
 */
public class ClipboardManager {

    private Clipboard clipboard;
    String clipboardContent;
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(ClipboardManager.class);

    /*
    *default constructor
    */
    public ClipboardManager(){
        this.clipboard = Clipboard.getSystemClipboard();
    }

    /**
     * Copy image to clipboard
     * @param image
     */
    public void putImageToClipboard(Image image){
        logger.info("New clipboard content set: " + clipboardContent);
        final ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putImage(image);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                clipboard.setContent(clipboardContent);
            }
        });
    }

    /**
     * Get String of clipboard
     *
     * @return any text found on the Clipboard; if none found, return an
     * empty String.
     */
    public String getClipboardContents() {
        logger.info("Get clipboard content");
        clipboardContent = null; // Prevent clipboard content to contain the last copied string
        Platform.runLater(() -> {
            if(clipboard.hasString()){
                clipboardContent = clipboard.getString();
            }
        });
        try {
            Thread.sleep(2000);
        } catch (InterruptedException iex) {
            logger.error("Error while reading clipboard content: " + "\n" + iex);
        }
        return clipboardContent.trim();
    }

    public Clipboard getClipboard() {
        return clipboard;
    }

    public void setClipboard(Clipboard clipboard) {
        this.clipboard = clipboard;
    }

}
