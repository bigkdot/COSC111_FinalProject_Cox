import java.util.*;

public class MissAmericaScoringSystem {
    static Scanner kb = new Scanner(System.in);
    static final String[] PHASES = {"Interview", "On-Stage Question", "Fitness", "Talent", "Evening Gown"};

    public static void main(String[] args) {
        System.out.println("Enter the name of your state: ");
        String stateName = kb.nextLine();

        System.out.println("Is this a local or a state pageant?");
        String answer = kb.nextLine();
        boolean isLocal = "local".equalsIgnoreCase(answer);

        // If local, ask for the local pageant name
        String localName = null;
        if (isLocal) {
            System.out.print("Enter the name of the local pageant: ");
            localName = kb.nextLine().trim();
            if (localName.isEmpty()) localName = "Local";
        }

        // Expecting a String variable 'answer' with value "state" or "local"
        int minContestants;
        int maxContestants;

        if ("state".equalsIgnoreCase(answer)) {
            // State pageant: default Miss America requirement
            minContestants = 20;
            maxContestants = Integer.MAX_VALUE;
            System.out.println("State pageant selected: you must enter at least " + minContestants + " contestants.");
        } else if (isLocal) {
            // Local pageant: allow between 3 and 10 contestants
            minContestants = 3;
            maxContestants = 10;
            System.out.println("Local pageant selected: you must enter between " + minContestants + " and " + maxContestants + " contestants.");
        } else {
            // Fallback to state rules
            minContestants = 20;
            maxContestants = Integer.MAX_VALUE;
            System.out.println("Unrecognized input; defaulting to state rules (min " + minContestants + ").");
        }

        // Number of judges
        int numJudges;
        while (true) {
            System.out.print("Enter number of judges (must be at least 1): ");
            if (!kb.hasNextInt()) {
                System.out.println("Please enter a valid integer.");
                kb.nextLine();
                continue;
            }
            numJudges = kb.nextInt();
            kb.nextLine();
            if (numJudges < 1) {
                System.out.println("There must be at least one judge.");
                continue;
            }
            break;
        }

        // Number of contestants validated against min/max determined above
        int numContestants;
        while (true) {
            System.out.print("Enter number of contestants (");
            if (maxContestants == Integer.MAX_VALUE) {
                System.out.print("at least " + minContestants);
            } else {
                System.out.print("between " + minContestants + " and " + maxContestants);
            }
            System.out.print("): ");

            if (!kb.hasNextInt()) {
                System.out.println("Please enter a valid integer.");
                kb.nextLine();
                continue;
            }

            numContestants = kb.nextInt();
            kb.nextLine();

            if (numContestants < minContestants) {
                System.out.println("You must have at least " + minContestants + " contestants.");
                continue;
            }
            if (maxContestants != Integer.MAX_VALUE && numContestants > maxContestants) {
                System.out.println("You may have at most " + maxContestants + " contestants.");
                continue;
            }
            break;
        }

        // Step 1: Get contestant names
        List<String> contestants = getContestantNames(numContestants);

        if (isLocal) {
            // LOCAL COMPETITION: Collect one set of scores (overall) and decide winner(s)
            Map<String, Double> overallAverages = collectOverallScores(contestants, numJudges);

            // Find top score
            double maxScore = overallAverages.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
            final double EPS = 1e-9;

            List<String> winners = new ArrayList<>();
            for (Map.Entry<String, Double> e : overallAverages.entrySet()) {
                if (Math.abs(e.getValue() - maxScore) < EPS) {
                    winners.add(e.getKey());
                }
            }

            System.out.println("\n--- Local Results ---");
            for (Map.Entry<String, Double> e : overallAverages.entrySet()) {
                System.out.printf("%s - %.2f%n", e.getKey(), e.getValue());
            }

            if (winners.size() == 1) {
                System.out.printf("%n👑 Miss %s: %s (Score: %.2f)%n", localName, winners.get(0), maxScore);
            } else {
                System.out.printf("%n👑 Co-Winners for Miss %s: %s (Score: %.2f)%n",
                        localName, String.join(", ", winners), maxScore);
            }

        } else {
            // STATE COMPETITION: Full multi-phase flow as before

            // Step 2: Score each phase
            Map<String, double[]> phaseScores = new HashMap<>();
            for (String name : contestants) {
                phaseScores.put(name, new double[PHASES.length]);
            }

            for (int i = 0; i < PHASES.length; i++) {
                collectPhaseScores(PHASES[i], contestants, phaseScores, numJudges, i);
            }

            // Step 3: Calculate totals and determine Top 15
            Map<String, Double> totals = calculateTotals(phaseScores);
            List<String> top15 = getTopContestants(totals, 15, "Top 15 Contestants");

            // Step 4: Re-score Top 15 (no Interview, equal weights)
            Map<String, double[]> top15Scores = new HashMap<>();
            for (String name : top15) {
                top15Scores.put(name, new double[4]); // 4 remaining phases
            }

            for (int i = 1; i < PHASES.length; i++) { // Skip Interview
                collectPhaseScores(PHASES[i], top15, top15Scores, numJudges, i - 1);
            }

            // Step 5: Calculate new totals for Top 15 → Top 5
            Map<String, Double> top15Totals = new HashMap<>();
            for (String name : top15) {
                double[] s = top15Scores.get(name);
                double sum = 0;
                for (int k = 0; k < s.length; k++) sum += s[k];
                double total = (s.length > 0) ? (sum / s.length) : 0;
                top15Totals.put(name, total);
            }

            List<String> top5 = getTopContestants(top15Totals, 5, "Top 5 Finalists");

            // Step 6: Final ranking by judges
            rankFinalists(top5, numJudges);
        }

        kb.close();
    }

    // ------------------ METHODS ------------------

    /** Get contestant names before any scoring begins */
    public static List<String> getContestantNames(int count) {
        List<String> names = new ArrayList<>();
        System.out.println("\nEnter contestant names:");
        for (int i = 0; i < count; i++) {
            System.out.print("Contestant " + (i + 1) + ": ");
            names.add(kb.nextLine());
        }
        return names;
    }

    /** Collect scores for one phase for all contestants */
    public static void collectPhaseScores(String phase, List<String> contestants, Map<String, double[]> scores, int numJudges, int phaseIndex) {
        System.out.println("\n=== Scoring Phase: " + phase + " ===");

        for (String name : contestants) {
            List<Double> judgeScores = new ArrayList<>();

            for (int j = 1; j <= numJudges; j++) {
                System.out.print("Judge " + j + " - Enter " + phase + " score for " + name + ": ");
                while (!kb.hasNextDouble()) {
                    System.out.println("Please enter a valid number.");
                    kb.nextLine();
                    System.out.print("Judge " + j + " - Enter " + phase + " score for " + name + ": ");
                }
                judgeScores.add(kb.nextDouble());
            }
            kb.nextLine();

            // Drop highest and lowest
            Collections.sort(judgeScores);
            if (judgeScores.size() > 2) {
                judgeScores.remove(0);
                judgeScores.remove(judgeScores.size() - 1);
            }

            double average = judgeScores.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            // Ensure array exists and phaseIndex is in bounds
            double[] arr = scores.get(name);
            if (arr != null && phaseIndex >= 0 && phaseIndex < arr.length) {
                arr[phaseIndex] = average;
            }
        }
    }

    /** Collect a single overall score per contestant for local competitions */
    public static Map<String, Double> collectOverallScores(List<String> contestants, int numJudges) {
        Map<String, Double> averages = new LinkedHashMap<>();
        System.out.println("\n=== Local Competition: Overall Scoring ===");

        for (String name : contestants) {
            List<Double> judgeScores = new ArrayList<>();

            for (int j = 1; j <= numJudges; j++) {
                System.out.print("Judge " + j + " - Enter overall score for " + name + ": ");
                while (!kb.hasNextDouble()) {
                    System.out.println("Please enter a valid number.");
                    kb.nextLine();
                    System.out.print("Judge " + j + " - Enter overall score for " + name + ": ");
                }
                judgeScores.add(kb.nextDouble());
            }
            kb.nextLine();

            Collections.sort(judgeScores);
            if (judgeScores.size() > 2) {
                judgeScores.remove(0);
                judgeScores.remove(judgeScores.size() - 1);
            }
            double avg = judgeScores.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            averages.put(name, avg);
        }

        return averages;
    }

    /** Calculate total scores with Miss America phase weightings */
    public static Map<String, Double> calculateTotals(Map<String, double[]> avgScores) {
        Map<String, Double> totals = new HashMap<>();
        for (String name : avgScores.keySet()) {
            double[] s = avgScores.get(name);
            double total = 0;
            // Guard against missing phases by checking length
            if (s.length >= 5) {
                total = (s[0] * 0.3) + (s[1] * 0.15) + (s[2] * 0.2) + (s[3] * 0.2) + (s[4] * 0.15);
            } else {
                // Fallback: average available phases equally
                double sum = 0;
                for (double v : s) sum += v;
                total = (s.length > 0) ? (sum / s.length) : 0;
            }
            totals.put(name, total);
        }
        return totals;
    }

    /** Sort contestants by score and return the top N */
    public static List<String> getTopContestants(Map<String, Double> totals, int count, String title) {
        List<Map.Entry<String, Double>> sorted = new ArrayList<>(totals.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        List<String> topList = new ArrayList<>();
        System.out.println("\n--- " + title + " ---");
        for (int i = 0; i < count && i < sorted.size(); i++) {
            System.out.printf("%d. %s - %.2f%n", (i + 1), sorted.get(i).getKey(), sorted.get(i).getValue());
            topList.add(sorted.get(i).getKey());
        }
        return topList;
    }

    /** Judges rank Top 5 finalists and compute average ranks */
    public static void rankFinalists(List<String> top5, int numJudges) {
        Map<String, List<Integer>> rankings = new HashMap<>();
        for (String name : top5) {
            rankings.put(name, new ArrayList<>());
        }

        System.out.println("\n--- Final Rankings ---");
        for (int j = 1; j <= numJudges; j++) {
            System.out.println("\nJudge " + j + ", please rank the Top " + top5.size() + " (1 = best):");
            for (String name : top5) {
                System.out.print("Rank for " + name + ": ");
                while (!kb.hasNextInt()) {
                    System.out.println("Please enter a valid integer rank.");
                    kb.nextLine();
                    System.out.print("Rank for " + name + ": ");
                }
                int rank = kb.nextInt();
                kb.nextLine();
                rankings.get(name).add(rank);
            }
        }

        // Calculate average rank
        Map<String, Double> avgRanks = new HashMap<>();
        for (String name : top5) {
            double avg = rankings.get(name).stream().mapToInt(Integer::intValue).average().orElse(0);
            avgRanks.put(name, avg);
        }

        // Sort and announce results
        List<Map.Entry<String, Double>> finalResults = new ArrayList<>(avgRanks.entrySet());
        finalResults.sort(Map.Entry.comparingByValue());

        System.out.println("\n👑 FINAL RESULTS 👑");
        int place = 1;
        for (Map.Entry<String, Double> entry : finalResults) {
            String title;
            switch (place) {
                case 1 -> title = "Miss America (Winner)";
                case 2 -> title = "1st Runner-Up";
                case 3 -> title = "2nd Runner-Up";
                case 4 -> title = "3rd Runner-Up";
                case 5 -> title = "4th Runner-Up";
                default -> title = "";
            }
            System.out.printf("%d. %s - Average Rank: %.2f (%s)%n", place, entry.getKey(), entry.getValue(), title);
            place++;
        }
    }
}