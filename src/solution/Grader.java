package solution;

import java.io.*;
import java.util.*;

import game.*;
import game.GameRunner.GameScore;
import gui.TextIO;

/**
 * The grader. It grades things.
 * 
 * @author MPatashnik
 *
 */
public class Grader {
	
	private static final String SOLUTION = "solution.SuperShnikSolution";

	/**
	 * Percentage of score going to correctness (Getting all the parcels )
	 * 
	 */
	private static final double CORRECTNESS = 0.8;

	/**
	 * Percentage of score going to score (compared to instructor score)
	 * 
	 */
	private static final double SCORE = 0.2;

	/**
	 * Set this to true to show gui while grading. May slow down grading
	 * slightly, but nice if a solution is providing erratic behavior
	 * 
	 * TODO Graders - feel free to change this value.
	 */
	private static final boolean SHOW_GUI = false;

	/** Directory (within the project scope) where output files are written */
	private static final String GRADING_OUTPUT_DIRECTORY = "Submissions";

	/**
	 * HashMap of board JSON files to run each student's code on -> instructor's
	 * score
	 */
	private static final List<String> INSTRUCTOR_SCORE_FILE = new LinkedList<String>();

	/**
	 * HashMap of random seed to run each student's code on -> instructor's
	 * score
	 */
	private static final List<Long> INSTRUCTOR_SCORE_RANDOM = new LinkedList<Long>();

	private static final int NUM_RANDOMS = 12;

	private static final String INS_SCORE_FILE = "scoreFile";
	
	private static int[] insFiles;
	
	private static int[] insRandoms;

	/** Use to do printing */
	private static PrintStream stdout;

	/** Set to true if the student ever tries to print */
	private static boolean printingFlag = false;

	/** Fill in the with the grading tuples - called at class compilation time */
	static {
		INSTRUCTOR_SCORE_FILE.clear();
		INSTRUCTOR_SCORE_RANDOM.clear();

		INSTRUCTOR_SCORE_FILE.add("TestBoard1");
		INSTRUCTOR_SCORE_FILE.add("TestBoard2");
		INSTRUCTOR_SCORE_FILE.add("TestBoard3");
		Random r = new Random(20152110);
		for (int i = 0; i < NUM_RANDOMS; i++){
			INSTRUCTOR_SCORE_RANDOM.add(r.nextLong());
		}
	}

	/**
	 * Main grading program.
	 * 
	 * @param args
	 *            - [Name of grader, NetID of grader, [netID or
	 *            group_of_netID1_netID2]] Output should be printing
	 *            "NetID,grade"
	 * 
	 *            For groups of students, print on two different lines. Write
	 *            longer feedback file to memory.
	 * 
	 * 
	 */
	public static void main(String[] args) {
		stdout = System.out; // Use this to do printing.
		printingFlag = false;
		System.setOut(new PrintStream(new OutputStream() {
			@Override
			public void write(int b) {
				// YOU GET NOTHING - 5 point penalty if student does printing
				printingFlag = true;
			}
		}));

		// Get the netIDs from the args - will either have length 1 or 2.
		String[] netIDs = null;
		if (args[2].startsWith("group_of_")) {
			netIDs = new String[2];
			String p = args[2].substring(9); // length of group_of_
			netIDs[0] = p.substring(0, p.indexOf('_'));
			netIDs[1] = p.substring(p.indexOf('_') + 1);
		} else {
			netIDs = new String[1];
			netIDs[0] = args[2];
		}
		populateInsScores();
		long startTime = System.currentTimeMillis();
		System.err.println("Grading "
				 + (netIDs.length > 1 ? "group_of_" + netIDs[0] + "_" + netIDs[1]
							: netIDs[0]));

		// Make sure the solution directory exists
		File gradingRoot = new File(GRADING_OUTPUT_DIRECTORY);
		if (!gradingRoot.exists())
			gradingRoot.mkdir();

		final String header = "Hello, this is "
				+ args[0]
				+ " ("
				+ args[1]
				+ ") grading your A8. Your A8 is graded in two steps.\n"
				+ "First, we run your manager on a set of pre-determined maps, to test the corner cases of\n"
				+ "your code. Then we run it on a set of randomly generated maps, to test the regular behavior\n"
				+ "of your code.\n"
				+ "For a given map, "
				+ (CORRECTNESS * 100)
				+ "% of the points are for correctness - was your solution\n"
				+ "able to pick up and deliver every parcel? The rest of the points are for your score - full credit for\n"
				+ "achieving a score at least equal to the instructor's. If your code causes an uncaught error or a timeout\n"
				+ "(runs for much too long on a given map) you may receive some amount partial credit on that map, \n"
				+ "depending on the severity of the error or timeout.\n"
				+ "Now let's get shipping!\n"
				+ "<=|===================================================================================================|=>";
		Feedback feedback = runOn(Main.studentDirectory + "." + "MyManager");
		String finishedFeedback = header + "\n" + feedback.f;

		// Do grade printing to console where it will be picked up by graph
		for (String id : netIDs) {
			stdout.println(id + "," + feedback.grade);
		}

		// Write each output to a file the grading directory.
		// Will write based on netID, thus old runs with the same student netIdS
		// Will be overwritten.
		try {
			TextIO.write(GRADING_OUTPUT_DIRECTORY + "/" + args[2] + "/"
					+ args[2] + "_feedback.txt", finishedFeedback);
		} catch (IOException e) {
			e.printStackTrace();
		}
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.err.println("Grading "+ (netIDs.length > 1 ? "group_of_" + netIDs[0] + "_" + netIDs[1]
				: netIDs[0]) + " took " + elapsedTime /1000.0 + " seconds");
		System.exit(0);
	}

	private static void populateInsScores() {
		File f = new File(INS_SCORE_FILE);
		if (f.exists()){
			readScores(f);
		} else {
			calculateAndWriteScores(f);
		}
		
	}

	private static void calculateAndWriteScores(File f) {
		System.err.println("Instructor scores not up to date, populating now");
		long startTime = System.currentTimeMillis();
		GameRunner igr = new GameRunner(SOLUTION, SHOW_GUI, false);
		String[] fileBoards = INSTRUCTOR_SCORE_FILE.toArray(
				new String[INSTRUCTOR_SCORE_FILE.size()]);
		Long[] rndBoards = INSTRUCTOR_SCORE_RANDOM.toArray(new Long[INSTRUCTOR_SCORE_RANDOM.size()]);
		long[] randomBoards = new long[rndBoards.length];
		for (int i = 0; i < rndBoards.length; i++) {
			randomBoards[i] = rndBoards[i];
		}
		GameScore[] insFilesGs = igr.runFiles(fileBoards);
		GameScore[] insRandomsGs = igr.runSeeds(randomBoards);
		try (PrintWriter pw = new PrintWriter(f)) {
			insFiles = new int[fileBoards.length];
			int i = 0;
			for (GameScore s : insFilesGs){
				insFiles[i++] = s.score;
				pw.println(s.score);
			}
			insRandoms = new int[rndBoards.length];
			i = 0;
			for (GameScore s : insRandomsGs){
				insRandoms[i++] = s.score;
				pw.println(s.score);
			}
		} catch (IOException e){
			throw new RuntimeException(e);
		}
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.err.println("Populating instructor scores took " + elapsedTime/1000.0 + " seconds");
	}

	private static void readScores(File f) {
		insFiles = new int[INSTRUCTOR_SCORE_FILE.size()];
		insRandoms = new int[INSTRUCTOR_SCORE_RANDOM.size()];
		try (FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr)){
			int[] cur = insFiles;
			String line = br.readLine();
			int i = 0;
			while (line != null){
				cur[i++] = Integer.parseInt(line);
				if (cur == insFiles && i >= insFiles.length){
					i = 0;
					cur = insRandoms;
				}
				line = br.readLine();
			}
			if (i != insRandoms.length){
				calculateAndWriteScores(f);
				return;
			}
		} catch (Exception e) {
			calculateAndWriteScores(f);
			return;
		}
		
	}

	/** Feedback with string and associated grade. */
	private static class Feedback {
		private String f;
		private double grade;
	}

	/**
	 * Run managerClassname on the set maps and the number of given maps. Return
	 * the feedback.
	 */
	private static Feedback runOn(String managerClassname) {
		GameRunner gr = new GameRunner(managerClassname, SHOW_GUI, false);
		String[] fileBoards = INSTRUCTOR_SCORE_FILE.toArray(
				new String[INSTRUCTOR_SCORE_FILE.size()]);
		Long[] rndBoards = INSTRUCTOR_SCORE_RANDOM.toArray(new Long[INSTRUCTOR_SCORE_RANDOM.size()]);
		long[] randomBoards = new long[rndBoards.length];
		for (int i = 0; i < rndBoards.length; i++) {
			randomBoards[i] = rndBoards[i];
		}

		GameScore[] fileScores = gr.runFiles(fileBoards);
		GameScore[] randomScores = gr.runSeeds(randomBoards);

		// Start compiling feedback (without header - that will be added later)
		String s = "";
		double totalCompletenesScore = 0;
		double totalTests = 0;
		double totalPointsScore = 0;
		double totalInstructorPoints = 0;

		// From file maps
		s += "\nFrom File Games... ("
				+ fileBoards.length
				+ ")\n"
				+ "File.....................Completeness......Points...............InstructorPoints...Status";

		for (int i = 0; i < fileScores.length; i++) {
			double completenessScore = completenesScore(fileScores[i]);
			double instructorScore = insFiles[i];
			double pointScore = adjustedScore(fileScores[i]);

			totalCompletenesScore += completenessScore;
			totalTests++;
			totalPointsScore += pointScore;
			totalInstructorPoints += instructorScore;

			s += "\n"
					+ String.format("%20s", fileScores[i].game.getFile()
							.getName())
					+ "\t"
					+ String.format("%6.3f", completenessScore)
					+ "\t\t\t"
					+ String.format("%6.3f", pointScore)
					+ "  ("
					+ String.format("%3.2f",
							(pointScore / instructorScore) * 100) + "%)\t"
					+ "\t" + String.format("%9.0f", instructorScore) + "\t\t"
					+ fileScores[i].message;
		}

		// From seed maps
		s += "\n\nFrom Random Seed Games... ("
				+ randomBoards.length
				+ ")\n"
				+ "Seed.....................Completeness......Points...............InstructorPoints...Status";
		for (int i = 0; i < randomScores.length; i++) {
			double completenessScore = completenesScore(randomScores[i]);
			double instructorScore = insRandoms[i];
			double pointScore = adjustedScore(randomScores[i]);

			totalCompletenesScore += completenessScore;
			totalTests++;
			totalPointsScore += pointScore;
			totalInstructorPoints += instructorScore;

			s += "\n"
					+ String.format("%20s", randomBoards[i])
					+ "\t"
					+ String.format("%6.3f", completenessScore)
					+ "\t\t\t"
					+ String.format("%6.3f", pointScore)
					+ "  ("
					+ String.format("%3.2f",
							(pointScore / instructorScore) * 100) + "%)\t"
					+ "\t" + String.format("%9.0f", instructorScore) + "\t\t"
					+ randomScores[i].message;
		}

		// Add finishing stats.
		Feedback f = new Feedback();

		double weightedCompletenessScore = (totalCompletenesScore / totalTests)
				* 100 * CORRECTNESS;
		double weightedPointsScore = Math.max(0,
				Math.min(1, totalPointsScore / totalInstructorPoints))
				* 100 * SCORE;

		f.grade = weightedCompletenessScore + weightedPointsScore;

		if (printingFlag) {
			f.grade -= 3;
			s += "\n 3 point penalty - your code contained print statements. This is not good"
					+ "for code you are submitting.";
		}

		s += "\n\n\n"
				+ "In-Game Score: "
				+ String.format("%11s", (int) totalPointsScore + " of "
						+ (int) totalInstructorPoints)
				+ " ("
				+ String.format("%3.1f", totalPointsScore
						/ totalInstructorPoints * 100) + "%)";
		if (totalPointsScore > totalInstructorPoints) {
			f.grade += 3;
			s += "\n3 point bonus! - Congratulations on beating the Instructor solution!";
		}
		s += "\n\n"
				+ "Total Correctness ("
				+ (CORRECTNESS * 100)
				+ "%) :"
				+ String.format("%4.2f",
						(totalCompletenesScore * 100.0 / totalTests))
				+ "\nTotal Points (" + (SCORE * 100) + "%) :\t   "
				+ String.format("%4.2f", weightedPointsScore / SCORE);
		if (printingFlag) {
			s += "\n - 3 point printing pentalty";
		}
		if (totalPointsScore > totalInstructorPoints) {
			s += "\n + 3 point super solution bonus";
		}
		s += "\nGrade: " + String.format("%3.1f", f.grade);
		f.f = s;
		return f;
	}

	/** Portion of completenss score that is parcels */
	private static final double COMPLETENESS_PARCELS = 1;

	/** Portion of completeness score that is trucks getting home */
	private static final double COMPLETENESS_TRUCKS = 0;

	/**
	 * Returns a completeness score for the given gameScore - out of 1 for full
	 * completeness
	 */
	private static double completenesScore(GameScore gs) {
		return COMPLETENESS_PARCELS * (gs.deliveredParcels / gs.initialParcels)
				+ COMPLETENESS_TRUCKS * (gs.homeTrucks / gs.trucks);
	}

	/** Penalty for a heavy error */
	private static final double HEAVY_PENALTY = 0.65;

	/** Penalty for a medium error */
	private static final double MED_PENALTY = 0.75;

	/** Penalty for other (light) error */
	private static final double LIGHT_PENALTY = 0.85;

	/**
	 * Very small penalty for timeout - they were probably already losing points
	 * for it
	 */
	private static final double TIMEOUT_PENALTY = 0.98;

	/** Return an adjusted score for gs based on its return message */
	private static double adjustedScore(GameScore gs) {
		switch (gs.status) {
		case ERROR:
			// For error case, depends on type of error.
			// More preventable and more studied errors are more harshly
			// punished.
			if (gs.game.getThrownThrowable() != null) {
				Class<?> errClass = gs.game.getThrownThrowable().getClass();

				// Heavy punishment exceptions - either preventable or
				// they shouldn't have been messing with thread interrupting
				if (errClass.equals(NullPointerException.class))
					return gs.score * HEAVY_PENALTY;
				if (errClass.equals(ArrayIndexOutOfBoundsException.class))
					return gs.score * HEAVY_PENALTY;
				if (errClass.equals(InterruptedException.class))
					return gs.score * HEAVY_PENALTY;

				if (errClass.equals(ClassCastException.class))
					return gs.score * MED_PENALTY;
				if (errClass.equals(ConcurrentModificationException.class))
					return gs.score * MED_PENALTY;
			}
			// Unusual error - penalize less heavily. Have to penalize all
			// errors to prevent abuse
			return gs.score * LIGHT_PENALTY;

		case SUCCESS:
			// No adjustment for successful case
			return gs.score;

		case TIMEOUT:
			// Slight adjustment for timeout-ing
			return gs.score * TIMEOUT_PENALTY;

		default:
			// Hopefully unreachable case
			return gs.score;
		}
	}

}
