package cpujobscheduler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CPUJobScheduler {

    public static void main(String[] args) {

        //queues
        ArrayList<Job> readyQueue = new ArrayList<>();
        ArrayList<Job> ioQueue = new ArrayList<>();

        ArrayList<Job> completedList = new ArrayList<>();

        //metric related fields
        double cpuUtilization = 0;
        int systemClock = 0;
        int timeQuantum = 0;
        double avgIoWait = 0;
        double avgCpuWait = 0;
        double avgTat = 0;

        //bools to determine if safe to perform job
        boolean cpuIsEmpty = true;
        boolean ioIsEmpty = true;

        //fields to pull job from file
        Job nextJobFromFile = null;
        ArrayList<Job> jobsFromFile = new ArrayList<>();

        //cpu and io processor
        Job cpuJob = null;
        Job ioJob = null;

        //to read user input
        Scanner input = new Scanner(System.in);

        //files
        String fileName;
        String line;
        String[] lineArr;
        int[] lineIntArr;
        BufferedWriter logFile = null;
        BufferedWriter outputFile = null;
        BufferedReader jobFile = null;

        int jobNumber = 0;
        boolean actionPerformed = false;

        int lastSystemClockEntry = 0;

        //start main processes
        System.out.print("Welcome to the CPU Job Scheduler\n"
                + "You will need to enter the name of three files and enter a quantum number\n\n"
                + "Please enter the name of the file you wish to write the output to: ");
        fileName = input.next();

        //open output file
        try {
            outputFile = new BufferedWriter(new FileWriter(fileName));
        } catch (IOException ex) {
            Logger.getLogger(CPUJobScheduler.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.print("Please enter the name of the file containing the jobs: ");
        fileName = input.next();

        //open job file
        try {
            jobFile = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException ex) {
            System.out.println("ERROR, " + fileName + " does not exist. Terminating program...");
            return;
        }

        //file is empty
        try {
            if (!jobFile.ready()) {
                System.out.println("ERROR, " + fileName + " is empty. Terminating program...");
                return;
            }
        } catch (IOException ex) {
            Logger.getLogger(CPUJobScheduler.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.print("Please enter the name of the file you wish to write the log to: ");
        fileName = input.next();

        //open log file
        try {
            logFile = new BufferedWriter(new FileWriter(fileName));
        } catch (IOException ex) {
            Logger.getLogger(CPUJobScheduler.class.getName()).log(Level.SEVERE, null, ex);
        }

        //get time quantum
        while (timeQuantum <= 0) {
            System.out.print("Please enter the desired time quantum: ");
            if (input.hasNextInt()) {   //user entered int
                timeQuantum = input.nextInt();
            } else {    //user entered something other than an int
                System.out.println("ERROR, input was not an integer. Please enter an integer.");
                input.next();
                continue;
            }//end if-else

            if (timeQuantum <= 0) {
                System.out.println("ERROR, input was <=0. Please enter an integer >0.");
            }//end if
        }//end while

        //pull from file
        try {
            while ((line = jobFile.readLine()) != null) { //not end of file
                lineArr = line.split(" ");
                lineIntArr = new int[lineArr.length];
                for (int i = 0; i < lineArr.length; ++i) {  //store in int arr
                    lineIntArr[i] = Integer.parseInt(lineArr[i]);
                }

                if (lineIntArr[0] < lastSystemClockEntry || lineIntArr.length < 3) { //job arrived out of order or improper format
                    System.out.println("ERROR, bad file format. P" + jobNumber++ + " thrown out.");
                    continue;
                } else if ((lineIntArr.length - 2) != (lineIntArr[1] * 2 - 1)) { //has incorrect number of bursts
                    System.out.println("ERROR, incorrect number of bursts. P" + jobNumber++ + " thrown out.");
                    continue;
                } else {
                    nextJobFromFile = new Job(lineIntArr[1], 0, jobNumber, lineIntArr[0], 0, 0, 0, 0);
                    for (int i = 2; i < lineIntArr.length; ++i) {   //set burst array in object
                        if (lineIntArr[i] <= 0) { //job has burst value of <=0
                            System.out.println("ERROR, job contains burst value of <=0. P" + jobNumber++ + " thrown out.");
                            if (jobsFromFile.size() > 0) {
                                nextJobFromFile = jobsFromFile.get(jobsFromFile.size() - 1);
                                jobsFromFile.remove(jobsFromFile.size() - 1);
                            } else {
                                nextJobFromFile = null;
                            }
                            break;
                        }
                        nextJobFromFile.addBurstValues(lineIntArr[i]);
                    } //end for loop
                    lastSystemClockEntry = lineIntArr[0];
                    jobNumber++;
                    jobsFromFile.add(nextJobFromFile);
                    if (jobsFromFile.size() > 0 && jobsFromFile.get(0) == null) {
                        jobsFromFile.remove(0);
                    }
                }
            } //end while
        } catch (IOException ex) {
            Logger.getLogger(CPUJobScheduler.class.getName()).log(Level.SEVERE, null, ex);
        }//end file pull

        //output if no simulation
        if (!(!jobsFromFile.isEmpty() || !ioQueue.isEmpty() || !readyQueue.isEmpty()
                || !cpuIsEmpty || !ioIsEmpty)) {
            System.out.println("NO SIMULATION.");
        }
        
        //main simulation loop
        //will stop simulating once 20 time units of inactivity pass
        while (!jobsFromFile.isEmpty() || !ioQueue.isEmpty() || !readyQueue.isEmpty()
                || !cpuIsEmpty || !ioIsEmpty) {

            //print current time
            System.out.print(systemClock + ": ");
            try {
                logFile.write(systemClock + ": ");
                outputFile.write(systemClock + ": ");
            } catch (IOException ex) {
                Logger.getLogger(CPUJobScheduler.class.getName()).log(Level.SEVERE, null, ex);
            }

            //print off the contents of everything every 5 time units
            try {
                if (systemClock % 5 == 0 && systemClock != 0) {

                    //print contents of CPU
                    if (!cpuIsEmpty) {
                        outputFile.write("CPU: P" + cpuJob.getJobNumber() + "; ");
                    } else {
                        outputFile.write("CPU: empty; ");
                    }//end cpu

                    //print contents of IO
                    if (!ioIsEmpty) {
                        outputFile.write("IO: P" + ioJob.getJobNumber() + "; ");
                    } else {
                        outputFile.write("IO: empty; ");
                    }//end io

                    //print contents of ready queue
                    if (readyQueue.size() > 0) {
                        outputFile.write("Ready Queue: ");
                        for (int i = 0; i < readyQueue.size() - 1; ++i) {
                            outputFile.write("P" + readyQueue.get(i).getJobNumber() + ", ");
                        }
                        outputFile.write("P" + readyQueue.get(readyQueue.size() - 1).getJobNumber() + "; ");
                    } else {
                        outputFile.write("Ready Queue: empty; ");
                    }//end ready queue

                    //print contents of io queue
                    if (ioQueue.size() > 0) {
                        outputFile.write("IO Queue: ");
                        for (int i = 0; i < ioQueue.size() - 1; ++i) {
                            outputFile.write("P" + ioQueue.get(i).getJobNumber() + ", ");
                        }
                        outputFile.write("P" + ioQueue.get(ioQueue.size() - 1).getJobNumber() + "\n");
                    } else {
                        outputFile.write("IO Queue: empty\n");
                    }

                }
            } catch (IOException ex) {
                Logger.getLogger(CPUJobScheduler.class.getName()).log(Level.SEVERE, null, ex);
            }//end "every 5 time unit" printing

            //priority 1
            //put job from file in ready queue if at arrival time
            if (jobsFromFile.size() > 0 && jobsFromFile.get(0).getArrivalTime() == systemClock) {
                System.out.println("Start of " + systemClock + ": P" + jobsFromFile.get(0).getJobNumber()
                        + " arrives - new process - enters ready queue - CPU burst of "
                        + jobsFromFile.get(0).getBurstValues().get(0));
                try {
                    logFile.write("Start of " + systemClock + ": P" + jobsFromFile.get(0).getJobNumber()
                            + " arrives - new process - enters ready queue - CPU burst of "
                            + jobsFromFile.get(0).getBurstValues().get(0) + "\n");
                    outputFile.write("P" + jobsFromFile.get(0).getJobNumber() + " arrives - New process changes state to Ready\n");
                } catch (IOException ex) {
                    Logger.getLogger(CPUJobScheduler.class.getName()).log(Level.SEVERE, null, ex);
                }

                readyQueue.add(jobsFromFile.get(0));
                jobsFromFile.remove(0);
                nextJobFromFile = null;
                actionPerformed = true;
            }

            //priority 2
            //add job from io to ready queue when burst complete
            if (!ioIsEmpty && ioJob.getBurstValues().get(ioJob.getCurrentBurst()) == 0) {
                ioJob.setCurrentBurst(ioJob.getCurrentBurst() + 1);

                System.out.println("Start of " + systemClock + ": P" + ioJob.getJobNumber()
                        + " IO burst completed at end of " + (systemClock - 1) + " - moved to ready queue - "
                        + "CPU burst of " + ioJob.getBurstValues().get(ioJob.getCurrentBurst()));
                try {
                    logFile.write("Start of " + systemClock + ": P" + ioJob.getJobNumber()
                            + " IO burst completed at end of " + (systemClock - 1) + " - moved to ready queue - "
                            + "CPU burst of " + ioJob.getBurstValues().get(ioJob.getCurrentBurst()) + "\n");
                    outputFile.write("P" + ioJob.getJobNumber() + " changes state from Blocked to Ready\n");
                } catch (IOException ex) {
                    Logger.getLogger(CPUJobScheduler.class.getName()).log(Level.SEVERE, null, ex);
                }

                readyQueue.add(ioJob);
                ioJob = null;
                ioIsEmpty = true;
                actionPerformed = true;
            }

            //add job to the io queue from cpu when burst complete
            if (!cpuIsEmpty && cpuJob.getBurstValues().get(cpuJob.getCurrentBurst()) == 0) {
                cpuJob.setCurrentBurst(cpuJob.getCurrentBurst() + 1);

                System.out.println("Start of " + systemClock + ": P" + cpuJob.getJobNumber()
                        + " CPU burst completed at end of " + (systemClock - 1) + " - moved to IO queue - "
                        + "IO burst of " + cpuJob.getBurstValues().get(cpuJob.getCurrentBurst()));
                try {
                    logFile.write("Start of " + systemClock + ": P" + cpuJob.getJobNumber()
                            + " CPU burst completed at end of " + (systemClock - 1) + " - moved to IO queue - "
                            + "IO burst of " + cpuJob.getBurstValues().get(cpuJob.getCurrentBurst()) + "\n");
                    outputFile.write("P" + cpuJob.getJobNumber() + " changes state from Running to Blocked\n");
                } catch (IOException ex) {
                    Logger.getLogger(CPUJobScheduler.class.getName()).log(Level.SEVERE, null, ex);
                }

                cpuJob.setTimeProcessed(0);
                ioQueue.add(cpuJob);
                cpuJob = null;
                cpuIsEmpty = true;
                actionPerformed = true;
            }

            //priority 3
            //preempt job from cpu
            if (!cpuIsEmpty && cpuJob.getTimeProcessed() == timeQuantum) {
                System.out.println("Start of " + systemClock + ": P" + cpuJob.getJobNumber()
                        + " preempted from CPU at end of " + (systemClock - 1) + " - moved to ready queue - "
                        + "CPU burst of " + cpuJob.getBurstValues().get(cpuJob.getCurrentBurst()));
                try {
                    logFile.write("Start of " + systemClock + ": P" + cpuJob.getJobNumber()
                            + " preempted from CPU at end of " + (systemClock - 1) + " - moved to ready queue - "
                            + "CPU burst of " + cpuJob.getBurstValues().get(cpuJob.getCurrentBurst()) + "\n");
                    outputFile.write("P" + cpuJob.getJobNumber() + " preempted - changes state from Running to Ready\n");
                } catch (IOException ex) {
                    Logger.getLogger(CPUJobScheduler.class.getName()).log(Level.SEVERE, null, ex);
                }

                cpuJob.setTimeProcessed(0);
                readyQueue.add(cpuJob);
                cpuJob = null;
                cpuIsEmpty = true;
                actionPerformed = true;
            }

            //add job to cpu if it is empty
            if (cpuIsEmpty && readyQueue.size() > 0 && readyQueue.get(0) != null) {
                cpuJob = readyQueue.get(0);

                System.out.println("Start of " + systemClock + ": CPU empty - P" + cpuJob.getJobNumber()
                        + " moved from ready queue into CPU - CPU burst of " + cpuJob.getBurstValues().get(cpuJob.getCurrentBurst()));
                try {
                    logFile.write("Start of " + systemClock + ": CPU empty - P" + cpuJob.getJobNumber()
                            + " moved from ready queue into CPU - CPU burst of " + cpuJob.getBurstValues().get(cpuJob.getCurrentBurst()) + "\n");
                    outputFile.write("P" + cpuJob.getJobNumber() + " changes state from Ready to Running\n");
                } catch (IOException ex) {
                    Logger.getLogger(CPUJobScheduler.class.getName()).log(Level.SEVERE, null, ex);
                }

                cpuIsEmpty = false;
                readyQueue.remove(0);
                actionPerformed = true;
            }

            //add job to io if it is empty
            if (ioIsEmpty && ioQueue.size() > 0 && ioQueue.get(0) != null) {
                ioJob = ioQueue.get(0);

                System.out.println("Start of " + systemClock + ": IO empty - P" + ioJob.getJobNumber()
                        + " moved into IO - IO burst of " + ioJob.getBurstValues().get(ioJob.getCurrentBurst()));
                try {
                    logFile.write("Start of " + systemClock + ": IO empty - P" + ioJob.getJobNumber()
                            + " moved into IO - IO burst of " + ioJob.getBurstValues().get(ioJob.getCurrentBurst()) + "\n");
                } catch (IOException ex) {
                    Logger.getLogger(CPUJobScheduler.class.getName()).log(Level.SEVERE, null, ex);
                }

                ioIsEmpty = false;
                ioQueue.remove(0);
                actionPerformed = true;
            }

            //decrement counts in cpu/io
            //aka perform 1 time unit of operation in cpu/io
            if (!cpuIsEmpty) {
                cpuJob.setBurstValues(cpuJob.getCurrentBurst(), cpuJob.getBurstValues().get(cpuJob.getCurrentBurst()) - 1);
                cpuJob.setTimeProcessed(cpuJob.getTimeProcessed() + 1);
                cpuUtilization++;
            }
            if (!ioIsEmpty) {
                ioJob.setBurstValues(ioJob.getCurrentBurst(), ioJob.getBurstValues().get(ioJob.getCurrentBurst()) - 1);
            }

            //increment wait times
            if (!readyQueue.isEmpty()) {
                for (int i = 0; i < readyQueue.size(); ++i) {
                    readyQueue.get(i).setCpuWait(readyQueue.get(i).getCpuWait() + 1);
                }
            }
            if (!ioQueue.isEmpty()) {
                for (int i = 0; i < ioQueue.size(); ++i) {
                    ioQueue.get(i).setIoWait(ioQueue.get(i).getIoWait() + 1);
                }
            }

            //finish final CPU burst
            if (!cpuIsEmpty && (cpuJob.getBurstValues().size() == (cpuJob.getCurrentBurst() + 1)) && cpuJob.getBurstValues().get(cpuJob.getCurrentBurst()) == 0) {
                cpuJob.setTimeFinished(systemClock);
                cpuJob.setTurnAroundTime(systemClock - cpuJob.getArrivalTime() + 1);

                System.out.println("End of " + systemClock + ": P" + cpuJob.getJobNumber()
                        + " CPU burst completed - no more events for P"
                        + cpuJob.getJobNumber() + " - process terminated: "
                        + "Turn Around Time: " + (systemClock - cpuJob.getArrivalTime() + 1)
                        + ", Wait time: " + cpuJob.getCpuWait()
                        + ", I/O wait: " + cpuJob.getIoWait());
                try {
                    logFile.write("End of " + systemClock + ": P" + cpuJob.getJobNumber()
                            + " CPU burst completed - no more events for P"
                            + cpuJob.getJobNumber() + " - process terminated: "
                            + "Turn Around Time: " + (systemClock - cpuJob.getArrivalTime() + 1)
                            + ", Wait time: " + cpuJob.getCpuWait()
                            + ", I/O wait: " + cpuJob.getIoWait() + "\n");
                    outputFile.write("P" + cpuJob.getJobNumber() + " changes state from Running to Terminated\n");
                } catch (IOException ex) {
                    Logger.getLogger(CPUJobScheduler.class.getName()).log(Level.SEVERE, null, ex);
                }

                completedList.add(cpuJob);
                cpuJob = null;
                cpuIsEmpty = true;
                actionPerformed = true;
            }

            //print no event if nothing happened or only processing took place
            if (!actionPerformed) {
                System.out.println("no event");
                try {
                    logFile.write("no event\n");
                    outputFile.write("\n");
                } catch (IOException ex) {
                    Logger.getLogger(CPUJobScheduler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            actionPerformed = false;
            systemClock++;
        }//end sim while

        //calculate metrics
        if (systemClock > 0) {
            cpuUtilization /= systemClock;
            cpuUtilization *= 100;
        }
        for (int i = 0; i < completedList.size(); ++i) {
            avgCpuWait += completedList.get(i).getCpuWait();
            avgIoWait += completedList.get(i).getIoWait();
            avgTat += completedList.get(i).getTurnAroundTime();
        }
        if (completedList.size() > 0) {
            avgCpuWait /= completedList.size();
            avgIoWait /= completedList.size();
            avgTat /= completedList.size();
        }
        System.out.println("Metrics:\n"
                + "CPU utilization: " + cpuUtilization + "%\n"
                + "Average Turn Around Time: " + avgTat + " time units\n"
                + "Average wait time: " + avgCpuWait + " time units\n"
                + "Average IO wait time: " + avgIoWait + " time units\n");
        try {
            outputFile.write("Metrics:\n"
                    + "CPU utilization: " + cpuUtilization + "%\n"
                    + "Average Turn Around Time: " + avgTat + " time units\n"
                    + "Average wait time: " + avgCpuWait + " time units\n"
                    + "Average IO wait time: " + avgIoWait + " time units\n");
        } catch (IOException ex) {
            Logger.getLogger(CPUJobScheduler.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            logFile.close();
            outputFile.close();
        } catch (IOException ex) {
            Logger.getLogger(CPUJobScheduler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//end main    
}//end CPUJobScheduler
