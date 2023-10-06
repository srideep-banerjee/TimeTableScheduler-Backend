package org.example.algorithms;

import org.example.interfaces.GeneticTask;

public class GeneticThread extends Thread {
    private GeneticTask task;
    private int index;
    private int populationCount;
    private volatile boolean completed=true;

    public GeneticThread(int index, int populationCount){
        this.index=index;
        this.populationCount=populationCount;
    }

    public void perform(GeneticTask task){
        this.task = task;
        completed=false;
    }

    @Override
    public void run() {
        while(!isInterrupted()){
            if(!completed){
                task.run(index,populationCount);
                completed=true;
            }
        }
    }

    public boolean isCompleted() {
        return completed;
    }
}
