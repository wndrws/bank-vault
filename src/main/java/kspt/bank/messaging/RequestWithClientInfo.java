package kspt.bank.messaging;

import kspt.bank.domain.entities.PassportInfo;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RequestWithClientInfo extends Request {
    public final PassportInfo passportInfo;
}
