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
@SerialName("GetMessageRequest")
data class GetMessageRequest(
    val messageID: String
) : GetRequest()

@Serializable
@SerialName("GetUserRequest")
data class GetUserRequest(
    val userID: String
) : GetRequest()

@Serializable
@SerialName("GetGroupRequest")
data class GetGroupRequest(
    val groupID: String
) : GetRequest()

@Serializable
@SerialName("GetTripRequest")
data class GetTripRequest(
    val tripID: String
): GetRequest()

@Serializable
sealed class CreateRequest : Request()

@Serializable
@SerialName("CreateUserRequest")
data class CreateUserRequest(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val password: String
) : CreateRequest()

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
): FindRequest()
