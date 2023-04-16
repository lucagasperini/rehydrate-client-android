package com.lucagasperini.rehydrate

import kotlinx.serialization.Serializable
import java.security.InvalidParameterException
import java.text.FieldPosition
import java.text.Format
import java.text.ParsePosition

@Serializable
data class PlanModel(val date: String, val quantity: Int)

@Serializable
data class PlanRequestModel(val plan: List<PlanModel>, val need: Int)

@Serializable
data class ReceiveRequestModel(val date: String, val quantity: Int)