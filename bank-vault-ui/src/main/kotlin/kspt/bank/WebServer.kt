//package kspt.bank
//
//import io.ktor.application.call
//import io.ktor.http.ContentType
//import io.ktor.response.respondText
//import io.ktor.routing.get
//import io.ktor.routing.routing
//import io.ktor.server.engine.embeddedServer
//import io.ktor.server.netty.Netty
//import kspt.bank.views.ClientMainView
//import tornadofx.Controller
//import tornadofx.JsonBuilder
//import java.util.concurrent.TimeUnit
//
//class WebServer : Controller() {
//    val server = embeddedServer(Netty, port = 8080) {
//        routing {
//            get("/") {
//                call.respondText("Bank Vault Application is ON", ContentType.Text.Plain)
//            }
//            get("/cellTable") {
//                val list = find(ClientMainView::class).cellTableItems.toList()
//                val builder = JsonBuilder()
//                builder.add("cells",  list.map { entry -> JsonBuilder()
//                        .add("id", entry.id)
//                        .add("status", entry.status)
//                        .add("cellSize", entry.size)
//                        .add("containedPrecious", entry.precious)
//                        .add("leaseEnding", entry.leaseEnding)
//                        .build() })
//                call.respondText(builder.build().toString())
//            }
//        }
//    }
//
//    fun start() = server.start(wait = true)
//
//    fun stop() = server.stop(2, 5, TimeUnit.SECONDS)
//}