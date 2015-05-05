package solution;

/** A tuple of two values, of types A and B, respectively */
public class Tuple2<A,B> extends Tuple{

	/** The first value stored within this tuple */
	public final A _1; 
	
	/** The second value stored within this tuple */
	public final B _2;
	
	/** Constructs a new tuple of the values (first, second) */
	public Tuple2(A first, B second){
		super(first, second);
		_1 = first;
		_2 = second;
	}
}
