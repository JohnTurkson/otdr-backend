package otdr.backend.server

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Revision(@SerialName("_rev") val version: String)
