package kspt.bank.domain;

public class CellApplicationInteractor {

    public void acceptClientInfo(final PassportInfo clientInfo) {
        ClientPassportValidator.checkValidity(clientInfo);
    }

    //TODO chooseCellSize
}
