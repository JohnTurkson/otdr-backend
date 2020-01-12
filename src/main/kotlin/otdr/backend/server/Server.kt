package otdr.backend.server

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.cio.CIO
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import otdr.backend.api.Data

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
    val database = Database("localhost")
    val encoder =
        Json(JsonConfiguration(encodeDefaults = false, strictMode = false, prettyPrint = true))
    
    routing {
        get("/user/{username}") {
            kotlin.runCatching {
                database.getUser(call.parameters["username"] ?: throw Exception())
            }.onSuccess {
                call.respond(HttpStatusCode.OK, encoder.stringify(Data.serializer(), it))
            }.onFailure {
                it.printStackTrace()
                call.respond(HttpStatusCode.NotFound, "User not found")
            }
        }
    }
}
