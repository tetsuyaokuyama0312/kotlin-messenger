package com.to.kotlinmessenger.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

/**
 * 複数のEditTextに対する統一的な操作を可能にする`TextWatcher`。
 *
 * たとえば複数のEditTextがすべて入力されているときのみボタンを活性化するというような操作を
 * 実装する際に本クラスを利用できる。
 *
 * @property actionTiming 指定された操作を実行するタイミング
 * @property action 複数のEditTextに対して実行する操作
 *
 * @constructor
 * 複数のEditTextに対して指定された操作を指定されたタイミングで実行する`MultiTextWatcher`を構築する。
 * @param editTexts 統一的な操作を実行する対象のEditText(複数指定可)
 */
class MultiTextWatcher(
    vararg editTexts: EditText,
    private val actionTiming: ActionTiming = ActionTiming.ALL,
    private val action: (List<EditText>) -> Unit
) :
    TextWatcher {

    companion object {
        /** beforeTextChanged にて action を実行する ActionTiming のリスト */
        private val BEFORE_ACTION_TIMING = setOf(
            ActionTiming.BEFORE,
            ActionTiming.BEFORE_AND_ON, ActionTiming.BEFORE_AND_AFTER,
            ActionTiming.ALL
        )

        /** onTextChanged にて action を実行する ActionTiming のリスト */
        private val ON_ACTION_TIMING = setOf(
            ActionTiming.ON,
            ActionTiming.BEFORE_AND_ON, ActionTiming.ON_AND_AFTER,
            ActionTiming.ALL
        )

        /** afterTextChanged にて action を実行する ActionTiming のリスト */
        private val AFTER_ACTION_TIMING = setOf(
            ActionTiming.AFTER,
            ActionTiming.BEFORE_AND_AFTER, ActionTiming.ON_AND_AFTER,
            ActionTiming.ALL
        )
    }

    /** 操作対象のEditTextリスト */
    private val editTexts = editTexts.toList()

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        if (BEFORE_ACTION_TIMING.contains(actionTiming)) {
            action(editTexts)
        }
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (ON_ACTION_TIMING.contains(actionTiming)) {
            action(editTexts)
        }
    }

    override fun afterTextChanged(s: Editable?) {
        if (AFTER_ACTION_TIMING.contains(actionTiming)) {
            action(editTexts)
        }
    }

    /**
     * `MultiTextWatcher`にて操作を実行するタイミングを示す列挙型。
     */
    enum class ActionTiming {
        /** beforeTextChanged にて操作を実行する */
        BEFORE,

        /** onTextChanged にて操作を実行する */
        ON,

        /** afterTextChanged にて操作を実行する */
        AFTER,

        /** beforeTextChanged と onTextChanged にて操作を実行する */
        BEFORE_AND_ON,

        /** beforeTextChanged と afterTextChanged にて操作を実行する */
        BEFORE_AND_AFTER,

        /** onTextChanged と afterTextChanged にて操作を実行する */
        ON_AND_AFTER,

        /** beforeTextChanged と onTextChanged と afterTextChanged のすべてのタイミングにて操作を実行する */
        ALL
    }
}