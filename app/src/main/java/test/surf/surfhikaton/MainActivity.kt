package test.surf.surfhikaton

import android.graphics.Bitmap
import android.os.Bundle
import android.view.animation.OvershootInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import test.surf.surfhikaton.model.CatHolder
import test.surf.surfhikaton.ui.theme.SurfHikatonTheme
import test.surf.surfhikaton.ui.theme.White
import test.surf.surfhikaton.ui.theme.YellowOrange
import test.surf.surfhikaton.vm.CatState
import test.surf.surfhikaton.vm.HomeViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SurfHikatonTheme {
                Surface(color = MaterialTheme.colors.background) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "splash_screen") {
                        composable("splash_screen") {
                            SplashScreen(navController)
                        }

                        composable("main_screen") {
                            HomeScreen()
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun SplashScreen(
    navController: NavHostController
) {
    val scale = remember {
        Animatable(0f)
    }

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 0.7f,
            animationSpec = tween(
                durationMillis = 800,
                easing = {
                    OvershootInterpolator(4f).getInterpolation(it)
                })
        )
        delay(2000L)
        navController.navigate("main_screen") {
            popUpTo("splash_screen") {
                inclusive = true
            }
        }
    }


    Column(
        modifier = Modifier
            .padding()
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = painterResource(R.drawable.ic_splash),
            contentDescription = "Surf logo",
            Modifier
                .padding(20.dp, 96.dp)
        )

        Image(
            painter = painterResource(R.drawable.android_logo),
            contentDescription = "Android logo",
            Modifier
                .padding(vertical = 24.dp)
                .scale(scale.value)
        )
    }
}

@ExperimentalFoundationApi
@Composable
fun HomeScreen(

) {
    val viewModel = hiltViewModel<HomeViewModel>()
    val state by viewModel.catState.collectAsState(initial = CatState.Loading)
    val counterState by viewModel.counterFlow.collectAsState(initial = 0)

    LaunchedEffect(key1 = true) {
        viewModel.updateData()
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Здесь только один настоящий котик. Остальных же нарисовала нейросеть. Какой котик настоящий?",
            modifier = Modifier
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .shadow(1.dp)
                .padding(8.dp)
        )
        when(state){
            CatState.Loading -> LinearProgressIndicator()
            else -> {}
        }


        LazyVerticalGrid(
            cells = GridCells.Fixed(2)
        ) {
            items((state as? CatState.Success)?.dataList ?: listOf()) { cat ->
                CatCard(cat.bitmap) { viewModel.onClickCat(cat.isFake) }
            }
        }

        Text(
            text = "Очки: $counterState",
            fontSize = 26.sp,
            modifier = Modifier
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .padding(2.dp)
                .shadow(1.dp)
                .padding(4.dp)
        )
    }
}

@Composable
fun CatCard(
    bitmap: Bitmap,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .padding(10.dp)
            .shadow(2.dp, RoundedCornerShape(size = 8.dp))
            .background(White)
            .clickable { onClick.invoke() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "A Cat",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(165.dp)
                .fillMaxSize()
        )
        Text(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(size = 8.dp))
                .background(YellowOrange.copy(alpha = .3f))
                .padding(horizontal = 9.dp, vertical = 2.dp),
            text = "Я настоящий!"
        )
    }
}
