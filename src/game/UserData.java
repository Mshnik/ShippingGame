package game;

/** Classes that implement the UserData interface
 * allow the user to store data in them. The data is of type Object,
 * so it can be of any type the user gives. (As long as they remember
 * what that type was to cast later). <br><br>
 * 
 * Thread-safety is up to the user, so no locking should be implemented for
 * the UserData methods. <br><br>
 * 
 * With very rare exceptions, UserData methods are not used internally.
 * 
 * @author MPatashnik
 *
 */
public interface UserData {

	/** Return the object the User has stored in this. */
	public Object getUserData();
	
	/** Change the stored object ot ob. */
	public void setUserData(Object ob);
	
}
