package jmr.application;

import detection.PythonClassifier;
import ui.AddGroupDialog;
import ui.ImageListInternalFrame;
import ui.LabelSetPanel;
import ui.ColorSetPanel;
import ui.JMRImageInternalFrame;
import detection.ImageRegionLabelDescriptor;
import jfi.events.PixelEvent;
import jfi.events.PixelListener;
import jfi.iu.ImageInternalFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import jmr.db.ListDB;
import jmr.descriptor.DescriptorList;
import jmr.descriptor.GriddedDescriptor;
import jmr.descriptor.MediaDescriptor;
import jmr.descriptor.color.SingleColorDescriptor;
import jmr.initial.descriptor.mpeg7.MPEG7DominantColors;
import jmr.initial.descriptor.mpeg7.MPEG7DominantColors.MPEG7SingleDominatColor;
import jmr.descriptor.color.MPEG7ColorStructure;
import jmr.descriptor.color.MPEG7ScalableColor;
import jmr.descriptor.label.LabelDescriptor;
import jmr.descriptor.label.LabelDescriptor.WeightBasedComparator;
import jmr.media.JMRExtendedBufferedImage;
import jmr.result.FloatResult;
import jmr.result.ResultMetadata;
import jmr.result.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ui.PreferencesDialogClassifier;

public class JMRFrame extends javax.swing.JFrame {

    private PythonClassifier clasificador;
    private boolean dbOpen;
    private ArrayList<LabelGroup> listLabelGroup;
    private ArrayList<LabelGroup> listLabelGroupRCNN;
    private Map<Integer, String> classMap = new HashMap<>();
    private Map<Integer, String> classMapRCNN = new HashMap<>();

    private final String BASE_PATH_CODE_PYTHON = "/Users/mirismr/MEGAsync/Universidad/Master/TFM_ALL/TFM/";
    private final String BASE_PATH_DBS = "/Users/mirismr/MEGAsync/Universidad/Master/TFM_ALL/Prototipo/JMR.Application/bds/";
    private final String PATH_FILE_CLASS = BASE_PATH_CODE_PYTHON + "imagenet_class_index.json";
    private final String PATH_FILE_CLASS_RCNN = BASE_PATH_CODE_PYTHON + "coco_class_index.json";

    /**
     * Crea una ventana principal
     */
    public JMRFrame() {
        initComponents();
        setIconImage((new ImageIcon(getClass().getResource("/icons/iconoJMR.png"))).getImage());

        //Desactivamos botonos de BD
        this.botonCloseDB.setEnabled(false);
        this.botonSaveDB.setEnabled(false);
        this.botonAddRecordDB.setEnabled(false);
        this.botonSearchDB.setEnabled(false);
        
        this.botonCompara.setVisible(false);

        this.actualizaBotonesRCNN(this.toggleActiveRCNN.isSelected());
        this.dbOpen = false;

        // load classes from .json
        this.loadClasses();
        this.clasificador = new PythonClassifier();

        //close connection with tcp server when press x button
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                clasificador.closeConnection();
                System.exit(0);
            }
        });

    }

    /**
     * Devuelve la ventana interna seleccionada de tipo imagen (null si no
     * hubiese ninguna selecionada o si fuese de otro tipo)
     *
     * @return la ventana interna seleccionada de tipo imagen
     */
    public JMRImageInternalFrame getSelectedImageFrame() {
        JInternalFrame vi = escritorio.getSelectedFrame();
        if (vi instanceof JMRImageInternalFrame) {
            return (JMRImageInternalFrame) escritorio.getSelectedFrame();
        } else {
            return null;
        }
    }

    /**
     * Devuelve la imagen de la ventana interna selecionada
     *
     * @return la imagen seleccionada
     */
    private BufferedImage getSelectedImage() {
        BufferedImage img = null;
        ImageInternalFrame vi = this.getSelectedImageFrame();
        if (vi != null) {
            if (vi.getType() == ImageInternalFrame.TYPE_STANDAR) {
                img = vi.getImage();
            } else {
                JOptionPane.showInternalMessageDialog(escritorio, "An image must be selected", "Image", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        return img;
    }

    /**
     * Devuelve el título de la ventana interna selecionada
     *
     * @return el título de la ventana interna selecionada
     */
    private String getSelectedFrameTitle() {
        String title = "";
        JInternalFrame vi = escritorio.getSelectedFrame();
        if (vi != null) {
            title = vi.getTitle();
        }
        return title;
    }

    /**
     * Sitúa la ventana interna <tt>vi</tt> debajo de la ventana interna activa
     * y con el mismo tamaño.
     *
     * @param vi la ventana interna
     */
    private void locateInternalFrame(JInternalFrame vi) {
        JInternalFrame vSel = escritorio.getSelectedFrame();
        if (vSel != null) {
            vi.setLocation(vSel.getX() + 20, vSel.getY() + 20);
            vi.setSize(vSel.getSize());
        }
    }

    /**
     * Muestra la ventana interna <tt>vi</tt>
     *
     * @param vi la ventana interna
     */
    private void showInternalFrame(JInternalFrame vi) {
        if (vi instanceof ImageInternalFrame) {
            ((ImageInternalFrame) vi).setGrid(this.verGrid.isSelected());
            ((ImageInternalFrame) vi).addPixelListener(new ManejadorPixel());
        }
        this.locateInternalFrame(vi);
        this.escritorio.add(vi);
        vi.setVisible(true);
    }

    /**
     * Clase interna manejadora de eventos de pixel
     */
    private class ManejadorPixel implements PixelListener {

        /**
         * Gestiona el cambio de localización del pixel activo, actualizando la
         * información de la barra de tareas.
         *
         * @param evt evento de pixel
         */
        @Override
        public void positionChange(PixelEvent evt) {
            String text = " ";
            Point p = evt.getPixelLocation();
            if (p != null) {
                Color c = evt.getRGB();
                Integer alpha = evt.getAlpha();
                text = "(" + p.x + "," + p.y + ") : [" + c.getRed() + "," + c.getGreen() + "," + c.getBlue();
                text += alpha == null ? "]" : ("," + alpha + "]");
            }
            posicionPixel.setText(text);
        }
    }

    /*
     * Código generado por Netbeans para el diseño del interfaz
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupMenuPanelOutput = new javax.swing.JPopupMenu();
        clear = new javax.swing.JMenuItem();
        popupMenuSeleccionDescriptores = new javax.swing.JPopupMenu();
        colorDominante = new javax.swing.JRadioButtonMenuItem();
        colorEstructurado = new javax.swing.JRadioButtonMenuItem();
        colorEscalable = new javax.swing.JRadioButtonMenuItem();
        colorMedio = new javax.swing.JRadioButtonMenuItem();
        separadorDescriptores = new javax.swing.JPopupMenu.Separator();
        labelDescriptor = new javax.swing.JRadioButtonMenuItem();
        popupMenuGrid = new javax.swing.JPopupMenu();
        jRadioButtonMenuItem1 = new javax.swing.JRadioButtonMenuItem();
        popupMenuSeleccionDescriptoresDB = new javax.swing.JPopupMenu();
        colorDominanteDB = new javax.swing.JRadioButtonMenuItem();
        colorEstructuradoDB = new javax.swing.JRadioButtonMenuItem();
        colorEscalableDB = new javax.swing.JRadioButtonMenuItem();
        colorMedioDB = new javax.swing.JRadioButtonMenuItem();
        separadorDescriptoresDB = new javax.swing.JPopupMenu.Separator();
        labelDescriptorDB = new javax.swing.JRadioButtonMenuItem();
        popupSeleccionEtiquetasBD = new javax.swing.JPopupMenu();
        splitPanelCentral = new javax.swing.JSplitPane();
        escritorio = new javax.swing.JDesktopPane();
        showPanelInfo = new javax.swing.JLabel();
        panelTabuladoInfo = new javax.swing.JTabbedPane();
        panelOutput = new javax.swing.JPanel();
        scrollEditorOutput = new javax.swing.JScrollPane();
        editorOutput = new javax.swing.JEditorPane();
        panelBarraHerramientas = new javax.swing.JPanel();
        barraArchivo = new javax.swing.JToolBar();
        botonAbrir = new javax.swing.JButton();
        botonGuardar = new javax.swing.JButton();
        botonPreferencias = new javax.swing.JButton();
        barraBD = new javax.swing.JToolBar();
        botonNewDB = new javax.swing.JButton();
        botonOpenDB = new javax.swing.JButton();
        botonSaveDB = new javax.swing.JButton();
        botonCloseDB = new javax.swing.JButton();
        botonAddRecordDB = new javax.swing.JButton();
        botonSearchDB = new javax.swing.JButton();
        botonCompara = new javax.swing.JButton();
        barraParametrosConsulta = new javax.swing.JToolBar();
        buttonExplore = new javax.swing.JButton();
        buttonWindowSliding = new javax.swing.JButton();
        buttonBbox = new javax.swing.JButton();
        buttonHeatmap = new javax.swing.JButton();
        comboBoxClasesCargadas = new javax.swing.JComboBox<>();
        botonAniadirGrupoLabels = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        toggleActiveRCNN = new javax.swing.JToggleButton();
        buttonRCNN = new javax.swing.JButton();
        comboBoxClasesCargadasRCNN = new javax.swing.JComboBox<>();
        botonAniadirGrupoLabelsRCNN = new javax.swing.JButton();
        checkBoxInclusion = new javax.swing.JCheckBox();
        checkBoxConsultaLabel = new javax.swing.JCheckBox();
        barraEstado = new javax.swing.JPanel();
        posicionPixel = new javax.swing.JLabel();
        infoDB = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        menuArchivo = new javax.swing.JMenu();
        menuAbrir = new javax.swing.JMenuItem();
        menuGuardar = new javax.swing.JMenuItem();
        separador1 = new javax.swing.JPopupMenu.Separator();
        closeAll = new javax.swing.JMenuItem();
        menuVer = new javax.swing.JMenu();
        verGrid = new javax.swing.JCheckBoxMenuItem();
        usarTransparencia = new javax.swing.JCheckBoxMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        showResized = new javax.swing.JCheckBoxMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        menuZoom = new javax.swing.JMenu();
        menuZoomIn = new javax.swing.JMenuItem();
        menuZoomOut = new javax.swing.JMenuItem();

        popupMenuPanelOutput.setAlignmentY(0.0F);
        popupMenuPanelOutput.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        clear.setText("Clear");
        clear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearActionPerformed(evt);
            }
        });
        popupMenuPanelOutput.add(clear);

        colorDominante.setText("Dominant color");
        popupMenuSeleccionDescriptores.add(colorDominante);

        colorEstructurado.setText("Structured color");
        popupMenuSeleccionDescriptores.add(colorEstructurado);

        colorEscalable.setText("Scalable color");
        popupMenuSeleccionDescriptores.add(colorEscalable);

        colorMedio.setText("Mean color");
        popupMenuSeleccionDescriptores.add(colorMedio);
        popupMenuSeleccionDescriptores.add(separadorDescriptores);

        labelDescriptor.setSelected(true);
        labelDescriptor.setText("Label");
        popupMenuSeleccionDescriptores.add(labelDescriptor);

        jRadioButtonMenuItem1.setSelected(true);
        jRadioButtonMenuItem1.setText("jRadioButtonMenuItem1");
        popupMenuGrid.add(jRadioButtonMenuItem1);

        colorDominanteDB.setText("Dominant color");
        colorDominanteDB.setEnabled(false);
        popupMenuSeleccionDescriptoresDB.add(colorDominanteDB);

        colorEstructuradoDB.setText("Structured color");
        popupMenuSeleccionDescriptoresDB.add(colorEstructuradoDB);

        colorEscalableDB.setText("Scalable color");
        popupMenuSeleccionDescriptoresDB.add(colorEscalableDB);

        colorMedioDB.setText("Mean color");
        popupMenuSeleccionDescriptoresDB.add(colorMedioDB);
        popupMenuSeleccionDescriptoresDB.add(separadorDescriptoresDB);

        labelDescriptorDB.setSelected(true);
        labelDescriptorDB.setText("Label");
        popupMenuSeleccionDescriptoresDB.add(labelDescriptorDB);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Detecting Objects");
        setName("ventanaPrincipal"); // NOI18N

        splitPanelCentral.setDividerLocation(1.0);
        splitPanelCentral.setDividerSize(3);
        splitPanelCentral.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitPanelCentral.setPreferredSize(new java.awt.Dimension(0, 0));
        splitPanelCentral.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                splitPanelCentralPropertyChange(evt);
            }
        });

        escritorio.setBackground(java.awt.Color.lightGray);
        escritorio.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        showPanelInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/desplegar20.png"))); // NOI18N
        showPanelInfo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                showPanelInfoMousePressed(evt);
            }
        });

        escritorio.setLayer(showPanelInfo, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout escritorioLayout = new javax.swing.GroupLayout(escritorio);
        escritorio.setLayout(escritorioLayout);
        escritorioLayout.setHorizontalGroup(
            escritorioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, escritorioLayout.createSequentialGroup()
                .addGap(0, 1118, Short.MAX_VALUE)
                .addComponent(showPanelInfo))
        );
        escritorioLayout.setVerticalGroup(
            escritorioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, escritorioLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(showPanelInfo))
        );

        splitPanelCentral.setTopComponent(escritorio);

        panelTabuladoInfo.setMinimumSize(new java.awt.Dimension(0, 0));
        panelTabuladoInfo.setPreferredSize(new java.awt.Dimension(0, 0));

        panelOutput.setMinimumSize(new java.awt.Dimension(0, 0));
        panelOutput.setPreferredSize(new java.awt.Dimension(0, 0));
        panelOutput.setLayout(new java.awt.BorderLayout());

        scrollEditorOutput.setBorder(null);
        scrollEditorOutput.setMinimumSize(new java.awt.Dimension(0, 0));

        editorOutput.setBorder(null);
        editorOutput.setMinimumSize(new java.awt.Dimension(0, 0));
        editorOutput.setPreferredSize(new java.awt.Dimension(0, 0));
        editorOutput.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                editorOutputMouseReleased(evt);
            }
        });
        scrollEditorOutput.setViewportView(editorOutput);

        panelOutput.add(scrollEditorOutput, java.awt.BorderLayout.CENTER);

        panelTabuladoInfo.addTab("Output", panelOutput);

        splitPanelCentral.setBottomComponent(panelTabuladoInfo);

        getContentPane().add(splitPanelCentral, java.awt.BorderLayout.CENTER);

        panelBarraHerramientas.setAlignmentX(0.0F);
        panelBarraHerramientas.setAlignmentY(0.0F);
        panelBarraHerramientas.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        barraArchivo.setRollover(true);
        barraArchivo.setAlignmentX(0.0F);

        botonAbrir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/open24.png"))); // NOI18N
        botonAbrir.setToolTipText("Open");
        botonAbrir.setFocusable(false);
        botonAbrir.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonAbrir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botonAbrir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonAbrirActionPerformed(evt);
            }
        });
        barraArchivo.add(botonAbrir);

        botonGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/save24.png"))); // NOI18N
        botonGuardar.setToolTipText("Save");
        botonGuardar.setFocusable(false);
        botonGuardar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonGuardar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botonGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonGuardarActionPerformed(evt);
            }
        });
        barraArchivo.add(botonGuardar);

        botonPreferencias.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/settings24.png"))); // NOI18N
        botonPreferencias.setToolTipText("Configuration");
        botonPreferencias.setFocusable(false);
        botonPreferencias.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonPreferencias.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botonPreferencias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonPreferenciasActionPerformed(evt);
            }
        });
        barraArchivo.add(botonPreferencias);

        panelBarraHerramientas.add(barraArchivo);

        barraBD.setRollover(true);

        botonNewDB.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/database.png"))); // NOI18N
        botonNewDB.setToolTipText("Create a new database");
        botonNewDB.setBorderPainted(false);
        botonNewDB.setComponentPopupMenu(popupMenuSeleccionDescriptoresDB);
        botonNewDB.setFocusable(false);
        botonNewDB.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonNewDB.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botonNewDB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonNewDBActionPerformed(evt);
            }
        });
        barraBD.add(botonNewDB);

        botonOpenDB.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/openDB.png"))); // NOI18N
        botonOpenDB.setToolTipText("Open a database");
        botonOpenDB.setFocusable(false);
        botonOpenDB.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonOpenDB.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botonOpenDB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonOpenDBActionPerformed(evt);
            }
        });
        barraBD.add(botonOpenDB);

        botonSaveDB.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/saveDB.png"))); // NOI18N
        botonSaveDB.setToolTipText("Save the database");
        botonSaveDB.setFocusable(false);
        botonSaveDB.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonSaveDB.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botonSaveDB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonSaveDBActionPerformed(evt);
            }
        });
        barraBD.add(botonSaveDB);

        botonCloseDB.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/deleteBD.png"))); // NOI18N
        botonCloseDB.setToolTipText("Close the database");
        botonCloseDB.setFocusable(false);
        botonCloseDB.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonCloseDB.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botonCloseDB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCloseDBActionPerformed(evt);
            }
        });
        barraBD.add(botonCloseDB);

        botonAddRecordDB.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/addBD.png"))); // NOI18N
        botonAddRecordDB.setFocusable(false);
        botonAddRecordDB.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonAddRecordDB.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botonAddRecordDB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonAddRecordDBActionPerformed(evt);
            }
        });
        barraBD.add(botonAddRecordDB);

        botonSearchDB.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/seacrhDB.png"))); // NOI18N
        botonSearchDB.setFocusable(false);
        botonSearchDB.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonSearchDB.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botonSearchDB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonSearchDBActionPerformed(evt);
            }
        });
        barraBD.add(botonSearchDB);

        panelBarraHerramientas.add(barraBD);

        botonCompara.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/compare24.png"))); // NOI18N
        botonCompara.setToolTipText("Compare");
        botonCompara.setComponentPopupMenu(popupMenuSeleccionDescriptores);
        botonCompara.setFocusable(false);
        botonCompara.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonCompara.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botonCompara.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonComparaActionPerformed(evt);
            }
        });
        panelBarraHerramientas.add(botonCompara);

        barraParametrosConsulta.setRollover(true);

        buttonExplore.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/binoculars.png"))); // NOI18N
        buttonExplore.setFocusable(false);
        buttonExplore.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        buttonExplore.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        buttonExplore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonExploreActionPerformed(evt);
            }
        });
        barraParametrosConsulta.add(buttonExplore);

        buttonWindowSliding.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/window.png"))); // NOI18N
        buttonWindowSliding.setFocusable(false);
        buttonWindowSliding.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        buttonWindowSliding.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        buttonWindowSliding.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonWindowSlidingActionPerformed(evt);
            }
        });
        barraParametrosConsulta.add(buttonWindowSliding);

        buttonBbox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/bbox.png"))); // NOI18N
        buttonBbox.setFocusable(false);
        buttonBbox.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        buttonBbox.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        buttonBbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonBboxActionPerformed(evt);
            }
        });
        barraParametrosConsulta.add(buttonBbox);

        buttonHeatmap.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/heatmap.png"))); // NOI18N
        buttonHeatmap.setFocusable(false);
        buttonHeatmap.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        buttonHeatmap.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        buttonHeatmap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonHeatmapActionPerformed(evt);
            }
        });
        barraParametrosConsulta.add(buttonHeatmap);

        comboBoxClasesCargadas.setMinimumSize(new java.awt.Dimension(28, 15));
        comboBoxClasesCargadas.setPreferredSize(new java.awt.Dimension(150, 20));
        barraParametrosConsulta.add(comboBoxClasesCargadas);

        botonAniadirGrupoLabels.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/etiquetaAdd.png"))); // NOI18N
        botonAniadirGrupoLabels.setToolTipText("Add label group");
        botonAniadirGrupoLabels.setFocusable(false);
        botonAniadirGrupoLabels.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonAniadirGrupoLabels.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botonAniadirGrupoLabels.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonAniadirGrupoLabelsActionPerformed(evt);
            }
        });
        barraParametrosConsulta.add(botonAniadirGrupoLabels);
        barraParametrosConsulta.add(jSeparator3);

        toggleActiveRCNN.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/power-on.png"))); // NOI18N
        toggleActiveRCNN.setFocusable(false);
        toggleActiveRCNN.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toggleActiveRCNN.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toggleActiveRCNN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleActiveRCNNActionPerformed(evt);
            }
        });
        barraParametrosConsulta.add(toggleActiveRCNN);

        buttonRCNN.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/brain24_color.png"))); // NOI18N
        buttonRCNN.setToolTipText("Label image");
        buttonRCNN.setFocusable(false);
        buttonRCNN.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        buttonRCNN.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        buttonRCNN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRCNNActionPerformed(evt);
            }
        });
        barraParametrosConsulta.add(buttonRCNN);

        panelBarraHerramientas.add(barraParametrosConsulta);
        panelBarraHerramientas.add(comboBoxClasesCargadasRCNN);

        botonAniadirGrupoLabelsRCNN.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/etiquetaAdd.png"))); // NOI18N
        botonAniadirGrupoLabelsRCNN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonAniadirGrupoLabelsRCNNActionPerformed(evt);
            }
        });
        panelBarraHerramientas.add(botonAniadirGrupoLabelsRCNN);

        checkBoxInclusion.setText("Inclusion");
        panelBarraHerramientas.add(checkBoxInclusion);

        checkBoxConsultaLabel.setText("Label query");
        panelBarraHerramientas.add(checkBoxConsultaLabel);

        getContentPane().add(panelBarraHerramientas, java.awt.BorderLayout.PAGE_START);

        barraEstado.setLayout(new java.awt.BorderLayout());

        posicionPixel.setText("  ");
        barraEstado.add(posicionPixel, java.awt.BorderLayout.LINE_START);

        infoDB.setText("Not open");
        barraEstado.add(infoDB, java.awt.BorderLayout.EAST);

        getContentPane().add(barraEstado, java.awt.BorderLayout.SOUTH);

        menuArchivo.setText("File");

        menuAbrir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/open16.png"))); // NOI18N
        menuAbrir.setText("Open");
        menuAbrir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuAbrirActionPerformed(evt);
            }
        });
        menuArchivo.add(menuAbrir);

        menuGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/save16.png"))); // NOI18N
        menuGuardar.setText("Save");
        menuGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuGuardarActionPerformed(evt);
            }
        });
        menuArchivo.add(menuGuardar);
        menuArchivo.add(separador1);

        closeAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/closeall16.png"))); // NOI18N
        closeAll.setText("Close all");
        closeAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeAllActionPerformed(evt);
            }
        });
        menuArchivo.add(closeAll);

        menuBar.add(menuArchivo);

        menuVer.setText("View");

        verGrid.setSelected(true);
        verGrid.setText("Show grid");
        verGrid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verGridActionPerformed(evt);
            }
        });
        menuVer.add(verGrid);

        usarTransparencia.setSelected(true);
        usarTransparencia.setText("Use transparency");
        menuVer.add(usarTransparencia);
        menuVer.add(jSeparator2);

        showResized.setText("Show resized images");
        menuVer.add(showResized);
        menuVer.add(jSeparator1);

        menuZoom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/zoom16.png"))); // NOI18N
        menuZoom.setText("Zoom");

        menuZoomIn.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PLUS, 0));
        menuZoomIn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/zoom-in16.png"))); // NOI18N
        menuZoomIn.setText("Zoom in");
        menuZoomIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuZoomInActionPerformed(evt);
            }
        });
        menuZoom.add(menuZoomIn);

        menuZoomOut.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_MINUS, 0));
        menuZoomOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/zoom-out16.png"))); // NOI18N
        menuZoomOut.setText("Zoom out");
        menuZoomOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuZoomOutActionPerformed(evt);
            }
        });
        menuZoom.add(menuZoomOut);

        menuVer.add(menuZoom);

        menuBar.add(menuVer);

        setJMenuBar(menuBar);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void menuAbrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAbrirActionPerformed
        BufferedImage img;
        File directorioBD = new File("/Users/mirismr/MEGAsync/Universidad/Master/TFM_ALL/TFM/images");
        JFileChooser dlg = new JFileChooser(directorioBD);
        dlg.setMultiSelectionEnabled(true);
        int resp = dlg.showOpenDialog(this);
        if (resp == JFileChooser.APPROVE_OPTION) {
            try {
                File files[] = dlg.getSelectedFiles();
                for (File f : files) {
                    img = ImageIO.read(f);
                    if (img != null) {
                        ImageInternalFrame vi = new JMRImageInternalFrame(this, img, f.toURI().toURL());
                        vi.setTitle(f.getName());
                        this.showInternalFrame(vi);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showInternalMessageDialog(escritorio, "Error in image opening", "Image", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }//GEN-LAST:event_menuAbrirActionPerformed

    private void menuGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuGuardarActionPerformed
        BufferedImage img = this.getSelectedImage();
        if (img != null) {
            JFileChooser dlg = new JFileChooser();
            int resp = dlg.showSaveDialog(this);
            if (resp == JFileChooser.APPROVE_OPTION) {
                File f = dlg.getSelectedFile();
                try {
                    ImageIO.write(img, "png", f);
                    escritorio.getSelectedFrame().setTitle(f.getName());
                } catch (Exception ex) {
                    JOptionPane.showInternalMessageDialog(escritorio, "Error in image saving", "Image", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }//GEN-LAST:event_menuGuardarActionPerformed

    private void botonAbrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonAbrirActionPerformed
        this.menuAbrirActionPerformed(evt);
    }//GEN-LAST:event_botonAbrirActionPerformed

    private void botonGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonGuardarActionPerformed
        this.menuGuardarActionPerformed(evt);
    }//GEN-LAST:event_botonGuardarActionPerformed

    private void clearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearActionPerformed
        this.editorOutput.setText("");
    }//GEN-LAST:event_clearActionPerformed

    private void splitPanelCentralPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_splitPanelCentralPropertyChange
        if (evt.getPropertyName().equals("dividerLocation")) {
            float dividerLocation = (float) splitPanelCentral.getDividerLocation() / splitPanelCentral.getMaximumDividerLocation();
            if (dividerLocation >= 1) {//Está colapsada
                //showPanelInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/desplegar20.png")));
            } else {
                //showPanelInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/cerrar16.png")));
            }
        }
    }//GEN-LAST:event_splitPanelCentralPropertyChange

    private void closeAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeAllActionPerformed
        escritorio.removeAll();
        escritorio.repaint();
    }//GEN-LAST:event_closeAllActionPerformed

    private void botonPreferenciasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonPreferenciasActionPerformed
        PreferencesDialogClassifier dlg = new PreferencesDialogClassifier(this);
        dlg.showDialog();
    }//GEN-LAST:event_botonPreferenciasActionPerformed

    private void menuZoomOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuZoomOutActionPerformed
        ImageInternalFrame vi = this.getSelectedImageFrame();
        if (vi != null) {
            int zoom = vi.getZoom();
            if (zoom >= 2) {
                vi.setZoom(zoom - 1);
                vi.repaint();
            }
        }
    }//GEN-LAST:event_menuZoomOutActionPerformed

    private void menuZoomInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuZoomInActionPerformed
        ImageInternalFrame vi = this.getSelectedImageFrame();
        if (vi != null) {
            vi.setZoom(vi.getZoom() + 1);
            vi.repaint();
        }
    }//GEN-LAST:event_menuZoomInActionPerformed

    private void verGridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verGridActionPerformed
        JInternalFrame ventanas[] = escritorio.getAllFrames();
        for (JInternalFrame vi : ventanas) {
            ((ImageInternalFrame) vi).setGrid(this.verGrid.isSelected());
            vi.repaint();
        }
    }//GEN-LAST:event_verGridActionPerformed

    private void botonComparaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonComparaActionPerformed
        JMRImageInternalFrame viAnalyzed, viQuery = this.getSelectedImageFrame();
        if (viQuery != null) {
            java.awt.Cursor cursor = this.getCursor();
            setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

            //Calculamos descriptores en la imagen consulta
            ArrayList<MediaDescriptor> descriptores_query = new ArrayList();
            if (this.colorDominante.isSelected()) {
                MPEG7DominantColors dcd_query = viQuery.getDominantColorDescriptor();
                if (dcd_query == null) {
                    dcd_query = new MPEG7DominantColors();
                    dcd_query.calculate(this.getSelectedImage(), true);
                    viQuery.setDominantColorDescriptor(dcd_query);
                }
                descriptores_query.add(dcd_query);
            }
            if (this.colorEstructurado.isSelected()) {
                JMRExtendedBufferedImage imgJMR = new JMRExtendedBufferedImage(this.getSelectedImage());
                MPEG7ColorStructure dcs_query = new MPEG7ColorStructure(imgJMR);
                descriptores_query.add(dcs_query);
            }
            if (this.colorEscalable.isSelected()) {
                JMRExtendedBufferedImage imgJMR = new JMRExtendedBufferedImage(this.getSelectedImage());
                MPEG7ScalableColor dsc_query = new MPEG7ScalableColor(imgJMR);
                descriptores_query.add(dsc_query);
            }
            if (this.colorMedio.isSelected()) {
                JMRExtendedBufferedImage imgJMR = new JMRExtendedBufferedImage(this.getSelectedImage());
                SingleColorDescriptor dmean_query = new SingleColorDescriptor(imgJMR);
                descriptores_query.add(dmean_query);
            }

            if (this.labelDescriptor.isSelected()) {
                LabelDescriptor ld_query = new ImageRegionLabelDescriptor(viQuery.getURL().getFile(), clasificador);
                this.editorOutput.setText(ld_query.toString());
                descriptores_query.add(ld_query);
            }

            //Comparamos la imagen consulta con el resto de imágenes del escritorio                        
            Vector vresult;

            List<ResultMetadata> resultList = new LinkedList<>();

            String text = editorOutput.getText();
            JInternalFrame ventanas[] = escritorio.getAllFrames();
            for (JInternalFrame vi : ventanas) {
                if (vi instanceof JMRImageInternalFrame) {
                    viAnalyzed = (JMRImageInternalFrame) vi;

                    Iterator<MediaDescriptor> itQuery = descriptores_query.iterator();
                    MediaDescriptor current_descriptor;
                    vresult = new Vector(descriptores_query.size());
                    int index = 0;

                    //DCD
                    if (this.colorDominante.isSelected()) {
                        MPEG7DominantColors dcd_analyzed = viAnalyzed.getDominantColorDescriptor();
                        if (dcd_analyzed == null) {
                            dcd_analyzed = new MPEG7DominantColors();
                            dcd_analyzed.calculate(viAnalyzed.getImage(), true);
                            viAnalyzed.setDominantColorDescriptor(dcd_analyzed);
                        }
                        current_descriptor = itQuery.next();
                        FloatResult result = (FloatResult) current_descriptor.compare(dcd_analyzed);
                        vresult.setCoordinate(index++, result.toDouble());
                    }
                    //CSD
                    if (this.colorEstructurado.isSelected()) {
                        JMRExtendedBufferedImage imgJMR = new JMRExtendedBufferedImage(viAnalyzed.getImage());
                        MPEG7ColorStructure dcs_analyzed = new MPEG7ColorStructure(imgJMR);
                        current_descriptor = itQuery.next();
                        Double result = (Double) current_descriptor.compare(dcs_analyzed);
                        vresult.setCoordinate(index++, result);
                    }
                    //SCD
                    if (this.colorEscalable.isSelected()) {
                        JMRExtendedBufferedImage imgJMR = new JMRExtendedBufferedImage(viAnalyzed.getImage());
                        MPEG7ScalableColor dsc_analyzed = new MPEG7ScalableColor(imgJMR);
                        current_descriptor = itQuery.next();
                        Double result = (Double) current_descriptor.compare(dsc_analyzed);
                        vresult.setCoordinate(index++, result);
                    }
                    // Mean color
                    if (this.colorMedio.isSelected()) {
                        JMRExtendedBufferedImage imgJMR = new JMRExtendedBufferedImage(viAnalyzed.getImage());
                        SingleColorDescriptor dmean_analyzed = new SingleColorDescriptor(imgJMR);
                        current_descriptor = itQuery.next();
                        Double compare = (Double) current_descriptor.compare(dmean_analyzed);
                        FloatResult result = new FloatResult(compare.floatValue());
                        vresult.setCoordinate(index++, result.toDouble());
                    }

                    if (this.labelDescriptor.isSelected()) {
                        LabelDescriptor ld_analyzed = new ImageRegionLabelDescriptor(viAnalyzed.getURL().getFile(), clasificador);
                        current_descriptor = itQuery.next();
                        Double result = (Double) current_descriptor.compare(ld_analyzed);
                        vresult.setCoordinate(index++, result);
                        this.editorOutput.setText(ld_analyzed.toString());
                    }

                    resultList.add(new ResultMetadata(vresult, viAnalyzed.getImage()));
                    text += "\nDist(" + viQuery.getTitle() + "," + viAnalyzed.getTitle() + ") = ";
                    text += vresult != null ? vresult.toString() + "\n" : "No calculado\n";
                }
            }
            this.editorOutput.setText(this.editorOutput.getText() + text);
            setCursor(cursor);
            //Creamas la ventana interna con los resultados
            resultList.sort(null);
            ImageListInternalFrame listFrame = new ImageListInternalFrame(resultList);
            this.escritorio.add(listFrame);
            listFrame.setVisible(true);
        }
    }//GEN-LAST:event_botonComparaActionPerformed

    private void setDataBaseButtonStatus(boolean closed) {
        this.botonNewDB.setEnabled(closed);
        this.botonOpenDB.setEnabled(closed);
        this.botonCloseDB.setEnabled(!closed);
        this.botonSaveDB.setEnabled(!closed);
        this.botonAddRecordDB.setEnabled(!closed);
        this.botonSearchDB.setEnabled(!closed);
    }

    private Class[] getDBDescriptorClasses() {
        ArrayList<Class> outputL = new ArrayList<>();
        if (this.colorEstructuradoDB.isSelected()) {
            outputL.add(MPEG7ColorStructure.class);
        }
        if (this.colorEscalableDB.isSelected()) {
            outputL.add(MPEG7ScalableColor.class);
        }
        if (this.colorMedioDB.isSelected()) {
            outputL.add(SingleColorDescriptor.class);
        }
        if (this.labelDescriptorDB.isSelected()) {
            outputL.add(ImageRegionLabelDescriptor.class);
        }
        Class output[] = new Class[outputL.size()];
        for (int i = 0; i < outputL.size(); i++) {
            output[i] = outputL.get(i);
        }
        return output;
    }

    private void botonNewDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonNewDBActionPerformed
        // Creamos la base de datos vacía
        database = new ListDB(getDBDescriptorClasses());
        // Activamos/desactivamos botones
        setDataBaseButtonStatus(false);
        this.dbOpen = false;

        updateInfoDBStatusBar("New DB (not saved)");
    }//GEN-LAST:event_botonNewDBActionPerformed

    private void botonCloseDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonCloseDBActionPerformed
        database.clear();
        database = null;
        // Activamos/desactivamos botones
        setDataBaseButtonStatus(true);
        this.dbOpen = true;

        updateInfoDBStatusBar(null);
    }//GEN-LAST:event_botonCloseDBActionPerformed

    private void botonAddRecordDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonAddRecordDBActionPerformed

        LabelDescriptor.setDefaultClassifier(clasificador);

        if (database != null) {
            java.awt.Cursor cursor = this.getCursor();
            setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            //Incorporamos a la BD todas las imágenes del escritorio
            JInternalFrame ventanas[] = escritorio.getAllFrames();
            JMRImageInternalFrame viAnalyzed;
            for (JInternalFrame vi : ventanas) {
                if (vi instanceof JMRImageInternalFrame) {
                    viAnalyzed = (JMRImageInternalFrame) vi;
                    database.add(viAnalyzed.getURL().getFile(), viAnalyzed.getURL());
                }
            }
            setCursor(cursor);
            updateInfoDBStatusBar("Updated DB (not saved)");
        }
    }//GEN-LAST:event_botonAddRecordDBActionPerformed

    private void botonSearchDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonSearchDBActionPerformed
        if (database != null) {
            java.awt.Cursor cursor = this.getCursor();
            setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            ImageRegionLabelDescriptor.setDefaultWeightComparator(new WeightBasedComparator(WeightBasedComparator.TYPE_MIN, this.checkBoxInclusion.isSelected()));
            ImageRegionLabelDescriptor.setDefaultComparator(new WeightBasedComparator(WeightBasedComparator.TYPE_MIN, this.checkBoxInclusion.isSelected()));

            if (!this.checkBoxConsultaLabel.isSelected()) {
                String pathImg = this.getSelectedImageFrame().getURL().getFile();
                if (pathImg != null) {

                    LabelDescriptor.setDefaultClassifier(clasificador);

                    ListDB.Record record = database.new Record(pathImg);
                    List<ResultMetadata> queryResult = database.queryMetadata(record);
                    ImageListInternalFrame listFrame = new ImageListInternalFrame();
                    String text = this.editorOutput.getText();
                    MediaDescriptor inicial = record.get(0);
                    text += "Imagen consulta: " + inicial.toString() + "\n";
                    for (ResultMetadata r : queryResult) {
                        text += r.getMetadata().toString().trim() + ": ";
                        text += r.getResult().toString() + "\n";
                        ListDB.Record rec = (ListDB.Record) r.getMetadata();
                        listFrame.add(rec.getLocator(), (String) r.getResult().toString());
                    }

                    this.editorOutput.setText(text);
                    this.escritorio.add(listFrame);
                    listFrame.setVisible(true);
                    setCursor(cursor);
                }
            } //consulta por label
            else {
                int indiceSeleccionado = this.comboBoxClasesCargadasRCNN.getSelectedIndex();
                LabelGroup lgSeleccionado = this.listLabelGroupRCNN.get(indiceSeleccionado);
                String queryLabel[] = (String[]) lgSeleccionado.getLabels().toArray(new String[0]);
                String first = "";
                LabelDescriptor<String> queryDescriptor = null;
                if (queryLabel.length > 1) {
                    first = queryLabel[0];
                    String[] aux = new String[queryLabel.length - 1];
                    for (int i = 1; i < queryLabel.length; i++) {
                        aux[i - 1] = queryLabel[i];
                    }
                    queryLabel = aux;
                    queryDescriptor = new ImageRegionLabelDescriptor(first, queryLabel);
                } else {
                    queryDescriptor = new ImageRegionLabelDescriptor(queryLabel[0], new String[0]);
                }

                Double weights[] = new Double[queryDescriptor.size()];
                for (int i = 0; i < weights.length; i++) {
                    weights[i] = 1.0;
                }
                queryDescriptor.setWeights(weights);

                DescriptorList<String> dList = new DescriptorList(null);
                dList.add(queryDescriptor);
                ListDB.Record record = database.new Record(dList);

                List<ResultMetadata> queryResult = database.queryMetadata(record);
                ImageListInternalFrame listFrame = new ImageListInternalFrame();
                String text = this.editorOutput.getText();
                for (ResultMetadata r : queryResult) {
                    ListDB.Record rec = (ListDB.Record) r.getMetadata();
                    text += r.getMetadata().toString().trim() + ": ";
                    text += r.getResult().toString() + "\n";
                    listFrame.add(rec.getLocator(), (String) r.getResult().toString());
                }

                this.editorOutput.setText(text);
                this.escritorio.add(listFrame);
                listFrame.setVisible(true);
                setCursor(cursor);
            }
        }
    }//GEN-LAST:event_botonSearchDBActionPerformed

    private void updateInfoDBStatusBar(String fichero) {
        String infoDB = "Not open";
        if (database != null) {
            infoDB = fichero + " [#" + database.size() + "] [";
            for (Class c : database.getDescriptorClasses()) {
                infoDB += c.getSimpleName() + ",";
            }
            infoDB = infoDB.substring(0, infoDB.length() - 1) + "]";
        }
        this.infoDB.setText(infoDB);
    }

    private void botonOpenDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonOpenDBActionPerformed
        File directorioBD = new File(BASE_PATH_DBS);
        JFileChooser dlg = new JFileChooser(directorioBD);
        dlg.setMultiSelectionEnabled(true);
        int resp = dlg.showOpenDialog(this);
        if (resp == JFileChooser.APPROVE_OPTION) {
            java.awt.Cursor cursor = this.getCursor();
            setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            File file = dlg.getSelectedFile();
            try {
                database = ListDB.open(file);
                setDataBaseButtonStatus(false);
                this.dbOpen = true;

                updateInfoDBStatusBar(file.getName());
            } catch (IOException | ClassNotFoundException ex) {
                System.err.println(ex);
            }
            setCursor(cursor);
        }
    }//GEN-LAST:event_botonOpenDBActionPerformed

    private void botonSaveDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonSaveDBActionPerformed
        File file = new File(BASE_PATH_DBS + "prueba.jmr.db");
        try {

            java.awt.Cursor cursor = this.getCursor();
            setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            database.save(file);
            setCursor(cursor);
            updateInfoDBStatusBar(file.getName());
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println(ex.getLocalizedMessage());
        }
    }//GEN-LAST:event_botonSaveDBActionPerformed

    private void buttonRCNNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRCNNActionPerformed
        JMRImageInternalFrame frameSelected = (JMRImageInternalFrame) this.escritorio.getSelectedFrame();
        String pathImageSelected = frameSelected.getURL().getFile();
        java.awt.Cursor cursor = this.getCursor();
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        ImageRegionLabelDescriptor ld_query = new ImageRegionLabelDescriptor(pathImageSelected, this.clasificador);
        setCursor(cursor);
        Random rand = new Random();

        List<String> labels = new ArrayList();
        for (int i = 0; i < ld_query.size(); i++) {
            String peso = "Not avaliable";
            if (ld_query.getWeight(ld_query.getLabel(i)) != null) {
                peso = "" + String.format("%.3g%n", ld_query.getWeight(ld_query.getLabel(i)));
            }
            String labelWeight = ld_query.getLabel(i) + ": " + peso;
            labels.add(labelWeight);

            Rectangle2D rectangle = ld_query.getBoundingBox(i);
            BufferedImage image = frameSelected.getImage();

            Graphics2D g2d = image.createGraphics();
            float r = rand.nextFloat();
            float g = rand.nextFloat();
            float b = rand.nextFloat();
            Color randomColor = new Color(r, g, b);
            g2d.setColor(randomColor);
            g2d.draw(rectangle);
            g2d.drawString(labelWeight, (int) rectangle.getX(), (int) (rectangle.getY() - 10));

        }

        if (labels.size() == 0) {
            labels.add("No classes found");
        }
        LabelSetPanel panelEtiquetas = new LabelSetPanel(labels);
        JMRImageInternalFrame vi = this.getSelectedImageFrame();
        vi.add(panelEtiquetas, BorderLayout.EAST);
        vi.validate();
        vi.repaint();
    }//GEN-LAST:event_buttonRCNNActionPerformed

    private void actualizaBotonesRCNN(boolean statusRCNN) {
        this.buttonRCNN.setEnabled(statusRCNN);
        this.comboBoxClasesCargadasRCNN.setEnabled(statusRCNN);
        this.botonAniadirGrupoLabelsRCNN.setEnabled(statusRCNN);
        this.checkBoxConsultaLabel.setEnabled(statusRCNN);
        this.checkBoxInclusion.setEnabled(statusRCNN);

        this.comboBoxClasesCargadas.setEnabled(!statusRCNN);
        this.botonAniadirGrupoLabels.setEnabled(!statusRCNN);
        this.buttonBbox.setEnabled(!statusRCNN);
        this.buttonWindowSliding.setEnabled(!statusRCNN);
        this.buttonExplore.setEnabled(!statusRCNN);
        this.buttonHeatmap.setEnabled(!statusRCNN);
    }

    private void loadClasses() {
        this.listLabelGroup = new ArrayList<>();
        this.listLabelGroupRCNN = new ArrayList<>();

        try {
            JSONParser parser = new JSONParser();
            JSONObject content = (JSONObject) parser.parse(new FileReader(PATH_FILE_CLASS));
            this.classMap.clear();
            content.keySet().forEach(k -> {
                String key = k.toString();
                JSONArray names = (JSONArray) content.get(key);
                this.classMap.put(Integer.parseInt(key), (String) names.get(1));
            });

            JSONObject content2 = (JSONObject) parser.parse(new FileReader(PATH_FILE_CLASS_RCNN));
            this.classMapRCNN.clear();
            content2.keySet().forEach(k -> {
                String key = k.toString();
                String name = (String) content2.get(key);
                this.classMapRCNN.put(Integer.parseInt(key), name);
            });
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JMRFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(JMRFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(JMRFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

        List<String> clases = new ArrayList();

        //cargar comboBoxClasesCargadas
        this.classMap.values().forEach(k -> {
            clases.add(k);
            //usado para añadir nuevos
            this.listLabelGroup.add(new LabelGroup(new ArrayList<String>(Arrays.asList(k))));
        });

        this.comboBoxClasesCargadas.setModel(new DefaultComboBoxModel(clases.toArray(new String[0])));

        clases.clear();
        this.classMapRCNN.values().forEach(k -> {
            clases.add(k);
            //usado para añadir nuevos
            this.listLabelGroupRCNN.add(new LabelGroup(new ArrayList<String>(Arrays.asList(k))));
        });

        this.comboBoxClasesCargadasRCNN.setModel(new DefaultComboBoxModel(clases.toArray(new String[0])));
    }
    private void botonAniadirGrupoLabelsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonAniadirGrupoLabelsActionPerformed

        if (this.listLabelGroup != null) {
            AddGroupDialog dlg = new AddGroupDialog(this, this.listLabelGroup);
            ArrayList<String> seleccionadas = dlg.showDialog();
            LabelGroup nuevoLG = new LabelGroup(seleccionadas);
            this.listLabelGroup.add(nuevoLG);

            DefaultComboBoxModel aModel = (DefaultComboBoxModel) this.comboBoxClasesCargadas.getModel();
            aModel.addElement((String) nuevoLG.toString());
            this.comboBoxClasesCargadas.setModel(aModel);
        }
    }//GEN-LAST:event_botonAniadirGrupoLabelsActionPerformed

    private String findKey(String value) {
        for (Integer key : this.classMap.keySet()) {
            if (this.classMap.get(key).equals(value)) {
                return key.toString();
            }
        }
        return "";
    }
    private void buttonBboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonBboxActionPerformed
        try {
            java.awt.Cursor cursor = this.getCursor();
            setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

            JMRImageInternalFrame frameSelected = (JMRImageInternalFrame) this.escritorio.getSelectedFrame();
            String pathImageSelected = frameSelected.getURL().getFile();

            ArrayList<String> classesSelected = new ArrayList<>();
            int indiceSeleccionado = this.comboBoxClasesCargadas.getSelectedIndex();
            LabelGroup lgSeleccionado = this.listLabelGroup.get(indiceSeleccionado);
            for (String label : lgSeleccionado.getLabels()) {
                classesSelected.add(this.findKey(label));
            }

            JSONObject response = this.clasificador.getBoundingBoxs(classesSelected, pathImageSelected, SettingsClassifier.getThresholdHeatmap());

            String pathOutputImage = (String) response.get("path_img_output");

            File f = new File(pathOutputImage);
            BufferedImage img = ImageIO.read(f);
            if (img != null) {
                ImageInternalFrame vi = new JMRImageInternalFrame(this, img, f.toURI().toURL());
                vi.setTitle(f.getName());
                this.showInternalFrame(vi);
            }
            setCursor(cursor);
        } catch (IOException ex) {
            Logger.getLogger(JMRFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_buttonBboxActionPerformed

    private void buttonExploreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonExploreActionPerformed

        JMRImageInternalFrame frameSelected = (JMRImageInternalFrame) this.escritorio.getSelectedFrame();
        String pathImageSelected = frameSelected.getURL().getFile();
        java.awt.Cursor cursor = this.getCursor();
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        LabelDescriptor ld_query = new LabelDescriptor(pathImageSelected, clasificador);
        setCursor(cursor);

        List<String> labels = new ArrayList();
        for (int i = 0; i < ld_query.size(); i++) {
            String peso = "Not avaliable";
            if (ld_query.getWeight(ld_query.getLabel(i)) != null) {
                peso = "" + String.format("%.3g%n", ld_query.getWeight(ld_query.getLabel(i)));
            }
            labels.add(ld_query.getLabel(i) + ": " + peso);
        }
        if (labels.size() == 0) {
            labels.add("No classes found");
        }
        LabelSetPanel panelEtiquetas = new LabelSetPanel(labels);
        JMRImageInternalFrame vi = this.getSelectedImageFrame();
        vi.add(panelEtiquetas, BorderLayout.EAST);
        vi.validate();
        vi.repaint();
    }//GEN-LAST:event_buttonExploreActionPerformed

    private void buttonWindowSlidingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonWindowSlidingActionPerformed
        // TODO add your handling code here:
        try {
            java.awt.Cursor cursor = this.getCursor();
            setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

            JMRImageInternalFrame frameSelected = (JMRImageInternalFrame) this.escritorio.getSelectedFrame();
            String pathImageSelected = frameSelected.getURL().getFile();

            JSONObject response = this.clasificador.windowSliding(pathImageSelected, SettingsClassifier.getWidthWS(), SettingsClassifier.getHeigthWS(), SettingsClassifier.getStepSizeWS());

            String pathOutputImage = (String) response.get("path_img_output");

            File f = new File(pathOutputImage);
            BufferedImage img = ImageIO.read(f);
            if (img != null) {
                ImageInternalFrame vi = new JMRImageInternalFrame(this, img, f.toURI().toURL());
                vi.setTitle(f.getName());
                this.showInternalFrame(vi);
            }
            setCursor(cursor);
        } catch (IOException ex) {
            Logger.getLogger(JMRFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_buttonWindowSlidingActionPerformed

    private void toggleActiveRCNNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleActiveRCNNActionPerformed
        this.clasificador.setActiveRCNN(this.toggleActiveRCNN.isSelected());
        this.actualizaBotonesRCNN(this.toggleActiveRCNN.isSelected());

        try {
            if (this.toggleActiveRCNN.isSelected()) {
                this.toggleActiveRCNN.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/icons/power-off.png"))));

            } else {
                this.toggleActiveRCNN.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/icons/power-on.png"))));
            }
        } catch (IOException ex) {
            Logger.getLogger(JMRFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_toggleActiveRCNNActionPerformed

    private void botonAniadirGrupoLabelsRCNNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonAniadirGrupoLabelsRCNNActionPerformed
        // TODO add your handling code here:
        if (this.listLabelGroupRCNN != null) {
            AddGroupDialog dlg = new AddGroupDialog(this, this.listLabelGroupRCNN);
            ArrayList<String> seleccionadas = dlg.showDialog();
            LabelGroup nuevoLG = new LabelGroup(seleccionadas);
            this.listLabelGroupRCNN.add(nuevoLG);

            DefaultComboBoxModel aModel = (DefaultComboBoxModel) this.comboBoxClasesCargadasRCNN.getModel();
            aModel.addElement((String) nuevoLG.toString());
            this.comboBoxClasesCargadasRCNN.setModel(aModel);
        }
    }//GEN-LAST:event_botonAniadirGrupoLabelsRCNNActionPerformed

    private void showPanelInfoMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_showPanelInfoMousePressed
        float dividerLocation = (float) splitPanelCentral.getDividerLocation() / splitPanelCentral.getMaximumDividerLocation();
        if (dividerLocation >= 1) {//Está colapsada
            splitPanelCentral.setDividerLocation(0.8);
        } else {
            splitPanelCentral.setDividerLocation(1.0);
        }
    }//GEN-LAST:event_showPanelInfoMousePressed

    private void editorOutputMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_editorOutputMouseReleased
        if (evt.isPopupTrigger()) {
            Point p = this.scrollEditorOutput.getMousePosition();
            this.popupMenuPanelOutput.show(this.panelOutput, p.x, p.y);
        }
    }//GEN-LAST:event_editorOutputMouseReleased

    private void buttonHeatmapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonHeatmapActionPerformed
        try {
            java.awt.Cursor cursor = this.getCursor();
            setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

            JMRImageInternalFrame frameSelected = (JMRImageInternalFrame) this.escritorio.getSelectedFrame();
            String pathImageSelected = frameSelected.getURL().getFile();

            ArrayList<String> classesSelected = new ArrayList<>();
            int indiceSeleccionado = this.comboBoxClasesCargadas.getSelectedIndex();
            LabelGroup lgSeleccionado = this.listLabelGroup.get(indiceSeleccionado);
            for (String label : lgSeleccionado.getLabels()) {
                classesSelected.add(this.findKey(label));
            }

            JSONObject response = this.clasificador.getHeatmap(classesSelected, pathImageSelected);

            String pathOutputImage = (String) response.get("path_img_output");

            File f = new File(pathOutputImage);
            BufferedImage img = ImageIO.read(f);
            if (img != null) {
                ImageInternalFrame vi = new JMRImageInternalFrame(this, img, f.toURI().toURL());
                vi.setTitle(f.getName());
                this.showInternalFrame(vi);
            }
            setCursor(cursor);
        } catch (IOException ex) {
            Logger.getLogger(JMRFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_buttonHeatmapActionPerformed

    // Variables no generadas automáticamente 
    ListDB<String> database = null;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar barraArchivo;
    private javax.swing.JToolBar barraBD;
    private javax.swing.JPanel barraEstado;
    private javax.swing.JToolBar barraParametrosConsulta;
    private javax.swing.JButton botonAbrir;
    private javax.swing.JButton botonAddRecordDB;
    private javax.swing.JButton botonAniadirGrupoLabels;
    private javax.swing.JButton botonAniadirGrupoLabelsRCNN;
    private javax.swing.JButton botonCloseDB;
    private javax.swing.JButton botonCompara;
    private javax.swing.JButton botonGuardar;
    private javax.swing.JButton botonNewDB;
    private javax.swing.JButton botonOpenDB;
    private javax.swing.JButton botonPreferencias;
    private javax.swing.JButton botonSaveDB;
    private javax.swing.JButton botonSearchDB;
    private javax.swing.JButton buttonBbox;
    private javax.swing.JButton buttonExplore;
    private javax.swing.JButton buttonHeatmap;
    private javax.swing.JButton buttonRCNN;
    private javax.swing.JButton buttonWindowSliding;
    private javax.swing.JCheckBox checkBoxConsultaLabel;
    private javax.swing.JCheckBox checkBoxInclusion;
    private javax.swing.JMenuItem clear;
    private javax.swing.JMenuItem closeAll;
    private javax.swing.JRadioButtonMenuItem colorDominante;
    private javax.swing.JRadioButtonMenuItem colorDominanteDB;
    private javax.swing.JRadioButtonMenuItem colorEscalable;
    private javax.swing.JRadioButtonMenuItem colorEscalableDB;
    private javax.swing.JRadioButtonMenuItem colorEstructurado;
    private javax.swing.JRadioButtonMenuItem colorEstructuradoDB;
    private javax.swing.JRadioButtonMenuItem colorMedio;
    private javax.swing.JRadioButtonMenuItem colorMedioDB;
    private javax.swing.JComboBox<String> comboBoxClasesCargadas;
    private javax.swing.JComboBox<String> comboBoxClasesCargadasRCNN;
    private javax.swing.JEditorPane editorOutput;
    private javax.swing.JDesktopPane escritorio;
    private javax.swing.JLabel infoDB;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JRadioButtonMenuItem labelDescriptor;
    private javax.swing.JRadioButtonMenuItem labelDescriptorDB;
    private javax.swing.JMenuItem menuAbrir;
    private javax.swing.JMenu menuArchivo;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem menuGuardar;
    public javax.swing.JMenu menuVer;
    private javax.swing.JMenu menuZoom;
    private javax.swing.JMenuItem menuZoomIn;
    private javax.swing.JMenuItem menuZoomOut;
    private javax.swing.JPanel panelBarraHerramientas;
    private javax.swing.JPanel panelOutput;
    private javax.swing.JTabbedPane panelTabuladoInfo;
    private javax.swing.JPopupMenu popupMenuGrid;
    private javax.swing.JPopupMenu popupMenuPanelOutput;
    private javax.swing.JPopupMenu popupMenuSeleccionDescriptores;
    private javax.swing.JPopupMenu popupMenuSeleccionDescriptoresDB;
    private javax.swing.JPopupMenu popupSeleccionEtiquetasBD;
    public javax.swing.JLabel posicionPixel;
    private javax.swing.JScrollPane scrollEditorOutput;
    private javax.swing.JPopupMenu.Separator separador1;
    private javax.swing.JPopupMenu.Separator separadorDescriptores;
    private javax.swing.JPopupMenu.Separator separadorDescriptoresDB;
    private javax.swing.JLabel showPanelInfo;
    private javax.swing.JCheckBoxMenuItem showResized;
    public javax.swing.JSplitPane splitPanelCentral;
    private javax.swing.JToggleButton toggleActiveRCNN;
    private javax.swing.JCheckBoxMenuItem usarTransparencia;
    private javax.swing.JCheckBoxMenuItem verGrid;
    // End of variables declaration//GEN-END:variables

}
