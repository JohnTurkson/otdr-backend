package otdr.backend.server

import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.header
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import otdr.backend.api.*

class Database(
    private val host: String,
    private val port: Int = 5984,
    private val loginDatabase: String = "logins",
    private val userDatabase: String = "users",
    private val tripDatabase: String = "trips"
) {
    private val client = HttpClient(CIO)
    private val encoder =
        Json(JsonConfiguration(encodeDefaults = false, strictMode = false, prettyPrint = true))
    
    private suspend fun query(
        database: String,
        parameter: String,
        type: HttpMethod,
        content: String = ""
    ): HttpResponse {
        val call = client.call("http://$host:$port/$database/$parameter") {
            method = type
            header("Accept", "application/json")
            header("Content-Type", "application/json")
            body = content
        }
        return call.response
    }
    
    private suspend fun exists(database: String, id: String): Boolean {
        val method = HttpMethod.Head
        val response = query(database, id, method)
        return response.status == HttpStatusCode.OK
    }
    
    suspend fun userExists(userID: String): Boolean {
        return exists(userDatabase, userID)
    }
    
    private suspend fun get(database: String, id: String): Data {
        val method = HttpMethod.Get
        val response = query(database, id, method)
        when (response.status) {
            HttpStatusCode.BadRequest -> TODO()
            HttpStatusCode.Unauthorized -> TODO()
            HttpStatusCode.NotFound -> throw NotFoundException()
        }
        return encoder.parse(Data.serializer(), response.readText())
    }
    
    suspend fun getUser(userID: String): User {
        return get(userDatabase, userID) as User
    }
    
    private suspend fun <T : Data> create(database: String, item: T) {
        val method = HttpMethod.Put
        val id = item.id
        val json = encoder.stringify(Data.serializer(), item)
        val response = query(database, id, method, json)
        when (response.status) {
            HttpStatusCode.BadRequest -> TODO()
            HttpStatusCode.Unauthorized -> TODO()
            HttpStatusCode.NotFound -> throw NotFoundException()
            HttpStatusCode.Conflict -> throw ConflictException()
        }
    }
    
    suspend fun createLogin(login: Login) {
        create(loginDatabase, login)
    }
    
    suspend fun createUser(user: User) {
        create(userDatabase, user)
    }
    
    private suspend fun find(database: String, selector: String): String {
        val parameter = "_find"
        val method = HttpMethod.Post
        val response = query(database, parameter, method, selector)
        when (response.status) {
            HttpStatusCode.BadRequest -> TODO()
            HttpStatusCode.Unauthorized -> TODO()
            HttpStatusCode.InternalServerError -> TODO()
        }
        return response.readText()
    }
    
    suspend fun findUsers(userSelector: UserSelector): List<User> {
        val selector = encoder.stringify(UserSelector.serializer(), userSelector)
        val response = find(userDatabase, selector)
        val wrapper = encoder.parse(Wrapper.serializer(User.serializer().list), response)
        return wrapper.contents
    }
    
    suspend fun findTrips(tripSelector: TripSelector): List<Trip> {
        val selector = encoder.stringify(TripSelector.serializer(), tripSelector)
        val response = find(tripDatabase, selector)
        val wrapper = encoder.parse(Wrapper.serializer(Trip.serializer().list), response)
        return wrapper.contents
    }
}
