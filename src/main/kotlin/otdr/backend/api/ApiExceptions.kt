package otdr.backend.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("ApiException")
sealed class ApiException : Exception()

@Serializable
@SerialName("GenericApiException")
class GenericApiException(val reason: String) : ApiException()

@Serializable
@SerialName("NotFoundException")
class NotFoundException : ApiException()

@Serializable
@SerialName("ConflictException")
class ConflictException : ApiException()
