package otdr.backend.api

import kotlinx.serialization.Serializable

@Serializable
data class UserSelector(val name: String = "", val email: String = "")
