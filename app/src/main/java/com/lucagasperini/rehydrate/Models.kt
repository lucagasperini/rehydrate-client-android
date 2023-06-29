package com.lucagasperini.rehydrate

import kotlinx.serialization.Serializable

// those class are used by json to decode information
@Serializable
data class HelloModel(val name: String, val version: Int)
@Serializable
data class PlanModel(val date: String, val quantity: Int)

@Serializable
data class PlanRequestModel(val plan: List<PlanModel>, val need: Int)

@Serializable
data class ReceiveRequestModel(val date: String, val quantity: Int)