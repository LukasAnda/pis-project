package sk.pismaniacs.fiitcompany.model.requests

import sk.pismaniacs.fiitcompany.model.Item


data class ModifyRequest(
        val item_ids: List<String> = listOf()
)

data class ModifyRequest2(
        val item_ids: List<Item> = listOf()
)

data class ModifyResponse(
        val data: String
)