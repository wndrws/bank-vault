package kspt.bank.domain;

import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.Precious;

public class PutManipulationValidator {
    static void checkManipulation(final Cell cell, final Precious precious) {
        if (!canBeFit(cell, precious)) {
           throw new ManipulationNotAllowed("Precious " + precious + " is too big for cell " + cell);
        }
    }

    static boolean canBeFit(final Cell cell, final Precious precious) {
        return cell.getSize().getVolume() >= precious.getVolume();
    }

    static class ManipulationNotAllowed extends RuntimeException {
        ManipulationNotAllowed(String msg) {
            super(msg);
        }
    }
}
