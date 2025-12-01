
public class PhasesOfCompetiton {
    // Instance variables (each object will have these)
    private double interview;
    private double osq;
    private double fitness;
    private double talent;
    private double eveningGown;

    // Constructor — used to create objects and store scores
    public PhasesOfCompetiton(double interview, double osq, double fitness, double talent, double eveningGown) {
        this.interview = interview;
        this.osq = osq;
        this.fitness = fitness;
        this.talent = talent;
        this.eveningGown = eveningGown;
    }

    // Method to calculate the total score
    public double calculateScore() {
        return (interview * 0.3) + (osq * 0.15) + (fitness * 0.2) + (talent * 0.2) + (eveningGown * 0.15);
    }
}