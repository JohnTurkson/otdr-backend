package otdr.backend.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Request

@Serializable
@SerialName("LoginRequest")
data class LoginRequest(
    val email: String,
    val password: String
) : Request()

@Serializable
sealed class GetRequest : Request()

@Serializable
@SerialName("GetUserRequest")
data class GetUserRequest(
    val userId: String
) : GetRequest()

@Serializable
@SerialName("GetTripRequest")
data class GetTripRequest(
    val tripId: String
) : GetRequest()

@Serializable
sealed class UpdateRequest : Request()

@Serializable
@SerialName("UpdateTripRequest")
data class UpdateTripRequest(val userId: String, val tripId: String) : UpdateRequest()

@Serializable
sealed class CreateRequest : Request()

@Serializable
@SerialName("CreateUserRequest")
data class CreateUserRequest(
    val username: String,
    val name: String,
    val email: String,
    val phone: String,
    val password: String
) : CreateRequest()

@Serializable
@SerialName("CreateTripRequest")
data class CreateTripRequest(
    val name: String,
    val start: String,
    val end: String,
    val creatorId: String,
    val participantIds: List<String>
)

@Serializable
sealed class FindRequest : Request()

@Serializable
@SerialName("FindUserRequest")
data class FindUserRequest(
    @SerialName("selector") val userSelector: UserSelector
) : FindRequest()

@Serializable
@SerialName("FindTripsRequest")
data class FindTripsRequest(
    @SerialName("selector") val tripSelector: TripSelector
) : FindRequest()
