package otdr.backend.server

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.cio.CIO
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import otdr.backend.api.*

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
        install(ContentNegotiation)
        get("/user/{username}") {
            kotlin.runCatching {
                val username = call.parameters["username"]
                if (username.isNullOrBlank()) throw GeneralApiException("Invalid username")
                database.getUser(username)
            }.onSuccess {
                call.respond(HttpStatusCode.OK, encoder.stringify(Data.serializer(), it))
            }.onFailure {
                it.printStackTrace()
                call.respond(
                    HttpStatusCode.BadRequest,
                    encoder.stringify(ApiException.serializer(), it as ApiException)
                )
            }
        }
        
        get("/trip/{tripId}") {
            kotlin.runCatching {
                val id = call.parameters["tripId"]
                if (id.isNullOrBlank()) throw GeneralApiException("Invalid tripId")
                database.getTrip(id)
            }.onSuccess {
                call.respond(HttpStatusCode.OK, encoder.stringify(Data.serializer(), it))
            }.onFailure {
                it.printStackTrace()
                call.respond(
                    HttpStatusCode.BadRequest,
                    encoder.stringify(ApiException.serializer(), it as ApiException)
                )
            }
        }
        
        get("/user/{username}/trips") {
            kotlin.runCatching {
                val username = call.parameters["username"]
                if (username.isNullOrBlank()) throw GeneralApiException("Invalid username")
                val user = database.getUser(username)
                val trips: List<Trip> = listOf()
                trips
            }.onSuccess {
                call.respond(HttpStatusCode.OK, encoder.stringify(Data.serializer().list, it))
            }.onFailure {
                it.printStackTrace()
                call.respond(
                    HttpStatusCode.BadRequest,
                    encoder.stringify(ApiException.serializer(), it as ApiException)
                )
            }
        }
        
        post("/find/users") {
            val body = call.receiveText()
            if (body.isBlank()) throw GeneralApiException("Invalid body")
            kotlin.runCatching {
                val request = try {
                    encoder.parse(FindUserRequest.serializer(), body)
                } catch (e: Exception) {
                    throw GeneralApiException("Invalid request body")
                }
                database.findUsers(request)
            }.onSuccess {
                call.respond(HttpStatusCode.OK, encoder.stringify(Data.serializer().list, it))
            }.onFailure {
                it.printStackTrace()
                call.respond(
                    HttpStatusCode.BadRequest,
                    encoder.stringify(ApiException.serializer(), it as ApiException)
                )
            }
        }
        
        post("/find/trips") {
            val body = call.receiveText()
            if (body.isBlank()) throw GeneralApiException("Invalid body")
            kotlin.runCatching {
                val request = try {
                    encoder.parse(FindTripsRequest.serializer(), body)
                } catch (e: Exception) {
                    throw GeneralApiException("Invalid request body")
                }
                database.findTrips(request)
            }.onSuccess {
                call.respond(HttpStatusCode.OK, encoder.stringify(Data.serializer().list, it))
            }.onFailure {
                it.printStackTrace()
                call.respond(
                    HttpStatusCode.BadRequest,
                    encoder.stringify(ApiException.serializer(), it as ApiException)
                )
            }
        }
        
        post("/create/user") {
            val body = call.receiveText()
            if (body.isBlank()) throw GeneralApiException("Invalid body")
            kotlin.runCatching {
                val request = try {
                    encoder.parse(CreateUserRequest.serializer(), body)
                } catch (e: Exception) {
                    throw GeneralApiException("Invalid request body")
                }
                val (username, name, email, phone, password) = request
                val login = Login(username, password)
                val friends = emptyList<User>()
                val user = User(username, name, email, phone, friends)
                database.createLogin(login)
                database.createUser(user)
                user
            }.onSuccess {
                call.respond(HttpStatusCode.OK, encoder.stringify(Data.serializer(), it))
            }.onFailure {
                it.printStackTrace()
                call.respond(
                    HttpStatusCode.BadRequest,
                    encoder.stringify(ApiException.serializer(), it as ApiException)
                )
            }
        }
        
        post("/create/trip") {
            val body = call.receiveText()
            if (body.isBlank()) throw GeneralApiException("Invalid body")
            kotlin.runCatching {
                val request = try {
                    encoder.parse(CreateTripRequest.serializer(), body)
                } catch (e: Exception) {
                    throw GeneralApiException("Invalid request body")
                }
                val (name, start, end, creatorId, participantsIds) = request
                val id = ID.generate()
                val participants = participantsIds.map { database.getUser(it) }
                val returned = emptyList<User>()
                val trip = Trip(id, name, start, end, creatorId, participants, returned)
                database.createTrip(trip)
                trip
            }.onSuccess {
                call.respond(HttpStatusCode.OK, encoder.stringify(Data.serializer(), it))
            }.onFailure {
                it.printStackTrace()
                call.respond(
                    HttpStatusCode.BadRequest,
                    encoder.stringify(ApiException.serializer(), it as ApiException)
                )
            }
        }
    }
}
