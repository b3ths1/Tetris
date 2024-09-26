package pckTetris;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;

public class ImageLoader {

    public static BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(ImageLoader.class.getResource(path));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    public static Clip loadSound(String direction) {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(ImageLoader.class.getResource(direction)));
            return clip;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static FloatControl getVolumeControl(Clip clip) {
        return (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
    }
}
