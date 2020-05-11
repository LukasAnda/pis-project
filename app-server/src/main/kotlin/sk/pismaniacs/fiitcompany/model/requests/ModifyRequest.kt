package sk.pismaniacs.fiitcompany.model.requests

import sk.pismaniacs.fiitcompany.model.Item


data class ModifyRequest(
        val item_ids: List<String> = listOf()
)

data class ModifyRequest2(
        val item_ids: List<Item> = listOf()
)

data class ModifyRequest3(
        val item_ids: ModifyItem = ModifyItem(),
        val report_id: List<String> = listOf()
)

data class ModifyItem(
        val id: String = "",
        val name: String = "",
        val price: String = ""
)

data class ModifyRequest4(
        val item_ids: List<String> = listOf(),
        val report_id: List<String> = listOf()
)

data class ModifyResponse(
        val data: String
)