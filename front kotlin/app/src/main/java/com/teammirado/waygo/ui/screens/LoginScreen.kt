package com.teammirado.waygo.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teammirado.waygo.data.api.RetrofitClient
import com.teammirado.waygo.data.model.LoginRequest
import com.teammirado.waygo.data.model.UserResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun LoginScreen(onLoginSuccess: (String, UserResponse) -> Unit, onSignUpClick: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val blueColor = Color(0xFF003399)
    val lightBlue = Color(0xFFF0F4FF)

    // Animation du logo
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "logoScale"
    )

    fun performLogin() {
        if (email.isBlank() || password.isBlank()) return
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val request = LoginRequest(email.trim(), password)
                val response = RetrofitClient.apiService.login(request)
                RetrofitClient.authToken = response.token

                // ✅ Connexion anonyme Firebase après login Laravel
                com.google.firebase.auth.FirebaseAuth.getInstance()
                    .signInAnonymously()
                    .addOnCompleteListener {
                        // Que Firebase réussisse ou non, on continue
                        onLoginSuccess(response.token, response.user)
                    }

            } catch (e: HttpException) {
                errorMessage = if (e.code() == 401) "Email ou mot de passe incorrect." else "Erreur serveur"
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Vérifiez votre connexion internet."
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color.White, lightBlue)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // Logo avec effet
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale)
                    .shadow(8.dp, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp)),
                color = blueColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.DirectionsCar, null, modifier = Modifier.size(54.dp), tint = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "WayGo",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = blueColor,
                letterSpacing = 2.sp
            )
            Text(
                text = "Covoiturez en toute confiance",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(56.dp))

            AnimatedVisibility(visible = errorMessage != null) {
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it }, 
                label = { Text("Adresse e-mail") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                enabled = !isLoading,
                leadingIcon = { Icon(Icons.Default.Email, null, tint = blueColor) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = blueColor,
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it }, 
                label = { Text("Mot de passe") },
                enabled = !isLoading,
                shape = RoundedCornerShape(20.dp),
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = blueColor) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = blueColor,
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); performLogin() })
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { performLogin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = blueColor),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                } else {
                    Text("Se connecter", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            
            Row(
                modifier = Modifier.padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Nouveau ici ?", color = Color.Gray)
                TextButton(onClick = onSignUpClick) {
                    Text("Créer un compte", color = blueColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
