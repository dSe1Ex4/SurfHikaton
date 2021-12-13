package test.surf.surfhikaton.model.dto

data class CatRandomResponse(
    val breeds: List<String> = listOf(),
    val id: String,
    val url: String,
    val width: Int,
    val height: Int
)
