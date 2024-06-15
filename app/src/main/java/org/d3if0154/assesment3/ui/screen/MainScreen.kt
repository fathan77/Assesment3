@file:Suppress("DEPRECATION")

package org.d3if0154.assesment3.ui.screen

import android.content.ContentResolver
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.d3if0154.assesment3.BuildConfig
import org.d3if0154.assesment3.R
import org.d3if0154.assesment3.model.Film
import org.d3if0154.assesment3.model.User
import org.d3if0154.assesment3.network.FilmApi
import org.d3if0154.assesment3.network.FilmApi.getFilmUrl
import org.d3if0154.assesment3.network.UserDataStore
import org.d3if0154.assesment3.ui.theme.assesment3Theme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val dataStore = UserDataStore(context = context)
    val user by dataStore.userFlow.collectAsState(initial = User())
    var showDialog by remember {
        mutableStateOf(false)
    }
    var showFilmDialog by remember {
        mutableStateOf(false)
    }

    var bitmap: Bitmap? by remember {
        mutableStateOf(null)
    }

    val launcher = rememberLauncherForActivityResult(contract = CropImageContract()) {
        bitmap = getCroppedImage(context.contentResolver, it)
        if (bitmap != null) showFilmDialog = true
    }

    val viewModel : MainViewModel = viewModel()
    val errorMessage by viewModel.errorMessage

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.app_name))
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = {
                        if (user.email.isEmpty()){
                            CoroutineScope(Dispatchers.IO).launch {
                                signIn(context = context, dataStore = dataStore)
                            }
                        }else{
                            Log.d("SIGN-IN","User: $user")
                            showDialog = true
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.account_circle_24),
                            contentDescription = stringResource(
                                id = R.string.profil
                            ),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val options = CropImageContractOptions(
                    uri = null, CropImageOptions(
                        imageSourceIncludeGallery = false,
                        imageSourceIncludeCamera = true,
                        fixAspectRatio = true
                    )
                )
                launcher.launch(options)
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = R.string.tambah_Film))
            }
        }
    ){paddingValues ->
        ScreenContent(
            modifier = Modifier.padding(paddingValues = paddingValues),
            viewModel = viewModel,
            userId = user.email,
            user = user
        )
        if (showDialog){
            ProfilDialog(
                user = user, onDismissRequest = { showDialog = false }) {
                CoroutineScope(Dispatchers.IO).launch {
                    signOut(context = context, dataStore = dataStore)
                }
                showDialog = false
            }
        }

        if (showFilmDialog){
            FilmDialog(
                bitmap = bitmap,
                onDismissRequest = { showFilmDialog = false },
                onConfirmation = { namaFilm, namaPembuat ->
                    Log.d("TAMBAH", "$namaFilm $namaPembuat ditambahkan.")
                    showFilmDialog = false
                    viewModel.saveData(user.email, namaFilm, namaPembuat, bitmap!!)
                })
        }

        if (errorMessage != null){
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }
}

@Composable
fun ScreenContent(modifier: Modifier, viewModel: MainViewModel, userId: String, user: User) {
    val data by viewModel.data
    val status by viewModel.status.collectAsState()
    LaunchedEffect(key1 = userId) {
        viewModel.retrieveData(userId = userId)
    }

    when(status){
        FilmApi.ApiStatus.LOADING -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                CircularProgressIndicator()
            }
        }
        FilmApi.ApiStatus.SUCCESS -> {
            LazyVerticalGrid(
                modifier = modifier
                    .fillMaxSize()
                    .padding(4.dp),
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 80.dp)
            ){
                items(data){
                    ListItem(film = it, user = user, viewModel = viewModel)
                }
            }
        }
        FilmApi.ApiStatus.FAILED -> {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally){
                Text(text = stringResource(id = R.string.error))
                Button(onClick = { viewModel.retrieveData(userId = userId) }, modifier = Modifier.padding(16.dp), contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)) {
                    Text(text = stringResource(id = R.string.try_again))
                }
            }
        }
    }
}


@Composable
fun ListItem(film: Film, user: User, viewModel: MainViewModel) {
    var onShowDeleting by remember {
        mutableStateOf(false)
    }
    Box(modifier = Modifier
        .padding(4.dp)
        .border(1.dp, Color.Gray), contentAlignment = Alignment.BottomCenter) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(
                    getFilmUrl(film.imageId)
                )
                .crossfade(true)
                .build(),
            contentDescription = stringResource(id = R.string.gambar, film.namaFilm),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.loading_img),
            error = painterResource(id = R.drawable.broken_img),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),

            )
        Row(Modifier.fillMaxWidth().background(Color(red = 0f, green = 0f, blue = 0f, alpha = 0.5f)), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically){
            Column(modifier = Modifier
                .padding(4.dp)
                .padding(4.dp)){
                Text(text = film.namaFilm, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = film.namaPembuat, fontStyle = FontStyle.Italic, fontSize = 14.sp, color = Color.White)
            }
            if (film.mine == 1){
                IconButton(onClick = {
                    onShowDeleting = true
                }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(id = R.string.hapus), tint = MaterialTheme.colorScheme.surface)
                }
            }
            if (onShowDeleting){
                DeleteDialog(onDismissRequest = { onShowDeleting = false }, onConfirmation = {
                    onShowDeleting = false
                    viewModel.deletingData(userId = user.email, id = film.id)
                }, id = film.id, film = film)
            }
        }
    }
}

private suspend fun signIn(context: Context,dataStore: UserDataStore){
    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.API_KEY)
        .build()

    val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val credentialManager = CredentialManager.create(
            context = context
        )
        val result = credentialManager.getCredential(context = context, request = request)
        handleSignIn(result = result, dataStore = dataStore)
    }catch (e: GetCredentialException){
        Log.e("SIGN-IN","Error: ${e.errorMessage}")
    }
}

private suspend fun handleSignIn(result: GetCredentialResponse, dataStore: UserDataStore){
    val credential = result.credential
    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL){
        try {
            val googleId = GoogleIdTokenCredential.createFrom(
                credential.data
            )
            Log.d("SIGN-IN","User email: ${googleId.id}")
            val name = googleId.displayName ?: ""
            val email = googleId.id
            val photoUrl = googleId.profilePictureUri.toString()
            dataStore.saveData(User(name = name, email = email, photoUrl = photoUrl ))
        }catch (e: GoogleIdTokenParsingException){
            Log.e("SIGN-IN","Error: ${e.message}")
        }
    }else{
        Log.e("SIGN-IN","Error: unrecognized custom credential type")
    }
}

private suspend fun signOut(context: Context,dataStore: UserDataStore){
    try {
        val credentialManager = CredentialManager.create(context = context)
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        dataStore.saveData(User())
    }catch (e: ClearCredentialException){
        Log.e("SIGN-IN","Error: ${e.errorMessage}")
    }
}

private fun getCroppedImage(
    resolver: ContentResolver,
    result: CropImageView.CropResult
) : Bitmap? {
    if (!result.isSuccessful){
        Log.e("IMAGE", "Error: ${result.error}")
        return null
    }

    val uri = result.uriContent ?: return null

    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P){
        MediaStore.Images.Media.getBitmap(resolver, uri)
    }else{
        val source = ImageDecoder.createSource(resolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun GreetingPreview() {
    assesment3Theme{
        MainScreen()
    }
}