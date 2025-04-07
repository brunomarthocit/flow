package com.bt.taptopay.features.login

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bt.taptopay.R
import com.bt.taptopay.components.commons.BtButtonComponent
import com.bt.taptopay.components.commons.BtLoadingComponent
import com.bt.taptopay.components.commons.BtTextButtonComponent
import com.bt.taptopay.components.commons.BtTitleTextComponent
import com.bt.taptopay.components.networkfailure.ConnectionErrorComponent
import com.bt.taptopay.extensions.findActivity
import com.bt.taptopay.extensions.tryOpenOnBrowser
import com.bt.taptopay.features.login.ChooseLoginScreenTestTags.CHOOSE_LOGIN_ERROR_CONTAINER
import com.bt.taptopay.features.login.ChooseLoginScreenTestTags.CHOOSE_LOGIN_LOADING_CONTAINER
import com.bt.taptopay.features.login.ChooseLoginScreenTestTags.CHOOSE_LOGIN_SCREEN_CONTAINER
import com.bt.taptopay.helpers.commons.btRegularFontFamily
import com.bt.taptopay.helpers.commons.textSizeResource
import com.bt.taptopay.models.AuthenticationType
import com.bt.taptopay.models.SharedGlobalMessages
import org.koin.androidx.compose.koinViewModel

@Composable
fun ChooseLoginScreen(
    viewModel: ChooseLoginViewModel = koinViewModel(),
    blockSignUp: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycle = lifecycleOwner.lifecycle
    val state by viewModel.state.collectAsState()
    val showBlockSignUpNotification by viewModel.showBlockSignUpNotification.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_CREATE) {
                viewModel.onAppear()
                if (blockSignUp) {
                    viewModel.checkBlockSignUp()
                }
            } else if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkLoading()
            }
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(showBlockSignUpNotification) {
        if (showBlockSignUpNotification
            && SharedGlobalMessages.globalMessages.blockSignUpText != null
        ) {
            snackBarHostState.showSnackbar(
                message = "",
                actionLabel = null,
                duration = SnackbarDuration.Long
            )
            viewModel.checkBlockSignUpToastShown()
        }
    }

    when (state) {
        ViewState.Idle -> {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(colorResource(id = R.color.backgroundColor))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        snackBarHostState.currentSnackbarData?.dismiss()
                    }
                    .testTag(CHOOSE_LOGIN_SCREEN_CONTAINER)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = dimensionResource(id = R.dimen.triplePadding)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BtTitleTextComponent(
                        modifier = Modifier
                            .padding(
                                vertical = dimensionResource(id = R.dimen.veryBigPadding)
                            )
                    )

                    Text(
                        text = stringResource(id = R.string.loginScreenTitle),
                        fontFamily = btRegularFontFamily,
                        color = colorResource(id = R.color.neutralDarkerColor),
                        fontSize = textSizeResource(id = R.dimen.titleRegular),
                        modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.veryBigPadding))
                    )

                    BtButtonComponent(
                        modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.defaultPadding)),
                        text = stringResource(id = R.string.loginBTButton)
                    ) {
                        viewModel.goToLogin(
                            context.findActivity(),
                            AuthenticationType.BT
                        )
                    }

                    BtButtonComponent(
                        text = stringResource(id = R.string.loginEEButton),
                        backgroundColor = colorResource(id = R.color.neutralDarkestColor)
                    ) {
                        viewModel.goToLogin(
                            context.findActivity(),
                            AuthenticationType.EE
                        )
                    }

                    if (viewModel.remoteConfiguration.isCreateEEAccountEnabled) {
                        BtTextButtonComponent(
                            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.defaultPadding)),
                            text = stringResource(id = R.string.loginCreateAccountButton)
                        ) {
                            viewModel.didTapCreateAccountButton { createAccountUrl ->
                                context.tryOpenOnBrowser(Uri.parse(createAccountUrl))
                            }
                        }
                    }
                }

                BtTextButtonComponent(
                    modifier = Modifier
                        .padding(bottom = dimensionResource(id = R.dimen.doublePadding))
                        .align(Alignment.BottomCenter),
                    text = stringResource(id = R.string.needHelp),
                ) {
                    viewModel.didTapHelpButton()
                }

                if (showBlockSignUpNotification) {
                    SharedGlobalMessages.globalMessages.blockSignUpText?.let { text ->
                        SnackbarHost(
                            hostState = snackBarHostState,
                            snackbar = {
                                Snackbar(
                                    modifier = Modifier
                                        .padding(
                                            horizontal = dimensionResource(id = R.dimen.doublePadding),
                                            vertical = dimensionResource(id = R.dimen.doublePadding),
                                        ),
                                    shape = MaterialTheme.shapes.medium,
                                    contentColor = colorResource(id = R.color.neutralLightestColor),
                                    containerColor = colorResource(R.color.neutralDarkerColor)
                                ) {
                                    Text(
                                        modifier = Modifier.padding(
                                            horizontal = dimensionResource(id = R.dimen.defaultPadding),
                                            vertical = dimensionResource(id = R.dimen.doublePadding),
                                        ),
                                        text = text,
                                        fontWeight = FontWeight.W700,
                                        fontFamily = btRegularFontFamily,
                                        fontSize = textSizeResource(id = R.dimen.titleSmall),
                                        lineHeight = textSizeResource(id = R.dimen.lineHeightLarge),
                                        textAlign = TextAlign.Start,
                                        color = colorResource(R.color.neutralLightestColor)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        is ViewState.Error -> {
            val connectionError = (state as ViewState.Error).connectionError
            ConnectionErrorComponent(
                modifier = Modifier.testTag(CHOOSE_LOGIN_ERROR_CONTAINER),
                connectionError = connectionError
            ) {
                viewModel.loadWebAuthentication(context.findActivity())
            }
        }

        ViewState.Loading -> {
            BtLoadingComponent(
                modifier = Modifier.testTag(CHOOSE_LOGIN_LOADING_CONTAINER)
            )
        }
    }
}


object ChooseLoginScreenTestTags {
    const val CHOOSE_LOGIN_SCREEN_CONTAINER = "CHOOSE_LOGIN_SCREEN_CONTAINER"
    const val CHOOSE_LOGIN_ERROR_CONTAINER = "CHOOSE_LOGIN_ERROR_CONTAINER"
    const val CHOOSE_LOGIN_LOADING_CONTAINER = "CHOOSE_LOGIN_LOADING_CONTAINER"
}