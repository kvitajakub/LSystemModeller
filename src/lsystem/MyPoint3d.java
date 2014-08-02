package lsystem;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Bod v prostoru spolecne s normalou k povrchu objektu, ktereho je bod soucasti.
 * @author Jakub Kvita
 */
public class MyPoint3d extends Point3d {

    /**
     * Vektor normaly.
     */
    public Vector3d normal;
    
    public MyPoint3d(double d, double d1, double d2) {
        super(d, d1, d2);
        normal = new Vector3d();
    }

    public MyPoint3d(double[] doubles) {
        super(doubles);
        normal = new Vector3d();
    }

    public MyPoint3d(Point3d pntd) {
        super(pntd);
        normal = new Vector3d();
    }

    public MyPoint3d() {
        normal = new Vector3d();
    }
    
}
