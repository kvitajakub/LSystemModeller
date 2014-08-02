package lsystem;

import java.awt.Color;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.media.opengl.GL2;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 *  Trida pro cely L-system. Zde se navazuji jednotlive soucasti.
 * @author Jakub Kvita
 */
public class LSystem {

    /**
     * Konstanta pro zakladni barevnou sadu.
     */
    public static final int COLORSET_BASIC = 1000;
    /**
     * Konstanta pro rozsirenou barevnou sadu.
     */
    public static final int COLORSET_EXTENDED = 1001;
        
    private String axiom = "F";
    private Map<Character,List<LRule>> rulesMap = new HashMap<>();
    private String rules = "";
    
    private String ignoredSymbols = "";
    
    private Turtle turtle = new Turtle();
    private Stack<Turtle> turtleStack = new Stack<>(); //zasobnik stavu zelvy pro []
    
    private List<String> derivations = new ArrayList<>(); //seznam derivaci
    private List<Point3d> center = new ArrayList<>(); //stred modelu kolem ktereho se delaji transformace
    private int actDeriv = 0;  //aktivni derivace
   
    private float[] floats;
    
    /**
     * Konstruktor.
     */
    public LSystem(){
        derivations.add(axiom);
        computeCenter();
    }
    
    /**
     * Getter pro aktivni derivaci.
     * @return Vrati cislo aktivni derivace.
     */
    public int getActDerivation(){
        return actDeriv;
    }
    
    /**
     * Nastavi krok otoceni zelvy.
     * @param angle Uhel o ktery se bude zelva otacet.
     */
    public void setAngleStep(double angle){
        turtle.setAngleStep((float)angle);
        computeCenter();
    }
    
    /**
     * Getter pro velikost uhlu.
     * @return Velikost kroku zmeny uhlu.
     */
    public double getAngleStep(){
        return turtle.getAngleStep();
    }
    
    /**
     * Nastavi koeficient zmensovani segmentu.
     * @param coef 
     */
    public void setThicknessCoef(double coef){
        turtle.setThicknessCoef((float)coef);
    }
    
    /**
     * Vrati koeficient zmensovani segmentu.
     * @return Koeficient.
     */
    public double getThicknessCoef(){
        return turtle.getThicknessCoef();
    }
    
    /**
     * Vymaze vsechny uz vypocitane derivace a zacne odznova.
     * Vhodne pro nedeterministicke systemy.
     */
    public void clear(){
        derivations.clear();
        derivations.add(axiom);
        actDeriv = 0;
        computeCenter();
    }
    
    /**
     * Derivuje do daneho stupne a nastavi na tento stupen actDer. Vse provadi rekurzivne.
     * @param level Do jakeho stupne ma derivovat.
     * @throws LsysException Stala se nejaka chyba, uzivatel to udelal spatne.
     */
    public void derivateTo(int level) throws LsysException{       
        //pokud chce nejaky nesmyslny level tak nic nedelam
        if(level < 0){
            throw new LsysException("Derivovat do zapornych hodnot nejde.");
        }
        //pokud mi nejaka chybi tak derivuju
        if(level >= derivations.size()){
            
            //pokud mi jich chybi vice tak derivuju dal
            if(level > derivations.size()){
                derivateTo(level-1);
            }
            
            List<LRule> list; //pomocna promenna - pravidla k aktualnimu symbolu
            LRule actRule;  //aktualni pravidlo ktere pouziju
            double probability;

            derivations.add("");
            //pro kazdy symbol retezce
            for(int symbolPos=0;symbolPos<derivations.get(level-1).length();symbolPos++){
                //najdu si seznam pravidel
                list = rulesMap.get(derivations.get(level-1).charAt(symbolPos));
                if(list!=null){

                    probability = Math.random();
                    
                    //vyber pravidla ze seznamu - musi splnovat pravdepodobnost a kontext
                    actRule = null;
                    for(int j=0;j<list.size();j++){                            
                        //hledam takove pravidlo ktere splnuje podminky pravdepodobnosti a zaroven pro nej plati podminky kontextu
                        if(list.get(j).probBottom <= probability && 
                           probability <= list.get(j).probTop &&
                           hasPrecontext(derivations.get(level-1), symbolPos, list.get(j).preContext) &&
                           hasPostcontext(derivations.get(level-1), symbolPos, list.get(j).postContext)){
                                //ulozim odkaz na pravidlo ktery pak pouziju
                                actRule = list.get(j);
                                break;
                        } 
                    }

                    if(actRule==null){
                        //nenasel jsem zadne pravidlo takze pouziju identitu
                        derivations.set(level, derivations.get(level).concat(Character.toString(derivations.get(level-1).charAt(symbolPos))));
                    }
                    else{
                        //pridam pravou stranu aktivniho pravidla na konec derivovaneho retezce
                        derivations.set(level, derivations.get(level).concat(actRule.rightSide));
                    }
                }
                else{
                    //nemam pro takovy symbol zadne pravidlo takze pouziju identitu
                    derivations.set(level, derivations.get(level).concat(Character.toString(derivations.get(level-1).charAt(symbolPos))));
                }
            }//for kazdy symbol retezce
        }//if
        
        actDeriv=level;
        
        computeCenter();
    }
    
    /**
     * Krokovani derivaci pro tlacitko v GUI.
     * @throws LsysException 
     */
    public void derivateStep() throws LsysException{   
        derivateTo(actDeriv+1);
    }
    
    /**
     * Krokovani derivaci pro tlacitko v GUI.
     * @throws LsysException 
     */
    public void undoDerivateStep() throws LsysException{
        //pokud uz jsem na nule tak nic nedelam
        if(actDeriv<1){
            return;
        }
            derivateTo(actDeriv-1);
    }
   
    /**
     * Zkontroluje levy kontext. Jestli znaku v retezci predchazi retezec precont.
     * @param str Retezec nad kterym pracujeme.
     * @param actchar Pozice znaku u ktereho kontrolujeme kontext.
     * @param precont Levy kontext ktery tam ma byt.
     * @return TRUE pokud tam kontext je, FALSE pokud neni.
     */
    private boolean hasPrecontext(String str, int actchar,String precont){
                
//        System.out.print(">>"+str+"  "+actchar+"  "+precont+"  == ");
        
         if(precont.length()==0){
              return true;
         }
        //je tam nejaky prekontext
        //delka musi byt mensi nebo rovna delce retezce pred symbolem
        if(precont.length()<=actchar){
            
            int bracketcount=0;
            int actcontchar = precont.length()-1;
            //pro kazdy symbol kontextu hledam symbol
            while(actcontchar>=0){
                //
                actchar--;
                //hledam dalsi symbol
                for(;;){
                    //pokud uz nemam misto tak konec
                    if(actchar<0){
                        return false;
                    }
                    //pokud je to zavorka tak skacu
                    if(str.charAt(actchar) == ']'){
                        bracketcount++;
                        actchar--;
                        continue;
                    }
                    //pokud je to zavorka tak skacu
                    if(str.charAt(actchar) == '['){
                        bracketcount--;
                        actchar--;
                        continue;
                    }
                    //pokud jsem v zavorce nebo je to ignorovany symbol tak jedu dal
                    if(bracketcount>0 || ignoredSymbols.contains(str.substring(actchar,actchar+1))){
                        actchar--;
                    }
                    else{
                    //jinak skocim
                        break;
                    }
                }
                //nasel jsem symbol
                if(precont.charAt(actcontchar) == str.charAt(actchar)){
                    //skocim na dalsi
                    actcontchar--;
                }
                else{
                    return false;
                }
            }
            return true;
        }    
        return false;
    }   
    
    /**
    * Zkontroluje pravy kontext. Jestli po znaku v retezci nasleduje postcont.
    * Preskakuje ignorovane symboly a skace po vetvich.
    * @param str Retezec nad kterym pracujeme.
    * @param actchar Pozice znaku u ktereho kontrolujeme kontext.
    * @param postcont Pravy kontext ktery tam ma byt.
    * @return TRUE pokud tam kontext je, FALSE pokud neni.
    */
    private boolean hasPostcontext(String str, int actchar, String postcont){
                
//        System.out.print("<<"+str+"  "+actchar+"  "+postcont+"  == ");        
        
        if(postcont.length()==0){
            return true;
        }

        //je tam nejaky postkontext
        //zkontroluju delku
        if(postcont.length()<=str.length()-(actchar+1)){

            Integer actcontchar = new Integer(0); //aktivni symbol v kontextu
            Stack<Integer> stack = new Stack<>(); //zasobnik pro vetveni kontextu
            
            for(;;){
                actchar++;//posunu se na dalsi znak
                //pokud jsem za tak nemam
                if(actchar>=str.length()){
                    return false;
                }
                //zpracuju zavorku
                if(str.charAt(actchar) == '['){
                    //ulozim si soucasny symbol na zasobnik kvuli dalsim vetvim
                    stack.push(actcontchar);
                    continue;
                }
                //zpracuju zavorku
                if(str.charAt(actchar) == ']'){
                    //konec vetve, vratim si kontext
                    if(stack.empty()){
                        return false;
                    }
                    actcontchar=stack.pop();
                    continue;
                }
                //pokud tam je neco co ignoruju tak jedu dal
                if(ignoredSymbols.contains(str.substring(actchar,actchar+1))){
                   continue; 
                }
                //nasel jsem  nejaky symbol
                if(postcont.charAt(actcontchar) == str.charAt(actchar)){
                    //skocim na dalsi
                    actcontchar++;
                    if(actcontchar>=postcont.length()){
                        return true;
                    }
                }
                else{
                    //skocim na konec soucasne vetve nebo na konec retezce
                    while((actchar+1<str.length())&&(str.charAt(actchar+1) != ']')){
                        actchar++;
                    }
                    continue;
                }                
            }//for
        }//if   
        return false;
    }
    
    /**
     * Pomocna funkce pro ulozeni vertexu v opengl.
     * @param gl2 Kontext.
     * @param tuple Vektor nebo bod.
     */
    private void vertex(GL2 gl2,Tuple3d tuple){
        gl2.glVertex3f((float)tuple.x,(float)tuple.y,(float)tuple.z);
    }
    
    /**
     * Nastavi normalu vrcholu. Pomocna funkce.
     * @param gl2 Kontext.
     * @param tuple Vektor nebo bod.
     */
    private void normal(GL2 gl2,Tuple3d tuple){
        gl2.glNormal3f((float)tuple.x,(float)tuple.y,(float)tuple.z);
    }
    
    /**
     * Nastavi barvu v JOGL.
     * @param gl2 Kontext.
     * @param color Barva segmentu.
     * @param type 0 - valec, 1 - list(polygon)
     */
    private void glColor(GL2 gl2, Color color, int type){

        float[]  col = {color.getColorComponents(floats)[0],
                        color.getColorComponents(floats)[1],
                        color.getColorComponents(floats)[2],
                        1.0f};
        
        if(type == 0){
        gl2.glMaterialfv(GL2.GL_FRONT,GL2.GL_DIFFUSE,col,0);
        col[0] = 0.3f;
        col[1] = 0.3f;
        col[2] = 0.3f;
        gl2.glMaterialfv(GL2.GL_FRONT,GL2.GL_AMBIENT,col,0);
        }
        else{
        gl2.glMaterialfv(GL2.GL_FRONT,GL2.GL_AMBIENT,col,0);
        col[0] = 0.3f;
        col[1] = 0.3f;
        col[2] = 0.3f;
        gl2.glMaterialfv(GL2.GL_FRONT,GL2.GL_DIFFUSE,col,0);
        }
    }
    
    /**
     * Vola se v inicializacni fazi. Nic nedela.
     */
    public void init(){
        
    }
    
    /**
     * Automat, ktery interpretuje aktivni derivaci.
     * @param gl2 Kontext.
     */
    public void render(GL2 gl2){
        
        //veskere nastavovani bude mimo
        //inicializace zelvy pred novym vykreslovanim
        turtle.init();
        
        String str = derivations.get(actDeriv);
        int actchar = 0;
        float pom;
        int actcharpom;
        int bracketCount = 0;
        Point3d point;
        ArrayList<Point3d> leaf = new ArrayList<>();
        ArrayList<Color> colors = new ArrayList<>();
        
        //nastaveni pocatecni barvy
        glColor(gl2,turtle.getColor(),0);
        
        //pro kazdy znak
        while(actchar<str.length()){
            
            switch(str.charAt(actchar)){
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                            point=(Point3d)turtle.getPos().clone();
                            turtle.goForward();
                            drawLine(gl2,point,turtle.getPos());                  
                    break;
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                        turtle.goForward();
                    break;
                case '+':
                        turtle.rotTurn(true);
                    break;
                case '-':
                        turtle.rotTurn(false);
                    break;
                case '&':
                        turtle.rotPitch(true);
                    break;
                case '^':
                        turtle.rotPitch(false);
                    break;
                case '\\': 
                        turtle.rotRoll(true);
                    break;
                case '/': 
                        turtle.rotRoll(false);
                    break;
                case '|':
                        pom=turtle.getAngleStep();
                        turtle.setAngleStep(180.0f);
                        turtle.rotTurn(true);
                        turtle.setAngleStep(pom);
                    break;
                case '[':
                        turtleStack.push(new Turtle(turtle));
                    break;
                case ']':
                        turtle=turtleStack.pop();
                        //barva je nastavena v OpenGL takze ji musim vratit
                        glColor(gl2,turtle.getColor(),0);
                    break;
                case '\'':
                        turtle.colorInc();
                        glColor(gl2,turtle.getColor(),0);
                    break;
                case ',':
                        turtle.colorDec();
                        glColor(gl2,turtle.getColor(),0);
                    break;
                case '{':
                        leaf.clear();
                    break;
                case '}':
                        drawLeaf(gl2,leaf,colors);
                    break;
                case '.':
                        leaf.add((Point3d)turtle.getPos().clone());
                        colors.add(turtle.getColor());
                    break;
                case '!':
                        turtle.reduceThickness();
                    break;
                case '%':
                        //odriznuti konce vetve
                        //najdu ] ktera nema spolecnou [ nebo konec                    
                        for(actcharpom=actchar;actcharpom<str.length() && (str.charAt(actcharpom)!=']' || bracketCount!=0);actcharpom++){
                            if(str.charAt(actcharpom)=='['){
                                bracketCount++;
                            }
                        }
                        if(actcharpom==str.length()){
                            //konec
                            str = str.substring(0,actchar);
                        }
                        else{
                            //]
                            str = str.substring(0, actchar).concat(str.substring(actcharpom));
                        }
                        //nastavim orezany string na puvodni verzi
                        derivations.set(actDeriv,str);
                    break;
                default:
                    break;
            } //konec switche
            actchar++;
        }//konec whilu

        gl2.glFlush();
    }

    /**
     * Nakresli caru z bodu do bodu.
     * @param gl2 Kontext
     * @param from Bod z ktereho, kreslim.
     * @param to Bod kam kreslim.
     */
    private void drawLine(GL2 gl2,Point3d from, Point3d to){
        
        //provadim vlastni transformace takze nechci nic rozhodit
        gl2.glPushMatrix();
                
        ///http://www.thjsmith.com/40/cylinder-between-two-points-opengl-c
        
        //vektor podle ktereho chci aby byl valec
        Vector3d vect= (Vector3d)turtle.getHead().clone();
        //vektorovy soucin s Z-osou
        Vector3d cross = new Vector3d();
        cross.cross(new Vector3d(0.0,0.0,1.0),vect);
        //uhel 
        double angle = 180/Math.PI * Math.acos(vect.dot(new Vector3d(0.0,0.0,1.0))/vect.length());
                
        //transformace na spravne misto 
        gl2.glTranslated(from.x, from.y, from.z);
        gl2.glRotated(angle,cross.x,cross.y,cross.z);        
        
        //kontrola jestli tloustka neni nulova
        double thickness = turtle.getThickness()!=0.0?turtle.getThickness():0.005;
        
        //spocitam vrcholy valce zakladniho valce
        HashMap<String,ArrayList<MyPoint3d>> cylinder = generateCylinder(thickness, 1.0, 20);
        //rozhodim do dvou seznamu
        ArrayList<MyPoint3d> downpoints = cylinder.get("down");
        ArrayList<MyPoint3d> uppoints = cylinder.get("up");
        
        if(turtle.downCylinder!=null){
            downpoints = turtle.downCylinder;
        }
        
        //vykresleni samotneho valce
        MyPoint3d point;
        gl2.glBegin(GL2.GL_QUAD_STRIP);
        //vsechny je postupne vykreslim
        Iterator<MyPoint3d> i=downpoints.iterator();
        Iterator<MyPoint3d> j=uppoints.iterator();
        while(i.hasNext() && j.hasNext()){
            point = i.next();
            normal(gl2, point.normal);
            vertex(gl2, point);
            point = j.next();
            normal(gl2, point.normal);
            vertex(gl2, point);
        }
        gl2.glEnd();
       
        //>>vykreslim spodek a vrsek valce
        gl2.glBegin(GL2.GL_POLYGON);
        i=downpoints.iterator();
        j=uppoints.iterator();
        while(i.hasNext()){
            point=i.next();
            normal(gl2,new Point3d(0.0,0.0,-1.0));
            vertex(gl2,point);
        }
        gl2.glEnd();
        gl2.glBegin(GL2.GL_POLYGON);
        while(j.hasNext()){
            point=j.next();
            normal(gl2,new Point3d(0.0,0.0,1.0));         
            vertex(gl2,point);
        }
        gl2.glEnd();
        //<<
        
        //horni kruh bodu pouziju priste  pro navazovani zmensenych segmentu
        //pouziju ho ale jako dolni takze ho musim dostat na zem
        for(j=uppoints.iterator();j.hasNext();){
           j.next().z=0.0;
        }
        //ulozim do zelvy jako misto na ktere navazuju
        turtle.downCylinder = uppoints;
        
        //vratim matici zpatky
        gl2.glPopMatrix();
        
    }
    
    /**
     * Vygeneruje seznam bodu pro valec, ktery lze pak vykreslit pomoci QUAD_STRIP.
     * QUAD_STRIP 
     * Valec zacina v pocatku a jeho osou je osa Z.
     * @param width  Sirka valce.
     * @param height Vyska valce.
     * @param slices Pocet obdelniku na tele valce.
     * @return Jako dva seznamy pod klici down a up.
     */
    private HashMap<String,ArrayList<MyPoint3d>> generateCylinder(double width,double height, int slices){
        
        ArrayList<MyPoint3d> downpoints = new ArrayList<>();
        ArrayList<MyPoint3d> uppoints = new ArrayList<>();
        
        double anglestep=2*Math.PI/slices;
        double angle=0.0;
        double x,y;
        MyPoint3d point;
        
        for(int i=0;i<slices;i++){
            
            //spodni bod
            point = new MyPoint3d();
            point.normal.x = Math.cos(angle);
            point.normal.y = Math.sin(angle);
            point.normal.z = 0.0;
            point.x = width*point.normal.x;
            point.y = width*point.normal.y;
            point.z = 0.0;
            downpoints.add((MyPoint3d)point.clone());
            //horni bod
            point.z = height;
            uppoints.add((MyPoint3d)point.clone());
            
            angle+=anglestep;
        }
        //zduplikuju prvni dva aby byl cely
        downpoints.add(downpoints.get(0));
        uppoints.add(uppoints.get(0));
        
        HashMap<String,ArrayList<MyPoint3d>> ret = new HashMap<>();
        ret.put("down", downpoints);
        ret.put("up", uppoints);
        
        return ret;
    }
    
    /**
     * Vykresli polygon listu i s normalami, ktere nejdrive spocita.
     * @param leaf Vrcholy polygonu.
     */
    private void drawLeaf(GL2 gl2,ArrayList<Point3d> leaf, ArrayList<Color> colors){
                
       Point3d cent = new Point3d(0,0,0);
        Vector3d centernormal = new Vector3d();
        Vector3d vec1;
        Vector3d vec2;
        Point3d point;
        Color color;
        
        for(Point3d p : leaf){
            cent.add(p);
        }
        cent.x/=leaf.size();
        cent.y/=leaf.size();
        cent.z/=leaf.size();
        //mam souradnice stredu
        
        vec1=new Vector3d(leaf.get(0).x-cent.x,
                          leaf.get(0).y-cent.y,
                          leaf.get(0).z-cent.z);
        vec2=new Vector3d(leaf.get(1).x-cent.x,
                          leaf.get(1).y-cent.y,
                          leaf.get(1).z-cent.z);
        centernormal.cross(vec2, vec1);
        //mam normalu stredu
        
        //kreslim
        gl2.glBegin(GL2.GL_POLYGON);
                
        for(int i = 0;i<leaf.size();i++){
            point=leaf.get(i);
            color=colors.get(i);
            
            //spocitam normalu v bodu
            vec1=new Vector3d((point.x-cent.x)/2,
                              (point.y-cent.y)/2,
                              (point.z-cent.z)/2);
            vec1.add(centernormal);
            vec1.normalize();

//            //spocitam normalu v bodu
//            vec1=new Vector3d(point.x-cent.x,
//                              point.y-cent.y,
//                              point.z-cent.z);
//            vec1.normalize();
            
            //nastavim barvu
            glColor(gl2,colors.get(i),1);
            //mam normalu v bodu
            normal(gl2,vec1);
            //vzkreslim bod
            vertex(gl2,point);
        }
        gl2.glEnd();
        
    }   
    
    /**
     * Getter pro axiom.
     * @return the axiom
     */
    public String getAxiom() {
        return axiom;
    }

    /**
     * Setter pro axiom.
     * @param axiom the axiom to set
     */
    public void setAxiom(String axiom) {
        this.axiom = axiom;
        clear();
    }

    /**
     * Vrati puvodni retezec ktery se pouzil pro rozparsovani pravidel.
     * @return Retezec.
     */
    public String getRules() {
        return rules;
    }

    /**
     * Rozparsuje retezec pravidel. Pravidla maji presne dany tvar, ktery lze zpracovat
     * pomoci regularnich vyrazu. Vysledek ulozi to promenne rulesMap, kterou 
     * system pouziva pro vykreslovani modelu.
     * @param rules Retezec pravidel.
     * @throws LsysException
     */
    public void setRules(String rules) throws LsysException {
        //pokud jsou od posledne stejne tak nic nedelam
        if(rules.equals(this.rules)){
            return;
        }
        
        rulesMap = new HashMap<>();
        
        //smaze vsechny tabulatory a mezery a prazdne radky
        String rulesNoSpaces = rules.replaceAll("[ \t]","").replaceAll("\n{2,}","\n");
        
        //pokud tam nic neni tak skoncim protoze mam hotovo
        if(rulesNoSpaces.equals("")){
            return;
        }
        
        //rozdeli retezec na jednotlive radky na ktere budu aplikovat RE
        String[] matcharray = rulesNoSpaces.split("\n");
        
        //moje abeceda symbolu ktere mohou byt v pravidle
        String symbol = "[-a-zA-Z0-9+\\\\/&^|\\[\\]',{}.!%]";
        //jednotlive casti jednoho pravidla
        String precontextRE = "^(?:(?:("+symbol+"*)<)|(?:b{0}))";
        String leftsideRE = "("+symbol+")";
        String postcontextRE = "(?:(?:>("+symbol+"*))|(?:b{0}))";
        String probabilityRE = "=(?:([0-9]*(?:[,.][0-9]+)?)=)?";
        String rightsideRE = "("+symbol+"*)$";
        
        //promenne udrzujici vysledky
        String precontext;
        Character leftside;
        String postcontext;
        String probability;
        String rightside;
        
        //pomocne promenne pro ulozeni vysledku do spravneho tvaru
        List<LRule> list;
        LRule rule;
        //promenna pro zpracovani pravdepodobnosti
        Map<String,List<LRule>> probabilityMap = new HashMap<>();      
        
        
        //kompilovany RE
        Pattern pattern = Pattern.compile(precontextRE+leftsideRE+postcontextRE+probabilityRE+rightsideRE);
        Matcher matcher;
        
        //pro kazdy radek
        for(int i=0;i<matcharray.length;i++){
            //zkusim napasovat regularni vyraz
            matcher = pattern.matcher(matcharray[i]);
            //pokud jsem neco nasel tak to zpracuju
            if(matcher.find()){ 
                    //prekopiruju vysledky do mych promennych do tvaru kteremu rozumim
                    if(matcher.start(1)==matcher.end(1)){
                        precontext="";
                    }
                    else{
                        precontext=matcher.group(1);
                    }
                    leftside=new Character(matcher.group(2).charAt(0));
                    if(matcher.start(3)==matcher.end(3)){
                        postcontext="";
                    }
                    else{
                        postcontext=matcher.group(3);
                    }                
                    if(matcher.start(4)==matcher.end(4)){
                        probability="";
                    }
                    else{
                        probability=matcher.group(4);
                        //nahradim desetinnou carku za anglickou tecku
                        probability = probability.replace(',', '.');
                    }            
                    if(matcher.start(5)==matcher.end(5)){
                        rightside="";
                    }
                    else{
                        rightside=matcher.group(5);
                    }   
                    
                    //vytvorim z vysledku objekt pravidla
                    //pokud nemam pravdepodobnost tak jim dam defaultni
                    if(probability.equals("")){
                        rule = new LRule(precontext, leftside.charValue(), postcontext, rightside, 0.0f, 0.0f);
                    }
                    else{
                        rule = new LRule(precontext, leftside.charValue(), postcontext, rightside, Float.valueOf(probability), 0.0f);
                    }
                    
                    //pokusim se vysledek ulozit do mapy pravidel
                    //pokud tam nemam klic tak ho vytvorim
                    if(!rulesMap.containsKey(leftside)){
                        list = new ArrayList<>();
                        rulesMap.put(leftside, list);
                    }
                    list=rulesMap.get(leftside);
                    //pridam pravidlo do mapy
                    list.add(rule);
                    
                    //dalsi slovnik kde jsou klice cely retezec precontext+leftside.toString()+postcontext
                    //tento retezec se musi podelit o pravdepodobnost takze pak zpracovavam seznamy
                    if(!probabilityMap.containsKey(precontext+leftside.toString()+postcontext)){
                        list = new ArrayList<>();
                        probabilityMap.put(precontext+leftside.toString()+postcontext, list);
                    }
                    list=probabilityMap.get(precontext+leftside.toString()+postcontext);
                    //pridam ho do pravdepodobnostniho seznamu
                    if(probability.equals("")){
                        //pokud nema pravdepodobnost tak ho dam na konec
                        list.add(rule);
                    }
                    else{
                        //pravidla s pravdepodobnosti davam na zacatek
                        list.add(0, rule);                        
                    }
            }
            else{
                //RE nepasuje takze je neco spatne
               throw new LsysException("Spatne pravidlo na radku "+(i+1)+".");
            }
        }
        
//        printRules();
        
        //vypocitam pravdepodobnosti
        processProbability(probabilityMap);

//        printRules();
        
        //ulozim si retezec pravidel do tech ktere mam zpracovane
        this.rules=rules;
    }
    
    /**
     * Vytiskne pravidla.
     * @param rm Asociativni pole seznamu pravidel.
     */
    public void printRules(){ 
        
        System.out.println("________Pravidla_____");
        //pro kazdy seznam v mape
        for(List<LRule> listrules : rulesMap.values()){
            //pro kazde pravidlo v seznamu
            for(LRule rule : listrules){
                //vytisknu ho
                System.out.println(rule.toString());
            }
        } 
        System.out.println("--------------------");
    }

    /**
     * Vypocita pravdepodobnosti celych sad pravidel.
     * Klicem je retezec z precontext+lside+postcontext. Pravdepodobnosti pravidel jsou
     * v probBottom, jestli nema zadanou pravdepodobnost tak 0.0.
     * @param probabilityMap Mapa seznamu pravidel ktere maji spolecnou pravdepodobnost. Jednotlive polozky
     * jsou odkazy na pravidla v rulesMap, ktera se pouziva pro praci.
     * @throws LsysException
     */
    private void processProbability(Map<String, List<LRule>> probabilityMap) throws LsysException {

        int index;
        float step;
        
        for(List<LRule> listrules : probabilityMap.values()){
            index=0;
            //zpracovavam ty ktere maji pravdepodobnost
            for(;index<listrules.size() && listrules.get(index).probBottom!=0.0f;index++){
                //prvni polozku musim udelat jinak
                if(index==0){
                    listrules.get(index).probTop=listrules.get(index).probBottom;
                    listrules.get(index).probBottom=0.0f;
                }
                else{
                    listrules.get(index).probTop=listrules.get(index).probBottom+listrules.get(index-1).probTop;
                    listrules.get(index).probBottom=listrules.get(index-1).probTop;
                }
            }
            //pokud presahli nebo nedosahli pravdepodobnost tak je chyba
            if((index>0 && listrules.get(index-1).probTop>1.0f)||
               (index==listrules.size() && listrules.get(index-1).probTop!=1.0f)){
                    throw new LsysException("Spatne zadane pravdepodobnosti u pravidel se symbolem "+listrules.get(0).leftSide+".");
            }
            //zpracovavam ty ktere nemaji pravdepodobnost
            if(index<listrules.size()){
                //pokud presahli pravdepodobnost tak je chyba
                if(listrules.get(index).probTop==1.0f){
                        throw new LsysException("Pro nektera pravidla u symbolu "+listrules.get(0).leftSide+" chybi pravdepodobnost.");
                }
                //vypocet kroku pravdepodobnosti
                if(index==0){
                    step=1.0f/listrules.size();
                }
                else{
                    step=(1.0f-listrules.get(index-1).probTop)/(listrules.size()-index);
                }
                
                //ostatni maji stejnou pravdepodobnost
                for(int i=index;i<listrules.size();i++){
                    //pro uplne prvni prvek to musime udelat jinak
                    if(i==0){
                        listrules.get(i).probBottom=0.0f;
                        listrules.get(i).probTop=step;
                    }
                    else{
                        listrules.get(i).probBottom=listrules.get(i-1).probTop;
                        listrules.get(i).probTop=listrules.get(i).probBottom+step;
                    }
                }
            }
        }
    }

    /**
     * Vrati symboly ktere se ignoruji pri hledani kontextu.
     * @return Ignorovane symboly.
     */
    public String getIgnoredSymbols() {
        return ignoredSymbols;
    }

    /**
     * Nastavi symboly ktere se ignoruji pri hledani kontextu.
     * @param ignoredSymbols Retezec symbolu, ktere mam ignorovat.
     */
    public void setIgnoredSymbols(String ignoredSymbols) {
        this.ignoredSymbols = ignoredSymbols;
    }

    /**
     * Vypocita stred modelu, kolem ktereho se budou delat rotace.
     */
    private void computeCenter() {
        
        turtle.init();
        
        Point3d max=new Point3d(0,0,0),
                min=new Point3d(0,0,0);
           
        String str = derivations.get(actDeriv);
        int actchar = 0;
        float pom;
        int actcharpom;
        int bracketCount = 0;

        //pro kazdy znak
        while(actchar<str.length()){
            
            switch(str.charAt(actchar)){
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                        turtle.goForward();
                            
                        max.x= turtle.getPos().x>max.x ?turtle.getPos().x : max.x;
                        max.y= turtle.getPos().y>max.y ?turtle.getPos().y : max.y;
                        max.z= turtle.getPos().z>max.z ?turtle.getPos().z : max.z;
                        min.x= turtle.getPos().x<min.x ?turtle.getPos().x : min.x;
                        min.y= turtle.getPos().y<min.y ?turtle.getPos().y : min.y;
                        min.z= turtle.getPos().z<min.z ?turtle.getPos().z : min.z;
                    break;
                case '+':
                        turtle.rotTurn(true);
                    break;
                case '-':
                        turtle.rotTurn(false);
                    break;
                case '&':
                        turtle.rotPitch(true);
                    break;
                case '^':
                        turtle.rotPitch(false);
                    break;
                case '\\': 
                        turtle.rotRoll(true);
                    break;
                case '/': 
                        turtle.rotRoll(false);
                    break;
                case '|':
                        pom=turtle.getAngleStep();
                        turtle.setAngleStep(180.0f);
                        turtle.rotTurn(true);
                        turtle.setAngleStep(pom);
                    break;
                case '[':
                        turtleStack.push(new Turtle(turtle));
                    break;
                case ']':
                        turtle=turtleStack.pop();
                    break;
                case '.':
                        max.x= turtle.getPos().x>max.x ?turtle.getPos().x : max.x;
                        max.y= turtle.getPos().y>max.y ?turtle.getPos().y : max.y;
                        max.z= turtle.getPos().z>max.z ?turtle.getPos().z : max.z;
                        min.x= turtle.getPos().x<min.x ?turtle.getPos().x : min.x;
                        min.y= turtle.getPos().y<min.y ?turtle.getPos().y : min.y;
                        min.z= turtle.getPos().z<min.z ?turtle.getPos().z : min.z;                   
                    break;
                default:
                    break;
            } //konec switche
            actchar++;
        }//konec whilu
        
        center.add(actDeriv, new Point3d((max.x+min.x)/2.0,(max.y+min.y)/2.0,(max.z+min.z)/2.0));    
    }

    /**
     * Vrati souradnice stredu modelu pro rotace.
     * @return Souradnice stredu.
     */
    public Point3d getCenter(){
        return center.get(actDeriv);

    }

    /**
     * Vrati retezec aktivni derivace, ktery se vykresluje.
     * @return 
     */
    public String getActiveDerivation(){
        return derivations.get(actDeriv);
    }
    
    
    /**
     * Nastavi tloustku segmentu.
     * @param d  Sirka.
     */
    public void setThickness(double d){
        turtle.setThicknessStart((float)d);
    }

    /**
     * Vrati pocatecni sirku segmentu.
     * @return POcatecni sirka segmentu.
     */
    public double getThickness(){
        return (double)turtle.getThicknessStart();
    }
    
    /**
     * Getter pro barvu.
     * @return the colorSet
     */
    public int getColorSet() {
        return turtle.getColorSet()==Turtle.COLORSET_BASIC ? COLORSET_BASIC
                                                           : COLORSET_EXTENDED;
    }

    /**
     * Setter pro barvu.
     * @param colorSet the colorSet to set
     */
    public void setColorSet(int colorSet) {
        if(colorSet==COLORSET_BASIC){
            turtle.setColorSet(Turtle.COLORSET_BASIC);
        }
        else{
            turtle.setColorSet(Turtle.COLORSET_EXTENDED);
        }
    }
    
    /**
     * Prevede LSystem do xml vhodneho pro ulozeni do souboru.
     * @return Retezec s xml.
     */
    public String toXML() throws LsysException{
        
        Document doc = DocumentHelper.createDocument();
        Element elem = doc.addElement("lsystem");
        
        elem.addElement("axiom").addText(axiom);
        elem.addElement("angle").addText(Float.toString(turtle.getAngleStep()));
        elem.addElement("thickness").addText(Float.toString(turtle.getThicknessStart()));
        elem.addElement("thicknessCoef").addText(Float.toString(turtle.getThicknessCoef()));
        elem.addElement("colorset").addText(turtle.getColorSet()==Turtle.COLORSET_BASIC?"basic":"extended");
        elem.addElement("contextIgnored").addText(ignoredSymbols);
        elem = elem.addElement("rules");
        
        for(List<LRule> list : rulesMap.values()){
            for(LRule rule : list){
                elem.addElement("rule")
                    .addAttribute("precontext",rule.preContext)
                    .addAttribute("lside",Character.toString(rule.leftSide))
                    .addAttribute("postcontext",rule.postContext)
                    .addAttribute("rside",rule.rightSide)
                    .addAttribute("probability",Float.toString(rule.probTop-rule.probBottom));
            }
        }
        
//        return doc.asXML();
        
        //udela pekne formatovane xml
        OutputFormat format = OutputFormat.createPrettyPrint();
        StringWriter wr = new StringWriter();   //stream do retezce
        XMLWriter writer = new XMLWriter(wr,format);
        try {
                writer.write(doc);
                writer.close();
        } catch (IOException e) {
            throw new LsysException("Nepovedlo se prevest do XML.");
        }
        
        return wr.toString();
    }
    
    /**
     * Rozparsuje xml Lsystem a inicializuje jim tento LSystem.
     * @param xml Retezec s hodnotami.
     * @throws LsysException Pokud je neco spatne.
     */
    public void fromXML(String xml) throws LsysException{
        Document doc;
        try {
            doc = DocumentHelper.parseText(xml);
        } catch (DocumentException ex) {
            throw new LsysException("Nepovedlo se rozparsovat z XML.");
        }
        
        
        //inicializace pravidel
        rulesMap = new HashMap<>();
        Map<String, List<LRule>> probabilityMap = new HashMap<>();
        LRule rule;
        
        Element root = doc.getRootElement();
        Element el;
        Element elem;
        boolean hadAxiom = false;
        boolean hadAngle = false;
        boolean hadThickness = false;
        boolean hadThicknessCoef = false;
        boolean hadColorset = false;
        boolean hadRules = false;
        boolean hadIgnored = false;
        
        
        for(Iterator i = root.elementIterator(); i.hasNext();){
            el = (Element) i.next();
            
            switch (el.getQualifiedName()) {
                case "rules":
                    if(!hadRules){
                        hadRules = true;
                        
                        //zpracuju pravidlo 
                        for(Iterator j = el.elementIterator();j.hasNext();){
                            elem = (Element) j.next();
                            rule = new LRule(elem.attributeValue("precontext"),
                                             elem.attributeValue("lside").charAt(0),
                                             elem.attributeValue("postcontext"),
                                             elem.attributeValue("rside"),
                                             Float.valueOf(elem.attributeValue("probability")),
                                             0.0f);
                            
                            //pridam ho do mapy pravidel
                            if(rulesMap.containsKey(rule.leftSide)){
                                rulesMap.get(rule.leftSide).add(rule);
                            }
                            else{
                                rulesMap.put(rule.leftSide, new ArrayList<LRule>());
                                rulesMap.get(rule.leftSide).add(rule);
                            }
                            
                            //odkaz dam i do mapy pravidel pro pravdepodobnosti
                            if(probabilityMap.containsKey(rule.preContext+rule.leftSide+rule.postContext)){
                                probabilityMap.get(rule.preContext+rule.leftSide+rule.postContext).add(rule);
                            }
                            else{
                                probabilityMap.put(rule.preContext+rule.leftSide+rule.postContext,new ArrayList<LRule>());
                                probabilityMap.get(rule.preContext+rule.leftSide+rule.postContext).add(rule);
                            }
                        }    
                        
                        //vyresim pravdepodobnosti
                        processProbability(probabilityMap);
                        
                    }
                    else{
                        throw new LsysException("Pri parsovani se objevily dva elementy s pravidly.");
                    }
                    break;
                case "axiom":
                    if(!hadAxiom){
                        hadAxiom = true;
                        setAxiom(el.getText());
                    }
                    else{
                        throw new LsysException("Pri parsovani se objevily dva axiomy.");
                    }
                    break;
                case "angle":
                    if(!hadAngle){
                        hadAngle = true;
                        turtle.setAngleStep(Float.valueOf(el.getText()));
                    }
                    else{
                        throw new LsysException("Pri parsovani se objevily dva uhly.");
                    }
                    break;
                case "thickness":
                    if(!hadThickness){
                        hadThickness = true;
                        turtle.setThicknessStart(Float.valueOf(el.getText()));
                    }
                    else{
                        throw new LsysException("Pri parsovani se objevily dve pocatecni sirky.");
                    }
                    break;
                case "thicknessCoef":
                    if(!hadThicknessCoef){
                        hadThicknessCoef = true;
                        turtle.setThicknessCoef(Float.valueOf(el.getText()));
                    }
                    else{
                        throw new LsysException("Pri parsovani se objevily dva koeficienty zmensovani.");
                    }
                    break;
                case "colorset":
                    if(!hadColorset){
                        hadColorset = true;
                        int colorset;
                        if(el.getText().equals("basic")){
                            colorset = Turtle.COLORSET_BASIC;
                        }
                        else if(el.getText().equals("extended")){
                            colorset = Turtle.COLORSET_EXTENDED;
                        }
                        else{
                            throw new LsysException("Neznama polozka v colorset v XML.");
                        }
                        turtle.setColorSet(colorset);
                    }
                    else{
                        throw new LsysException("Pri parsovani se objevily dve mnoziny barev.");
                    }
                    break;
                case "contextIgnored":
                    if(!hadIgnored){
                        hadIgnored = true;
                        ignoredSymbols = el.getText();
                    }
                    else{
                        throw new LsysException("Pri parsovani se objevily dve skupiny ignorovanych symbolu.");
                    }
                    break;
                default:
                    throw new LsysException("Neznama polozka v XML.");
            }
                
        }
        
        //musim jeste presunout pravidla do stringu aby se vypsala
        rules = "";
        //pro kazdy seznam v mape
        for(List<LRule> lr : rulesMap.values()){
            //pro kazde pravidlo v seznamu
            for(LRule r: lr){
                rules+=r.preContext.equals("")?"":r.preContext+" < ";
                rules+=r.leftSide;
                rules+=r.postContext.equals("")?"":" > "+r.postContext;
                rules+=" ="+(r.probTop-r.probBottom)+"= "+r.rightSide+"\n";
            }
        }
        
    }
}
