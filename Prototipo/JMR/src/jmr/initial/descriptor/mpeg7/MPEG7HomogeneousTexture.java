package jmr.initial.descriptor.mpeg7;

import java.awt.image.Raster;
import jmr.media.JMRExtendedBufferedImage;
import jmr.result.JMRResult;
import jmr.result.FloatResult;
import jmr.descriptor.MediaDescriptor;

/**
 * Color Structure Descriptor from MPEG7 standard.
 *
 * <p> Homogeneous texture has emerged as an important visual primitive for searching and browsing
 * through large collections of similar looking patterns.
 * An image can be considered as a mosaic of homogeneous textures so that these texture features
 * associated with the regions can be used to index the image data.</p>
 *
 * <p>To support such image retrieval, an effective representation of texture is required.
 * The Homogeneous Texture Descriptor provides a quantitative representation
 * using 62 numbers (quantified to 8 bits each) that is useful for similarity retrieval.
 * The extraction is done as follows; the image is first filtered with a bank of orientation
 * and scale tuned filters (modeled using Gabor functions) using Gabor filters. The first and the second
 * moments of the energy in the frequency domain in the corresponding sub-bands are then used as the
 * components of the texture descriptor. The number of filters used is 5x6 = 30 where 5 is the number
 * of "scales" and 6 is the number of "directions" used in the multi-resolution decomposition using
 * Gabor functions. An efficient implementation using projections and 1-D filtering operations exists
 * for feature extraction. The Homogeneous Texture descriptor provides a precise quantitative
 * description of a texture that can be used for accurate search and retrieval in this respect.
 * The computation of this descriptor is based on filtering using scale and orientation selective kernels.
 *  </p>
 *
 *
 * 
 *
 */
public class MPEG7HomogeneousTexture extends MPEG7TextureDescriptor {

  private int width, height; //size in intensity domain
  private int altoC, anchoC;

  private float[] intensityVec;
  private float[] fourrierReVec;
  private float[] fourrierImVec;
  private float[] IFourier;
  private float[] IReal;
  private byte[] Particion;
  private float[] energiaMediaCanal;
  private float[] desviacionCanal;
  private float media, desviacion;
  private float DC; //Mean of the image

  private int MIN_WAVELENGTH = 3;
  private boolean HASTA_BORDE_IMG = (MIN_WAVELENGTH == 3 ? true : false);

  /** The nofScale is equivalent at the number of track in a harddrive    */
  private int nofScale;

  /** the nofOrientation is equivalent at the number of sector in a harddrive */
  private int nofOrient;

  /** Normalize value respectively to the block (track x sector) size */
  private boolean toNormalize;

  protected static final int DEFAULT_NUM_SCALES = 5;
  protected static final int DEFAULT_NUM_ORIENTATIONS = 6;

  /**
   * Default constructor without parameters.
   * 
   */
  public MPEG7HomogeneousTexture() {
    this(DEFAULT_NUM_SCALES, DEFAULT_NUM_ORIENTATIONS, true);
  }

  /**
   * Constructor
   * @param 	nofScale	Number of Scale in which we divide our frequency 2D space ({@link #nofScale}).
   * @param	nofOrient	Number of orientation in which we divide our frequency space. ({@link #nofOrient}).
   * @param 	toNormalize If we normalize the value in each sector or not ({@link #toNormalize}).
   *
   */
  public MPEG7HomogeneousTexture(int nofScale, int nofOrient, boolean toNormalize) {
    super(true);
    this.nofScale = nofScale;
    this.nofOrient = nofOrient;
    this.toNormalize = toNormalize;
  }

  /**
   * Constructs the object for an image using the default parameters.
   * @param im	The image
   */
  public MPEG7HomogeneousTexture(JMRExtendedBufferedImage im) {
    this();
    init(im);
  }

  /** Constructs the object for an image using the specified parameters.
   * @param 	im			The image in the correct format.
   * @param 	nofScale	The number of scale.
   * @param 	nofOrient	The number of orientation.
   * @param 	toNormalize	If we normalize or not.
   */
  public MPEG7HomogeneousTexture(JMRExtendedBufferedImage im, int nofScale, int nofOrient, boolean toNormalize) {
    this(nofScale,nofOrient,toNormalize);
    init(im);
  }

  /** Computes the <code>MPEG7HomogeneousTexture</code> descriptor for
   * the media given by parameter
   * @param media The media from which the descriptor is calculated
   */
  @Override
  public void init(Object media) {
    // The MPEG7HomogeneousTexture can be calculated only from JMRExtendedBufferedImage
    if (media instanceof JMRExtendedBufferedImage) {
      init( (JMRExtendedBufferedImage) media);
    }
  }

  /** Computes the <code>MPEG7HomogeneousTexture</code> descriptor for
   * the image given by parameter
   */
  public void init(JMRExtendedBufferedImage im) {
    if (!checkImage(im)) {
      im = convertImg(im);
    }
    Raster imRst = im.getData();
    float[] imVec = getIntensityVec(imRst);
    IFourier = CalcularImagenComplejaFourier(imVec, width, height);
    DC = IFourier[0];
    IFourier = TransladarCompleja(IFourier, altoC / 2, anchoC / 2);
    Particion = ParticionCanales(nofScale, nofOrient);
    ObtenerVectorEnergia_Desviacion(IFourier, Particion);
  }

  /**
   * @param imRst
   */
  private float[] getIntensityVec(Raster imRst) {

    float[] intensityVec = null;

    //Set the size of the image has a power of two : 2^x=width
    if (!isPower2(imRst.getWidth())) {
      width = (int) Math.pow(2, ( (int) Log2(imRst.getWidth())));
    } else{
      width = imRst.getWidth();  
    }
        
    if (!isPower2(imRst.getHeight())) {
      height = (int) Math.pow(2, ( (int) Log2(imRst.getHeight())));

      //Last argument is optional and it is used only if we have a preallocation of the space.
    } else {
      height = imRst.getHeight();  
    }
    intensityVec = imRst.getPixels(0, 0, width, height, (float[])null);

    altoC = width * 2;
    anchoC = height;

    return intensityVec;
  }

  private float[] TransladarCompleja(float[] f, int nrows, int ncols) {
    int i, j, dx, dy;
    ncols = (ncols >= 0) ? (ncols % anchoC) : (anchoC + ncols % anchoC);
    nrows = (nrows >= 0) ? (nrows % altoC) : (altoC + nrows % altoC);
    float[] temp = new float[f.length];
    for (i = 0; i < altoC; i++) {
      for (j = 0; j < anchoC; j++) {
        dx = (j + ncols) % anchoC;
        dy = (i + nrows) % altoC;
        temp[dy * anchoC + dx] = f[i * anchoC + j];
        //temp[2*(dy * anchoC + dx)] = f[2*(i * anchoC + j)];
      }
    }
    return temp;
  }

  private byte[] ParticionCanales(int nscale, int norient /*, Sensor[] Canal*/) {
    byte[] Particion;
    Sensor[] Canal;

    int o, s, filtro, i, j, nfiltros;
    boolean encontrado;
    double M_PI = Math.PI;
    float angl, wavelength;
    float o_centro, s_centro;
    float anchoDos, radius, theta;
    int rowsDiv2, colsDiv2, coordX, coordY;

    /* Inicializaciones */
    nfiltros = nscale * norient;
    anchoDos = (float) (M_PI / norient) / (float) 2.0;

    rowsDiv2 = (int) (height / 2);
    colsDiv2 = (int) (width / 2);
    Particion = new byte[height * width];
    Canal = new Sensor[nfiltros];
    /* Inicializamos */
    for (i = 0; i < nfiltros; i++) {
      Canal[i] = new Sensor();

      /* Calculo de los limites de cada canal */
    }
    for (o = 1, filtro = 0; o <= norient; o++) /* Para cada orientacion */
         {
      angl = (float) ( (o - 1) * (M_PI)) / (float) norient;
      wavelength = MIN_WAVELENGTH;
      for (s = 1; s <= nscale; s++) /* Para cada escala      */
           {
        o_centro = angl;
        s_centro = (float) (1.0 / wavelength) * width;
        Canal[filtro].o_sup = o_centro + anchoDos;
        Canal[filtro].o_inf = o_centro - anchoDos;
        Canal[filtro].s_sup = (float) ( (s == 1 && HASTA_BORDE_IMG) ? colsDiv2 :
                                       s_centro * Math.sqrt(2.0));
        Canal[filtro].s_inf = s_centro / (float) Math.sqrt(2.0);
        wavelength *= 2;
        filtro++;
      }
    }

    /* Generacion de la imagen de canales */
    for (i = 0; i < height; i++) {
      for (j = 0; j < width; j++) {
        coordX = j - colsDiv2;
        coordY = rowsDiv2 - i;
        //radius = Math.hypot( (double) coordX, (double) coordY);
        radius = (float) Math.sqrt( (coordX * coordX + coordY * coordY));
        theta = (float) Math.atan2( (double) coordY, (double) coordX);
        /* Buscamos si esta en alguno de los canales asociados a un filtro Gabor */
        for (filtro = 0, encontrado = false; filtro < nfiltros && !encontrado;
             filtro++) {
          if (theta > Canal[filtro].o_inf && theta <= Canal[filtro].o_sup) {
            if (radius > Canal[filtro].s_inf && radius <= Canal[filtro].s_sup) {
              Particion[i * width + j] = (byte) (filtro + 1);
              Canal[filtro].nPtos++;
              encontrado = true;
            }
          }
        }
      }
    }
    return Particion;
  }

  private void ObtenerVectorEnergia_Desviacion(float[] imgFour,
                                               byte[] Particion) {
    // Obtiene un vector asociado a la imagen 'imgFour'. Dicho vector tendra un componente
    // por cada sensor de la particion y dicho componente representara la magnitud media de ese sensor.
    int t, indFiltro, tam, nPtos[];
    int dimVector = nofScale * nofOrient;
    int ptrByte, ptrFloat;
    //inicializaci�n
    energiaMediaCanal = new float[dimVector];
    desviacionCanal = new float[dimVector];
    float[] sumaCuadrado = new float[dimVector];
    nPtos = new int[dimVector];
    tam = width * height;

    for (t = 0, ptrByte = 0, ptrFloat = 0; t < tam; t++, ptrByte++,
         ptrFloat += 2) {
      if ( (indFiltro = Particion[ptrByte]) != 0) { // Si es un punto de un sensor...
        float med = (float) Math.sqrt( (imgFour[ptrFloat] * imgFour[ptrFloat] +
                                        imgFour[ptrFloat +
                                        1] * imgFour[ptrFloat + 1]));
        energiaMediaCanal[indFiltro - 1] += med;
        sumaCuadrado[indFiltro - 1] += med * med;
        nPtos[indFiltro - 1]++;
      }
    }

    //calculamos energia media x canal y la desviaci�n t�pica
    for (t = 0; t < dimVector; t++) {
      if (nPtos[t] != 0) {
        energiaMediaCanal[t] /= nPtos[t];
        desviacionCanal[t] = (float) Math.sqrt( (sumaCuadrado[t] -
                                                 (energiaMediaCanal[t] *
                                                  energiaMediaCanal[t])) /
                                               (double) nPtos[t]);
      }
      else {
        energiaMediaCanal[t] = 0;
        desviacionCanal[t] = 0;
      }
    }

    //calculamos la media de la imagen
    media = (float) DC / width * height;

    //calculamos la desviacion
    desviacion = 0;
  }

  private void NormalizarVectorEnergia(float[] energia, int nscales,
                                       int norients) {
    int s, o;
    float sum;
    for (s = 0; s < nscales; s++) { /* Para cada escala */
      for (o = 0, sum = 0; o < norients; o++) {
        sum += energia[o * nscales + s];
      }
      /* Normalizamos */
      for (o = 0; o < norients; o++) {
        if (sum != 0) {
          energia[o * nscales + s] /= sum;
        }
      }
    }
  }

  private float[] CalcularImagenComplejaFourier(float[] intens, int ancho,
                                                int alto) {
    int[] nn = new int[3];
    int ntot, ntot2, cnt2, numnn;
    float[] fourier;
    nn[1] = alto;
    nn[2] = ancho;
    numnn = 2;
    ntot = nn[1] * nn[2];
    IReal = new float[ntot * 2];

    fourn(intens, nn, numnn, 1);

    fourier = removeFirstElement(intens);

    return fourier;
  }

  private float[] removeFirstElement(float[] f) {
    float[] four = new float[f.length - 1];
    for (int i = 0; i < four.length; i++) {
      four[i] = f[i + 1];
    }
    return four;
  }

  private boolean isPower2(int a) {
    double p = Log2(a);
    if (p == (int) p) {
      return true;
    }
    else {
      return false;
    }
  }

  private double Log2(double a) {
    return Math.log(a) / Math.log(2.0);
  }

  private void fourn(float data[], int nn[], int ndim, int isign) {
    int idim;
    int i1, i2, i3, i2rev, i3rev, ip1, ip2, ip3, ifp1, ifp2;
    int ibit, k1, k2, n, nprev, nrem, ntot;
    float tempi, tempr;
    double theta, wi, wpi, wpr, wr, wtemp;

    System.out.println(ndim+" ");
    for(int a: nn) System.out.println(a+" ");
    
    for (ntot = 1, idim = 1; idim <= ndim; idim++) {
      ntot *= nn[idim];
    }
    nprev = 1;
    for (idim = ndim; idim >= 1; idim--) {
      n = nn[idim];
      
      System.out.println(n+" "+nprev);
      
      nrem = ntot / (n * nprev);
      ip1 = nprev << 1;
      ip2 = ip1 * n;
      ip3 = ip2 * nrem;
      i2rev = 1;
      for (i2 = 1; i2 <= ip2; i2 += ip1) {
        if (i2 < i2rev) {
          for (i1 = i2; i1 <= i2 + ip1 - 2; i1 += 2) {
            for (i3 = i1; i3 <= ip3; i3 += ip2) {
              i3rev = i2rev + i3 - i2;
              //SWAP(data[i3], data[i3rev]);
              //SWAP(data[i3 + 1], data[i3rev + 1]);
              SWAP(data, i3, i3rev);
              SWAP(data, i3 + 1, i3rev + 1);
            }
          }
        }
        ibit = ip2 >> 1;
        while (ibit >= ip1 && i2rev > ibit) {
          i2rev -= ibit;
          ibit >>= 1;
        }
        i2rev += ibit;
      }
      ifp1 = ip1;
      while (ifp1 < ip2) {
        ifp2 = ifp1 << 1;
        theta = isign * 6.28318530717959 / (ifp2 / ip1);
        wtemp = Math.sin(0.5 * theta);
        wpr = -2.0 * wtemp * wtemp;
        wpi = Math.sin(theta);
        wr = 1.0;
        wi = 0.0;
        for (i3 = 1; i3 <= ifp1; i3 += ip1) {
          for (i1 = i3; i1 <= i3 + ip1 - 2; i1 += 2) {
            for (i2 = i1; i2 <= ip3; i2 += ifp2) {
              k1 = i2;
              k2 = k1 + ifp1;
              tempr = (float) wr * data[k2] - (float) wi * data[k2 + 1];
              tempi = (float) wr * data[k2 + 1] + (float) wi * data[k2];
              data[k2] = data[k1] - tempr;
              data[k2 + 1] = data[k1 + 1] - tempi;
              data[k1] += tempr;
              data[k1 + 1] += tempi;
            }
          }
          wr = (wtemp = wr) * wpr - wi * wpi + wr;
          wi = wi * wpr + wtemp * wpi + wi;
        }
        ifp1 = ifp2;
      }
      nprev *= n;

    }
  }

  private void SWAP(float[] d, int a, int b) {
    float tempr = d[a];
    d[a] = d[b];
    d[b] = tempr;
  }

  /**
   * M�todo para obtener el vector con la energia media de cada canal
   * @return float[] vector con la energia media de cada canal
   */
  public float[] getEnergiaMediaCanales() {
    return energiaMediaCanal;
  }

  /**
   * M�todo para obtener el vector con la desviaci�n de cada canal
   * @return float[] vector con la desviaci�n de cada canal
   */
  public float[] getDesviacionCanales() {
    return desviacionCanal;
  }

  /**
   * M�todo para obtener la media de la imagen
   * @return float media de la imagen
   */
  public float getMedia() {
    return media;
  }

  /**
   * M�todo para obtener la desviaci�n de la imagen
   * @return float desviaci�n de la imagen
   */
  public float getDesviacion() {
    return desviacion;
  }

  /** Compare this <code>MPEG7HomogeneousTexture</code> obtect with the
   * <code>MPEG7HomogeneousTexture</code> given by parameter
   * <p> This method is valid only for <code>MPEG7HomogeneousTexture</code>
   *  media descriptors
   * @param mediaDescriptor MediaDescriptor object to be compared
   * @see #compare(MPEG7HomogeneousTexture desc)
   * @return The difference between descriptors
   */
  public JMRResult compare(MediaDescriptor mediaDescriptor) {
    // Only MPEG7HomogeneousTexture objects can be compared
    if (! (mediaDescriptor instanceof MPEG7HomogeneousTexture)) {
      return (null);
    }
    return (compare( (MPEG7HomogeneousTexture) mediaDescriptor));
  }

  /** Compare two <code>MPEG7HomogeneousTexture</code> objects
   * @param desc <code>MPEG7HomogeneousTexture</code> object to be compared
   */
  public FloatResult compare(MPEG7HomogeneousTexture desc) {
    // TODO Auto-generated method stub
    return (null);
  }

  /* (non-Javadoc)
   * @see es.ugr.siar.ip.desc.VisualDescriptor#checkImage(es.ugr.siar.ip.ImageJMR)
   */
  protected boolean checkImage(JMRExtendedBufferedImage im) {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see es.ugr.siar.ip.desc.VisualDescriptor#isComputed()
   */
  public boolean isComputed() {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see es.ugr.siar.ip.desc.VisualDescriptor#multiply(float)
   */
  public void multiply(float factor) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see es.ugr.siar.ip.desc.VisualDescriptor#sum(es.ugr.siar.ip.desc.VisualDescriptor)
   */
  public void sum(MPEG7HomogeneousTexture desc) {
    // TODO Auto-generated method stub

  }

  /**
   * Inner class used to describe a channel in the Fourier spectrum.
   */
  public class Sensor {
    float o_sup;
    float o_inf;
    float s_sup;
    float s_inf;
    int nPtos;
    float peso;
    double media;
    double desv;

    public Sensor() {
      o_sup = 0;
      o_inf = 0;
      s_sup = 0;
      s_inf = 0;
      nPtos = 0;
      peso = 0;
      media = 0;
      desv = 0;
    }
  }

}
