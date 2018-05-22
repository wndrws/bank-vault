package kspt.bank

import kspt.bank.dao.InMemoryApplicationsRepository
import kspt.bank.dao.InMemoryClientsRepository
import kspt.bank.domain.CellApplicationInteractor
import kspt.bank.external.SimplePaymentSystem
import kspt.bank.services.ClientInfoService
import tornadofx.Controller

class ClientInfoServiceBean : Controller() {
    private val cellApplicationInteractorBean : CellApplicationInteractorBean by inject()
    val service by lazy { ClientInfoService(cellApplicationInteractorBean.interactor) }
}

class CellApplicationInteractorBean : Controller() {
    private val applicationsRepositoryBean : ApplicationsRepositoryBean by inject()

    private val clientsRepositoryBean : ClientsRepositoryBean by inject()

    private val paymentGateBean : PaymentGateBean by inject()

    val interactor : CellApplicationInteractor by lazy {
        CellApplicationInteractor(
                clientsRepositoryBean.repository,
                applicationsRepositoryBean.repository,
                paymentGateBean.paymentSystem
        )
    }
}

class ApplicationsRepositoryBean : Controller() {
    val repository = InMemoryApplicationsRepository()
}

class ClientsRepositoryBean : Controller() {
    val repository = InMemoryClientsRepository()
}

class PaymentGateBean : Controller() {
    val paymentSystem = SimplePaymentSystem()
}