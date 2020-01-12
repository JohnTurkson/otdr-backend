package otdr.backend.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Data : Identifiable

@Serializable
@SerialName("Location")
data class Location(
    @SerialName("_id") override val id: String,
    val latitude: String,
    val longitude: String
) : Data()

@Serializable
@SerialName("Trip")
data class Trip(
    @SerialName("_id") override val id: String,
    val name: String,
    val start: String,
    val end: String,
    val userId: String,
    val participants: List<User>,
    val returned: List<User>
) : Data()

@Serializable
@SerialName("User")
data class User(
    @SerialName("_id") override val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val friends: List<User>
) : Data()

@Serializable
@SerialName("Login")
data class Login(
    @SerialName("_id") override val id: String,
    val password: String
) : Data()
