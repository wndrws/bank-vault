package kspt.bank.domain;

import kspt.bank.boundaries.RequestGate;
import kspt.bank.boundaries.ResponseGate;
import kspt.bank.domain.entities.Cell;
import kspt.bank.messaging.RequestWithCellChoice;
import kspt.bank.messaging.RequestWithClientInfo;
import kspt.bank.messaging.RequestWithPayment;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class BankVaultFacade implements RequestGate {
    private final CellApplicationInteractor caInteractor;

    private final ResponseGate responseGate;

    @Override
    public void acceptClientInfo(RequestWithClientInfo request) {
        try {
            caInteractor.acceptClientInfo(request.passportInfo);
            responseGate.notifyAsCompleted(request);
        } catch (Throwable e) {
            responseGate.notifyAsFailed(request, e.getMessage());
        }
    }

    @Override
    public void acceptCellChoice(RequestWithCellChoice request) {
        try {
            final Optional<Cell> optionalCell = caInteractor.requestCellOfSize(request.cellSize);
            if (optionalCell.isPresent()) {
                responseGate.notifyAsCompleted(request);
            } else {
                responseGate.notifyAsFailed(request,
                        "No cells of size " + request.cellSize + " are available now.");
            }
        } catch (Throwable e) {
            responseGate.notifyAsFailed(request, e.getMessage());
        }
    }

    @Override
    public void acceptPayment(RequestWithPayment request) {
        try {
            caInteractor.acceptPayment(request.paymentSum, request.paymentMethod);
            responseGate.notifyAsCompleted(request);
        } catch (Throwable e) {
            responseGate.notifyAsFailed(request, e.getMessage());
        }
    }
}
