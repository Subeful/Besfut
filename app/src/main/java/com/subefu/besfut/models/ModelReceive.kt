package com.subefu.besfut.models


sealed class ReceiveObj{
data class ReceiveItem(val name: String, val price: Int, val categoryName: String, val quantity: Int): ReceiveObj()
data class ReceiveReward(val name: String, val price: Int, val categoryName: String, val isSeries: Int): ReceiveObj()
}
