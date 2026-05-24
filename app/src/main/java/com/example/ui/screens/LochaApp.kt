package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.*
import com.example.ui.components.GarmentRenderer
import com.example.ui.theme.*
import com.example.viewmodel.LochaViewModel
import com.example.viewmodel.SortOption
import java.text.SimpleDateFormat
import java.util.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Catalog : Screen("catalog", "Shop", Icons.Outlined.GridView)
    object Wishlist : Screen("wishlist", "Wishlist", Icons.Outlined.FavoriteBorder)
    object Cart : Screen("cart", "Cart", Icons.Outlined.ShoppingBag)
    object Profile : Screen("profile", "Profile", Icons.Outlined.Person)
    object OrderSuccess : Screen("success", "Success", Icons.Outlined.CheckCircle)
}

@Composable
fun LochaApp(
    viewModel: LochaViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val isTablet = screenWidth >= 600

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Synchronize UI bar states
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val cartCount = cartItems.sumOf { it.quantity }

    Box(modifier = modifier.fillMaxSize()) {
        if (isTablet) {
            // Adaptive horizontal grid for wide screen layouts
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Start + WindowInsetsSides.Vertical))
                ) {
                    LogoHeader(isCollapsed = true)

                    Spacer(modifier = Modifier.weight(1f))

                    NavigationRailItem(
                        selected = currentRoute == Screen.Catalog.route,
                        onClick = { navController.navigate(Screen.Catalog.route) { popUpTo(0) } },
                        icon = { Icon(if (currentRoute == Screen.Catalog.route) Icons.Filled.GridView else Icons.Outlined.GridView, contentDescription = "Shop") },
                        label = { Text("Shop") },
                        modifier = Modifier.testTag("rail_shop")
                    )

                    NavigationRailItem(
                        selected = currentRoute == Screen.Wishlist.route,
                        onClick = { navController.navigate(Screen.Wishlist.route) { popUpTo(0) } },
                        icon = { Icon(if (currentRoute == Screen.Wishlist.route) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = "Wishlist") },
                        label = { Text("Wishlist") },
                        modifier = Modifier.testTag("rail_wishlist")
                    )

                    NavigationRailItem(
                        selected = currentRoute == Screen.Cart.route,
                        onClick = { navController.navigate(Screen.Cart.route) { popUpTo(0) } },
                        icon = {
                            BadgedBox(badge = { if (cartCount > 0) Badge { Text("$cartCount") } }) {
                                Icon(if (currentRoute == Screen.Cart.route) Icons.Filled.ShoppingBag else Icons.Outlined.ShoppingBag, contentDescription = "Cart")
                            }
                        },
                        label = { Text("Cart") },
                        modifier = Modifier.testTag("rail_cart")
                    )

                    NavigationRailItem(
                        selected = currentRoute == Screen.Profile.route,
                        onClick = { navController.navigate(Screen.Profile.route) { popUpTo(0) } },
                        icon = { Icon(if (currentRoute == Screen.Profile.route) Icons.Filled.Person else Icons.Outlined.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        modifier = Modifier.testTag("rail_profile")
                    )

                    Spacer(modifier = Modifier.weight(1f))
                }

                VerticalDivider(color = MaterialTheme.colorScheme.outline)

                Box(modifier = Modifier.weight(1f)) {
                    LochaNavHost(navController = navController, viewModel = viewModel, isTablet = true)
                }
            }
        } else {
            // Normal Mobile vertical structure
            Scaffold(
                bottomBar = {
                    val hideBottomBarRoutes = listOf("detail/{productId}", "checkout", "success")
                    val shouldShowBottomBar = currentRoute in listOf(
                        Screen.Catalog.route,
                        Screen.Wishlist.route,
                        Screen.Cart.route,
                        Screen.Profile.route
                    )

                    if (shouldShowBottomBar) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp,
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            NavigationBarItem(
                                selected = currentRoute == Screen.Catalog.route,
                                onClick = { navController.navigate(Screen.Catalog.route) { popUpTo(Screen.Catalog.route) { saveState = true }; launchSingleTop = true; restoreState = true } },
                                icon = { Icon(if (currentRoute == Screen.Catalog.route) Icons.Filled.GridView else Icons.Outlined.GridView, contentDescription = "Shop") },
                                label = { Text("Shop", style = MaterialTheme.typography.bodyMedium) },
                                modifier = Modifier.testTag("nav_shop")
                            )

                            NavigationBarItem(
                                selected = currentRoute == Screen.Wishlist.route,
                                onClick = { navController.navigate(Screen.Wishlist.route) { popUpTo(Screen.Catalog.route) { saveState = true }; launchSingleTop = true; restoreState = true } },
                                icon = { Icon(if (currentRoute == Screen.Wishlist.route) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = "Wishlist") },
                                label = { Text("Saved", style = MaterialTheme.typography.bodyMedium) },
                                modifier = Modifier.testTag("nav_wishlist")
                            )

                            NavigationBarItem(
                                selected = currentRoute == Screen.Cart.route,
                                onClick = { navController.navigate(Screen.Cart.route) { popUpTo(Screen.Catalog.route) { saveState = true }; launchSingleTop = true; restoreState = true } },
                                icon = {
                                    BadgedBox(badge = { if (cartCount > 0) Badge { Text("$cartCount") } }) {
                                        Icon(if (currentRoute == Screen.Cart.route) Icons.Filled.ShoppingBag else Icons.Outlined.ShoppingBag, contentDescription = "Cart")
                                    }
                                },
                                label = { Text("Bag", style = MaterialTheme.typography.bodyMedium) },
                                modifier = Modifier.testTag("nav_cart")
                            )

                            NavigationBarItem(
                                selected = currentRoute == Screen.Profile.route,
                                onClick = { navController.navigate(Screen.Profile.route) { popUpTo(Screen.Catalog.route) { saveState = true }; launchSingleTop = true; restoreState = true } },
                                icon = { Icon(if (currentRoute == Screen.Profile.route) Icons.Filled.Person else Icons.Outlined.Person, contentDescription = "Profile") },
                                label = { Text("Profile", style = MaterialTheme.typography.bodyMedium) },
                                modifier = Modifier.testTag("nav_profile")
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    LochaNavHost(navController = navController, viewModel = viewModel, isTablet = false)
                }
            }
        }
    }
}

@Composable
fun LogoHeader(isCollapsed: Boolean = false) {
    if (isCollapsed) {
        Box(
            modifier = Modifier
                .padding(vertical = 24.dp)
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(LochaObsidian),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "L",
                color = LochaAcidGreen,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "L",
                    color = LochaAcidGreen,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "LOCHA",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(LochaOrange)
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "HYPE",
                    fontSize = 8.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun LochaNavHost(
    navController: androidx.navigation.NavHostController,
    viewModel: LochaViewModel,
    isTablet: Boolean
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Catalog.route,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Screen.Catalog.route) {
            CatalogScreen(navController = navController, viewModel = viewModel, isTablet = isTablet)
        }
        composable(
            route = "detail/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailScreen(navController = navController, viewModel = viewModel, productId = productId)
        }
        composable(Screen.Wishlist.route) {
            WishlistScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.Cart.route) {
            CartScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController, viewModel = viewModel)
        }
        composable("checkout") {
            CheckoutScreen(navController = navController, viewModel = viewModel)
        }
    }
}

// --- CATALOG SCREEN ---

@Composable
fun CatalogScreen(
    navController: NavController,
    viewModel: LochaViewModel,
    isTablet: Boolean
) {
    val products by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val categories = ProductCatalog.categories
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val wishlistedIds by viewModel.wishlistedProductIds.collectAsStateWithLifecycle()

    var showSortMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
    ) {
        // Upper brand bar
        LogoHeader()

        // Persistent Search and Filter Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search Locha gear...", color = LochaMutedGrey) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("catalog_search_input")
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box {
                IconButton(
                    onClick = { showSortMenu = true },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .testTag("sort_button")
                ) {
                    Icon(Icons.Default.Tune, contentDescription = "Sort Products")
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Popularity / Rating") },
                        onClick = { viewModel.setSortOption(SortOption.POPULARITY); showSortMenu = false },
                        leadingIcon = { Icon(Icons.Default.Star, contentDescription = "Stars") }
                    )
                    DropdownMenuItem(
                        text = { Text("Price: Low to High") },
                        onClick = { viewModel.setSortOption(SortOption.PRICE_LOW_TO_HIGH); showSortMenu = false },
                        leadingIcon = { Icon(Icons.Default.ArrowDownward, contentDescription = "Low High") }
                    )
                    DropdownMenuItem(
                        text = { Text("Price: High to Low") },
                        onClick = { viewModel.setSortOption(SortOption.PRICE_HIGH_TO_LOW); showSortMenu = false },
                        leadingIcon = { Icon(Icons.Default.ArrowUpward, contentDescription = "High Low") }
                    )
                }
            }
        }

        // Categories List (Horizontal slider)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .border(1.dp, if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline, RoundedCornerShape(32.dp))
                        .clickable { viewModel.setCategory(category) }
                        .padding(horizontal = 18.dp, vertical = 9.dp)
                        .testTag("category_pill_$category")
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Active Banner (rebellious street look)
        if (searchQuery.isEmpty() && selectedCategory == "All") {
            Card(
                colors = CardDefaults.cardColors(containerColor = LochaObsidian),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .height(90.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        Text(
                            text = "LOCHA DROPS / VOL 3",
                            color = LochaAcidGreen,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Get free shipping on orders over $150. Wear your rebellion.",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clip(RoundedCornerShape(32.dp))
                            .background(LochaOrange)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Active", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Products Grid
        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Outlined.SearchOff,
                        contentDescription = "Not found",
                        tint = LochaMutedGrey,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No Gear Fits Your Search",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Try a different search tag or clear active filters to discover Locha drop items.",
                        fontSize = 12.sp,
                        color = LochaMutedGrey,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            val gridColumns = if (isTablet) 3 else 2
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("products_grid")
            ) {
                items(products) { product ->
                    val isWishlisted = wishlistedIds.contains(product.id)
                    ProductGridCard(
                        product = product,
                        isWishlisted = isWishlisted,
                        onWishlistToggle = { viewModel.toggleWishlist(product) },
                        onCardClick = { navController.navigate("detail/${product.id}") }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductGridCard(
    product: Product,
    isWishlisted: Boolean,
    onWishlistToggle: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clickable { onCardClick() }
            .testTag("product_card_${product.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color.Black.copy(alpha = 0.03f))
                    .padding(8.dp)
            ) {
                // Interactive dynamic Canvas drawing of the garment!
                GarmentRenderer(
                    garmentType = product.garmentType,
                    baseColor = product.defaultColor.toComposeColor(),
                    accentColor = product.defaultColor.toAccentColor(),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                )

                // Hype overlay badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (product.isNewRelease) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LochaOrange)
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text("NEW", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        if (product.isBestSeller) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LochaObsidian)
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text("HYPE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = LochaAcidGreen)
                            }
                        }
                    }

                    IconButton(
                        onClick = onWishlistToggle,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                        ),
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("wishlist_toggle_${product.id}")
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite product",
                            tint = if (isWishlisted) LochaOrange else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Text Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = product.category.uppercase(),
                    color = LochaMutedGrey,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = product.name,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", product.price)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Rating",
                            tint = LochaGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${product.rating}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// --- PRODUCT DETAIL SCREEN ---

@Composable
fun ProductDetailScreen(
    navController: NavController,
    viewModel: LochaViewModel,
    productId: String
) {
    val product = ProductCatalog.getProductById(productId)
    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Product not found.")
        }
        return
    }

    val wishlistedIds by viewModel.wishlistedProductIds.collectAsStateWithLifecycle()
    val isWishlisted = wishlistedIds.contains(product.id)

    var selectedColor by remember { mutableStateOf(product.defaultColor) }
    var selectedSize by remember { mutableStateOf("M") }
    val sizes = listOf("S", "M", "L", "XL")

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .size(44.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .testTag("detail_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }

                Text(
                    text = "CATALOG / DETAIL",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = LochaMutedGrey
                )

                IconButton(
                    onClick = { viewModel.toggleWishlist(product) },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .size(44.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .testTag("detail_wishlist_toggle")
                ) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Save to wishlist",
                        tint = if (isWishlisted) LochaOrange else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Big interactive image container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .background(Color.Black.copy(alpha = 0.02f))
                    .drawBehind {
                        // Drawing grid paper pattern indicating structural design
                        val interval = 40f
                        for (i in 0..(size.width / interval).toInt()) {
                            drawLine(
                                color = Color.Black.copy(alpha = 0.02f),
                                start = Offset(i * interval, 0f),
                                end = Offset(i * interval, size.height),
                                strokeWidth = 1f
                            )
                        }
                        for (i in 0..(size.height / interval).toInt()) {
                            drawLine(
                                color = Color.Black.copy(alpha = 0.02f),
                                start = Offset(0f, i * interval),
                                end = Offset(size.width, i * interval),
                                strokeWidth = 1f
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Interactive Canvas rendering that instantly repaints when custom color is clicked!
                GarmentRenderer(
                    garmentType = product.garmentType,
                    baseColor = selectedColor.toComposeColor(),
                    accentColor = selectedColor.toAccentColor(),
                    modifier = Modifier
                        .size(230.dp)
                )

                // Quick Floating code pill
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(LochaObsidian)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "REF: LC-${product.garmentType.name}-${product.id}0${selectedSize}",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Specs Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Category & Rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.category.uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = LochaOrange,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Product Rating",
                            tint = LochaGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${product.rating} (${product.reviewCount} Hype Votes)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Brand Name
                Text(
                    text = product.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 30.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Price tag
                Text(
                    text = "$${String.format(Locale.US, "%.2f", product.price)}",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Description
                Text(
                    text = product.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Interactive Color Picker
                Text(
                    text = "SELECT COLOR : ${selectedColor.name.uppercase()}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    product.availableColors.forEach { color ->
                        val isColorSelected = color == selectedColor
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color.toComposeColor())
                                .border(
                                    width = if (isColorSelected) 3.dp else 1.dp,
                                    color = if (isColorSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(
                                        alpha = 0.15f
                                    ),
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color }
                                .testTag("color_${color.name.replace(" ", "_")}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Size Picker
                Text(
                    text = "SELECT SIZE : $selectedSize",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sizes.forEach { size ->
                        val isSizeSelected = size == selectedSize
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSizeSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                .border(
                                    width = 1.dp,
                                    color = if (isSizeSelected) Color.Transparent else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedSize = size }
                                .padding(4.dp)
                                .testTag("size_$size"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = size,
                                color = if (isSizeSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Checkout adding
                Button(
                    onClick = {
                        viewModel.addToCart(product, selectedSize, selectedColor)
                        // Trigger simple alert snackbar
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Added ${product.name} [$selectedSize / ${selectedColor.name}] to bag!",
                                duration = SnackbarDuration.Short
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("add_to_cart_cta")
                ) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = "Add garment")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "ADD TO BAG",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// --- WORK-IN-PROGRESS CART SCREEN ---

@Composable
fun CartScreen(
    navController: NavController,
    viewModel: LochaViewModel
) {
    val items by viewModel.cartItems.collectAsStateWithLifecycle()
    val subtotal by viewModel.subtotal.collectAsStateWithLifecycle()
    val shipping by viewModel.shippingFee.collectAsStateWithLifecycle()
    val total by viewModel.orderTotal.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
    ) {
        // App header
        LogoHeader()

        Text(
            text = "YOUR SHOPPING BAG",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Outlined.ShoppingBag,
                        contentDescription = "Empty bag",
                        tint = LochaMutedGrey,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        "Bag is Empty",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Ready to start? Discover exclusive street shirts, jackets and accessories in our main releases.",
                        fontSize = 12.sp,
                        color = LochaMutedGrey,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { navController.navigate(Screen.Catalog.route) { popUpTo(0) } },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("EXPLORE SHOP", fontFamily = FontFamily.Monospace)
                    }
                }
            }
        } else {
            Column(modifier = Modifier.weight(1f)) {
                // List of cart products
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        CartListItem(
                            item = item,
                            onQtyIncrease = { viewModel.updateCartQty(item, item.quantity + 1) },
                            onQtyDecrease = { viewModel.updateCartQty(item, item.quantity - 1) },
                            onRemove = { viewModel.removeCartItem(item.id) }
                        )
                    }
                }

                // Billing calculations Card
                Card(
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("BAG SUBTOTAL", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = LochaMutedGrey)
                            Text("$${String.format(Locale.US, "%.2f", subtotal)}", fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("SHIPPING FEE", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = LochaMutedGrey)
                            val shipText = if (shipping == 0.0) "FREE ($0.00)" else "$${String.format(Locale.US, "%.2f", shipping)}"
                            Text(shipText, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = if (shipping == 0.0) LochaOrange else MaterialTheme.colorScheme.onSurface)
                        }

                        if (shipping > 0.0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Tip: Add $${String.format(Locale.US, "%.2f", 150.0 - subtotal)} more for FREE shipping",
                                fontSize = 10.sp,
                                color = LochaOrange,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ORDER TOTAL", fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Text(
                                "$${String.format(Locale.US, "%.2f", total)}",
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                viewModel.resetCheckout()
                                navController.navigate("checkout")
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("checkout_button")
                        ) {
                            Text("PROCEED TO CHECKOUT", fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartListItem(
    item: DbCartItem,
    onQtyIncrease: () -> Unit,
    onQtyDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Little dynamic Canvas of the active product color/garment
            val garmentEnum = try { GarmentType.valueOf(item.garmentType) } catch(e: Exception) { GarmentType.TEE }
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.03f))
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                GarmentRenderer(
                    garmentType = garmentEnum,
                    baseColor = Color(item.colorHex),
                    accentColor = Color(item.colorHex), // Solid flat
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Body
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.outline)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("SIZE ${item.selectedSize}", fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(item.colorHex))
                            .border(0.5.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(item.colorName.uppercase(), fontSize = 9.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$${String.format(Locale.US, "%.2f", item.price)}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }

            // Controls
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove item", tint = LochaOrange, modifier = Modifier.size(18.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quantity Row Selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .padding(horizontal = 4.dp)
                ) {
                    IconButton(onClick = onQtyDecrease, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(14.dp))
                    }

                    Text(
                        text = "${item.quantity}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    IconButton(onClick = onQtyIncrease, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

// --- WISHLIST SCREEN ---

@Composable
fun WishlistScreen(
    navController: NavController,
    viewModel: LochaViewModel
) {
    val items by viewModel.wishlistItems.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
    ) {
        LogoHeader()

        Text(
            text = "SAVED STYLINGS / WISHLIST",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Outlined.FavoriteBorder,
                        contentDescription = "Empty wishlist",
                        tint = LochaMutedGrey,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        "No Saved Gear",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Heart items during explore to save stylings for quick additions later.",
                        fontSize = 12.sp,
                        color = LochaMutedGrey,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(items) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .clickable { navController.navigate("detail/${item.productId}") }
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.2f)
                                    .background(Color.Black.copy(alpha = 0.02f))
                                    .padding(8.dp)
                            ) {
                                val garmentEnum = try { GarmentType.valueOf(item.garmentType) } catch(e: Exception) { GarmentType.TEE }
                                GarmentRenderer(
                                    garmentType = garmentEnum,
                                    baseColor = Color(item.colorHex),
                                    accentColor = Color(item.colorHex),
                                    modifier = Modifier.fillMaxSize()
                                )

                                IconButton(
                                    onClick = { viewModel.toggleWishlist(ProductCatalog.getProductById(item.productId) ?: return@IconButton) },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(32.dp)
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Favorite,
                                        contentDescription = "Remove",
                                        tint = LochaOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = item.productName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$${String.format(Locale.US, "%.2f", item.price)}",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.primary)
                                            .clickable { navController.navigate("detail/${item.productId}") }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("ADD", color = MaterialTheme.colorScheme.onPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- PROFILE & ORDER HISTORY SCREEN ---

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: LochaViewModel
) {
    val orders by viewModel.pastOrders.collectAsStateWithLifecycle()
    val sdf = remember { SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.US) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
    ) {
        LogoHeader()

        // User profile Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LochaObsidian),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Silhouette avatar
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(LochaAcidGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LC",
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = LochaObsidian,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        "LOCHA REBEL",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Text(
                        "Status: Hype Member Level 3",
                        fontSize = 11.sp,
                        color = LochaAcidGreen,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Joined May 2026 • Official Partner",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Text(
            text = "ORDER HISTORY / DESIGNS RECEIVED",
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Outlined.ReceiptLong,
                        contentDescription = "Empty orders",
                        tint = LochaMutedGrey,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No Previous Orders",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Completed checkout packages will display inside this history tracker log.",
                        fontSize = 11.sp,
                        color = LochaMutedGrey,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(orders) { order ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ORD-#${1000 + order.id}",
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(LochaOrange.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = order.trackingStatus.uppercase(),
                                        color = LochaOrange,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = sdf.format(Date(order.orderDate)),
                                fontSize = 11.sp,
                                color = LochaMutedGrey
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = order.itemsSummary,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "SHIP TO: ${order.shippingName.uppercase()}",
                                        fontSize = 9.sp,
                                        color = LochaMutedGrey,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "PAID VIA CARD *${order.paymentCardLast4}",
                                        fontSize = 9.sp,
                                        color = LochaMutedGrey,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", order.totalAmount)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- STEP BASED CHECKOUT SCREEN ---

@Composable
fun CheckoutScreen(
    navController: NavController,
    viewModel: LochaViewModel
) {
    val step by viewModel.checkoutStep.collectAsStateWithLifecycle()
    val total by viewModel.orderTotal.collectAsStateWithLifecycle()

    val name by viewModel.shippingName.collectAsStateWithLifecycle()
    val address by viewModel.shippingAddress.collectAsStateWithLifecycle()
    val city by viewModel.shippingCity.collectAsStateWithLifecycle()
    val zip by viewModel.shippingZip.collectAsStateWithLifecycle()

    val cardNum by viewModel.cardNumber.collectAsStateWithLifecycle()
    val cardExp by viewModel.cardExpiry.collectAsStateWithLifecycle()
    val cardCv by viewModel.cardCvv.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (step > 1 && step < 3) {
                            viewModel.checkoutStep.value = step - 1
                        } else {
                            navController.popBackStack()
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.size(44.dp).border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }

                Text(
                    text = "CHECKOUT SECURE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = LochaMutedGrey
                )

                Box(modifier = Modifier.size(44.dp)) // Equalizer placeholder
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Interactive visual checkout step progress
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                StepIndicator(num = 1, title = "Shipping", active = step >= 1)
                Spacer(modifier = Modifier.width(8.dp))
                HorizontalDivider(modifier = Modifier.width(32.dp), color = if (step >= 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.width(8.dp))
                StepIndicator(num = 2, title = "Payment", active = step >= 2)
                Spacer(modifier = Modifier.width(8.dp))
                HorizontalDivider(modifier = Modifier.width(32.dp), color = if (step >= 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.width(8.dp))
                StepIndicator(num = 3, title = "Success", active = step >= 3)
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (step) {
                1 -> {
                    // Shipping Screen
                    Text("SHIPPING AND DISPATCH ADDRESS", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { viewModel.shippingName.value = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("shipping_name")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { viewModel.shippingAddress.value = it },
                        label = { Text("Street Address") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("shipping_address")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { viewModel.shippingCity.value = it },
                            label = { Text("City") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("shipping_city")
                        )

                        OutlinedTextField(
                            value = zip,
                            onValueChange = { viewModel.shippingZip.value = it },
                            label = { Text("Postal / ZIP") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("shipping_zip")
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ORDER TOTAL DUE", fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Text("$${String.format(Locale.US, "%.2f", total)}", fontSize = 18.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.checkoutStep.value = 2 },
                        enabled = name.isNotEmpty() && address.isNotEmpty() && city.isNotEmpty() && zip.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("shipping_next_button")
                    ) {
                        Text("CONTINUE TO PAYMENT", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
                2 -> {
                    // Payment Screen
                    Text("SECURE SIMULATED PAYMENT GATEWAY", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Minimal aesthetic Credit Card container
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = LochaObsidian),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp)
                        ) {
                            Column {
                                Text("LOCHA INC. SECURE ACCESS", color = LochaAcidGreen, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(30.dp))
                                // Card number mask
                                val maskedDigits = if (cardNum.isEmpty()) "•••• •••• •••• ••••" else cardNum.chunked(4).joinToString(" ")
                                Text(
                                    text = maskedDigits,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }

                            Row(
                                modifier = Modifier.align(Alignment.BottomCenter),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("CARD HOLDER", fontSize = 8.sp, color = Color.White.copy(alpha = 0.5f))
                                    Text(name.uppercase(), fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("EXPIRE", fontSize = 8.sp, color = Color.White.copy(alpha = 0.5f))
                                    Text(if (cardExp.isEmpty()) "MM/YY" else cardExp, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = cardNum,
                        onValueChange = { if (it.length <= 16) viewModel.cardNumber.value = it },
                        label = { Text("Credit Card Number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("payment_card_number")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cardExp,
                            onValueChange = { viewModel.cardExpiry.value = it },
                            label = { Text("Expiration (MM/YY)") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("payment_expiry")
                        )

                        OutlinedTextField(
                            value = cardCv,
                            onValueChange = { if (it.length <= 3) viewModel.cardCvv.value = it },
                            label = { Text("CVV Security") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("payment_cvv")
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            viewModel.processOrderCheckout(onSuccess = {})
                        },
                        enabled = cardNum.length >= 12 && cardExp.isNotEmpty() && cardCv.length >= 3,
                        colors = ButtonDefaults.buttonColors(containerColor = LochaOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("payment_submit_button")
                    ) {
                        Text("AUTHORIZE PAYMENT", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                3 -> {
                    // Success Screen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Order checked out",
                                tint = LochaAcidGreen,
                                modifier = Modifier
                                    .size(96.dp)
                                    .background(LochaObsidian, CircleShape)
                                    .border(2.dp, LochaAcidGreen, CircleShape)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                "LOCHA ORDER AUTHORIZED",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Your street gears are registered successfully! Package shipping label will be drafted in 24 hours.",
                                fontSize = 12.sp,
                                color = LochaMutedGrey,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            Button(
                                onClick = {
                                    navController.navigate(Screen.Profile.route) {
                                        popUpTo(Screen.Catalog.route)
                                    }
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("VIEW RECIPIENT LOGS", fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepIndicator(num: Int, title: String, active: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$num",
                color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace,
            color = if (active) MaterialTheme.colorScheme.onBackground else LochaMutedGrey
        )
    }
}
