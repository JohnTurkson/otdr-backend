package otdr.backend.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Wrapper<T>(@SerialName("docs") val contents: T)
