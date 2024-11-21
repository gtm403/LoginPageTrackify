package com.example.loginpagetrackify

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.loginpagetrackify.ui.theme.LoginPageTrackifyTheme
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginPageTrackifyTheme {
                val navController = rememberNavController()
                Scaffold { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") {
                            LoginScreen { userId ->
                                // Navigate to the Income and Saving Goal screen on login success
                                navController.navigate("incomeSavingGoal/$userId") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        }
                        composable(
                            "incomeSavingGoal/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getString("userId")
                            IncomeSavingGoalScreen(userId) {
                                // Navigate to the home screen after input
                                navController.navigate("home/$userId") {
                                    popUpTo("incomeSavingGoal") { inclusive = true }
                                }
                            }
                        }
                        composable(
                            "home/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getString("userId")
                            HomeScreen(userId)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUpMode by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = if (isSignUpMode) "Create an Account" else "Welcome!",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isSignUpMode) "Fill the form to sign up" else "Login below or create an account",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Email Input
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Password Input
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Error or Success Messages
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        if (successMessage != null) {
            Text(
                text = successMessage!!,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sign In/Sign Up Button
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Email and password cannot be empty."
                    successMessage = null
                    return@Button
                }

                if (isSignUpMode) {
                    // Handle Sign Up
                    signUpWithEmail(auth, email, password, { userId ->
                        onLoginSuccess(userId)
                        successMessage = "Account created successfully!"
                        errorMessage = null
                    }) { success, message ->
                        if (success) {
                            successMessage = message
                            errorMessage = null
                        } else {
                            errorMessage = message
                            successMessage = null
                        }
                    }
                } else {
                    // Handle Sign In
                    signInWithEmail(auth, email, password, { userId ->
                        onLoginSuccess(userId)
                        successMessage = "Welcome back!"
                        errorMessage = null
                    }) { success, message ->
                        if (success) {
                            successMessage = message
                            errorMessage = null
                        } else {
                            errorMessage = message
                            successMessage = null
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .height(50.dp)
        ) {
            Text(
                text = if (isSignUpMode) "Sign Up" else "Sign In",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Forgot Password (only in Sign-In mode)
        if (!isSignUpMode) {
            TextButton(
                onClick = {
                    forgotPassword(auth, email) { success, message ->
                        if (success) {
                            successMessage = message
                            errorMessage = null
                        } else {
                            errorMessage = message
                            successMessage = null
                        }
                    }
                }
            ) {
                Text(text = "Forgot Password?", color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Toggle Between Sign-In and Sign-Up
        TextButton(onClick = { isSignUpMode = !isSignUpMode }) {
            Text(
                text = if (isSignUpMode) "Already have an account? Sign In" else "Don't have an account? Sign Up",
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

fun signUpWithEmail(
    auth: FirebaseAuth,
    email: String,
    password: String,
    onSuccess: (String) -> Unit,  // User ID callback
    onResult: (Boolean, String?) -> Unit // Success/Error callback
) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                user?.let {
                    onSuccess(it.uid) // Pass user ID on success
                }
                onResult(true, "Account created successfully!")
            } else {
                onResult(false, task.exception?.localizedMessage ?: "Sign-up failed.")
            }
        }
}



fun signInWithEmail(
    auth: FirebaseAuth,
    email: String,
    password: String,
    onSuccess: (String) -> Unit,
    onResult: (Boolean, String?) -> Unit
) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                user?.let {
                    onSuccess(it.uid) // Pass user ID on success
                }
                onResult(true, "Welcome back!")
            } else {
                onResult(false, task.exception?.localizedMessage ?: "Sign-in failed.")
            }
        }
}



@Composable
fun HomeScreen(userId: String?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Welcome, User ID: $userId")
    }
}

fun forgotPassword(
    auth: FirebaseAuth,
    email: String,
    onResult: (Boolean, String?) -> Unit
) {
    if (email.isBlank()) {
        onResult(false, "Please enter a valid email address.")
        return
    }

    auth.sendPasswordResetEmail(email)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult(true, "Password reset email sent.")
            } else {
                onResult(false, task.exception?.localizedMessage ?: "Failed to send reset email.")
            }
        }
}

@Composable
fun IncomeSavingGoalScreen(userId: String?, onContinue: () -> Unit) {
    var income by remember { mutableStateOf("") }
    var savingGoal by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "Total Income",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Income Input
        OutlinedTextField(
            value = income,
            onValueChange = { income = it },
            label = { Text("$$") },
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Saving Goal
        Text(
            text = "Saving Goal",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = savingGoal,
            onValueChange = { savingGoal = it },
            label = { Text("%") },
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (income.isNotBlank() && savingGoal.isNotBlank()) {
                    onContinue() // Proceed to the next screen
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Set")
        }
    }
}









