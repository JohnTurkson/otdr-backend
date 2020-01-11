package otdr.backend.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Data : Identifiable

@Serializable
data class Location(
    @SerialName("_id") override val id: String,
    val latitude: String,
    val longitude: String
) : Data()

@Serializable
data class Trip(
    @SerialName("_id") override val id: String,
    val name: String,
    val user: User
) : Data()

@Serializable
data class User(
    @SerialName("_id") override val id: String,
    val name: String
) : Data()
