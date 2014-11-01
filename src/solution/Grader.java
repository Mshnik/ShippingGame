package solution;

import java.io.*;
import java.util.ConcurrentModificationException;
import java.util.HashMap;

import game.*;
import game.GameRunner.GameScore;
import gui.TextIO;

/** The grader. It grades things.
 * @author MPatashnik
 *
 */
public class Grader {

	/** Set this to true to show gui while grading.
	 * May slow down grading slightly, but nice if a solution is providing erratic behavior
	 * 
	 * TODO Graders - feel free to change this value.
	 */
	private static final boolean SHOW_GUI = false;

	/** TODO Grader's Net ID */
	private static final String GRADER_NETID = "MGP57";

	/** Directory (within the project scope) where output files are written */
	private static final String GRADING_OUTPUT_DIRECTORY = "Submissions";

	/** Instructor solution of shippingGame, classname */
	private static final String INSTRUCTOR_SOLUTION_CLASSNAME = "solution.BasicShnikSolution";

	/** Game Runner for Instructor solution */
	private static final GameRunner INSTRUCTOR_GAME_RUNNER = 
			new GameRunner(INSTRUCTOR_SOLUTION_CLASSNAME, SHOW_GUI, false);

	/** HashMap of board JSON files to run each student's code on -> instructor's score */
	private static final HashMap<String, Integer> JSON_BOARD_MAP = new HashMap<String, Integer>();

	/** Number of random seeds to run each student's code on, in addition to the above maps */
	private static final int NUMBER_RANDOM_MAPS = 10;

	/** Use to do printing */
	private static PrintStream stdout;
	
	/** Fills in the JSON_BOARD_MAP with the grading tuples - called at class compilation time */
	static {
		JSON_BOARD_MAP.clear();
	}

	/** Filled in by main at beginning of grading program.
	 * Grader should complete fillInManagerNetIDMap method so that this
	 * collection is populated correctly.
	 */
	private static HashMap<String, String> managerToNetIDMap = new HashMap<String, String>();

	private static void fillInManagerNetIDMap(){
		managerToNetIDMap.clear();
		//TODO - Add the mappings of names of manager classes -> net ids
		//managerToNetIDMap.put("userManager1","abc12");
		//...
		//...
		managerToNetIDMap.put("LarryManager", "LAR12");
	}

	/**
	 * @param args - [Name of grader, NetID of grader, [netID or group_of_netID1_netID2]]
	 * Output should be printing
	 * "NetID,grade"
	 * 
	 *  For groups of students, print on two different lines.
	 *  Write longer feedback file to memory.
	 * 
	 * 
	 */
	public static void main(String[] args) {
		stdout = System.out; //Use this to do printing. 
		System.setOut(new PrintStream(new OutputStream(){
			@Override
			public void write(int b){
				//YOU GET NOTHING
				//TODO
				//set flag to true if they've done printing - 5 point penalty
			}
		}));
		
		fillInManagerNetIDMap();

		//Make sure the solution directory exists
		File gradingRoot = new File(GRADING_OUTPUT_DIRECTORY);
		if(! gradingRoot.exists()) gradingRoot.mkdir();

		final String header = "Hello, this is " + GRADER_NETID + " grading your A6. Your A6 is graded in two steps.\n" +
				"First, we run your manager on a set of pre-determined maps, to test the corner cases of\n" +
				"your code. Then we run it on a set of randomly generated maps, to test the regular behavior\n" +
				"of your code.\n" +
				"For a given map, you receive full credit so long as your score is at least equal to the instructor's\n" +
				"simple solution, at a handicap. If your code causes an uncaught error or a timeout (runs for much too long on a\n" +
				"given map) you may receive some amount partial credit on that map, depending on the severity of\n" +
				"the error or timeout.\n" +
				"After the grading program runs, I will look back through your code to see if any simple mistakes caused\n" +
				"you to loose to many points. Now let's get shipping!\n" +
				"<=|===================================================================================================|=>";

		for(String managerClass : managerToNetIDMap.keySet()){
			String netID = managerToNetIDMap.get(managerClass);
			stdout.println("Grading Student " + netID);
			String feedback = runOn(Main.studentDirectory + "." + managerClass);

			String finishedFeedback = header + "\n" + feedback;
			stdout.println("Done. Writing output..");
			
			//Write each output to a file the grading directory.
			//Will write based on netID, thus old runs with the same student netIdS
			//Will be overwritten.
			try {
				TextIO.write(GRADING_OUTPUT_DIRECTORY + "/" + netID + "feedback.txt",finishedFeedback);
			} catch (IOException e) {
				System.err.println("Issue writing feedback to file for " + netID);
				e.printStackTrace();
			}
		}
		
		//Actually terminate the goddam thing.
		System.exit(0);
	}


	/** Runs the given managerClassname on the set maps and the number of given maps.
	 * Returns the  */
	private static String runOn(String managerClassname){
		GameRunner gr = new GameRunner(managerClassname, SHOW_GUI, true);
		String[] boards = JSON_BOARD_MAP.keySet().toArray(new String[0]);
		stdout.println("Running student code on specified games..");
		GameScore[] fileScores = gr.runFiles(boards);
		stdout.println("Ok.\n");

		stdout.println("Running instructor solution on random maps..");
		GameScore[] instructorRandomScores = INSTRUCTOR_GAME_RUNNER.runRandom(NUMBER_RANDOM_MAPS);
		stdout.println("Ok.\n");

		long[] randomSeeds = new long[NUMBER_RANDOM_MAPS];
		for(int i = 0; i < NUMBER_RANDOM_MAPS; i++){
			randomSeeds[i] = instructorRandomScores[i].game.getSeed();
		}

		stdout.println("Running student code on random maps..");
		GameScore[] randomScores = gr.runSeeds(randomSeeds);
		stdout.println("Ok.\n");

		//Start compiling feedback (without header - that will be added later)
		double earnedPoints = 0;
		double instructorPoints = 0;
		String s = "";

		//From file maps
		s += "\nFrom File Games... (" + boards.length + ")\n" +
				"Seed...........................Score...............InstructorScore...Status";
		for(int i = 0; i < fileScores.length; i++){
			double score = adjustedScore(fileScores[i]);
			double instructorScore = JSON_BOARD_MAP.get(boards[i]) * INSTRUCTOR_HANDICAP;
			earnedPoints += score;
			instructorPoints += instructorScore;
			s += "\n\t" + String.format("%20s",boards[i]) + "\t" 
					+ String.format("%9.0f",score) + "  (" + String.format("%3.2f", (score/instructorScore) * 100) + "%)" 
					+ "\t" + String.format("%9.0f",instructorScore) + "\t\t" + fileScores[i].message;
		}
		//From seed maps
		s += "\n\nFrom Random Seed Games... (" + randomSeeds.length + ")\n" +
				"Seed...........................Score...............InstructorScore...Status";
		for(int i = 0; i < randomScores.length; i++){
			double score = adjustedScore(randomScores[i]);
			double instructorScore = adjustedScore(instructorRandomScores[i]) * INSTRUCTOR_HANDICAP;
			earnedPoints += score;
			instructorPoints += instructorScore;
			s += "\n\t" + String.format("%20s",randomSeeds[i]) + "\t" 
					+ String.format("%9.0f",score) + "  (" + String.format("%3.2f", (score/instructorScore) * 100) + "%)" 
					+ "\t" + String.format("%9.0f",instructorScore) + "\t\t" + randomScores[i].message;
		}

		//Add finishing stats.
		s += "\n\n" +
				"Earned Points: " + String.format("%11.0f", earnedPoints)+
				"\t\tPossible Points: " + String.format("%11.0f", instructorPoints) + 
				"\nGrade: " + String.format("%3.1f",Math.max(0, Math.min(1, earnedPoints / instructorPoints)) * 100);
		
		return s;
	}

	/** Instructor handicap */
	private static final double INSTRUCTOR_HANDICAP = 0.9;
	
	/** Penalty for a heavy error */
	private static final double HEAVY_PENALTY = 0.65;

	/** Penalty for a medium error */
	private static final double MED_PENALTY = 0.75;

	/** Penalty for other (light) error */
	private static final double LIGHT_PENALTY = 0.85;
	
	/** Very small penalty for timeout - they were probably already losing points for it */
	private static final double TIMEOUT_PENALTY = 0.98;

	/** Returns an adjusted score for the given GameScore object based on its return message */
	private static double adjustedScore(GameScore gs){
		switch(gs.status){

		case ERROR:
			//For error case, depends on type of error.
			//More preventable and more studied errors are more harshly punished.
			Class<?> errClass = gs.game.getThrownThrowable().getClass();

			//Heavy punishment exceptions - either preventable or
			//they shouldn't have been messing with thread interrupting
			if(errClass.equals(NullPointerException.class))
				return gs.score * HEAVY_PENALTY;
			if(errClass.equals(ArrayIndexOutOfBoundsException.class))
				return gs.score * HEAVY_PENALTY;
			if(errClass.equals(InterruptedException.class))
				return gs.score * HEAVY_PENALTY;

			if(errClass.equals(ClassCastException.class))
				return gs.score * MED_PENALTY;
			if(errClass.equals(ConcurrentModificationException.class))
				return gs.score * MED_PENALTY;

			//Unusual error - penalize less heavily. Have to penalize all errors to prevent abuse
			return gs.score * LIGHT_PENALTY;

		case SUCCESS:
			//No adjustment for successful case
			return gs.score; 

		case TIMEOUT:
			//Slight adjustment for timeout-ing
			return gs.score * TIMEOUT_PENALTY;

		default:
			//Hopefully unreachable case 
			return gs.score;
		}
	}

}
