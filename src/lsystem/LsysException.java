package lsystem;

/**
 *  Vyjimky generovane v Lsystemu kvuli chybam uzivatele.
 * @author Jakub Kvita
 */
public class LsysException extends Exception{
    
    /**
     * Konstruktor.
     */
    public LsysException() {
       super();
    }
    /**
     * Konstruktor se zpravou.
     * @param a Zprava.
     */
    public LsysException(String a) {
       super(a);
    }
}
