package jmr.video;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import javax.imageio.ImageIO;

/**
 * Class for reading and writing videos as {@link jmr.video.FrameCollection} 
 * objects.
 *
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
public class FrameCollectionIO {

    /**
     * Returns a <code>FrameCollection</code> as the result of reading the given
     * sequence of file images.
     *
     * If any of the files is a directory, the images inside are read
     * recursively. If any of the files is not an image, it is skipped. If the
     * sequence is empty, an empty collection is returned.
     *
     * @param files the sequence of files/directories to read from
     * @return a <code>FrameCollection</code> containing the decoded contents of
     * the input.
     */
    public static FrameCollection read(File... files) {
        FrameCollection fc = new FrameCollection();
        for (File f : files) {
            addItem(f, fc);
        }
        return fc;
    }

    /**
     * Returns a <code>FrameCollection</code> as the result of reading the given
     * collection of file images.
     * 
     * If any of the files is a directory, the images inside are read
     * recursively. If any of the files is not an image, it is skipped. If the
     * sequence is empty, an empty collection is returned.
     * 
     * @param files a collection of files/directories to read from
     * @return  a <code>FrameCollection</code> containing the decoded contents of
     * the input.
     */
    public static FrameCollection read(Collection<File> files) {
        return read((File[]) files.toArray());
    }

    /**
     * Add a frame to the given collection read from the given file.
     * 
     * @param file file of the image to be read.
     * @param fc frame collection where the image is added.
     */
    private static void addItem(File file, FrameCollection fc) {
        if (!file.isDirectory()) { //Stop condition
            try {  
                BufferedImage img = ImageIO.read(file);
                if(img!=null) fc.add(img);
            } catch (IOException ex) {
                System.err.println("Can't read input file ("+file+")");
            }
        } else {
            File[] fList = file.listFiles();
            for (File f : fList) {
                addItem(f, fc); //Recursive call
            }
        }
    }

}
