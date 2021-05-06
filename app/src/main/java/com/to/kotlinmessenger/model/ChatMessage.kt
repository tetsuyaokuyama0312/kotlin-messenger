package com.to.kotlinmessenger.model

/**
 * チャットメッセージを表すデータクラス。
 *
 * @property id チャットメッセージのID
 * @property text メッセージテキスト
 * @property fromId メッセージの送信元ユーザーのID
 * @property toId メッセージの送信先ユーザーのID
 * @property timestamp メッセージの送信時刻
 */
data class ChatMessage(
    val id: String, val text: String, val fromId: String,
    val toId: String, val timestamp: Long
) {
    constructor() : this("", "", "", "", -1)
}