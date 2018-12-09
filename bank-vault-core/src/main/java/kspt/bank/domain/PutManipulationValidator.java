package kspt.bank.domain;

import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.Precious;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PutManipulationValidator {
    static void checkManipulation(final Cell cell, final Precious precious) {
        if (!cell.isEmpty()) {
            throw new IllegalStateException("Cell " + cell + " already contains a precious "
                    + prettyPrint(cell.getContainedPrecious()));
        }
        if (!canBeFit(cell, precious)) {
            final String msg = "Precious " + prettyPrint(precious) + " is too big for cell " +
                    cell.getId() + " of size " + cell.getSize();
            log.warn(msg);
            throw new ManipulationNotAllowed(msg);
        }
    }

    private static String prettyPrint(final Precious precious) {
        return "\"" + precious.getName() + "\" with volume " + precious.getVolume();
    }

    private static boolean canBeFit(final Cell cell, final Precious precious) {
        return cell.getSize().getVolume() >= precious.getVolume();
    }

    public static class ManipulationNotAllowed extends RuntimeException {
        ManipulationNotAllowed(String msg) {
            super(msg);
        }
    }
}
