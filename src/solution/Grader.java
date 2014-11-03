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

	/** Set to true if the student ever tries to print */
	private static boolean printingFlag = false;

	/** Fills in the JSON_BOARD_MAP with the grading tuples - called at class compilation time */
	static {
		JSON_BOARD_MAP.clear();

		//TODO - add required map jsons
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
		printingFlag = false;
		System.setOut(new PrintStream(new OutputStream(){
			@Override
			public void write(int b){
				//YOU GET NOTHING - 5 point penalty if student does printing
				printingFlag = true;
			}
		}));

		//Get the netIDs from the args - will either have length 1 or 2.
		String[] netIDs = null;
		if(args[2].startsWith("group_of_")){
			netIDs = new String[2];
			String p = args[2].substring(10); //length of group_of_
			netIDs[0] = p.substring(0, p.indexOf('_'));
			netIDs[1] = p.substring(p.indexOf('_') + 1);
		} else{
			netIDs = new String[1];
			netIDs[0] = args[2];
		}

		//Make sure the solution directory exists
		File gradingRoot = new File(GRADING_OUTPUT_DIRECTORY);
		if(! gradingRoot.exists()) gradingRoot.mkdir();

		final String header = "Hello, this is " + args[0] + "(" + args[1] + ") grading your A6. Your A6 is graded in two steps.\n" +
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
				
				
		Feedback feedback = runOn(Main.studentDirectory + "." + ""); //TODO! fill in with how to get the manager classname, and where it is located
		String finishedFeedback = header + "\n" + feedback.f;

		//Do grade printing to console where it will be picked up by graph
		for(String id : netIDs){
			stdout.println(id +"," + feedback.grade);
		}
		
		//Write each output to a file the grading directory.
		//Will write based on netID, thus old runs with the same student netIdS
		//Will be overwritten.
		try {
			TextIO.write(GRADING_OUTPUT_DIRECTORY + "/" + args[2] + "_feedback.txt",finishedFeedback);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Feedback with string and associated grade */
	private static class Feedback{
		private String f;
		private double grade;
	}
	
	/** Runs the given managerClassname on the set maps and the number of given maps.
	 * Returns the  */
	private static Feedback runOn(String managerClassname){
		GameRunner gr = new GameRunner(managerClassname, SHOW_GUI, true);
		String[] boards = JSON_BOARD_MAP.keySet().toArray(new String[0]);
		GameScore[] fileScores = gr.runFiles(boards);

		GameScore[] instructorRandomScores = INSTRUCTOR_GAME_RUNNER.runRandom(NUMBER_RANDOM_MAPS);

		long[] randomSeeds = new long[NUMBER_RANDOM_MAPS];
		for(int i = 0; i < NUMBER_RANDOM_MAPS; i++){
			randomSeeds[i] = instructorRandomScores[i].game.getSeed();
		}

		GameScore[] randomScores = gr.runSeeds(randomSeeds);

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
		Feedback f = new Feedback();
		f.grade = Math.max(0, Math.min(1, earnedPoints / instructorPoints)) * 100;
		
		if(printingFlag){
			f.grade -= 5;
			s += "\n 5 point penalty - your code contained print statements. This is not good" +
					"for code you are submitting.";
		}
		
		s += "\n\n" +
				"Earned Points: " + String.format("%11.0f", earnedPoints)+
				"\t\tPossible Points: " + String.format("%11.0f", instructorPoints) + 
				"\nGrade: " + String.format("%3.1f",f.grade);
		f.f = s;
		return f;
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
