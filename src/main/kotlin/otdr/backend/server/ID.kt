package otdr.backend.server

import kotlin.random.Random
import kotlin.random.nextInt


object ID {
    fun generate(): String {
        var generated = ""
        repeat(32) { generated += Random.nextInt(0..15).toString(16) }
        return generated
    }
}
