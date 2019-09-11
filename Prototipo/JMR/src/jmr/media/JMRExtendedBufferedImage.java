/**
 *
 */
package jmr.media;

import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;


import javax.imageio.ImageIO;

import jmr.colorspace.ColorSpaceJMR;


/**
 * A Subclass of {@link BufferedImage} that has a special implementation to use
 * {@link SampleModel} with {@link DataBuffer#TYPE_FLOAT} and {@link ColorSpaceJMR}.
 *
 *
 * (<a href="http://ivrg.epfl.ch" target="about_blank">IVRG-LCAV-EPFL</a> 
 *  <a href="http://decsai.ugr.es/vip" target="about_blank">VIP-DECSAI-UGR</a>)
 * @version 1.0
 * @since 5 dec. 07
 *
 */
public class JMRExtendedBufferedImage extends BufferedImage {

	public static final int TYPE_JMR_3F_INTERLEAVED = TYPE_BYTE_INDEXED + 1;
	public static final int TYPE_JMR_4F_INTERLEAVED = TYPE_BYTE_INDEXED + 2;
	public static final int TYPE_JMR_3I_INTERLEAVED = TYPE_BYTE_INDEXED + 3;

	protected boolean isClassicType;
	protected int specialType;

	/** When the image is load from a file it keep a trace of its source file name   */
	protected File srcfile = null;

	public JMRExtendedBufferedImage(ColorModel cM, WritableRaster wR, int type) {
		super(cM, wR, false,new Hashtable());
		this.isClassicType = false;
		specialType = type;

	}
	private JMRExtendedBufferedImage(int w, int h, int imType) {
		super(w, h, imType);
		this.isClassicType = true;
		this.specialType = imType;
	}

	public JMRExtendedBufferedImage(BufferedImage im) {
		super(im.getColorModel(),im.getRaster(),false,new Hashtable());
	}

	public JMRExtendedBufferedImage(BufferedImage im,File srcFile) {
		super(im.getColorModel(),im.getRaster(),false,new Hashtable());
		this.srcfile = srcFile;
	}


	/**
	 * Create an ImageJMR 
	 *
	 * @param width
	 * @param height
	 * @param imageType
	 * @param cS
	 * @return an ImageJMR file
	 */
	public static JMRExtendedBufferedImage getInstance(int width, int height, int imageType, ColorSpace cS) {
		if(imageType <= TYPE_BYTE_INDEXED)
			return new JMRExtendedBufferedImage(width,height,imageType);
		else {
			SampleModel sM;
			WritableRaster wR;
			ColorModel cM;

			switch(imageType) {
			case TYPE_JMR_3I_INTERLEAVED:
				/**
				PixelInterleavedSampleModel pISM = new PixelInterleavedSampleModel(DataBuffer.TYPE_INT,
						width,height,4,width*4, new int[] { 0, 1, 2, 3});
				DataBufferInt dBI = new DataBufferInt(width * height * 4); **/
			case TYPE_JMR_4F_INTERLEAVED:
				sM = new PixelInterleavedSampleModel(
						DataBuffer.TYPE_FLOAT,width,height,4,width*4, new int[] { 0, 1, 2, 3});
				wR = Raster.createWritableRaster(
						sM, new DataBufferFloat(width * height * 4), new Point(0,0));
				cM = new ComponentColorModel(
						cS,false,false,Transparency.OPAQUE,DataBuffer.TYPE_FLOAT);
				break;
			case TYPE_JMR_3F_INTERLEAVED: //Considered as default case
			default:
				imageType = TYPE_JMR_3F_INTERLEAVED;
			sM = new PixelInterleavedSampleModel(
					DataBuffer.TYPE_FLOAT,width,height,3,width*3, new int[] { 0, 1, 2});
			wR = Raster.createWritableRaster(
					sM, new DataBufferFloat(width * height * 3), new Point(0,0));
			cM = new ComponentColorModel(
					cS,false,false,Transparency.OPAQUE,DataBuffer.TYPE_FLOAT);
			}
			return new JMRExtendedBufferedImage(cM,wR,imageType);
		}
	}

	/**
	 * Create a  JMRImage}from a file keeping in memory the path of its
	 * source filename.
	 *
	 * @param 	file
	 * @see #getSrcfile()
	 */
	public static JMRExtendedBufferedImage loadFromFile(File file) {
		JMRExtendedBufferedImage im = null;
		try {
			im = new JMRExtendedBufferedImage(ImageIO.read(file),file);
		} catch (IOException e1) {
			System.err.println(">> Cannot open image "+file.getName());
			e1.printStackTrace();
		}
		return im;
	}



	/**
	 * Convert an ImageJMR}with <code>N</code> components in
	 * <code>N</code> {@link BufferedImage#TYPE_BYTE_GRAY} images.
	 *
	 * <p>The values of JMRImage are scaled using ColorSpaceJMR#getMaxValue(int) and
	 * ColorSpaceJMR#getMinValue(int) to convert them in 8-bit values between [0-255]</p>
	 *
	 *
	 * @return an array of {@link BufferedImage} with {@link BufferedImage#TYPE_BYTE_GRAY}
	 */
	public BufferedImage[] getLayeredByteImages() {

		int numBands = this.getSampleModel().getNumBands();
		if(numBands != this.getColorModel().getNumComponents()) {
			System.err.println("Error: Color Model is not valid with Sample model of Raster");
		}
		BufferedImage[] dst = new BufferedImage[numBands];
		//Special case when it's gray there is nothing to do!
		if(this.getColorModel().getColorSpace().getType() == ColorSpaceJMR.CS_GRAY) {
			dst[0] = (BufferedImage)this;
			return dst;
		}
		WritableRaster[] dstRsr = new WritableRaster[numBands];
		float[] pix = new float[numBands];
		float[] centerVal = new float[numBands];
		float[] normVal = new float[numBands];
		ColorSpace srcCs = this.getColorModel().getColorSpace();

		for(int i=0; i< numBands; i++) {
			dst[i] = new BufferedImage(this.getWidth(),this.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
			dstRsr[i] = dst[i].getRaster();
			normVal[i] = 1;
		}

		for(int i=0; i< this.getColorModel().getNumColorComponents(); i++) {
			centerVal[i] = srcCs.getMinValue(i);
			normVal[i] = (srcCs.getMaxValue(i) - centerVal[i])/255.0f;
		}


		Raster srcRsr = this.getRaster();

		int[] pixVal = new int[1];

		for(int x=0;x<this.getWidth();x++) {
			for(int y=0;y<this.getHeight();y++) {
				srcRsr.getPixel(x,y, pix); //Work even if image is in int or double
				for(int i=0;i<numBands;i++) {
					pixVal[0] = (Math.round((pix[i]-centerVal[i])/normVal[i]) & 0xFF);
					dstRsr[i].setPixel(x,y,pixVal);
					//if(y<50 && x%10==0) System.out.print(i+":"+ pixVal+">"+dst[i].getRGB(x, y)+"  ");
				}

			}
		}
		return dst;
	}



	/**
	 * Convert an JMRImage in a {@link BufferedImage} with values scaled between
	 * <code>[0-255]</code> to fit the {@link BufferedImage#TYPE_3BYTE_BGR} or
	 * {@link BufferedImage#TYPE_4BYTE_ABGR}.
	 *
	 *
	 * <p>The values of JMRImage are scaled using ColorSpaceJMRgetMaxValue and
	 * ColorSpaceJMR#getMinValue to convert them in 8-bit values between <code>[0-255]</code>
	 * </p>
	 *
	 *
	 * @return a BufferedImage with <code>TYPE_3/(4)BYTE_(A)BGR</code>.
	 */
	public BufferedImage getInterleavedBytesImage() {

		BufferedImage dst = null;
		WritableRaster dstRsr = null;
		int numBands = this.getSampleModel().getNumBands();
		if(numBands != this.getColorModel().getNumComponents()) {
			System.err.println("Error: Color Model is not valid with Sample model of Raster");
		}

		if(numBands == 3) {
			dst = new BufferedImage(this.getWidth(),this.getHeight(),BufferedImage.TYPE_3BYTE_BGR);
		}
		else {
			if( numBands == 4) {
				dst = new BufferedImage(this.getWidth(),this.getHeight(),BufferedImage.TYPE_4BYTE_ABGR);
			}
			else return dst;
		}


		float[] pix = new float[numBands];
		float[] centerVal = new float[numBands];
		float[] normVal = new float[numBands];
		ColorSpace srcCs = this.getColorModel().getColorSpace();

		for(int i=0; i< numBands; i++)  normVal[i] = 1.0f;
		for(int i=0; i< this.getColorModel().getNumColorComponents(); i++) {
			centerVal[i] = srcCs.getMinValue(i);
			normVal[i] = (srcCs.getMaxValue(i) - centerVal[i])/255.0f;
		}


		Raster srcRsr = this.getRaster();

		int[] pixVal = new int[1];

		for(int x=0;x<this.getWidth();x++) {
			for(int y=0;y<this.getHeight();y++) {
				srcRsr.getPixel(x,y, pix); //Work even if image is in int or double
				for(int i=0;i<numBands;i++) {
					pixVal[0] = (Math.round((pix[i]-centerVal[i])/normVal[i]));
					dstRsr.setPixel(x,y,pixVal);
				}

			}
		}
		return dst;
	}



	/** Return the srcFile if the ImageJMR has been created by {@link #loadFromFile(File)} or that
	 * somebody set this values.
	 *
	 * @return The Source File
	 */
	public File getSrcfile() {
		return srcfile;
	}

	/**
	 * Set the sourceFile if the ImageJMR has not been created by {@link #loadFromFile(File)}.
	 *
	 * @param srcfile
	 */
	public void setSrcfile(File srcfile) {
		this.srcfile = srcfile;
	}

	/**
	 * get the Number Of Element that exist in this ImageJMR.
	 *<p><code> width*height*numBands</code></p>
	 *
	 */
	public int getNofElement() {
		return this.getWidth()*this.getHeight()*this.getRaster().getNumBands();
	}

	/**
	 * Compute the maximum value for each components.
	 *
	 * @return a array of {@link Raster#getNumBands()} values.
	 */
	public float[] getMaxVal() {

		Raster srcRst = this.getRaster();

		float[] pix= new float[srcRst.getNumBands()];
		float[] max= new float[srcRst.getNumBands()];
		for(int i=0;i<srcRst.getNumBands();i++) max[i] = Float.MIN_VALUE;

		for(int x=0;x<this.getWidth();x++) {
			for(int y=0;y<this.getHeight();y++) {
				pix = srcRst.getPixel(x, y, pix);
				for(int i=0;i<srcRst.getNumBands();i++) {
					if(pix[i] > max[i]) max[i] = pix[i];
				}
			}
		}
		return max;
	}

	/**
	 * Compute the maximum value for a specific component.
	 *
	 * @param Component
	 */
	public float getMaxVal(int Component) {

		float max= Float.MIN_VALUE;
		Raster srcRst = this.getRaster();

		if( 0 < Component || Component > srcRst.getNumBands()) {
			System.err.println("Component as a bad value");
			return 0;
		}
		float[] pix= new float[srcRst.getNumBands()];

		for(int x=0;x<this.getWidth();x++) {
			for(int y=0;y<this.getHeight();y++) {
				pix = srcRst.getPixel(x, y, pix);
				if(pix[Component] > max) max = pix[Component];
			}
		}
		return max;
	}

	/**
	 * Compute the minimum value for each components.
	 *
	 * @return a array of {@link Raster#getNumBands()} values.
	 */
	public float[] getMinVal() {

		Raster srcRst = this.getRaster();

		float[] pix= new float[srcRst.getNumBands()];
		float[] min= new float[srcRst.getNumBands()];
		for(int i=0;i<srcRst.getNumBands();i++) min[i] = Float.MAX_VALUE;

		for(int x=0;x<this.getWidth();x++) {
			for(int y=0;y<this.getHeight();y++) {
				pix = srcRst.getPixel(x, y, pix);
				for(int i=0;i<srcRst.getNumBands();i++) {
					if(pix[i] < min[i]) min[i] = pix[i];
				}
			}
		}
		return min;
	}

	/**
	 * Compute the minimum value for a specific component.
	 *
	 * @param Component
	 */
	public float getMinVal(int Component) {

		float min= Float.MAX_VALUE;
		Raster srcRst = this.getRaster();

		if( 0 < Component || Component > srcRst.getNumBands()) {
			System.err.println("Component as a bad value");
			return 0;
		}

		float[] pix= new float[srcRst.getNumBands()];

		for(int x=0;x<this.getWidth();x++) {
			for(int y=0;y<this.getHeight();y++) {
				pix = srcRst.getPixel(x, y, pix);
				if(pix[Component] > min) min = pix[Component];
			}
		}
		return min;
	}

	/**
	 * Return the Type of ImageJMR.
	 *
	 */
	public int getType() {
		if(super.getType() != 0)
			return super.getType();
		else
			return specialType;
	}

	/**
	 * Return a String convertion of the type of the ImageJMR.
	 *
	 * <p><i>This function is mainly used for debugging and printing</i></p>
	 */
	public String getTypeName() {
		switch(getType()) {
		case TYPE_3BYTE_BGR :
			return "3BYTE_BGR";
		case TYPE_4BYTE_ABGR :
			return "4BYTE_ABGR";
		case TYPE_4BYTE_ABGR_PRE :
			return "4BYTE_ABGR_PRE";
		case TYPE_BYTE_BINARY:
			return "BYTE_BINARY";
		case TYPE_BYTE_GRAY :
			return "BYTE_GRAY";
		case TYPE_BYTE_INDEXED :
			return "BYTE_INDEXED";
		case TYPE_INT_ARGB:
			return "INT_ARGB";
		case TYPE_INT_ARGB_PRE:
			return "INT_ARGB_PRE";
		case TYPE_INT_BGR:
			return "INT_BGR";
		case TYPE_INT_RGB:
			return "INT_RGB";
		case TYPE_USHORT_555_RGB:
			return "USHORT_555_RGB";
		case TYPE_USHORT_565_RGB:
			return "USHORT_565_RGB";
		case TYPE_USHORT_GRAY:
			return "TYPE_USHORT_GRAY";
		case BufferedImage.TYPE_CUSTOM:
			return "TYPE_CUSTOM";
		case TYPE_JMR_3F_INTERLEAVED:
			return "TYPE_JMR_3F_INTERLEAVE (3 float components interleaved)";
		case TYPE_JMR_4F_INTERLEAVED:
			return "TYPE_JMR_4F_INTERLEAVE (4 float components interleaved)";
		case TYPE_JMR_3I_INTERLEAVED:
			return "TYPE_JMR_3I_INTERLEAVE (3 int components interleaved)";
		default:
			return "Unknown";
		}
	}

	/**
	 * Return a String with the Min and Max Values of this images
	 *
	 * <p><i>This function is mainly used for debugging and printing</i></p>
	 */
	public String minMaxVal() {
		float [] min = getMinVal();
		float [] max = getMaxVal();
		String strMin = "Min : "+min[0];
		String strMax = "Max : "+max[0];
		for(int i=1;i<getRaster().getNumBands();i++) {
			strMin +=", "+min[i];
			strMax +=", "+max[i];
		}
		return strMin+" / "+strMax;
	}

	/**
	 * Returns a string representation of this image
	 *
	 * <p><i>This function is mainly used for debugging and printing</i></p>
	 */
	public String toString() {
		return toString(false);
	}

	/**
	 * Returns a string representation of this image
	 *
	 * <p><i>This function is mainly used for debugging and printing</i></p>
	 */
	public String toString(boolean full) {
		String str ="";
		if(full) {
			str +="\n### Buffered Image information ###\n";
			str +="----------------------------------\n";
		}
		str +=super.toString()+"\n";
		str += " |- * Type: "+this.getTypeName()+"\n";
		str += " |- * Size: "+this.getWidth()+"x"+this.getHeight()+"\n";
		str += " |- * " +this.minMaxVal()+"\n";
		if(full) {
			str += " |- * Raster : "+this.getRaster().toString()+"\n";
			str += "    |- * DataBuffer : Type "+this.getRaster().getDataBuffer().toString()+"\n";
			str += "    |- * SampleModel "+this.getRaster().getSampleModel().toString()+"\n";
			str += "\n";
		}
		return str;
	}


}
