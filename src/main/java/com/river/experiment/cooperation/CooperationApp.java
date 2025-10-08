package com.river.experiment.cooperation;

import com.river.experiment.cooperation.CooperationExperiment.CooperationReport;

/**
 * 协同进化实验独立入口。
 */
public final class CooperationApp {

    public static void main(String[] args) {
        CooperationExperiment experiment = new CooperationExperiment();
        CooperationReport report = experiment.run();

        System.out.println("【协同的进化】" + report.sectionTitle());
        for (String paragraph : report.paragraphs()) {
            System.out.println(paragraph);
        }
    }

    private CooperationApp() {
    }
}
