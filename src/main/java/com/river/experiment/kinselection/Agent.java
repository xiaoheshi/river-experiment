package com.river.experiment.kinselection;

/**
 * 模拟中的个体，负责记录自己的直接适合度与包容适合度。
 */
final class Agent {

    private final Strategy strategy;
    private int familyId;

    private double directFitness;
    private double inclusiveFitness;
    private double benefitGiven;
    private double benefitReceived;

    Agent(Strategy strategy, int familyId) {
        this.strategy = strategy;
        this.familyId = familyId;
    }

    Strategy strategy() {
        return strategy;
    }

    int familyId() {
        return familyId;
    }

    void moveToFamily(int newFamilyId) {
        this.familyId = newFamilyId;
    }

    void resetForGeneration(double baseFitness) {
        directFitness = baseFitness;
        inclusiveFitness = baseFitness;
        benefitGiven = 0.0;
        benefitReceived = 0.0;
    }

    void addDirectFitness(double delta) {
        directFitness += delta;
    }

    void addBenefitReceived(double amount) {
        benefitReceived += amount;
    }

    void addBenefitGiven(double amount) {
        benefitGiven += amount;
    }

    void finalizeFitness(double relatedness) {
        directFitness += benefitReceived;
        inclusiveFitness = directFitness + relatedness * benefitGiven;
    }

    double directFitness() {
        return directFitness;
    }

    double inclusiveFitness() {
        return inclusiveFitness;
    }

    double benefitGiven() {
        return benefitGiven;
    }

    double benefitReceived() {
        return benefitReceived;
    }
}
