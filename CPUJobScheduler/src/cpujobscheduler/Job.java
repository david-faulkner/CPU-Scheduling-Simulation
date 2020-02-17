package cpujobscheduler;

import java.util.ArrayList;

public class Job {

    //fields    
    private int cpuBursts;
    private ArrayList<Integer> burstValues;
    private int currentBurst;
    private int jobNumber;

    private int arrivalTime;
    private int cpuWait;
    private int ioWait;
    private int timeFinished;
    private int timeProcessed;
    private int turnAroundTime;

    /*
    Description: Constructor for the class
    Pre-conditions: Take 8 integers as arguments
    Post-conditions: Creates object
    */
    public Job(int cpuBursts, int currentBurst, int jobNumber, int arrivalTime, int cpuWait, int ioWait, int timeFinished, int timeProcessed) {
        this.cpuBursts = cpuBursts;
        this.currentBurst = currentBurst;
        this.jobNumber = jobNumber;
        this.arrivalTime = arrivalTime;
        this.cpuWait = cpuWait;
        this.ioWait = ioWait;
        this.timeFinished = timeFinished;
        this.timeProcessed = timeProcessed;

        this.turnAroundTime = 0;
        this.burstValues = new ArrayList<>();
    }

    /*
    Description: Getter for turnAroundTime
    Pre-conditions: None
    Post-conditions: Returns turnAroundTime
    */
    public int getTurnAroundTime() {
        return turnAroundTime;
    }

    /*
    Description: Setter for turnAroundTime
    Pre-conditions: Takes integer as argument
    Post-conditions: Sets turnAroundTime for object
    */
    public void setTurnAroundTime(int turnAroundTime) {
        this.turnAroundTime = turnAroundTime;
    }

    /*
    Description: Getter for timeProcessed
    Pre-conditions: None
    Post-conditions: Returns timeProcessed
    */
    public int getTimeProcessed() {
        return timeProcessed;
    }

    /*
    Description: Setter for timeProcessed
    Pre-conditions: Takes integer as argument
    Post-conditions: Sets timeProcessed for object
    */
    public void setTimeProcessed(int timeProcessed) {
        this.timeProcessed = timeProcessed;
    }

    /*
    Description: Getter for cpuBursts
    Pre-conditions: None
    Post-conditions: Returns cpuBursts
    */
    public int getCpuBursts() {
        return cpuBursts;
    }

    /*
    Description: Setter for cpuBursts
    Pre-conditions: Takes integer as argument
    Post-conditions: Sets cpuBursts for object
    */
    public void setCpuBursts(int cpuBursts) {
        this.cpuBursts = cpuBursts;
    }

    /*
    Description: Getter for burstValues
    Pre-conditions: None
    Post-conditions: Returns the ArrayList for burstValues
    */
    public ArrayList<Integer> getBurstValues() {
        return burstValues;
    }

    /*
    Description: Setter for burstValues. Sets the value of a specific index.
    Pre-conditions: Takes an index and value as arguments
    Post-conditions: Sets the index to the argument value
    */
    public void setBurstValues(int index, int value) {
        this.burstValues.set(index, value);
    }
    
    /*
    Description: Pushes a value to the end of the burstValues ArrayList
    Pre-conditions: Takes integer as argument
    Post-conditions: Adds the value to the end of the ArrayList
    */
    public void addBurstValues(int value) {
        this.burstValues.add(value);
    }

    /*
    Description: Getter for currentBurst
    Pre-conditions: None
    Post-conditions: Returns currentBurst
    */
    public int getCurrentBurst() {
        return currentBurst;
    }

    /*
    Description: Setter for currentBurst
    Pre-conditions: Takes integer as argument
    Post-conditions: Sets currentBurst
    */
    public void setCurrentBurst(int currentBurst) {
        this.currentBurst = currentBurst;
    }

    /*
    Description: Getter for jobNumber
    Pre-conditions: None
    Post-conditions: Returns the jobNumber
    */
    public int getJobNumber() {
        return jobNumber;
    }

    /*
    Description: Setter for jobNumber
    Pre-conditions: Takes integer as argument
    Post-conditions: Sets the jobNumber
    */
    public void setJobNumber(int jobNumber) {
        this.jobNumber = jobNumber;
    }

    /*
    Description: Getter for arrivalTime
    Pre-conditions: None
    Post-conditions: Returns arrivalTime
    */
    public int getArrivalTime() {
        return arrivalTime;
    }

    /*
    Description: Setter for arrivalTime
    Pre-conditions: Takes integer as argument
    Post-conditions: Sets arrivalTime
    */
    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    /*
    Description: Getter for cpuWait
    Pre-conditions: None
    Post-conditions: Returns cpuWait
    */
    public int getCpuWait() {
        return cpuWait;
    }

    /*
    Description: Setter for cpuWait
    Pre-conditions: Takes integer as argument
    Post-conditions: Sets cpuWait to argument value
    */
    public void setCpuWait(int cpuWait) {
        this.cpuWait = cpuWait;
    }

    /*
    Description: Getter for ioWait
    Pre-conditions: None
    Post-conditions: Returns ioWait
    */
    public int getIoWait() {
        return ioWait;
    }

    /*
    Description: Setter for ioWait
    Pre-conditions: Takes integer as argument
    Post-conditions: Sets ioWait to argument value
    */
    public void setIoWait(int ioWait) {
        this.ioWait = ioWait;
    }

    /*
    Description: Getter for timeFinished
    Pre-conditions: None
    Post-conditions: Returns timeFinished
    */
    public int getTimeFinished() {
        return timeFinished;
    }

    /*
    Description: Setter for timeFinished
    Pre-conditions: Takes integer as argument
    Post-conditions: Sets timeFinished
    */
    public void setTimeFinished(int timeFinished) {
        this.timeFinished = timeFinished;
    }

}//end Job
