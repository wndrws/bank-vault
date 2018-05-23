package kspt.bank.boundaries;

import kspt.bank.messaging.Request;
import kspt.bank.messaging.Response;

public interface ResponseGate {
    void notifyAsCompleted(Request request);

    void notifyAsFailed(Request request, String reason);

    void answerWithPayload(Request request, Response response);
}
