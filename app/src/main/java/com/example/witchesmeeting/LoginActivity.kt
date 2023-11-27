package com.example.witchesmeeting


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.witchesmeeting.databinding.ActivityLoginBinding
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient


class LoginActivity : AppCompatActivity() {
    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onClick()
    }


    private fun onClick() {
        binding.kakao.setOnClickListener {
            // 카카오계정으로 로그인 공통 callback 구성
            // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용
            val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                if (error != null) {
                    Log.e("LOGIN@@", "카카오계정으로 로그인 실패", error)
                } else if (token != null) {
                    UserApiClient.instance.me { user, error ->
                        if (error != null) {
                            Log.e("getUserId@@", "사용자 정보 요청 실패", error)
                        } else if (user != null) {
                            Log.d(
                                "kakaoNickname@@",
                                user.kakaoAccount?.profile?.nickname.toString()
                            )
                            Toast.makeText(
                                this,
                                "${user.kakaoAccount?.profile?.nickname}님 로그인",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    Log.i("LOGIN@@", "카카오계정으로 로그인 성공 ${token.accessToken}")
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    finish()

                }
            }

            // 카톡 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                    if (error != null) {
                        Log.e("LOGIN@@", "카카오톡으로 로그인 실패", error)

                        // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                        // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            return@loginWithKakaoTalk
                        }

                        // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                        UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                    } else if (token != null) {
                        UserApiClient.instance.me { user, error ->
                            if (error != null) {
                                Log.e("getUserId@@", "사용자 정보 요청 실패", error)
                            } else if (user != null) {
                                Log.d(
                                    "kakaoNickname@@",
                                    user.kakaoAccount?.profile?.nickname.toString()
                                )
                                Toast.makeText(
                                    this,
                                    "${user.kakaoAccount?.profile?.nickname}님 로그인",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        Log.i("LoginTry@@", "카카오톡으로 로그인 성공 ${token.accessToken}")
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                        finish()
                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
            }

        }

    }


}
