package kspt.bank.boundaries;

import kspt.bank.messaging.RequestWithCellChoice;
import kspt.bank.messaging.RequestWithClientInfo;
import kspt.bank.messaging.RequestWithPayment;

public interface RequestGate {
    void acceptClientInfo(RequestWithClientInfo info);

    void acceptCellChoice(RequestWithCellChoice choice);

    void acceptPayment(RequestWithPayment payment);
}
