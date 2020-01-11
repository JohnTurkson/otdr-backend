package otdr.backend.api

import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName

@Serializable
data class Wrapper<T>(@SerialName("docs") val contents: T)
