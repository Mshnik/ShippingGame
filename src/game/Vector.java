package game;

import java.awt.Point;
import java.util.Objects;

/** A simple geometric vector class (a la Point) that allows doubles for its fields */
public class Vector {

	private double x;
	private double y;
	
	/** Constructs a vector with values <0,0> */
	public Vector(){
		x = 0;
		y = 0;
	}
	
	/** Constructs a vector with the given inputs  */
	public Vector(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	/** Constructs a vector with the same values as the input vector */
	public Vector(Vector v){
		this.x = v.x;
		this.y = v.y;
	}
	
	/** Returns this vector's x component */
	public double getX(){
		return x;
	}
	
	/** Returns this double's y component */
	public double getY(){
		return y;
	}
	
	/** Adds vector v's components to this vector.
	 * Returns a reference to itself */
	public Vector addVector(Vector v){
		this.x += v.x;
		this.y += v.y;
		return this;
	}
	
	/** Multiplies both of this vector's components by the given scalar 
	 * Returns a reference to itself */
	public Vector mult(double scalar){
		this.x *= scalar;
		this.y *= scalar;
		return this;
	}
	
	/** Inverts both of this' components - changes the vector to <1/x, 1/y> 
	 * Returns a reference to itself*/
	public Vector invert(){
		this.x = 1/this.x;
		this.y = 1/this.y;
		return this;
	}
	
	/** Makes this vector have length 1, with the same direction.
	 * Returns a reference to itself */
	public Vector unit(){
		double l = length();
		x = x/l;
		y = y/l;
		return this;
	}
	
	/** Returns the dot product of the two given vectors */
	public static double dot(Vector a, Vector b){
		return a.x * b.x + a.y * b.y;
	}
	
	/** Returns the dot product of this and the vector other */
	public double dot(Vector other){
		return dot(this, other);
	}
	
	/** Returns the cross product of the two given vectors.
	 * Because vectors are 2-d, the return is a vector of the form <0, 0, z>
	 * So a 1-D vector. */
	public static double cross(Vector a, Vector b){
		return a.x * b.y - b.x * a.y;
	}
	
	/** Returns the cross product of this and vector other */
	public double cross(Vector other){
		return cross(this, other);
	}
	
	/** Returns the length of the given vector */
	public static double length(Vector a){
		return a.length();
	}
	
	/** Returns the length of this vector */
	public double length(){
		return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	}
	
	/** Returns the length of this vector */
	public double magnitude(){
		return length();
	}
	
	/** Returns the distance between the two input vectors */
	public static double distance(Vector a, Vector b){
		return Point.distance(a.x, a.y, b.x, b.y);
	}
	
	/** Returns the distance between this vector and that vector */
	public double distance(Vector there){
		return distance(this, there);
	}
	
	/** Returns a new vector from this to there */
	public Vector to(Vector there){
		return new Vector(there.x - x, there.y - y);
	}
	
	/** Returns the cosine of the angle between a and b */
	public static double cos(Vector a, Vector b){
		return dot(a,b)/(a.length()*b.length());
	}
	
	/** Returns the cosine of the angle between this and vector other */
	public double cos(Vector other){
		return cos(this, other);
	}
	
	/** Returns the angle between a and b, in radians.
	 * Return is in the range [0.. PI] */
	public static double radAngle(Vector a, Vector b){
		return Math.acos(cos(a,b));
	}
	
	/** Returns the angle between this and other, in radians.
	 * Return is in the range [0 .. PI] */
	public double radAngle(Vector other){
		return radAngle(this, other);
	}
	
	/** Hashes a vector based on its components, using the Objects.hash method. */
	@Override
	public int hashCode(){
		return Objects.hash(x, y);
	}
	
	/** Tolerance to consider two vector components equal */
	public static final double TOLERANCE = 0.000001;
	
	/** Two vectors are equal if their components are equal.
	 * To fix for precision, checks if components are within TOLERANCE of eachother */
	@Override
	public boolean equals(Object o){
		if(! (o instanceof Vector))
			return false;
		
		Vector v = (Vector)o;
		return Math.abs(x - v.x) < TOLERANCE && Math.abs(y - v.y) < TOLERANCE;
	}

	/** Returns a simple string representation of this vector */
	@Override
	public String toString(){
		return "<" + x + "," + y + ">";
	}
}
