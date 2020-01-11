package otdr.backend.server

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.cio.CIO
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer

fun main() {
    val environment = applicationEngineEnvironment {
        module {
            main()
        }
        
        connector {
            host = "0.0.0.0"
            port = 7002
        }
    }
    
    val server = embeddedServer(CIO, environment)
    server.start(wait = true)
}

fun Application.main() {
    routing {
        get("/") {
            call.respond("success")
        }
    }
}
