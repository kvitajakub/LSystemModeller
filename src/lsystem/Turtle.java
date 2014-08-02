package lsystem;

import java.awt.Color;
import java.util.ArrayList;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Trida zelvy pro Lsystem. Udrzuje jeji stav a pomaha s jeho transformacemi.
 * @author Jakub Kvita
 */
public class Turtle {
    
    /**
     * Konstanta pro zakladni barevnou sadu.
     */
    public static final int COLORSET_BASIC = 1000;
    /**
     * Konstanta pro rozsirenou barevnou sadu.
     */
    public static final int COLORSET_EXTENDED = 1001;
    
    private int colorSet = COLORSET_BASIC;
    private int actColorBasic = 0;
    private float actColorExtended = 0.0f;
    private Color[] colors; //barvy podle seznamu
    
    private Point3d pos;
    private Vector3d head;
    private Vector3d left;
    private Vector3d up;
    private float angleStep;
    private float thicknessCoef;
    private float thicknessStart;
    private float thickness;

    /**
     * Pole bodu podstavy posledniho vykresleneho valce.
     */
    public ArrayList<MyPoint3d> downCylinder;    
    
    private Vector3d[] transformHelp; //pomocna promenna pro transformace
    
    private final float COLOR_EXTENDED_STEP = 0.002f;
    
    /**
     *  Konstruktor.
     */
    public Turtle() {

        //pozice zelvy a jeji vektory
        pos = new Point3d();
        head = new Vector3d();
        left = new Vector3d();
        up = new Vector3d();
        
        //pomocna matice pro transformace
        transformHelp = new Vector3d[3];
        transformHelp[0] = new Vector3d();
        transformHelp[1] = new Vector3d();
        transformHelp[2] = new Vector3d();

        //sada bodu pro valec
        downCylinder = null;
        
        //nastaveni barev
        setColors();
        
        //inicializace
        angleStep = 30;
        thicknessCoef = 0.707f;
        thicknessStart = 0.2f;
        colorSet = COLORSET_BASIC;    
        init();
        
    }

    /**
     *  Konstruktor kopirujici starou zelvu do nove zelvy.
     * @param turtle Puvodni zelva.
     */
    public Turtle(Turtle turtle) {
	
        //volani konstruktoru bez parametru.
	this();

        colorSet = turtle.colorSet;
        actColorBasic = turtle.actColorBasic;
        actColorExtended = turtle.actColorExtended;
        actColorBasic = turtle.actColorBasic;
        
        pos = (Point3d)turtle.pos.clone();
        head = (Vector3d)turtle.head.clone();
        up = (Vector3d)turtle.up.clone();
        left = (Vector3d)turtle.left.clone();
        
        angleStep = turtle.angleStep;
        thicknessCoef = turtle.thicknessCoef;
        thicknessStart = turtle.thicknessStart;
        thickness = turtle.thickness;
        
        downCylinder = turtle.downCylinder;
    }

    /**
     * Inicializace zelvy co pocatecniho stavu.
     */
    public final void init() {
        pos.x = 0.0;
        pos.y = 0.0;
        pos.z = 0.0;
        head.x = 0.0;
        head.y = 0.0;
        head.z = -1.0;
        left.x = -1.0;
        left.y = 0.0;
        left.z = 0.0;
        up.x = 0.0;
        up.y = 1.0;
        up.z = 0.0;
      
        actColorBasic = 0;
        actColorExtended = 0.0f;
        thickness = getThicknessStart();
        downCylinder = null;
    }

    /**
     * Zmena aktivni barvy.
     */
    public void colorInc() {
        if(colorSet == COLORSET_BASIC){
            actColorBasic = (actColorBasic + 1) % colors.length;
        }
        else{
            actColorExtended += COLOR_EXTENDED_STEP;
        }
    }

    /**
     * Zmena aktivni barvy.
     */
    public void colorDec() {
        if(colorSet == COLORSET_BASIC){
            actColorBasic = (actColorBasic - 1 + colors.length) % colors.length;
        }
        else{
            actColorExtended -= COLOR_EXTENDED_STEP;
        }
    }

    /**
     * Zuzeni segmentu rostliny. Podle koeficientu 0.707.
     */
    public void reduceThickness() {
        setThickness(getThickness() * thicknessCoef);
    }

    /**
     * Otoci zelvou doleva a doprava.
     * @param direction Do ktereho smeru zatacim. True - vlevo, false - vpravo.
     */
    public void rotTurn(boolean direction) {
        //prevod stupnu na radiany protoze to chce Math.sin
        //otoceni znamenka protoze pracuju v zaporne casti os
        float delta = 0.0174532925f * getAngleStep();
        delta *= direction ? -1 : 1;

        transformHelp[0].x = getHead().x * Math.cos(delta) - getLeft().x * Math.sin(delta);
        transformHelp[0].y = getHead().y * Math.cos(delta) - getLeft().y * Math.sin(delta);
        transformHelp[0].z = getHead().z * Math.cos(delta) - getLeft().z * Math.sin(delta);

        transformHelp[1].x = getHead().x * Math.sin(delta) + getLeft().x * Math.cos(delta);
        transformHelp[1].y = getHead().y * Math.sin(delta) + getLeft().y * Math.cos(delta);
        transformHelp[1].z = getHead().z * Math.sin(delta) + getLeft().z * Math.cos(delta);

        //nastaveni delky vektoru
        transformHelp[0].normalize();
        transformHelp[1].normalize();

        head.x = transformHelp[0].x;
        head.y = transformHelp[0].y;
        head.z = transformHelp[0].z;
        left.x = transformHelp[1].x;
        left.y = transformHelp[1].y;
        left.z = transformHelp[1].z;
    }

    /**
     * Zelva se predkloni a zakloni.
     * @param direction True - skloni, false - zakloni.
     */
    public void rotPitch(boolean direction) {

        //prevod stupnu na radiany protoze to chce Math.sin
        //otoceni znamenka protoze pracuju v zaporne casti os
        float delta = 0.0174532925f * getAngleStep();
        delta *= direction ? -1 : 1;

        transformHelp[0].x = getHead().x * Math.cos(delta) + getUp().x * Math.sin(delta);
        transformHelp[0].y = getHead().y * Math.cos(delta) + getUp().y * Math.sin(delta);
        transformHelp[0].z = getHead().z * Math.cos(delta) + getUp().z * Math.sin(delta);

        transformHelp[2].x = -head.x * Math.sin(delta) + getUp().x * Math.cos(delta);
        transformHelp[2].y = -head.y * Math.sin(delta) + getUp().y * Math.cos(delta);
        transformHelp[2].z = -head.z * Math.sin(delta) + getUp().z * Math.cos(delta);

        transformHelp[0].normalize();
        transformHelp[2].normalize();

        head.x = transformHelp[0].x;
        head.y = transformHelp[0].y;
        head.z = transformHelp[0].z;
        up.x = transformHelp[2].x;
        up.y = transformHelp[2].y;
        up.z = transformHelp[2].z;
    }

    /**
     * Zelva se prevali doleva a doprava.
     * @param direction False - vlevo, true - vpravo.
     */
    public void rotRoll(boolean direction) {

        //prevod stupnu na radiany protoze to chce Math.sin
        //otoceni znamenka protoze pracuju v zaporne casti os
        float delta = 0.0174532925f * getAngleStep();
        delta *= direction ? -1 : 1;

        transformHelp[1].x = getLeft().x * Math.cos(delta) + getUp().x * Math.sin(delta);
        transformHelp[1].y = getLeft().y * Math.cos(delta) + getUp().y * Math.sin(delta);
        transformHelp[1].z = getLeft().z * Math.cos(delta) + getUp().z * Math.sin(delta);

        transformHelp[2].x = -left.x * Math.sin(delta) + getUp().x * Math.cos(delta);
        transformHelp[2].y = -left.y * Math.sin(delta) + getUp().y * Math.cos(delta);
        transformHelp[2].z = -left.z * Math.sin(delta) + getUp().z * Math.cos(delta);

        transformHelp[1].normalize();
        transformHelp[2].normalize();

        left.x = transformHelp[1].x;
        left.y = transformHelp[1].y;
        left.z = transformHelp[1].z;
        up.x = transformHelp[2].x;
        up.y = transformHelp[2].y;
        up.z = transformHelp[2].z;
    }

    /**
     * Zelva pokroci dopredu ve smeru a velikosti vektoru head.
     */
    public void goForward() {
        getPos().add(getHead());
    }

    /**
     * Nastavi jakymi barvami muze zelva malovat pri modu BASIC.
     */
    private void setColors() {
                
        colors = new Color[11];
        
        colors[0] = Color.WHITE;
        colors[1] = Color.RED;  
        colors[2] = Color.ORANGE;
        colors[3] = Color.YELLOW;
        colors[4] = Color.GREEN;
        colors[5] = Color.CYAN;
        colors[6] = Color.BLUE;
        colors[7] = Color.MAGENTA;
        colors[8] = Color.PINK;
        colors[9] = Color.GRAY;
        colors[10]= new Color(160, 82, 45); //hneda    
        
    }

    /**
     * Nastavi jaky druh barev pouzivam.
     * @param colorSet 
     */
    public void setColorSet(int colorSet){
        this.colorSet = colorSet;
    }
    
    /**
     * Vrati aktivni barvu, kterou se kresli.
     * @return Aktivni barva.
     */
    public Color getColor(){
        if(colorSet==COLORSET_BASIC){
            return colors[actColorBasic];
        }
        else{
            return Color.getHSBColor(actColorExtended, 1.0f, 1.0f);
        }
    }
    
    /**
     * Vrati jakou barevnou sadu pouzivam.
     * @return Barevna sada.
     */
    public int getColorSet(){
        return colorSet;
    }

    /**
     * Vrati pozici zelvy.
     * @return Pozice zelvy.
     */
    public Point3d getPos() {
        return pos;
    }

    /**
     * Vektor zelvy smerujici dopredu.
     * @return Vektor dopredu.
     */
    public Vector3d getHead() {
        return head;
    }

    /**
     * Vektor zelvy smerujici doleva.
     * @return Vektor doleva.
     */
    public Vector3d getLeft() {
        return left;
    }

    /**
     * Vektor zelvy smerujici nahoru.
     * @return Vektor nahoru.
     */
    public Vector3d getUp() {
        return up;
    }

    /**
     * Vrati uhel pro rotace zelvy.
     * @return Uhel.
     */
    public float getAngleStep() {
        return angleStep;
    }

    /**
     * Nastavi uhel pro rotace zelvy.
     * @param angleStep Uhel, ktery se ma nastavit.
     */
    public void setAngleStep(float angleStep) {
        this.angleStep = angleStep;
    }

    /**
     * @return Sirku, kterou se ma aktualne nakrelit segment.
     */
    public float getThickness() {
        return thickness;
    }

    /**
     * Nastavi sirku, kterou se kresli segmenty.
     * @param thickness Sirka, na kterou se ma sirka nastavit.
     */
    public void setThickness(float thickness) {
        this.thickness = thickness;
    }

    /**
     * Vrati pocatecni sirku segmentu.
     * @return Pocatecni sirka.
     */
    public float getThicknessStart() {
        return thicknessStart;
    }

    /**
     * Nastavi pocatecni sirku segmentu.
     * @param thicknessStart Sirka ktera se ma nastavit.
     */
    public void setThicknessStart(float thicknessStart) {
        this.thicknessStart = thicknessStart;
    }

    /**
     * Vrati koeficient zmensovani tloustky.
     * @return the thicknessCoef
     */
    public float getThicknessCoef() {
        return thicknessCoef;
    }

    /**
     * Nastavi koeficient zmensovani tloustky.
     * @param thicknessCoef the thicknessCoef to set
     */
    public void setThicknessCoef(float thicknessCoef) {
        this.thicknessCoef = thicknessCoef;
    }
}
