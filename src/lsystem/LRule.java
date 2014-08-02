package lsystem;

/**
 *  Trida pro pravidlo Lsystemu. Obsahuje vsechny casti, ktere pravidlo muze mit.
 * @author Jakub Kvita
 */
public class LRule {
    /**
     *  Retezec leveho kontextu u pravidla.
     */
    public final String preContext; 
    /**
     * Retezec praveho kontextu u pravidla.
     */
    public final String postContext;
    /**
     * Leva strana pravidla.
     */
    public final char leftSide;
    /**
     * Prava strana pravidla.
     */
    public final String rightSide;
    /**
     * Spodni hladina intervalu pro pravděpodobnost.
     */
    public float probBottom;
    /**
     * Horni hladina intervalu pro pravdepodobnost.
     */
    public float probTop;
    
    /**
     *  Nejjednodussi pravidlo.
     * @param leftSide Leva strana pravidla - jeden symbol.
     * @param rightSide Prava strana - na co se prepise.
     */
    public LRule(char leftSide, String rightSide){
        this.preContext = "";
        this.leftSide = leftSide;
        this.postContext = ""; 
        this.rightSide = rightSide;
        this.probBottom = 0.0f;
        this.probTop = 1.0f;
    }
    
    /**
     *  Pravidlo s kontextem.
     * @param preContext Levy kontext.
     * @param leftSide  Leva strana pravidla - jeden symbol.
     * @param postContext Pravy kontext.
     * @param rightSide  Prava strana - na co se prepise.
     */
    public LRule(String preContext, char leftSide, String postContext, String rightSide){
        this.preContext = preContext;
        this.leftSide = leftSide;
        this.postContext = postContext;
        this.rightSide = rightSide;
        this.probBottom = 0.0f;
        this.probTop = 1.0f;        
    }
    
    /**
     * Pravidlo se vsemi castmi.
     * @param preContext Levy kontext.
     * @param leftSide  Leva strana pravidla - jeden symbol.
     * @param postContext Pravy kontext.
     * @param rightSide  Prava strana - na co se prepise.
     * @param pBottom Spodni hranice pravdepodobnosti.
     * @param pTop Horni hranice pravdepodobnosti.
     */
    public LRule(String preContext, char leftSide, String postContext, String rightSide, float pBottom, float pTop){
        this.preContext = preContext;
        this.leftSide = leftSide;
        this.postContext = postContext;
        this.rightSide = rightSide;
        this.probBottom = pBottom;
        this.probTop = pTop;
    }
    
    /**
     * Vypise pravidlo v jednotnem formatu pro vsechno.
     * @return Pravidlo.
     */
    @Override
    public String toString(){
        
        String result="";
        
        if(!preContext.equals("")){
            result=result.concat(preContext+" <");
        }
        result=result.concat(Character.toString(leftSide));
        if(!postContext.equals("")){
            result=result.concat("> "+postContext);
        }
        result=result.concat(" ="+probBottom+"  "+probTop+"= ");
//        result=result.concat(" ="+(probTop-probBottom)+"= ");
        result=result.concat(rightSide);
        
        return result;
    }
}
