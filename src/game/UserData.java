package game;

/** Classes that implement the UserData interface
 * allow the user to store data in them. The data is of type object,
 * so can be of any type the user gives. (So long as they remember
 * what that type was to cast later). <br><br>
 * 
 * Thread-safety is on the user, so no locking should be implemented for
 * the UserData methods. <br><br>
 * 
 * With very rare exceptions, UserData methods are not used internally.
 * 
 * @author MPatashnik
 *
 */
public interface UserData {

	/** Returns the object the User has stored in this. */
	public Object getUserData();
	
	/** Sets an object from the User stored in this. */
	public void setUserData(Object o);
	
}
