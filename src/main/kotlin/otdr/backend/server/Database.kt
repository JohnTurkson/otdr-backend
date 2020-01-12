package otdr.backend.server

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import otdr.backend.api.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class Database(
    private val host: String,
    private val port: Int = 5984,
    private val loginDatabase: String = "logins",
    private val userDatabase: String = "users",
    private val tripDatabase: String = "trips"
) {
    private val client = HttpClient.newHttpClient()
    private val encoder =
        Json(JsonConfiguration(encodeDefaults = false, strictMode = false, prettyPrint = true))
    
    private suspend fun query(
        database: String,
        parameter: String,
        type: HttpMethod,
        content: String = ""
    ): HttpResponse<String> {
        val builder = HttpRequest.newBuilder(URI.create("http://$host:$port/$database/$parameter"))
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
        
        when (type) {
            HttpMethod.Get -> builder.GET()
            HttpMethod.Post -> builder.POST(HttpRequest.BodyPublishers.ofString(content))
            HttpMethod.Put -> builder.PUT(HttpRequest.BodyPublishers.ofString(content))
        }
        
        val response = client.sendAsync(
            builder.build(), HttpResponse.BodyHandlers.ofString()
        ).join()
        return response
    }
    
    private suspend fun exists(database: String, id: String): Boolean {
        val method = HttpMethod.Head
        val response = query(database, id, method)
        return response.statusCode() == HttpStatusCode.OK.value
    }
    
    suspend fun userExists(userId: String): Boolean {
        return exists(userDatabase, userId)
    }
    
    private suspend fun get(database: String, id: String): Data {
        val method = HttpMethod.Get
        val response = query(database, id, method)
        when (response.statusCode()) {
            HttpStatusCode.BadRequest.value -> TODO()
            HttpStatusCode.Unauthorized.value -> TODO()
            HttpStatusCode.NotFound.value -> throw NotFoundException()
        }
        return encoder.parse(Data.serializer(), response.body())
    }
    
    suspend fun getUser(userId: String): User {
        return get(userDatabase, userId) as User
    }
    
    suspend fun getTrip(tripId: String): Trip {
        return get(tripDatabase, tripId) as Trip
    }
    
    private suspend fun <T : Data> create(database: String, item: T) {
        val method = HttpMethod.Put
        val id = item.id
        val json = encoder.stringify(Data.serializer(), item)
        val response = query(database, id, method, json)
        when (response.statusCode()) {
            HttpStatusCode.BadRequest.value -> TODO()
            HttpStatusCode.Unauthorized.value -> TODO()
            HttpStatusCode.NotFound.value -> throw NotFoundException()
            HttpStatusCode.Conflict.value -> throw ConflictException()
        }
    }
    
    suspend fun createLogin(login: Login) {
        create(loginDatabase, login)
    }
    
    suspend fun createUser(user: User) {
        create(userDatabase, user)
    }
    
    suspend fun createTrip(trip: Trip) {
        create(tripDatabase, trip)
    }
    
    private suspend fun find(database: String, selector: String): String {
        val parameter = "_find"
        val method = HttpMethod.Post
        val response = query(database, parameter, method, selector)
        when (response.statusCode()) {
            HttpStatusCode.BadRequest.value -> throw GeneralApiException("bad request")
            HttpStatusCode.Unauthorized.value -> throw GeneralApiException("unauthorized")
            HttpStatusCode.InternalServerError.value -> throw GeneralApiException("internal server error")
        }
        return response.body()
    }
    
    suspend fun findUsers(request: FindUserRequest): List<User> {
        val selector = encoder.stringify(FindUserRequest.serializer(), request)
        val response = find(userDatabase, selector)
        val wrapper = encoder.parse(Wrapper.serializer(User.serializer().list), response)
        return wrapper.contents
    }
    
    suspend fun findTrips(request: FindTripsRequest): List<Trip> {
        val selector = encoder.stringify(FindTripsRequest.serializer(), request)
        val response = find(tripDatabase, selector)
        val wrapper = encoder.parse(Wrapper.serializer(Trip.serializer().list), response)
        return wrapper.contents
    }
}
