package lmodeler;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.swing.SwingUtilities;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import lsystem.LSystem;

/**
 *  Posluchac udalosti pro lsystem. Take resi vykreslovani.
 * @author Jakub Kvita
 */
public class EListener implements GLEventListener,MouseMotionListener,MouseListener,MouseWheelListener {

    private GLJPanel glpanel; //panel do ktereho kreslim a ktery je soucasti okna
    private LSystem lsystem;

    //promenne pro praci s transformacemi
    private Point2d origin = new Point2d();
    private Point2d translate = new Point2d();
    private float zoom=1;   //promenna kvuli zoomovani - ma tri stavy 1-priblizeni -1-oddaleni 0-nic
    private float translationCoef=0.25f; //koeficient u translaci
    private ArcBall arcball;
    private float[] current_matrix = new float[16];
    
    
    /**
     * Konstruktor.
     * @param glpan Panel pro renderovani OpenGL nad kterym pracuju.
     * @param lsys Lsystem, ktery mam zobrazovat.
     */
    public EListener(GLJPanel glpan, LSystem lsys){
        glpanel = glpan;
        lsystem = lsys;
        
        arcball = new ArcBall(glpanel.getWidth(),glpanel.getHeight());
    }
    
    /**
     * Vymaze transformace aktualniho pohledu a nastavi zakladni.
     */
    public void initView(){
        zoom=1.0f;
        translate.x=0;
        translate.y=0;
        arcball.reset();
    }

    /**
     * Funkce vykreslujici model.
     * @param gl2 Kontext.
     */
    protected void render( GL2 gl2) {
        //musi se vycistit po kazdem malovani aby to fungovalo
        gl2.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        
        //ziskam souradnice stredu
        Point3d center=lsystem.getCenter();
                
        gl2.glPushMatrix();
            //rotace pomoci Arcballu
            gl2.glMultMatrixd(arcball.getTransform(), 0);
            //translace v rovine obrazovky
            gl2.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, current_matrix,0);
            gl2.glLoadIdentity();
            gl2.glTranslatef((float)translate.x, (float)translate.y, 0);
            gl2.glMultMatrixf(current_matrix,0);
            //scaling
            gl2.glScalef(zoom,zoom,zoom);

            //rotuju zakladni pohled
            gl2.glRotatef(90, 1.0f, -0.7f, 0.0f);
            //posunu pozici pro transformace kolem bodu
            gl2.glTranslated(-center.x,-center.y,-center.z);
            //vykreslim
            lsystem.render(gl2);
        //vratim zpet
        gl2.glPopMatrix();
    }    
    
    @Override
    public void init(GLAutoDrawable drawable) {
        
        GL2 gl2 = drawable.getGL().getGL2();
        
        final float h = (float)glpanel.getWidth() / (float)glpanel.getHeight();

        // Reset aktuálního nastavení na nové
        gl2.glViewport(0, 0, glpanel.getWidth(), glpanel.getHeight());

        // Přepnutí na projekční matici a její resetování
        gl2.glMatrixMode(GL2.GL_PROJECTION);
        gl2.glLoadIdentity();

        // Výpočet perspektivy
        new GLU().gluPerspective(45.0f, h, 1.0, 200.0);

         // Přepnutí na matici modelview a její resetování
        gl2.glMatrixMode(GL2.GL_MODELVIEW);
        gl2.glLoadIdentity();
        
        //zapnuti normalizace normal kvuli skalovani modelu   
        gl2.glEnable(GL2.GL_NORMALIZE);
        
        //nastaveni pocatecniho pohledu
        gl2.glTranslatef(0f, 0.0f, -5f);
        
        //Zapnuti osvetleni
        float[] lightambient = {0.4f,0.4f,0.4f,1.0f};
        float[] lightdiffuse = {0.6f,0.6f,0.6f,1.0f};
        
        gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightambient,0);
        gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightdiffuse,0);
        gl2.glEnable(GL2.GL_LIGHT0);
        gl2.glEnable(GL2.GL_LIGHTING);

        // zapnuti hloubkoveho bufferu a testu
        gl2.glEnable(GL.GL_DEPTH_TEST);
        gl2.glDepthFunc(GL.GL_LEQUAL);
        
        gl2.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
        //zapnuti antialiasingu
        gl2.glEnable(GL2.GL_BLEND);
        gl2.glBlendFunc(GL.GL_SRC_ALPHA,GL.GL_ONE_MINUS_SRC_ALPHA);
        gl2.glEnable(GL2.GL_LINE_SMOOTH);    
        
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        render( drawable.getGL().getGL2() );        
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        arcball.setBounds(width, height);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        
        if(SwingUtilities.isLeftMouseButton(e)){
            
            arcball.drag(new Point2d(e.getX(),e.getY()));
        }
        else if(SwingUtilities.isRightMouseButton(e)){
            
            translate.x += -(origin.x-e.getX())/(glpanel.getWidth()*translationCoef);
            translate.y += (origin.y-e.getY())/(glpanel.getHeight()*translationCoef);
            origin.x = e.getX();
            origin.y = e.getY();
        }
        
        glpanel.display();
    }

    @Override
    public void mouseMoved(MouseEvent me) {
    }

    @Override
    public void mouseClicked(MouseEvent me) {
    }

    @Override
    public void mousePressed(MouseEvent me) {
        if(SwingUtilities.isLeftMouseButton(me)){
            //inicializace rotace arcballu
            arcball.initDrag(new Point2d(me.getX(),me.getY()));
        }
        else if(SwingUtilities.isRightMouseButton(me)){ 
            //ulozim startovni pozici pro translaci
            origin.x = me.getX();
            origin.y = me.getY();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {    
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent mwe) {
        //pohnulo se kolecko
        // 1 pohnulo se k uzivateli
        //-1 pohnulo se od uzivatele  
        if(mwe.getWheelRotation()==-1){
            zoom*=0.9;
        }
        else{
            zoom*=1.1;
        }
        //vykreslim s zoomem
        glpanel.display();
    }

}
