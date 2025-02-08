    package com.example.composetuthw1

    import android.content.ContentValues.TAG
    import android.content.Context
    import android.content.res.Configuration
    import android.net.Uri
    import android.os.Bundle
    import android.util.Log
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.rememberLauncherForActivityResult
    import androidx.activity.compose.setContent
    import androidx.activity.enableEdgeToEdge
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.compose.animation.animateColorAsState
    import androidx.compose.animation.animateContentSize
    import androidx.compose.foundation.Image
    import androidx.compose.foundation.border
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.layout.Arrangement
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.Row
    import androidx.compose.foundation.layout.Spacer
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.height
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.layout.size
    import androidx.compose.foundation.layout.width
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.lazy.items
    import androidx.compose.foundation.shape.CircleShape
    import androidx.compose.material3.Button
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.material3.OutlinedTextField
    import androidx.compose.material3.Scaffold
    import androidx.compose.material3.Surface
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.LaunchedEffect
    import androidx.compose.runtime.collectAsState
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.rememberCoroutineScope
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.tooling.preview.Preview
    import androidx.compose.ui.unit.dp
    import androidx.datastore.preferences.core.edit
    import androidx.datastore.preferences.core.stringPreferencesKey
    import androidx.datastore.preferences.preferencesDataStore
    import androidx.navigation.NavHostController
    import androidx.navigation.compose.NavHost
    import androidx.navigation.compose.composable
    import androidx.navigation.compose.rememberNavController
    import coil.compose.AsyncImage
    import java.util.prefs.Preferences

    import com.example.composetuthw1.ui.theme.ComposeTutHW1Theme
    import kotlinx.coroutines.flow.Flow
    import kotlinx.coroutines.flow.map
    import kotlinx.coroutines.launch
    import java.io.File
    import java.io.FileOutputStream
    import java.io.InputStream


    @Composable
    fun MyAppNavHost(navController: NavHostController, context: Context) {
        // Sample messages list (assumes at least one message exists)
        val messages = SampleData.conversationSample
        val firstAuthor = messages.firstOrNull()?.author ?: "Unknown"

        val userPreferences = remember { UserPreferences(context, firstAuthor) }
        val coroutineScope = rememberCoroutineScope()

        // Use username
        val username by userPreferences.username.collectAsState(firstAuthor)

        // MutableState for profile picture
        var profilePicture by remember { mutableStateOf<Uri?>(null) }

        // Collect profile picture separately
        LaunchedEffect(Unit) {
            profilePicture = userPreferences.getProfilePictureUri()
        }

        NavHost(
            navController = navController,
            startDestination = "mainScreen"
        ) {
            composable("mainScreen") {
                MainScreen(
                    onNavigateToMessages = { navController.navigate("messageScreen") }
                )
            }
            composable("messageScreen") {
                Conversation(
                    onNavigateBack = { navController.popBackStack("mainScreen", false) },
                    onNavigateToSettings = { navController.navigate("settingScreen") },
                    messages = messages,
                    profilePicture = profilePicture,
                    username = username
                )
            }
            composable("settingScreen") {
                SettingScreen(
                    currentUsername = username,
                    currentProfilePicture = profilePicture,
                    onSaveChanges = { newUsername, newProfilePicture ->
                        profilePicture = newProfilePicture
                        coroutineScope.launch {
                            userPreferences.saveUserData(newUsername, newProfilePicture)
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }






    class MainActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContent {
                ComposeTutHW1Theme {
                    val navController = rememberNavController()
                    MyAppNavHost(navController = navController, context = this)
                    //Conversation(SampleData.conversationSample)
                }
            }
        }
    }

    data class Message(val author: String, val body: String)

    @Composable
    fun MainScreen(onNavigateToMessages:() -> Unit){
        Scaffold {contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Main Screen",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(onClick = onNavigateToMessages) {
                    Text(text = "Go to Messages")
                }
            }
        }

    }

    @Composable
    fun MessageCard(msg: Message, profilePicture: Uri?, username: String?) {
        Row(modifier = Modifier.padding(all = 8.dp)) {

            if (profilePicture != null && profilePicture != Uri.EMPTY) {
                AsyncImage(
                    model = profilePicture,
                    contentDescription = "Contact profile picture",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.profile_pic),
                    contentDescription = "Default profile picture",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            var isExpanded by remember { mutableStateOf(false) }
            val surfaceColor by animateColorAsState(
                if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                label = "",
            )

            Column(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
                Text(
                    text = username ?: msg.author,  // Use updated username or fallback to msg.author
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    shadowElevation = 1.dp,
                    color = surfaceColor,
                    modifier = Modifier
                        .animateContentSize()
                        .padding(1.dp)
                ) {
                    Text(
                        text = msg.body,
                        modifier = Modifier.padding(all = 4.dp),
                        maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }



    @Composable
    fun Conversation(
        onNavigateBack: () -> Unit,
        onNavigateToSettings: () -> Unit,
        messages: List<Message>,
        username: String, // Receive updated username
        profilePicture: Uri? // Receive updated profile picture URI
    ) {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 64.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = onNavigateBack) {
                        Text("Go Back")
                    }
                    Image(
                        painter = painterResource(R.drawable.settings_24px),
                        contentDescription = "Settings",
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onNavigateToSettings() }
                    )
                }
            }
        ) { paddingValues ->
            Column(modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
            ) {
                LazyColumn(modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)) {
                    items(messages) { message ->
                        MessageCard(msg = message, profilePicture = profilePicture, username = username)
                    }
                }
                // Show updated username and profile picture here
                if (profilePicture != null) {
                    AsyncImage(
                        model = profilePicture,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }
                Text(text = "Username: $username", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }

    @Composable
    fun SettingScreen(
        currentUsername: String,  // Pass the current username
        currentProfilePicture: Uri?,  // Pass current profile picture
        onSaveChanges: (String, Uri?) -> Unit,
        onNavigateBack: () -> Unit
    ) {
        var username by remember { mutableStateOf(currentUsername) }
        var imageUri by remember { mutableStateOf(currentProfilePicture) }

        val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            imageUri = uri

        }

        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = onNavigateBack) {
                        Text("Go Back")
                    }
                    Text("Settings", style = MaterialTheme.typography.headlineMedium)
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Change Username", style = MaterialTheme.typography.bodyLarge)
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier.padding(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { imagePicker.launch("image/*") }) {
                    Text("Pick a Profile Picture")
                }

                imageUri?.let {
                    AsyncImage(
                        model = it,
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    onSaveChanges(username, imageUri)  // Save new username and image
                    onNavigateBack()  // Go back to messages
                }) {
                    Text("Save Changes")
                }
            }
        }
    }

    // Extension property to create DataStore instance
    val Context.dataStore by preferencesDataStore(name = "user_prefs")
    class UserPreferences(private val context: Context, firstAuthor: String) {
        companion object {
            private val USERNAME_KEY = stringPreferencesKey("username")
        }

        // Get stored username
        val username: Flow<String> = context.dataStore.data.map { preferences ->
            preferences[USERNAME_KEY] ?: firstAuthor  // Ensure fallback to firstAuthor
        }



        suspend fun saveUserData(newUsername: String, profilePictureUri: Uri?) {
            val profilePictureFile = File(context.filesDir, "profile_picture.jpg")

            if (newUsername != "") {
                context.dataStore.edit { preferences ->
                    preferences[USERNAME_KEY] = newUsername
                }
            }

            profilePictureUri?.let { uri ->
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    saveFile(inputStream, profilePictureFile)
                }
            }
        }

        fun getProfilePictureUri(): Uri? {
            val profilePictureFile = File(context.filesDir, "profile_picture.jpg")
            return if (profilePictureFile.exists()) Uri.fromFile(profilePictureFile) else null
        }

        private fun saveFile(inputStream: InputStream, file: File) {
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

/**
    class UserPreferences(private val context: Context) {
        companion object {
            private val USERNAME_KEY = stringPreferencesKey("username")
            private val PROFILE_PICTURE_KEY = stringPreferencesKey("profile_picture")
        }

        // Get stored username
        val username: Flow<String> = context.dataStore.data.map { preferences ->
            preferences[USERNAME_KEY] ?: ""
        }

        // Get stored profile picture URI (nullable)
        val profilePicture: Flow<Uri?> = context.dataStore.data.map { preferences ->
            preferences[PROFILE_PICTURE_KEY]?.let { Uri.parse(it) }
        }

        suspend fun saveUserData(newUsername: String, profilePictureUri: Uri?) {
            context.dataStore.edit { preferences ->
                preferences[USERNAME_KEY] = newUsername
                preferences[PROFILE_PICTURE_KEY] = profilePictureUri?.toString() ?: Uri.EMPTY.toString()
            }
        }
    }
**/




    @Preview(name = "Light Mode")
    @Preview(
        uiMode = Configuration.UI_MODE_NIGHT_YES,
        showBackground = true,
        name = "Dark Mode"
    )
    @Composable
    fun PreviewMainScreen() {
        ComposeTutHW1Theme {
            MainScreen(onNavigateToMessages = {})
        }
    }



    /**
     * SampleData for Jetpack Compose Tutorial
     */
    object SampleData {
        // Sample conversation data
        val conversationSample = listOf(
            Message(
                "Lexi",
                "Test...Test...Test..."
            ),
            Message(
                "Lexi",
                """List of Android versions:
                |Android KitKat (API 19)
                |Android Lollipop (API 21)
                |Android Marshmallow (API 23)
                |Android Nougat (API 24)
                |Android Oreo (API 26)
                |Android Pie (API 28)
                |Android 10 (API 29)
                |Android 11 (API 30)
                |Android 12 (API 31)""".trim()
            ),
            Message(
                "Lexi",
                """I think Kotlin is my favorite programming language.
                |It's so much fun!""".trim()
            ),
            Message(
                "Lexi",
                "Searching for alternatives to XML layouts..."
            ),
            Message(
                "Lexi",
                """Hey, take a look at Jetpack Compose, it's great!
                |It's the Android's modern toolkit for building native UI.
                |It simplifies and accelerates UI development on Android.
                |Less code, powerful tools, and intuitive Kotlin APIs :)""".trim()
            ),
            Message(
                "Lexi",
                "It's available from API 21+ :)"
            ),
            Message(
                "Lexi",
                "Writing Kotlin for UI seems so natural, Compose where have you been all my life?"
            ),
            Message(
                "Lexi",
                "Android Studio next version's name is Arctic Fox"
            ),
            Message(
                "Lexi",
                "Android Studio Arctic Fox tooling for Compose is top notch ^_^"
            ),
            Message(
                "Lexi",
                "I didn't know you can now run the emulator directly from Android Studio"
            ),
            Message(
                "Lexi",
                "Compose Previews are great to check quickly how a composable layout looks like"
            ),
            Message(
                "Lexi",
                "Previews are also interactive after enabling the experimental setting"
            ),
            Message(
                "Lexi",
                "Have you tried writing build.gradle with KTS?"
            ),
        )
    }




