/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lmodeler;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point2d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

/**
 *
 * @author Jakub Kvita
 */
public class ArcBall {

    private double width;
    private double height;
    
    private static final float Epsilon = 1.0e-5f;
    
    private Vector4d quaternion = new Vector4d();
    private double[] transformarray = new double[16];
    private Matrix4d rotations = new Matrix4d(1.0, 0.0, 0.0, 0.0,
                                           0.0, 1.0, 0.0, 0.0,
                                           0.0, 0.0, 1.0, 0.0,
                                           0.0, 0.0, 0.0, 1.0);
    private Matrix4d activeRotation = new Matrix4d(1.0, 0.0, 0.0, 0.0,
                                           0.0, 1.0, 0.0, 0.0,
                                           0.0, 0.0, 1.0, 0.0,
                                           0.0, 0.0, 0.0, 1.0);
    private Vector3d beginVector = new Vector3d(0.0,0.0,0.0);
    private Vector3d endVector = new Vector3d(0.0,0.0,0.0);

    
    /**
     * Kostruktor.
     * @param width Sirka okna.
     * @param height Vyska okna.
     */
    public ArcBall(int width, int height){
        this.setBounds(width, height);   
    }
    
    /**
     * Zacatek tazeni.
     * @param click Souradnice mysi.
     */
    public void initDrag(Point2d click){
        
        beginVector = mapToSphere(click);
    }
    
    /**
     * Tazeni.
     * @param drag Koncove souradnice mysi.
     */
    public void drag(Point2d drag){
        
        endVector = mapToSphere(drag);
        
        //Return the quaternion equivalent to the rotation
        if (quaternion != null) {
            Vector3d perp = new Vector3d();

            //Compute the vector perpendicular to the begin and end vectors
            perp.cross(beginVector, endVector);

            //Compute the length of the perpendicular vector
            if (perp.length() > Epsilon)    //if its non-zero
            {
                //We're ok, so return the perpendicular vector as the transform after all
                quaternion.x = perp.x;
                quaternion.y = perp.y;
                quaternion.z = perp.z;
                //In the quaternion values, w is cosine (theta / 2), where theta is rotation angle
                quaternion.w = beginVector.dot(endVector);
            } else                                    //if its zero
            {
                //The begin and end vectors coincide, so return an identity transform
                quaternion.x = quaternion.y = quaternion.z = quaternion.w = 0.0f;
            }
        }
        quaternionToActiveRot();
        
        beginVector = endVector;
        
        activeRotation.mul(rotations);
        
        rotations = (Matrix4d)activeRotation.clone();
    }
    
    /**
     * Prevod kvaternionu do transformacni matice.
     */
    private void quaternionToActiveRot(){
        
        double n, s;
        double xs, ys, zs;
        double wx, wy, wz;
        double xx, xy, xz;
        double yy, yz, zz;

        n = (quaternion.x * quaternion.x) + (quaternion.y * quaternion.y) + (quaternion.z * quaternion.z) + (quaternion.w * quaternion.w);
        s = (n > 0.0f) ? (2.0f / n) : 0.0f;

        xs = quaternion.x * s;
        ys = quaternion.y * s;
        zs = quaternion.z * s;
        wx = quaternion.w * xs;
        wy = quaternion.w * ys;
        wz = quaternion.w * zs;
        xx = quaternion.x * xs;
        xy = quaternion.x * ys;
        xz = quaternion.x * zs;
        yy = quaternion.y * ys;
        yz = quaternion.y * zs;
        zz = quaternion.z * zs;

        activeRotation.m00 = 1.0f - (yy + zz);
        activeRotation.m01 = xy - wz;
        activeRotation.m02 = xz + wy;
        activeRotation.m03 = 0f;
        activeRotation.m10 = xy + wz;
        activeRotation.m11 = 1.0f - (xx + zz);
        activeRotation.m12 = yz - wx;
        activeRotation.m13 = 0f;
        activeRotation.m20 = xz - wy;
        activeRotation.m21 = yz + wx;
        activeRotation.m22 = 1.0f - (xx + yy);
        activeRotation.m23 = 0f;
        activeRotation.m30 = 0f;
        activeRotation.m31 = 0f;
        activeRotation.m32 = 0f;
        activeRotation.m33 = 1f;
    }
    
    /**
     * Nastaveni velikosti okna.
     * @param width Sirka.
     * @param height Vyska.
     */
    public void setBounds(int width, int height){
        this.width =  1.0 / ((width - 1.0) * 0.5);
        this.height = 1.0 / ((height - 1.0) * 0.5);
    }
        
    /**
     * Vrati transformacni matici pro rotaci.
     * @return Transformacni matice jako pole cisel.
     */
    public double[] getTransform(){

        for(int i=0;i<16;i++){
            //Opengl ma jine konvence
            transformarray[i]=rotations.getElement(i%4, i/4);
        }
        
        //vytisteni transformarray
//        for(int i=0;i<4;i++){
//            for(int j=0;j<4;j++){
//                System.out.print("["+transformarray[i*4+j]+"]");
//            }
//            System.out.println();
//        }

        return transformarray;
    }

    /**
     * Namapuje souradnice mysi na kouli arcballu.
     * @param click Souradnice.
     * @return Vektor smeru.
     */
    private Vector3d mapToSphere(Point2d click) {
        
        Vector3d vector = new Vector3d();
        
        //Copy paramter into temp point
        Point2d tempp = (Point2d)click.clone();
        
        //Adjust point coords and scale down to range of [-1 ... 1]
        tempp.x = (tempp.x * this.width) - 1.0f;
        tempp.y = 1.0f - (tempp.y * this.height);
        
        //Compute the square of the length of the vector to the point from the center
        double length = (tempp.x * tempp.x) + (tempp.y * tempp.y);        
        
        //If the point is mapped outside of the sphere... (length > radius squared)
        if (length > 1.0f) {
            //Compute a normalizing factor (radius / sqrt(length))
            float norm = (float) (1.0 / Math.sqrt(length));

            //Return the "normalized" vector, a point on the sphere
            vector.x = tempp.x * norm;
            vector.y = tempp.y * norm;
            vector.z = 0.0f;
        } else    //Else it's on the inside
        {
            //Return a vector to a point mapped inside the sphere sqrt(radius squared - length)
            vector.x = tempp.x;
            vector.y = tempp.y;
            vector.z = (float) Math.sqrt(1.0f - length);
        }
        
        return vector;
    }

    /**
     * Resetovani rotacnich matic.
     */
    public void reset() {
        
        activeRotation.m00 = 1;
        activeRotation.m01 = 0;
        activeRotation.m02 = 0;
        activeRotation.m03 = 0;
        activeRotation.m10 = 0;
        activeRotation.m11 = 1;
        activeRotation.m12 = 0;
        activeRotation.m13 = 0;
        activeRotation.m20 = 0;
        activeRotation.m21 = 0;
        activeRotation.m22 = 1;
        activeRotation.m23 = 0;
        activeRotation.m30 = 0;
        activeRotation.m31 = 0;
        activeRotation.m32 = 0;
        activeRotation.m33 = 1;
       
        rotations.m00 = 1;
        rotations.m01 = 0;
        rotations.m02 = 0;
        rotations.m03 = 0;
        rotations.m10 = 0;
        rotations.m11 = 1;
        rotations.m12 = 0;
        rotations.m13 = 0;
        rotations.m20 = 0;
        rotations.m21 = 0;
        rotations.m22 = 1;
        rotations.m23 = 0;
        rotations.m30 = 0;
        rotations.m31 = 0;
        rotations.m32 = 0;
        rotations.m33 = 1;
    }
    
}
