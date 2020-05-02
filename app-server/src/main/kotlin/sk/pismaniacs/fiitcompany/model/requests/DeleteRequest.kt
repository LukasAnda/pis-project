package sk.pismaniacs.fiitcompany.model.requests


data class DeleteRequest(
        val item_ids: List<String> = listOf()
)

data class DeleteResponse(
        val response: String
)