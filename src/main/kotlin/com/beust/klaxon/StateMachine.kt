package com.beust.klaxon

private data class TokenStatus(val status: Status, val tokenType: Type)

class StateMachine(private val streaming: Boolean) {
    private val map = hashMapOf<TokenStatus, (world: World, token: Token) -> World>()

    fun put(status: Status, tokenType: Type, processor: (world: World, token: Token) -> World) {
        map.put(TokenStatus(status, tokenType), processor)
    }

    fun next(world: World, token: Token) : World {
        val pair = TokenStatus(world.status, token.tokenType)
        val processor = map[pair]

        return if (processor != null) {
            processor(world, token)
        } else {
            if (!streaming) {
                val message = "Did not expect ${token.tokenType} at line ${world.line}" +
                    " (internal error: \"No processor found for: (${world.status}, $token)\""
                throw KlaxonException(message)
            } else {
                World(Status.EOF)
            }
        }
    }
}