package com.aim.project.ssp.runners;

import com.aim.project.ssp.hyperheuristics.ChoiceFunctionHH;
import AbstractClasses.HyperHeuristic;

/**
 * Runs the Choice Function‑based hyper‑heuristic.
 */
public class  ChoiceFunction_VisualRunner extends HH_Runner_Visual {

    @Override
    protected HyperHeuristic getHyperHeuristic(long seed) {
        return new ChoiceFunctionHH(seed);
    }

    public static void main(String[] args) {
        HH_Runner_Visual runner = new ChoiceFunction_VisualRunner();
        runner.run();
    }
}
