package com.to.kotlinmessenger.activity.message

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.to.kotlinmessenger.R
import com.to.kotlinmessenger.activity.account.RegisterActivity
import com.to.kotlinmessenger.activity.message.dialog.UserSettingsChangeListener
import com.to.kotlinmessenger.activity.message.dialog.UserSettingsDialogFragment
import com.to.kotlinmessenger.model.ChatMessage
import com.to.kotlinmessenger.model.User
import com.to.kotlinmessenger.util.*
import com.to.kotlinmessenger.view.LatestMessageRow
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_latest_messages.*

/**
 * 最新メッセージ画面。
 */
class LatestMessagesActivity : AppCompatActivity() {

    companion object {
        /** 現在ログイン中のユーザー */
        var currentUser: User? = null

        /**
         * `LatestMessagesActivity`を起動するためのIntentを作成する。
         *
         * @param context コンテキスト
         * @return `LatestMessagesActivity`を起動するためのIntent
         */
        fun createIntent(context: Context): Intent {
            val intent = Intent(context, LatestMessagesActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            return intent
        }
    }

    /** 最新メッセージ画面のオプションメニュー **/
    private var optionsMenu: Menu? = null

    /** 最新メッセージ画面の各行に対応するAdapter */
    private val latestMessagesAdapter = GroupAdapter<GroupieViewHolder>()

    /** チャット相手のユーザーIDごとに最新メッセージを保持するMap */
    private val latestMessagesMap = mutableMapOf<String, ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        // ログイン中かチェック
        verifyUserIsLoggedIn()

        recyclerview_latest_messages.adapter = latestMessagesAdapter
        recyclerview_latest_messages.isFocusable = false
        recyclerview_latest_messages.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        swiperefresh_latest_messages.setColorSchemeColors(
            ContextCompat.getColor(
                this,
                R.color.colorAccent
            )
        )

        // ログイン中のユーザー情報を取得
        fetchCurrentUser()

        // 最新メッセージを取得
        listenForLatestMessages()

        latestMessagesAdapter.setOnItemClickListener { item, view ->
            val row = item as LatestMessageRow
            // チャットログ画面へ遷移
            startActivity(
                ChatLogActivity.createIntent(
                    view.context,
                    requireNotNull(row.chatPartnerUser) { "chatPartnerUser must be initialized" }
                )
            )
        }

        new_message_fab.setOnClickListener {
            // 新規メッセージ画面へ遷移
            startActivity(
                NewMessageActivity.createIntent(this)
            )
        }

        swiperefresh_latest_messages.setOnRefreshListener {
            verifyUserIsLoggedIn()
            fetchCurrentUser()
            listenForLatestMessages()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        optionsMenu = menu
        menuInflater.inflate(R.menu.nav_menu, menu)

        // メニューにプロフィール画像を表示
        showProfileImageIntoMenu()

        return super.onCreateOptionsMenu(menu)
    }

    /**
     * ユーザーがログイン済みかどうかチェックする。
     */
    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuthAccessor.getUid()
        if (uid == null) {
            startActivity(RegisterActivity.createIntent(this))
        }
    }

    /**
     * ログイン中のユーザー情報を取得する。
     */
    private fun fetchCurrentUser() {
        val uid = FirebaseAuthAccessor.getUid() ?: return
        val ref = FirebaseDatabaseAccessor.getUsersRef(uid)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)
                logDebug("Current user is ${currentUser?.username}")

                // メニューにプロフィール画像を表示
                showProfileImageIntoMenu()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /**
     * 最新メッセージを取得する。
     */
    private fun listenForLatestMessages() {
        swiperefresh_latest_messages.isRefreshing = true
        val fromId = FirebaseAuthAccessor.getUid() ?: return
        val ref = FirebaseDatabaseAccessor.getLatestMessagesRef(fromId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                logDebug("Database error: ${databaseError.message}")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                logDebug("has children: ${dataSnapshot.hasChildren()}")
                if (!dataSnapshot.hasChildren()) {
                    // メッセージのやり取りがない場合はここでリフレッシュ動作を終了させる
                    swiperefresh_latest_messages.isRefreshing = false
                }
            }
        })

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                updateLatestMessages(snapshot)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                updateLatestMessages(snapshot)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onCancelled(error: DatabaseError) {}

            private fun updateLatestMessages(snapshot: DataSnapshot) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                // チャット相手ユーザーごとの最新メッセージを更新する
                latestMessagesMap[snapshot.key!!] = chatMessage
                // 更新結果を画面に反映する
                refreshRecyclerViewMessages()
            }
        })
    }

    /**
     * 最新メッセージ画面のRecyclerViewをリフレッシュする。
     */
    private fun refreshRecyclerViewMessages() {
        latestMessagesAdapter.clear()
        latestMessagesMap.values
            // 新しい順に表示する
            .sortedByDescending { it.timestamp }
            .forEach {
                val (date, time) = getFormattedDateTime(this, it.timestamp)
                latestMessagesAdapter.add(LatestMessageRow(this, it, date, time))
            }
        swiperefresh_latest_messages.isRefreshing = false
    }

    /**
     * オプションメニュー上にプロフィール画像を表示する。
     *
     * 合わせて、プロフィール画像のリスナーも設定する。
     */
    private fun showProfileImageIntoMenu() {
        if (optionsMenu == null || currentUser == null) {
            // ログアウトしてから再度ログインするとmenuとcurrentUserの初期化タイミングが変わるので、どちらか未設定なら処理しない
            // このメソッドはonCreateOptionsMenuと、fetchCurrentUserにて設定しているValueEventListenerのonDataChangeから呼ばれており、
            // どちらかのタイミングで処理が実行される
            logDebug("skip #showProfileImageIntoMenu() because menu is $optionsMenu, currentUser is $currentUser")
            return
        }
        logDebug("run #showProfileImageIntoMenu()")

        val optionsMenu = optionsMenu!!
        val currentUser = currentUser!!

        val profileImageView =
            optionsMenu.findItem(R.id.menu_user_settings).actionView as CircleImageView
        val progressBarItem = optionsMenu.findItem(R.id.menu_progressbar)

        // プロフィール画像をロード
        loadProfileImageIntoView(
            currentUser,
            profileImageView,
            ProgressBarImageLoadListener(progressBarItem)
        )

        profileImageView.setOnClickListener {
            // ユーザー設定ダイアログを起動
            val dialog = UserSettingsDialogFragment.newInstance(
                currentUser, createUserSettingsChangeListener(progressBarItem)
            )
            dialog.show(supportFragmentManager, dialog::class.simpleName)
        }
    }

    /**
     * `UserSettingsChangeListener`を構築し、ユーザー設定変更時に呼び出されるコールバックを設定する。
     *
     * @param progressBarItem オプションメニュー上のプログレスバー
     * @return ユーザー設定変更時時に使用される`UserSettingsChangeListener`
     */
    private fun createUserSettingsChangeListener(progressBarItem: MenuItem): UserSettingsChangeListener {
        return object : UserSettingsChangeListener() {
            override fun onStart(oldUser: User) {
                progressBarItem.isVisible = true
            }

            override fun onFinish(oldUser: User, newUser: User?) {
                // ここでは何もしない
                // ユーザー設定変更後に、fetchCurrentUserにて設定したValueEventListener#onChangeによりプロフィール画像のロードが行われるので、
                // プログレスバーを非表示にするのはメニュー上でプロフィール画像のロードが完了したタイミングに委ねる(ProgressBarImageLoadListener#onFinish)
                // ここでプログレスバーを非表示にすると、プロフィール画像のロードが完了する前にプログレスバーが非表示になってしまう
                // なおプロフィール画像は変更せずにユーザー名のみ変更した場合でも、ProgressBarImageLoadListener#onFinishは呼ばれる
            }

            override fun onSuccess(oldUser: User, newUser: User) {
                logDebug("Successfully changed user settings: oldUser=$oldUser, newUser=$newUser")

                // ユーザー設定変更を最新メッセージ画面に反映(自分宛てのチャット行に反映)
                refreshRecyclerViewMessages()

                Toast.makeText(
                    this@LatestMessagesActivity,
                    getString(R.string.change_user_settings_success),
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onFailure(ex: Exception, oldUser: User, newUser: User?) {
                logDebug("Failed to change user settings: oldUser=$oldUser, newUser=$newUser, ex=${ex}")
                Toast.makeText(
                    this@LatestMessagesActivity,
                    getString(R.string.change_user_settings_fail),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}