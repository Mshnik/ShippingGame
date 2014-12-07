package solution;

import java.io.*;
import java.util.*;

import game.*;
import game.GameRunner.GameScore;
import gui.TextIO;

/** The grader. It grades things.
 * @author MPatashnik
 *
 */
public class Grader {

	/** Percentage of score going to correctness (Getting all the parcels )
	 * 
	 */
	private static final double CORRECTNESS = 0.8;
	
	/** Percentage of score going to score (compared to instructor score)
	 * 
	 */
	private static final double SCORE = 1 - CORRECTNESS;
	
    /** Set this to true to show gui while grading.
     * May slow down grading slightly, but nice if a solution is providing erratic behavior
     * 
     * TODO Graders - feel free to change this value.
     */
    private static final boolean SHOW_GUI = false;

    /** Directory (within the project scope) where output files are written */
    private static final String GRADING_OUTPUT_DIRECTORY = "Submissions";

    /** HashMap of board JSON files to run each student's code on -> instructor's score */
    private static final HashMap<String, Integer> INSTRUCTOR_SCORE_FILE = new HashMap<String, Integer>();
    
    /** HashMap of random seed to run each student's code on -> instructor's score */
    private static final HashMap<Long, Integer> INSTRUCTOR_SCORE_RANDOM = new HashMap<Long, Integer>();

    /** Use to do printing */
    private static PrintStream stdout;

    /** Set to true if the student ever tries to print */
    private static boolean printingFlag = false;

    /** Fill in the with the grading tuples - called at class compilation time */
    static {
        INSTRUCTOR_SCORE_FILE.clear();
        INSTRUCTOR_SCORE_RANDOM.clear();
//
//        INSTRUCTOR_SCORE_FILE.put("TestBoard1", 1756);
//        INSTRUCTOR_SCORE_FILE.put("TestBoard2", 71508);
//        INSTRUCTOR_SCORE_FILE.put("TestBoard3", 1133);
        
        INSTRUCTOR_SCORE_RANDOM.put(new Long(2345724), 346038);
//        INSTRUCTOR_SCORE_RANDOM.put(new Long(542675), 400324);
//        INSTRUCTOR_SCORE_RANDOM.put(new Long(653836), 372729);
//        INSTRUCTOR_SCORE_RANDOM.put(new Long(971235), 639364);
//        INSTRUCTOR_SCORE_RANDOM.put(new Long(345353413), 267453);
//        INSTRUCTOR_SCORE_RANDOM.put(new Long(6761234), 966813);
//        INSTRUCTOR_SCORE_RANDOM.put(new Long(1290734269), 351324);
    }

    /** Main grading program.
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
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                //YOU GET NOTHING - 5 point penalty if student does printing
                printingFlag = true;
            }
        }));

        //Get the netIDs from the args - will either have length 1 or 2.
        String[] netIDs = null;
        if (args[2].startsWith("group_of_")) {
            netIDs = new String[2];
            String p = args[2].substring(10); //length of group_of_
            netIDs[0] = p.substring(0, p.indexOf('_'));
            netIDs[1] = p.substring(p.indexOf('_') + 1);
        } else {
            netIDs = new String[1];
            netIDs[0] = args[2];
        }
    	System.err.println("Grading " + 
    			(netIDs.length > 1 ? "group_of" + netIDs[0] + netIDs[1] : netIDs[0]));


        //Make sure the solution directory exists
        File gradingRoot = new File(GRADING_OUTPUT_DIRECTORY);
        if (!gradingRoot.exists()) gradingRoot.mkdir();

        final String header = "Hello, this is " + args[0] + "(" + args[1] + ") grading your A6. Your A6 is graded in two steps.\n" +
                "First, we run your manager on a set of pre-determined maps, to test the corner cases of\n" +
                "your code. Then we run it on a set of randomly generated maps, to test the regular behavior\n" +
                "of your code.\n" +
                "For a given map, " + (CORRECTNESS * 100) + "% of the points are for correctness - was your solution\n" +
                "able to pick up and deliver every parcel? The rest of the points are for your score - full credit for" +
                "achieving a score at least equal to the instructor's. If your code causes an uncaught error or a timeout\n" +
                "(runs for much too long on a given map) you may receive some amount partial credit on that map, \n" +
                "depending on the severity of the error or timeout.\n" +
                "Now let's get shipping!\n" +
                "<=|===================================================================================================|=>";


        Feedback feedback = runOn(Main.studentDirectory + "." + "MyManager");
        String finishedFeedback = header + "\n" + feedback.f;

        //Do grade printing to console where it will be picked up by graph
        for (String id : netIDs) {
            stdout.println(id +"," + feedback.grade);
        }

        //Write each output to a file the grading directory.
        //Will write based on netID, thus old runs with the same student netIdS
        //Will be overwritten.
        try {
            TextIO.write(GRADING_OUTPUT_DIRECTORY + "/" + args[2] + "/" + args[2] + "_feedback.txt",finishedFeedback);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Feedback with string and associated grade. */
    private static class Feedback{
        private String f;
        private double grade;
    }

    /** Run managerClassname on the set maps and the number of given maps.
     * Return the feedback. */
    private static Feedback runOn(String managerClassname) {
        GameRunner gr = new GameRunner(managerClassname, SHOW_GUI, false);
        String[] fileBoards = INSTRUCTOR_SCORE_FILE.keySet().toArray(new String[0]);
        Long[] rndBoards = INSTRUCTOR_SCORE_RANDOM.keySet().toArray(new Long[0]);
        long[] randomBoards = new long[rndBoards.length];
        for(int i = 0; i < rndBoards.length; i++){
        	randomBoards[i] = rndBoards[i];
        }
        
        GameScore[] fileScores = gr.runFiles(fileBoards);
        GameScore[] randomScores = gr.runSeeds(randomBoards);

        //Start compiling feedback (without header - that will be added later)
        String s = "";
        double totalCompletenesScore = 0;
        double totalTests = 0;
        double totalPointsScore = 0;
        double totalInstructorPoints = 0;
        
        //From file maps
        s += "\nFrom File Games... (" + fileBoards.length + ")\n" +
                "Seed.........................Completeness......Points...............InstructorPoints...Status";
        for (int i = 0; i < fileScores.length; i++) {
            double completenessScore = completenesScore(fileScores[i]);
            double instructorScore = INSTRUCTOR_SCORE_FILE.get(fileBoards[i]);
        	double pointScore = adjustedScore(fileScores[i]);
        	
        	totalCompletenesScore += completenessScore;
        	totalTests ++;
        	totalPointsScore += pointScore;
        	totalInstructorPoints += instructorScore;
        	
            s += "\n\t" + String.format("%20s",fileBoards[i]) + "\t" + String.format("%9.0f",completenessScore) + "\t" 
                    + String.format("%9.0f",pointScore) + "  (" + String.format("%3.2f", (pointScore/instructorScore) * 100) + "%)" 
                    + "\t" + String.format("%9.0f",instructorScore) + "\t\t" + fileScores[i].message;
        }

        //From seed maps
        s += "\n\nFrom Random Seed Games... (" + randomBoards.length + ")\n" +
                "Seed...........................Score...............InstructorScore...Status";
        for (int i = 0; i < randomScores.length; i++) {
        	double completenessScore = completenesScore(randomScores[i]);
            double instructorScore = INSTRUCTOR_SCORE_RANDOM.get(randomBoards[i]);
        	double pointScore = adjustedScore(fileScores[i]);
        	
        	totalCompletenesScore += completenessScore;
        	totalTests ++;
        	totalPointsScore += pointScore;
        	totalInstructorPoints += instructorScore;
        	
            s += "\n\t" + String.format("%20s",randomScores[i]) + "\t" + String.format("%9.0f",completenessScore) + "\t" 
                    + String.format("%9.0f",pointScore) + "  (" + String.format("%3.2f", (pointScore/instructorScore) * 100) + "%)" 
                    + "\t" + String.format("%9.0f",instructorScore) + "\t\t" + randomScores[i].message;
        }

        //Add finishing stats.
        Feedback f = new Feedback();
        
        double weightedCompletenessScore = (totalCompletenesScore / totalTests) * 100 * CORRECTNESS;
        double weightedPointsScore = Math.max(0, Math.min(1, 
        		totalPointsScore / totalInstructorPoints)) * 100 * SCORE;
        
        f.grade = weightedCompletenessScore + weightedPointsScore;

        if (printingFlag) {
            f.grade -= 3;
            s += "\n 3 point penalty - your code contained print statements. This is not good" +
                    "for code you are submitting.";
        }

        s += "\n\n" +
        		"Total Correctness (" + CORRECTNESS + "%) :" + String.format("%11.0f", totalCompletenesScore) +
                "Earned Points (" + SCORE + "%) :" + String.format("%11.0f", totalPointsScore)+
                "\t\tPossible Points: " + String.format("%11.0f", totalInstructorPoints) + 
                "\nGrade: " + String.format("%3.1f",f.grade);
        f.f = s;
        return f;
    }

    /** Portion of completenss score that is parcels */
    private static final double COMPLETENESS_PARCELS = 0.9;
    
    /** Portion of completeness score that is trucks getting home */
    private static final double COMPLETENESS_TRUCKS = 1 - COMPLETENESS_PARCELS;
    
    /** Returns a completeness score for the given gameScore - out of 1 for full completeness */
    private static double completenesScore(GameScore gs){
    	return COMPLETENESS_PARCELS * (gs.deliveredParcels / gs.initialParcels) +
    		   COMPLETENESS_TRUCKS * (gs.homeTrucks / gs.trucks);
    }
    
    /** Penalty for a heavy error */
    private static final double HEAVY_PENALTY = 0.65;

    /** Penalty for a medium error */
    private static final double MED_PENALTY = 0.75;

    /** Penalty for other (light) error */
    private static final double LIGHT_PENALTY = 0.85;

    /** Very small penalty for timeout - they were probably already losing points for it */
    private static final double TIMEOUT_PENALTY = 0.98;

    /** Return an adjusted score for gs based on its return message */
    private static double adjustedScore(GameScore gs) {
        switch (gs.status) {
            case ERROR:
                //For error case, depends on type of error.
                //More preventable and more studied errors are more harshly punished.
                Class<?> errClass = gs.game.getThrownThrowable().getClass();

                //Heavy punishment exceptions - either preventable or
                //they shouldn't have been messing with thread interrupting
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
