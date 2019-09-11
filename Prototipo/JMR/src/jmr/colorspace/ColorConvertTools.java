/**
 *
 */
package jmr.colorspace;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import jmr.descriptor.ColorData;
import jmr.media.JMRBufferedImage;
import jmr.media.JMRExtendedBufferedImage;


/**
 * This class implement the method for color conversion using JMRImage and
 * {@link ColorSpaceJMR} classes.
 *
 *
 *
 *
 * @author  SoTiLLo
 * @version 1.0
 */
public class ColorConvertTools {


	/** This tools is designed to replace ColorConvertOp that process too many operation
	 * when the ColorSpace is not a {@link ICC_ColorSpace}.
	 *
	 * <p>
	 * The ColorConvertOp contains a strange behaviour near:
	 * <code>
	 *	color = srcCM.getNormalizedComponents(spixel, color, 0); //From spixel obtain RGB in [0,1]
	 *	tmpColor = srcColorSpace.toCIEXYZ(color); //Transform from RGB to CIEXYZ
	 *       tmpColor = dstColorSpace.fromCIEXYZ(tmpColor); //Transform CIEXYZ to CS (strange why loop)
	 * 	 tmpColor = dstColorSpace.toCIEXYZ(tmpColor); //Transform CS to CIEXYZ
	 * 	tmpColor = dstColorSpace.fromCIEXYZ(tmpColor); //Transform CIEXYZ to CS
	 * </code></p>
	 *
	 * @param 	src 	The source image with type {@link BufferedImage#TYPE_INT_ARGB} or {@link BufferedImage#TYPE_INT_RGB}
	 * @param 	dstCs 	A color Space instance of {@link ColorSpaceJMR}.
	 * @return			An ImageJMR object 
	 */
	public static JMRExtendedBufferedImage colorConvertOp(BufferedImage src, ColorSpace dstCs) {


          //Create destination following the colorSpace.
          JMRExtendedBufferedImage dst = null;
          
          if(dstCs.getType() == src.getColorModel().getColorSpace().getType())
        	  return new JMRExtendedBufferedImage(src);
          
          if (dstCs.getType() == ColorSpaceJMR.CS_HMMD) {
            dst = JMRExtendedBufferedImage.getInstance(src.getWidth(), src.getHeight(),
                                                       JMRExtendedBufferedImage.
                                                       TYPE_JMR_4F_INTERLEAVED, dstCs);
          }
          else {
            dst = JMRExtendedBufferedImage.getInstance(src.getWidth(), src.getHeight(),
                                                       JMRExtendedBufferedImage.
                                                       TYPE_JMR_3F_INTERLEAVED, dstCs);
          }

          WritableRaster dstMx = dst.getRaster();

          //Check that source is not Gray nor Custom
          if (src.getType() != BufferedImage.TYPE_CUSTOM) {
            //ColorSpace dstCs = dst.getColorModel().getColorSpace();
            int RGB;
            float[] p_in = new float[3];
            float[] p_out = new float[dstMx.getNumBands()];
            //float[] p_prueba = new float[3];
            for (int x = 0; x < src.getWidth(); x++) {
              for (int y = 0; y < src.getHeight(); y++) {
                RGB = src.getRGB(x, y);
                p_in[0] = (float) ( (RGB >> 16) & 0xFF) / 255.0f;
                p_in[1] = (float) ( (RGB >> 8) & 0xFF) / 255.0f;
                p_in[2] = (float) (RGB & 0xFF) / 255.0f;
                p_out = dstCs.fromRGB(p_in);
                dstMx.setPixel(x, y, p_out);
                //p_prueba = dstCs.toRGB(p_out);
              }
            }
          }

          /*PARA PROBAR CONVERSIONES*/
          /*BufferedImage image = new BufferedImage(src.getWidth(), src.getHeight(),
                                                  BufferedImage.TYPE_INT_RGB);

          WritableRaster raster = image.getRaster();

          float[] p_in = new float[dstCs.getNumComponents()];
          float[] p_out = new float[raster.getNumBands()];
          for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
              //RGB = src.getRGB(x, y);
              dstMx.getPixel(x,y,p_in);
              //p_in[0] = (float) ( (RGB >> 16) & 0xFF) / 255.0f;
              //p_in[1] = (float) ( (RGB >> 8) & 0xFF) / 255.0f;
              //p_in[2] = (float) (RGB & 0xFF) / 255.0f;
              p_out = dstCs.toRGB(p_in);
              p_out[0]*=255.0f;
              p_out[1]*=255.0f;
              p_out[2]*=255.0f;
              raster.setPixel(x, y, p_out);

              for(int i=0; i<p_in.length;i++)
                p_in[i] = ColorConvertTools.domainTransform(p_in[i], dstCs.getMinValue(i), dstCs.getMaxValue(i), 0.0f,1.0f);
              Color col = new Color(dstCs,p_in,1.0f);
              float c1Components[] = null;
              c1Components = col.getColorComponents(null);
              c1Components.toString();
            }
          }

          image.setData(raster);

          File file = new File(".//transformation"+ColorSpaceJMR.getColorSpaceName(dstCs.getType())+".png");
          try {
            ImageIO.write(image, "png", file);
          }
          catch (Exception e) {
            System.out.println(e.getMessage());
          }*/

          return dst;
        }

        /**
         * A domain transform function. Transform a value in [a,b] domain to [c,d] domain
         *
         * <p>The transform applied is:
         *    ((x-a)/(b-a))*(d-c) + c   where [a,b], [c,d] and a &lt; b ; c &lt; d
         * </p>
         *
         * @param x value belongs to [a,b] interval
         * @param a min value of [a,b]
         * @param b max value of [a,b]
         * @param c min value of [c,d]
         * @param d max value of [c,d]
         * @return transformed value
         */

        public static float domainTransform(float x, float a, float b, float c, float d){
          return ((((x-a)/(b-a))*(d-c)) + c);
        }
        
        /**
         * 
         * @param x An array
         * @param c min value of [c,d]
         * @param d max value of [c,d]
         * @return A transformed Domain array
         */
        public static float[] domainTransform(float[] x, ColorSpace cs, float c, float d ){
        	float pixelQ[] = new float[x.length];
        	for (int i = 0; i < pixelQ.length; i++)
				pixelQ[i] = domainTransform(x[i], cs.getMinValue(i), cs.getMaxValue(i), c, d);
            return pixelQ;
          }

        public static float[] domainTransform(float[] x, ColorSpace cs1, ColorSpace cs2 ){
        	float pixelQ[] = new float[x.length];
        	float c,d;
        	for (int i = 0; i < pixelQ.length; i++){
        		c = cs1.getMinValue(i);
        		d= cs1.getMaxValue(i);
				pixelQ[i] = domainTransform(pixelQ[i], c, d, cs2.getMinValue(i), cs2.getMaxValue(i));
        	}
            return pixelQ;
          }
        
        public static float[] domainTransform(float[] x, float c, float d, ColorSpace cs ){
        	float pixelQ[] = new float[x.length];
        	for (int i = 0; i < pixelQ.length; i++)
				pixelQ[i] = domainTransform(pixelQ[i], c, d, cs.getMinValue(i), cs.getMaxValue(i));
            return pixelQ;
          }


	/**
	 * convertColor using a BufferedImage as source and the ColorSpace Type that
	 * can be find at <a href="colspace/ColorSpaceJMR.html#field_summary">Field Summary</a>
	 *
	 * <p>
	 * If the ColorSpaceType return an instance of {@link ColorSpaceJMR} it will call a special convert
	 * method otherwise we use the classic {@link ColorConvertOp}.
	 * </p>
	 *
	 * @param src
	 * @param colorSpaceType
	 * @return BufferedImage
	 */
	public static JMRExtendedBufferedImage convertColor(BufferedImage src, int colorSpaceType) {
		ColorConvertOp op =  null;
		ColorSpace cS = null;
		JMRExtendedBufferedImage dst = null;

		switch(colorSpaceType) {
		case ColorSpaceJMR.CS_CIEXYZ:
		case ColorSpaceJMR.CS_GRAY:
		case ColorSpaceJMR.CS_LINEAR_RGB:
		case ColorSpaceJMR.CS_PYCC:
		case ColorSpaceJMR.CS_sRGB:
			//op = new ColorConvertOp(ColorSpace.getInstance(colorSpaceType),null);
			//return new JMRExtendedBufferedImage(op.filter(src,null));
//		case ColorSpaceJMR.CS_HSI:
//			 cS = ColorSpaceHSI.getInstance();
//			 //dst = op.createCompatibleDestImage(src,new ComponentColorModel(cS, false,false,Transparency.BITMASK,DataBuffer.TYPE_BYTE));
//			 dst = ImageJMR.getInstance(src.getWidth(),src.getHeight(),ImageJMR.TYPE_JMR_3F_INTERLEAVED, cS);
//			 op = new ColorConvertOp(cS,null);
//			 dst = (ImageJMR)op.filter(src,dst);
//			 break;
		case ColorSpaceJMR.CS_HSI:
		case ColorSpaceJMR.CS_YCbCr:
		case ColorSpaceJMR.CS_HSV:
		case ColorSpaceJMR.CS_HMMD:
		case ColorSpaceJMR.CS_Lab:
		case ColorSpaceJMR.CS_Luv:
		case ColorSpaceJMR.CS_RGB:
			cS = ColorSpaceJMR.getInstance(colorSpaceType);
			dst = colorConvertOp(src,cS);
			break;
		default:
			System.err.println("No transformation find for this color space");
			return null;
		}
		//System.out.println(dst.toString());
		return dst;
	}



	/**
	 * Convert an {@link BufferedImage} with <code>N</code> components in
	 * <code>N</code> {@link BufferedImage#TYPE_BYTE_GRAY} images.
	 *
	 * <p>
	 * This method has been replaced by JMRImage#getLayeredByteImages.
	 * </p>
	 *
	 * @param src
	 * @return <code>N</code> {@link BufferedImage#TYPE_BYTE_GRAY} images.
	 * @deprecated not used in JMR SIAR
	 */
	public static BufferedImage[] convert2SlideImage(BufferedImage src) {

		int numBands = src.getSampleModel().getNumBands();
		BufferedImage[] dst = new BufferedImage[numBands];
		float[] pix = new float[numBands];
		float[] centerVal = new float[numBands];
		float[] normVal = new float[numBands];
		ColorSpace srcCs = src.getColorModel().getColorSpace();

		for(int i=0; i< numBands; i++) {
			centerVal[i] = srcCs.getMinValue(i);
			normVal[i] = (srcCs.getMaxValue(i) - centerVal[i])/255.0f;
			dst[i] = new BufferedImage(src.getWidth(),src.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
		}

		Raster srcRsr = src.getRaster();
		int pixVal;

		for(int x=0;x<src.getWidth();x++) {
			for(int y=0;y<src.getHeight();y++) {
				srcRsr.getPixel(x,y, pix); //Work even if image is in int or double
				for(int i=0;i<numBands;i++) {
					pixVal = Math.round((pix[i]-centerVal[i])/normVal[i]);
					dst[i].setRGB(x,y,pixVal);
				}
			}
		}
		return dst;
	}


	/**
	 * Same kind of function as {@link #convert2SlideImage(BufferedImage)} excepting that is
	 * not returning <code>N</code> {@link BufferedImage} with {@link BufferedImage#TYPE_BYTE_GRAY}
	 * but an array of byte.
	 *
	 * <p style="color:red">WARNING : byte are signed so values E [-127,128]</p>
	 *
	 * @param im
	 * @return <code>N</code> byte vectors of size im.getWidth()*im.getHeight().
	 * @deprecated  not really implemented.
	 */
	public static byte[][] layeredImArray(BufferedImage im) {
		int width = im.getWidth();
		int height = im.getHeight();
		byte[][] pixelarray = new byte[3][width * height];

		return pixelarray;
	}


	/**
	 * Convert a {@link BufferedImage} with <code>N</code> components or band in a
	 * <code>int[]</code> array with pixels interleaved.
	 *
	 * <p>
	 *	eg: <code>pixel(1,0) : {20,255,200}</code> is converted into
	 * <code>
	 * pixArray[3]=20;
	 * pixArray[4]=255;
	 * pixArray[5]=200;
	 * </code>
	 *
	 * @param im
	 * @return an array of size: numBands * width * height
	 * @deprecated not used in JMR SIAR
	 */
	public static int[] interleavedImArray(BufferedImage im) {
		int width = im.getWidth();
		int height = im.getHeight();
		int[] pixelarray = new int[im.getSampleModel().getNumBands() * width * height];
		int j = 0;
		WritableRaster raster = im.getRaster();
		int[] pixel = new int[3];
		for (int i = 0; i < width; i++) { //row
			for (int ii = 0; ii < height; ii++) {//column
				raster.getPixel(i, ii, pixel);
				pixelarray[3 * j] = pixel[0];
				pixelarray[3 * j + 1] = pixel[1];
				pixelarray[3 * j + 2] = pixel[2];
				j++;
			}
		}
		return pixelarray;
	}
	
	public static ColorData getColorData(int RGB, ColorSpace cs){
		float[] p_in = new float[3];
		//RGB = -724354;
        float[] p_out = new float[cs.getNumComponents()];
        p_in[0] = (float) ( (RGB >> 16) & 0xFF) / 255.0f;
        p_in[1] = (float) ( (RGB >> 8) & 0xFF) / 255.0f;
        p_in[2] = (float) (RGB & 0xFF) / 255.0f;
        p_out = cs.fromRGB(p_in);
        
        for(int i=0; i<p_out.length;i++)
            p_out[i] = ColorConvertTools.domainTransform(p_out[i], cs.getMinValue(i), cs.getMaxValue(i), 0.0f,1.0f);
        
        Color c = new Color(cs, p_out, 1.0f);        
                
        return (new ColorData(c));
	}
	
	public static JMRExtendedBufferedImage scaleImage(BufferedImage imgSource, float scaleFactor){
		AffineTransform at = AffineTransform.getScaleInstance(scaleFactor,scaleFactor);
		AffineTransformOp atop = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		BufferedImage imgdest = atop.filter( imgSource, null);
		
		return new JMRExtendedBufferedImage(imgdest);
	}
	
	 public static void saveImage(File f, BufferedImage img){
	    	String image = f.getAbsolutePath();
	    	if(!image.endsWith(".jpg"))
				image += ".jpg";
			
	    	if(img!=null){
		    	try {
		   			ImageIO.write(img, "png", new File(image));
		   		}
		   		catch (IOException io) {
		    		System.err.println(io.getMessage());
		    	}
	    	}
	    }
}
