package otdr.backend.server

import io.ktor.application.Application
import io.ktor.application.call
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
import java.time.Instant

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
        
        get("/trip/{tripId}/participants") {
            kotlin.runCatching {
                val id = call.parameters["tripId"]
                if (id.isNullOrBlank()) throw GeneralApiException("Invalid tripId")
                val trip = database.getTrip(id)
                trip.participantIds.map { database.getUser(it) }
            }.onSuccess {
                call.respond(HttpStatusCode.OK, encoder.stringify(Data.serializer().list, it))
            }.onFailure {
                call.respond(
                    HttpStatusCode.BadRequest,
                    encoder.stringify(ApiException.serializer(), it as ApiException)
                )
            }
        }
        
        get("/trip/{tripId}/returned") {
            kotlin.runCatching {
                val id = call.parameters["tripId"]
                if (id.isNullOrBlank()) throw GeneralApiException("Invalid tripId")
                val trip = database.getTrip(id)
                trip.returnedIds.map { database.getUser(it) }
            }.onSuccess {
                call.respond(HttpStatusCode.OK, encoder.stringify(Data.serializer().list, it))
            }.onFailure {
                call.respond(
                    HttpStatusCode.BadRequest,
                    encoder.stringify(ApiException.serializer(), it as ApiException)
                )
            }
        }
        
        get("/trip/{tripId}/unaccounted") {
            kotlin.runCatching {
                val id = call.parameters["tripId"]
                if (id.isNullOrBlank()) throw GeneralApiException("Invalid tripId")
                val trip = database.getTrip(id)
                trip.participantIds.minus(trip.returnedIds).map { database.getUser(it) }
            }.onSuccess {
                call.respond(HttpStatusCode.OK, encoder.stringify(Data.serializer().list, it))
            }.onFailure {
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
                val request = FindTripsRequest(TripSelector(creatorId = username))
                val trips: List<Trip> = database.findTrips(request)
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
        
        get("/user/{username}/trips/active") {
            kotlin.runCatching {
                val username = call.parameters["username"]
                if (username.isNullOrBlank()) throw GeneralApiException("Invalid username")
                val user = database.getUser(username)
                val trips = database.findTrips(FindTripsRequest(TripSelector(user.id)))
                trips.filter { it.participantIds.contains(user.id) }
                    .filter {
                        Instant.now().toString() >= it.start && Instant.now().toString() <= it.end
                    }
            }.onSuccess {
                call.respond(HttpStatusCode.OK, encoder.stringify(Data.serializer().list, it))
            }.onFailure {
                call.respond(
                    HttpStatusCode.BadRequest,
                    encoder.stringify(ApiException.serializer(), it as ApiException)
                )
            }
        }
        
        get("/user/{username}/trips/returned") {
            kotlin.runCatching {
                val username = call.parameters["username"]
                if (username.isNullOrBlank()) throw GeneralApiException("Invalid username")
                val user = database.getUser(username)
                val trips = database.findTrips(FindTripsRequest(TripSelector(user.id)))
                trips.filter { it.returnedIds.contains(user.id) }
            }.onSuccess {
                call.respond(HttpStatusCode.OK, encoder.stringify(Data.serializer().list, it))
            }.onFailure {
                call.respond(
                    HttpStatusCode.BadRequest,
                    encoder.stringify(ApiException.serializer(), it as ApiException)
                )
            }
        }
        
        get("/user/{username}/trips/unaccounted") {
            kotlin.runCatching {
                val username = call.parameters["username"]
                if (username.isNullOrBlank()) throw GeneralApiException("Invalid username")
                val user = database.getUser(username)
                val trips = database.findTrips(FindTripsRequest(TripSelector(user.id)))
                trips.filter { it.participantIds.minus(it.returnedIds).contains(user.id) }
                    .filter { it.end <= Instant.now().toString() }
            }.onSuccess {
                call.respond(HttpStatusCode.OK, encoder.stringify(Data.serializer().list, it))
            }.onFailure {
                call.respond(
                    HttpStatusCode.BadRequest,
                    encoder.stringify(ApiException.serializer(), it as ApiException)
                )
            }
        }
        
        get("/user/{username}/friends") {
            kotlin.runCatching {
                val username = call.parameters["username"]
                if (username.isNullOrBlank()) throw GeneralApiException("Invalid username")
                val user = database.getUser(username)
                user.friends.map { database.getUser(it) }
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
        
        post("/trip/add") {
            kotlin.runCatching {
                val body = call.receiveText()
                if (body.isBlank()) throw GeneralApiException("Invalid body")
                val (userId, tripId) = try {
                    encoder.parse(UpdateTripRequest.serializer(), body)
                } catch (e: Exception) {
                    throw GeneralApiException("Invalid request body")
                }
                database.addTripParticipant(userId, tripId)
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
        
        post("/trip/remove") {
            kotlin.runCatching {
                val body = call.receiveText()
                if (body.isBlank()) throw GeneralApiException("Invalid body")
                val (userId, tripId) = try {
                    encoder.parse(UpdateTripRequest.serializer(), body)
                } catch (e: Exception) {
                    throw GeneralApiException("Invalid request body")
                }
                database.removeTripParticipant(userId, tripId)
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
        
        post("/trip/set/returned") {
            kotlin.runCatching {
                val body = call.receiveText()
                if (body.isBlank()) throw GeneralApiException("Invalid body")
                val (userId, tripId) = try {
                    encoder.parse(UpdateTripRequest.serializer(), body)
                } catch (e: Exception) {
                    throw GeneralApiException("Invalid request body")
                }
                database.setUserReturned(userId, tripId)
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
        
        post("/trip/set/unaccounted") {
            kotlin.runCatching {
                val body = call.receiveText()
                if (body.isBlank()) throw GeneralApiException("Invalid body")
                val (userId, tripId) = try {
                    encoder.parse(UpdateTripRequest.serializer(), body)
                } catch (e: Exception) {
                    throw GeneralApiException("Invalid request body")
                }
                database.setUserUnaccounted(userId, tripId)
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
        
        post("/find/users") {
            kotlin.runCatching {
                val body = call.receiveText()
                if (body.isBlank()) throw GeneralApiException("Invalid body")
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
            kotlin.runCatching {
                val body = call.receiveText()
                if (body.isBlank()) throw GeneralApiException("Invalid body")
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
            kotlin.runCatching {
                val body = call.receiveText()
                if (body.isBlank()) throw GeneralApiException("Invalid body")
                val request = try {
                    encoder.parse(CreateUserRequest.serializer(), body)
                } catch (e: Exception) {
                    throw GeneralApiException("Invalid request body")
                }
                val (username, name, email, phone, password) = request
                val login = Login(username, password)
                val friends = emptyList<String>()
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
            kotlin.runCatching {
                val body = call.receiveText()
                if (body.isBlank()) throw GeneralApiException("Invalid body")
                val request = try {
                    encoder.parse(CreateTripRequest.serializer(), body)
                } catch (e: Exception) {
                    throw GeneralApiException("Invalid request body")
                }
                val (name, start, end, creatorId, participantIds) = request
                val id = ID.generate()
                val returnedIds = emptyList<String>()
                val trip = Trip(id, name, start, end, creatorId, participantIds, returnedIds)
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
