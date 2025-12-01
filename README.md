# MissAmericaScoringSystem

## Overview
MissAmericaScoringSystem is a Java console application that simulates a multi-phase pageant scoring system (inspired by Miss America). The program supports both **local** and **state** pageants and enforces different flows depending on the type:

- **Local pageant**: scores all five phases (Interview, OSQ, Fitness, Talent, Evening Gown), drops highest and lowest judge scores per phase, applies standard weights, and prints a ranked result. Local contests do not require 20+ contestants and do not perform Top 15 → Top 5 cuts.
- **State pageant**: full flow requiring at least 20 contestants. Judges score all contestants in all phases, highest and lowest judge scores per phase are dropped, totals are computed and Top 15 are selected. Top 15 are rescored (no Interview), Top 5 are selected, and judges rank the Top 5 to determine the winner and runners-up.

## Features
- Menu-driven console UI
- Input validation for numerical inputs
- Drop highest and lowest judge scores per phase before averaging
- Weighted totals and ranking
- Top 15 → Top 5 flow for state contests
- Final ranking via judge-provided ranks for Top 5 finalists
- Clear modular methods for maintainability

## Files
- `src/MissAmericaScoringSystem.java` — main program (menu-driven)
- `docs/UML_Diagram.png` — UML diagram (if you render PlantUML)
- `README.md` — this file

## Requirements
- Java 11+ (or any modern Java SE)
- Terminal / Command Prompt

## Compile and Run
1. Save `MissAmericaScoringSystem.java` in a `src` folder.
2. From the project root:
   ```bash
   javac src/MissAmericaScoringSystem.java
   java -cp src MissAmericaScoringSystem
