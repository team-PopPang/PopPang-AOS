package com.pappang.poppang_aos.view

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pappang.poppang_aos.R
import com.pappang.poppang_aos.ui.theme.google
import com.pappang.poppang_aos.ui.theme.homeTitle
import com.pappang.poppang_aos.ui.theme.kakao
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pappang.poppang_aos.viewmodel.loginViewModel
import androidx.compose.ui.platform.LocalContext
import com.pappang.poppang_aos.model.LoginResponse

@Composable
fun LoginScreen(onNextClick: (LoginResponse, String?) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFFFFF)),
        contentAlignment = BottomCenter
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = CenterHorizontally,
            modifier = Modifier.fillMaxWidth().fillMaxHeight()
        ) {
            Image(
                painter = painterResource(id = R.drawable.poppang_icon),
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .align(CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(50.dp))
            KakaoLoginButton(onClick = onNextClick, modifier = Modifier)
            Spacer(modifier = Modifier.height(10.dp))
            GoogleLoginButton(onClick = onNextClick, modifier = Modifier)
        }
    }
}


@Composable
fun GoogleLoginButton(
    onClick: (LoginResponse, String?) -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    val viewModel: loginViewModel = viewModel()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        viewModel.googleLogin(
            data = data,
            onSuccess = { response ->
                viewModel.saveUid(context, response.uid ?: "")
                onClick(response, null)
            },
            onError = {
                Toast.makeText(context, "구글 로그인 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .background(google, shape = RoundedCornerShape(5.dp))
            .border(width = 1.dp, color = Color(0xFFCCCCCC), shape = RoundedCornerShape(5.dp))
            .height(52.dp)
            .clickable {
                viewModel.GoogleLogOut(context) {
                    val intent = viewModel.getGoogleSignInIntent(context)
                    launcher.launch(intent)
                }
            }
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .align(Center)
        ) {
            Image(
                painter = painterResource(id = R.drawable.google_icon),
                contentDescription = null,
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = "구글 로그인",
                style = homeTitle,
                color = Color(0xFF333333),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun KakaoLoginButton(onClick: (LoginResponse, String?) -> Unit, modifier: Modifier) {
    val context = LocalContext.current
    val viewModel: loginViewModel = viewModel()
    val activity = context as? Activity

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .background(kakao, shape = RoundedCornerShape(5.dp))
            .height(52.dp)
            .clickable {
                viewModel.kakaoLogout(
                    onComplete = {
                        viewModel.kakaoLogin(
                            activity = activity,
                            context = context,
                            onSuccess = { response ->
                                viewModel.saveUid(context, response.uid ?: "")
                                onClick(response, null)
                            },
                            onError = {
                                Toast.makeText(
                                    context,
                                    "카카오 로그인 실패: ${it.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    },
                    onError = {
                        Toast.makeText(context, "카카오 로그아웃 실패: ${it.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
                )
            }
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .align(Center)
        ) {
            Image(
                painter = painterResource(id = R.drawable.kakaotalk_icon),
                contentDescription = null,
                modifier = Modifier
                    .size(17.dp)
            )
            Text(
                text = "카카오 로그인",
                style = homeTitle,
                color = Color(0xFF3D1D1C),
                modifier = Modifier
                    .padding(start = 8.dp)
            )
        }
    }
}
