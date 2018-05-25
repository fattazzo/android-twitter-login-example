/*
 * Project: android-twitter-login-example
 * File: MainActivity.kt
 *
 * Created by fattazzo
 * Copyright © 2018 Gianluca Fattarsi. All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.gmail.fattazzo.twitterlogin.example

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.squareup.picasso.Picasso
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterLoginButton
import com.twitter.sdk.android.core.models.User
import org.androidannotations.annotations.AfterViews
import org.androidannotations.annotations.Click
import org.androidannotations.annotations.EActivity
import org.androidannotations.annotations.ViewById


@EActivity(R.layout.activity_main)
open class MainActivity : AppCompatActivity() {

    @ViewById
    internal lateinit var twitterLoginButton: TwitterLoginButton
    @ViewById
    internal lateinit var disconnectButton: Button

    @ViewById
    internal lateinit var userImageView: ImageView
    @ViewById
    internal lateinit var notLoggedUserTV: TextView
    @ViewById
    internal lateinit var idTv: TextView
    @ViewById
    internal lateinit var userNameTV: TextView
    @ViewById
    internal lateinit var emailTV: TextView
    @ViewById
    internal lateinit var screenNameTV: TextView

    @AfterViews
    fun init() {

        updateUI()

        twitterLoginButton.callback = object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>) {
                updateUI()
            }

            override fun failure(e: TwitterException) {
                Toast.makeText(this@MainActivity, R.string.authentication_failed, Toast.LENGTH_SHORT).show()
                updateUI()
            }
        }

        if (isUserAuthenticated()) {
            Toast.makeText(this, R.string.user_already_authenticated, Toast.LENGTH_SHORT).show()
            updateUI()
        }
    }

    @Click
    fun disconnectButtonClicked() {
        TwitterCore.getInstance().sessionManager.clearActiveSession()
        updateUI()
    }

    private fun updateUI() {
        val authUser = isUserAuthenticated()

        twitterLoginButton.visibility = if (authUser) View.GONE else View.VISIBLE
        disconnectButton.visibility = if (authUser) View.VISIBLE else View.GONE

        userImageView.setImageResource(R.drawable.twitter)
        notLoggedUserTV.visibility = if (authUser) View.GONE else View.VISIBLE
        idTv.visibility = if (authUser) View.VISIBLE else View.GONE
        userNameTV.visibility = if (authUser) View.VISIBLE else View.GONE
        emailTV.visibility = if (authUser) View.VISIBLE else View.GONE
        screenNameTV.visibility = if (authUser) View.VISIBLE else View.GONE

        loadUserDetails()
    }

    /**
     * Check if user is authenticated.
     *
     * @return true if authenticated
     */
    private fun isUserAuthenticated(): Boolean {
        return TwitterCore.getInstance().sessionManager.activeSession != null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        twitterLoginButton.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Load all user details if authenticated
     */
    private fun loadUserDetails() {
        if (isUserAuthenticated()) {
            val twitterApiClient = TwitterCore.getInstance().apiClient
            val call = twitterApiClient.accountService.verifyCredentials(true, false, true)
            call.enqueue(object : Callback<User>() {
                override fun success(result: Result<User>) {
                    val user = result.data
                    idTv.text = getString(R.string.user_id,user.id.toString())
                    userNameTV.text = getString(R.string.user_name,user.name)
                    emailTV.text = getString(R.string.user_email,user.email)
                    screenNameTV.text = getString(R.string.user_screen_name,user.screenName)

                    Picasso.get()
                            .load(user.profileImageUrl.replace("_normal", ""))
                            .placeholder(R.drawable.twitter)
                            .into(userImageView)
                }

                override fun failure(exception: TwitterException) {
                    Toast.makeText(this@MainActivity, R.string.authentication_failed, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
