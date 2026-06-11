package com.teammirado.waygo.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teammirado.waygo.data.api.RetrofitClient
import com.teammirado.waygo.data.model.RegisterRequest
import com.teammirado.waygo.data.model.UserResponse
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(onBackClick: () -> Unit, onSignUpSuccess: (String, UserResponse) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("passenger") }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val blueColor = Color(0xFF003399)
    val lightBlue = Color(0xFFF0F4FF)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color.White, lightBlue)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header avec bouton retour
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour", tint = blueColor)
                }
                Text(
                    text = "Création de compte",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = blueColor
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Bienvenue sur WayGo",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = blueColor
                )
                Text(
                    text = "Remplissez les informations pour commencer.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
                )

                if (errorMessage != null) {
                    Surface(
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFD32F2F),
                            modifier = Modifier.padding(12.dp),
                            fontSize = 13.sp
                        )
                    }
                }

                // --- SÉLECTEUR DE RÔLE MODERNE ---
                Text(
                    text = "Je souhaite être :",
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    RoleCard(
                        title = "Passager",
                        icon = Icons.Default.Person,
                        isSelected = selectedRole == "passenger",
                        onClick = { selectedRole = "passenger" },
                        modifier = Modifier.weight(1f),
                        activeColor = blueColor
                    )
                    RoleCard(
                        title = "Chauffeur",
                        icon = Icons.Default.DirectionsCar,
                        isSelected = selectedRole == "driver",
                        onClick = { selectedRole = "driver" },
                        modifier = Modifier.weight(1f),
                        activeColor = blueColor
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- CHAMPS DE SAISIE ---
                CustomSignUpField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nom complet",
                    icon = Icons.Default.Badge,
                    isLoading = isLoading
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                CustomSignUpField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Adresse e-mail",
                    icon = Icons.Default.Email,
                    isLoading = isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mot de passe") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = blueColor) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = blueColor,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmer le mot de passe") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    leadingIcon = { Icon(Icons.Default.LockClock, null, tint = blueColor) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = blueColor,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = {
                        if (password != confirmPassword) {
                            errorMessage = "Les mots de passe ne correspondent pas"
                            return@Button
                        }
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            try {
                                val response = RetrofitClient.apiService.register(
                                    RegisterRequest(name, email, password, selectedRole)
                                )
                                RetrofitClient.authToken = response.token
                                onSignUpSuccess(response.token, response.user)
                            } catch (e: Exception) {
                                errorMessage = "Une erreur est survenue lors de l'inscription."
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .shadow(8.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = blueColor),
                    enabled = !isLoading && name.isNotBlank() && email.isNotBlank() && password.length >= 6
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                    } else {
                        Text("Créer mon compte", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun RoleCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    activeColor: Color
) {
    val backgroundColor by animateColorAsState(if (isSelected) activeColor else Color.White)
    val contentColor by animateColorAsState(if (isSelected) Color.White else Color.Gray)
    val borderStroke = if (isSelected) 0.dp else 1.dp

    Surface(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(borderStroke, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = backgroundColor,
        tonalElevation = if (isSelected) 8.dp else 0.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(32.dp), tint = contentColor)
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, color = contentColor, fontSize = 14.sp)
        }
    }
}

@Composable
fun CustomSignUpField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isLoading: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        enabled = !isLoading,
        leadingIcon = { Icon(icon, null, tint = Color(0xFF003399)) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF003399),
            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        singleLine = true
    )
}
