package network.roanoke.ttms.utils

data class Move(
    val name: String,
    val id: Int,
    val type: String
)

data class Generation(
    val name: String,
    val moves: List<Move>
)

data class TmsData(
    val gens: List<Generation>
)
