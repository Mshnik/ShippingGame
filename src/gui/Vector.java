package gui;

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
	
	/** Returns the length of this vector */
	public double length(){
		return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	}
	
	/** Returns the length of this vector */
	public double magnitude(){
		return length();
	}

	@Override
	/** Returns a simple string representation of this vector */
	public String toString(){
		return "<" + x + "," + y + ">";
	}
}
