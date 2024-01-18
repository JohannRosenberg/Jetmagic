<p align="center">
    <img src="../../blob/main/images/jetmagic-840x500.jpg" width="840" height="500" />
</p>

# Jetmagic - A framework for building responsive Android apps using Jetpack Compose

Jetmagic is an Android framework that can be used to develop responsive Android apps that are built using Jetpack Compose. It provides features beyond those offered by Android's own Jetpack Compose framework. Jetmagic's primary purpose is to provide infrastructure needed to manage composables while letting you focus on building your app's core business objective. The framework consists of the following components:<br /><br />

* [**Navigation Manager**](../../tree/main/jetmagic/readme-navigation.md)![](https://en.wikipedia.org/w/resources/src/mediawiki.skinning/images/external-ltr.svg?59558): A replacement for Android's standard Jetpack Compose navigation API. It supports animated navigation and the ability to pass objects between screens/composables. It also supports caching screens.

* [**Composable Resource Manager (CRM)**](../../tree/main/jetmagic/readme-crm.md)![](https://en.wikipedia.org/w/resources/src/mediawiki.skinning/images/external-ltr.svg?59558): Provides support for responsive layouts allowing apps to easily switch "layouts" in a way similar to how alternate xml layouts are selected based on the device configuration using qualifiers such as language/region, device orientation, screen size, screen density, etc. Instead of xml resource layouts, the CRM manages composable resources. Useful for apps that need to run on multiple devices including phones/tablets/TVs. Built-in support for managing viewmodels for each composable.

* [**Image Manager**](../../tree/main/jetmagic/readme-image-manager.md)![](https://en.wikipedia.org/w/resources/src/mediawiki.skinning/images/external-ltr.svg?59558): Manages composable images. Currently this component provides better support for images that use animations/transitions than the standard Image composable.

* [**Deep Linking**](../../tree/main/jetmagic/readme-deep-link.md)![](https://en.wikipedia.org/w/resources/src/mediawiki.skinning/images/external-ltr.svg?59558): Provides support for deep links. Jetmagic allows external URIs to launch one or more screens while integrating wth the CRM to provide the correct layout based on the current system configuration. Jetmagic's deep linking supports multiple paths for any screen, regular expressions in paths, the ability to intercept, alter or terminate the navigation as Jetmagic navigates towards the target screen. It is also possible to hide all the launched screens (in a multi-screen deep link) except the target screen allowing developers to preprocess any data before navigating to the next successive screen.

<br />

The Github project contains a fully functional app that demonstrates each API. For a detailed overview of the framework and a step-by-step guide on creating a minimal
responsive app, see:

https://johannblake.medium.com/creating-responsive-layouts-using-jetpack-compose-7746ba42666c

Add the following dependencies to your app's build.gradle file:

```
implementation 'dev.wirespec.jetmagic:jetmagic:1.5.0'
implementation "androidx.compose.runtime:runtime-livedata:$compose_version"
```

<br />

## Jetmagic Architecture

<p align="center">
    <img src="../../blob/main/images/jetmagic-architecture.png" width="700" height="638" />
</p>

<br />

Jetmagic treats your composables like resources in a similar way that Android treats xml-based layout resources under the older view-based system. Under the older system, Android detects the device configuration and any changes to the device such as an orientation change and then selects the xml resource layout that would apply to the configuration settings.

Using Jetmagic, you tell the Composable Resource Manager (CRM) which composables you want to use as "*layouts*". You do this by creating an instance of a **ComposableResource** and set properties in the constructor that indicate the configuration settings you want associated with your composable such as orientation, screen size, screen density, etc. You also provide a callback that will get called if the resource is selected. In the callback, you simply make a call to the composable function that you want to use to render the UI.

When your app starts up, it will normally call on the Navigation Manager to navigate to the start sceen (a.k.a, the home screen). This call is made by indicating the resource you want to display. The Navigation Manager will then call the CRM which will then go through the list of composable resources that have been set up and find the one that the Navigation Manager has requested to navigate to. There could be more than one resource available and each of them is specific to some device configuration setting (or may act as a default with no configuration settings). Using the same algorithm that Android uses when selecting xml resources - through a process of elimination - it will end up with one resource that best matches the device configurations. It then creates an instance of a **ComposableInstance** class which will be used to contain state data about the composable that can be used during the time it is displayed on the screen. It returns this composable instance to the Navigation Manager, which then pushes it on top of its navigation stack, making it the current screen on the stack.

The Navigation Manager, through the use of LiveData, then triggers the Screen Factory to perform an update. The Screen Factory acts like a container for all the screens. It is not actually a container such as a Surface, Box, Column, etc. It is however a composable and you would normally include the Screen Factory as one of your top most composables within your screen hierarchy. This means that you can place a Screen Factory inside something like Surface, Box, or the content section of a Scaffold. Any composables that the Screen Factory renders will be placed inside your own container.

When the Screen Factory is either composed/recomposed including when the Navigation Manager triggers an update, the Screen Factory will then access all the composable instances on the navigation stack. Each composable instance effectively represents a screen. It then iterates through the list of these composable instances and invokes the RenderComposable property that each composable instance has. As part of the process of rendering the composable, the **onRender** property - which is a property of a composable resource - will be called, causing the composable associated with the resource to render itself. Any animations that are assigned to the resource will also be executed when the composable is rendered.

Navigating to another screen is done by either calling on the Navigation Manager or the CRM. If you know that a screen does not have any alternate resources but will always be displayed the same regardless of what the device configuration settings are, you can use the Navigation Manager. You also use the Navigation Manager if you want to go back to the previous screen or directly to the home screen. To use the CRM for navigation, consider the case where you have list of cats (as in the demo app) that are shown when the device is in portrait mode and clicking on an item takes you to another screen to show the cat details. But if you are on the list screen and rotate the device to be in landscape mode, you might decide to show both the list and the details side-by-side, as there is enough space for this. In landscape mode, when you click on the list item, the details pane will update - no navigation to another screen is performed. In this example, you don't want to be concerned with whether you are peforming a navigation or an update. You would just call the CRM's updateOrNavigateTo function and let it figure it out and make the correct choice. If the CRM determines that the details screen is not on the same screen that is displaying the list, it will call on the Navigation Manager to navigate to a new screen to show the details.

If configuration change occurs while the app is running, the activity's **onDestroy** is called and a call is made to the CRM informing it that a configuration change has occurred. The CRM will obtain all the composable instances from the Navigation Manager and set their **recourceId** to null. The resourceId is used to identify which composable resource was used to render the composable. Although the activity has been destroyed, the Navigation Manager and the CRM both retain the state of all the composable resources and composable instances. When the activity is restarted after onDestroy, the Screen Factory will be rendered again and will proceed to render the composable instances as it normally does. However, when it calls RenderComposable for each composable instance, the CRM will notice that the resourceId is set to null, indicating that it may need to select a new resource to render the composable. Because the composable instance is separate from the composable resource, the CRM and the Navigation Manager are able to retain the state of the composable instance even when a new resource is selected. This state includes any optional viewmodel that you want associated with the composable.

<br /><br />

## Using the Demo App

Before setting up and working with Jetmagic's API, it is helpful to try out the demo app to see what Jetmagic is capable of. The following tests demonstrate the more important features although Jetmagic has more capabilities than are currently available in the demo app:

| Features                                            | Test                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| --------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Different orientation layouts                       | Make sure your device is set to allow for screen rotations. Tap on the image of a cat in the grid list to bring up the details screen. Notice the layout of the screen. Rotate the device. Notice the different layout.                                                                                                                                                                                                                                                                                                                                                                                                                              |
| Language changes                                    | Go to Android's language settings and add German to list of languages your device is currently set to use but don't make it the default language. Bring up the cat details screen. Without closing the activity, return back to the language settings screen and make German the default language. Switch back to the demo app and you'll notice that the details screen has been replaced with just some text to indicate that you have switched to German.                                                                                                                                                                                         |
| Smooth image transitions                            | Tap on the image of a cat in the grid list. Tap on the Back button. The details screen exits smoothly with no flickering of images.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| Custom animations when navigating to another screen | Make sure you're on the app's home screen. Open the navigation drawer either by tapping on the hamburger menu or swiping on the screen from left to right. Tap on any item in the navigation drawer. A test screen will become visible using animation that animates it from the bottom of the screen to the top. Tap on the Back button and it will animate off the screen from top to bottom.                                                                                                                                                                                                                                                      |
| Screen state maintained when navigating             | Bring up the test screen and note the screen id.  Tap on the button with the label **"Go to another test screen"**. Another test screen will be displayed. Note the screen id. Repeat this several times opening a number of test screens. Return back to each screen and notice how the screen id is what it was when you left the screen.                                                                                                                                                                                                                                                                                                          |
| Go directly to Home screen                          | Bring up the test screen and open up several more test screens by tapping on the **"Go to another test screen"** button. Tap on the button labeled **"Go to home screen"**. This will take you directly to the home screen. Hitting the Back button on the home screen will exit the activity.                                                                                                                                                                                                                                                                                                                                                       |
| Return a value from another screen                  | Bring up the test screen and tap on the button labeled **"Return value from another screen"**.  Another screen will appear with three option buttons. Select an item and tap on the button labeled **"Return selection"**. The screen goes back to the test screen and the selected value is displayed in a snackbar.                                                                                                                                                                                                                                                                                                                                |
| Prompt when returning to previous screen            | Bring up the test screen and tap on the button labeled **Prompt when returning**. On the screen that appears, either click on the button labeled **Return to previous screen** or tap on your device's Back button. A dialog will appear asking if you want to return. If you tap on **No**, the dialog is dismissed and you remain on the screen. Only when tapping **Yes** will you return to the previous screen.                                                                                                                                                                                                                                 |
| Restore activity to last used screen after exiting  | Start from the home screen and tap on a cat list item to bring up the details screen. Scroll down and tap on the **Adopt** button. The test screen will be displayed. Open up several test screens by tapping on the button **"Go to another test screen"**. On the last test screen you open, tap on the button labeled **"Terminate activity"**. The activity will close. Start the app again by tapping on the app's launch icon. You will notice that you are on the test screen where you exited the app. Tap on the Back button continously to return through all the previous screens until you reach the home screen.                        |
| Deep linking to a single target screen.             | Open up a terminal and execute the following adb command to launch a deep link:<br /><br />```adb shell am start -W -a android.intent.action.VIEW -d "http://www.wirespec.dev/jetmagic/sample/pet_info?name=a" dev.wirespec.jetmagic```<br /><br />The pet details screen will be displayed with a cat whose first name starts with the letter "a" (which happens to be "Axl"). Alternatively, instead of using the adb command, you can just launch the url from some app. For example, send yourself an email with a link to:<br/>```http://www.wirespec.dev/jetmagic/sample/pet_info?name=a```<br /><br /> and just tap on the link in the email. |
| Deep linking that opens successive screens.         | Open up a terminal and execute the following adb command to launch a deep link<br /><br />```adb shell am start -W -a android.intent.action.VIEW -d "http://www.wirespec.dev/jetmagic/sample/deeplink" dev.wirespec.jetmagic```<br /><br />Three screens will open in succession displaying some plain text. In a production app, these screens could be:<br /><br />```Customer Orders > Order Details > Product Info```<br/><br />Tapping on the Back button navigates back to each of these screens.                                                                                                                                              |
| Deep linking with an unknown URI                    | Open up a terminal and execute the following adb command to launch a deep link<br /><br />```adb shell am start -W -a android.intent.action.VIEW -d "http://www.wirespec.dev/jetmagic/sample/kittens" dev.wirespec.jetmagic```<br /><br />A screen is shown with text that indicates that the URI is unknown. This is useful to handle unknown URIs. You can use it to redirect the user to an alternative screen.                                                                                                                                                                                                                                   |

<br />

## Setting up Jetmagic

Although the documentation here for setting up Jetmagic seems long, the actual code to setup is very little. The majority of what is written here is a detailed description of each step to clarify the concepts involved.

Jetmagic is designed to work with single activity apps or apps that use multiple activities. Multi-activity apps however are rather out-dated at this stage in Android development and it is recommended that if you are developing a new app that you use a single activity. Single activity apps support multiple screens. Historically, building single activity apps has been somewhat of a challenge and often required a lot of infrastructure support and boilerplates to work seemlessly. That is not the case with Jetpack Compose and even less an issue when using Jetmagic.

The setup of Jetmagic described here is for a single activity app as it allows for easy access to Jetmagic APIs throughout your app. It also has the benefit that if the activity gets destroyed and the user restarts the activity, Jetmagic will restore the state of the activity as it previously was - navigating to the last screen the user was on. Achieving this using a multi-activity app is more challenging and left for the developer to use a solution that they feel is appropriate for their scenario. If you are required to build a multi-activity app, you simply need to manage an instance of NavigationManager and ComposableResourceManager for each activity. You won't have the benefit of restoring the app to its last state but this may be more of a nice-to-have feature rather than a must-have.
<br /><br />
Setting up Jetmagic consists of the following steps:

1. Initialize Jetmagic
2. Add some minimal boilerplate code to your activity.
3. Add a screen factory to render composables as screens.
4. Create composable resources that function similarly to layout resources.
5. Define the resource Ids that will map to composable resources.
6. Add the composable resources to the Composable Resource Manager for the CRM to manage.
7. Add code to navigate to your app's startup screen.

<br />

#### 1. Initialize Jetmagic

 If your app doesn't have a class that inherits from Application, create one and add a line of code that call initializeJetmagic . For example:

```kotlin
import android.app.Application

class App: Application() {
    override fun onCreate() {
        initializeJetmagic(this)
    }
}
```

Make sure the class is registered in your AndroidManifest.xml. Also, make sure tha launch mode for your activity is set to *singleInstance*. For example, the demo app uses:

```xml
<manifest>
    <application
        <android:name="dev.wirespec.jetmagic.App">
            <activity
                android:name=".ui.screens.MainActivity"
                android:launchMode="singleInstance">
        </activity>
    </application>
</manifest>
```

<br />

#### 2. Add Boilerplate to Activity

In your activity, add the following boilerplate code:

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navman.activity = this
    }

    override fun onBackPressed() {
        if (!navman.goBack())
            super.onBackPressed()
    }

    override fun onDestroy() {
        crm.onConfigurationChanged()
        super.onDestroy()
    }
}
```

The **navman** variable is globally accessible throughout your app and refers to an instance of NavigationManager.

<br />

#### 3. Add a Screen Factory

The screen factory is a composable function that is used to render composables that represent a screen. The Navigation Manager will trigger the screen factory whenever the user navigates to or from screens or when a screen needs to be updated. The screen factory should be one of your top most composables in your composable hierarchy. The demo app uses a scaffold which is essence the top most composable in the app. However, the actual screens that get displayed are rendered inside the scaffold's content section. It is here that the screen factory is added. If your app doesn't use a scaffold, just place it within whatever your top most composable happens to be. This could for example be a Box, Column, Row, Surface, etc. In the demo app, it is added as follows:

```kotlin
@Composable
fun Main(scaffoldState: ScaffoldState, drawerGesturesEnabled: Boolean, modifier: Modifier = Modifier) {
    Box {
        Scaffold(
            modifier = modifier,
            drawerGesturesEnabled = drawerGesturesEnabled,
            scaffoldState = scaffoldState,
            drawerBackgroundColor = Color.Transparent,
            drawerElevation = 0.dp,
            drawerContent = {
                NavDrawerHandler(scaffoldState = scaffoldState)
            },
            content = {
                ScreenFactoryHandler()
            }
        )
    }
}
```

<br />

#### 4. Create Composable "Resources"

Under the older xml view layout system, you would typically create a layout using xml resource files and place these in the<br />

```res/layout```<br />

 folder. If you had alternative resources you would place them in a folder with configuration qualifiers in the folder name. For example, the Jetmagic demo app shows a screen in portrait mode on a phone with a list of cats and clicking on a llst item takes you to the details screen. However, if the app is run on a tablet in landscape mode where there is more screen space, the list of pets and the details are shown side by side on a single screen. Under the older xml view system, you could create a layout called **cat_list.xml** and a **cat_details.xml**. These two layouts could be inflated when run on a phone. You would then have a third layout that combines the list and the detail layouts and you would call this layout **cat_list.xml** but place this under the folder:<br />

```res/layout-xlarge-land```

When the app runs and the user changes the orientation of the device, Android will automatically select the correct **cat_list.xml** layout based on the current device configuration.

Jetmagic operates in a similar way, only instead of xml resource files and folders with qualifier names in the folder names, the xml resource files are replaced with Composable functions and placed in source code folders. The qualifiers are set using properties as we'll see later on. In this step, we are only interested in creating the Composable functions. In the following step, we'll see how Jetmagic is able to treat these composable functions as "resources" and select the correct one at runtime.

In the demo app, the cat list composable is defined in the file:

```PetsListUI.kt```

This file contains two composable functions:

```kotlin
@Composable
fun PetsListHandler(composableInstance: ComposableInstance) {

    CompositionLocalProvider(LocalComposableInstance provides composableInstance) {

        // Code for handling viewmodel and other stuff not shown...

        PetsList(
            modifier = modifier,
            petsList = petsList,
            scrollState = vm.scrollState,
            onItemClick = { petInfo ->
                // Handle clicking on list item...
            },
            onToolbarMenuClick = {
                // Handle clicking on navigation drawer...
            }
        )
    }
}

@Composable
fun PetsList(
    modifier: Modifier = Modifier,
    petsList: List<PetListItemInfo>? = null,
    scrollState: ScrollState,
    onItemClick: (petInfo: PetListItemInfo) -> Unit,
    onToolbarMenuClick: () -> Unit
) {
    // Code to display list of cats goes here...
}
```

The code here has been simplified to illustrate the important aspects. The composable that gets rendered (composed) to the screen, is PetsList. PetsListHandler is used to handle the viewmodel and do any setup work that needs to be done. This follows the so-called "hoisting" pattern that is recommended when createing composable functions. It is recommended that you add the text "Handler" after the name of the composable function's name to distinguish it from the composable that gets renderered.

We now need to add the pet details composable. In the demo app, the **PetDetailsUI.kt** file is used for this:

```kotlin
@Composable
fun PetDetailsHandler(composableInstance: ComposableInstance) {

    CompositionLocalProvider(LocalComposableInstance provides composableInstance) {

        // Code for handling viewmodel and other stuff not shown...

        PetDetailsUI(
            modifier = modifier,
            pet = pet,
            scrollState = vm.screenScrollState,
            onAdoptClick = {

            },
            onBackButtonClick = {

            })
    }
}

@Composable
fun PetDetailsUI(
    pet: PetListItemInfo?,
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    onAdoptClick: () -> Unit,
    onBackButtonClick: () -> Unit
) {
   // Code to display the selected cat details goes here...
}
```

At this point we have a composable that will display a list of cats and another to display details of a cat. Neither of these composables however are considered "screens". The screens we need to create are also composables used to host the composables that we have defined. For the demo app, we need a screen composable to host the list of cats when it runs in portrait mode on any device while we need another screen composable that displays both the list and the details side by side when run on a tablet in landscape mode. In the demo app, the screen for hosting the list is **PetsListScreenHandler.kt**:

```kotlin
@Composable
fun PetsListScreenHandler(composableInstance: ComposableInstance) {

    CompositionLocalProvider(LocalComposableInstance provides composableInstance) {
        crm.RenderComposable(
            parentComposableId = composableInstance.id,
            composableResId = ComposableResourceIDs.PetsList,
            childComposableId = "petsList")
    }
}
```

You'll notice that in the screen handler the pets list composable is not embedded or referenced directly with its composable function. Instead, the RenderComposable function is used to define the resource that is to be rendered whenever the screen handler itself is rendered. The RenderComposable function is member function of ComposableResourceManager and the crm variable is a globally accessible instance of the CRM.

In this example, the resource is the PetsList and is identified by an id defined in the file ComposableResourceIDs.kt. As we'll see later on, this id will be mapped to the actual pets list composable that gets rendered. This is necessary because during runtime, the device's configuration can change and the CRM will select the correct composable to render using the id that is provided here. The key take away is to realize that you can create multiple composable resources that all use the same id. The only requirement is that each of them has a unique set of qualifiers when they are added to the CRM. This will be covered in a later step.

You never add a screen handler to your UI manually. Instead, the screen factory in collaboration with the CRM will determine when to render this. The screen handler composable itself is added to the CRM the same way as the other composables we have created and they too can have configuration qualifiers that determine which one gets selected at runtime.

When a screen handler is composed, a "composable instance" parameter will be provided which contains state information about a screen. When you want to render a child composable within the screen, you call RenderComposable and set the parentComposableId to the id provided by the composableInstance. This id is generated internally and uniquely identifies the screen. When children composables are added to the screen, they need to identify the parent they belong to.

The composableResId parameter identifies the composable resource that you want to render as a child within the parent.

The childComposableId is an id that you can optionally specify to identify the child composable. While it is optional, it is recommended that you provide one. It doesn't have to be unique throughout the app but it does need to be unique within a screen handler.

Finally, we need a screen handler for the case where both the list and the details are displayed side by side. In the demo app this located in the file **PetsListWithDetailsScreenHandler.kt**:

```kotlin
@Composable
fun PetsListWithDetailsScreenHandler(composableInstance: ComposableInstance) {

    CompositionLocalProvider(LocalComposableInstance provides composableInstance) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                crm.RenderComposable(
                    parentComposableId = composableInstance.id,
                    composableResId = ComposableResourceIDs.PetsList,
                    childComposableId = "petsList"
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                crm.RenderComposable(
                    parentComposableId = composableInstance.id,
                    composableResId = ComposableResourceIDs.PetDetails,
                    childComposableId = "petDetailsPane",
                    p = PetDetailsParams(displayAppBar = false)
                )
            }
        }
    }
}
```

In this screen handler, we use a row with two columns and place the cat list resource in the left column and the details in the right column. The details resource also contains a **p** parameter. This lets you pass in any optional data that the composable may need when it is rendered. In this example, we don't want to display the app bar when for the details when it renders on a tablet in landscape mode, but we do want it displayed when the details is shown on a phone without the list, or on a tablet in portrait mode.

It should be noted that when the app is run and you click on a list item, either the details get updated (if the details is show on the same screen), or the user will be navigated to the details screen if it is not on the same screen as the list. This is handled by making calls to the crm to determine what course of action needs to be taken and any data that needs to be passed from the list composable to the details composable is done through the crm. The **p** parameter shown above is mostly meant for optional UI settings that you might want to set when the composable is rendered.

Where you place your screen handlers in your Android project is your own choice. You also are not required to append the text *ScreenHandler** at the end but it certainly makes it more obvious that the composable refers to the root composable for the screen that gets rendered. A recommended convention is to create folders (packages) with names that match configuration qualifier names. This would align more with the way xml layout files are stored using the older view based system. In the demo app, the screen handlers are located as follows:

```
ui > screens > petslist > PetsListScreenHandler.kt
ui > screens > petslist > xlarge > land > PetsListWithDetailsScreenHandler.kt
```

Qualifiers have a precedence, so you could create these folders in the order of precedence to make it easier to visualize their priorities. For example, the screen size qualifier has a higher precedence than the orientation qualifier and the language/region qualifier has a higher precedence than the screen size qualifier. So you could store a composable that is for the french language, runs on a xlarge screen and only in landscape mode in this folder:

```
ui > screen > petslist > fr > xlarge > land > PetsListScreenHandler.kt
```

When you create alternate composable resources, you are not required to give the composable function the same name but it is recommended that you do so as it makes it easier to manage.

<br />

#### 5. Create Composable Resource IDs

Every screen in your app must be provided an id that Jetmagic will use to identify it. This id can be any string you want but needs to be unique throughout your app. The same screen can be rendered multiple times and each time a new instance of the screen is rendered, Jetmagic will create a unique internal id to distinguish between them. The id that you provide however is only used to distinguish the different **types of screens** your app is providing. For example, you might have a screen that lists users, another screen for user details, another for app settings, etc.
<br /><br />
In the previous step we created PetsListScreenHandler and PetsListWithDetailsScreenHandler. Collectively these are both referred to as the **Pets List Screen**. Even though PetsListWithDetailsScreenHandler contains the details composable as well, we still consider the screen the **Pets List Screen** because from the user's perspective, even though the screen changes its layout when the user rotates the device, they are still on the same screen. They haven't navigated away to a different screen. For this reason, the id given for the **Pets List Screen** is simply called **PetsListScreen**. Generally speaking, if the user navigates to a different screen, that screen requires its own unique id.

Here is the contents of ComposableResourceIds.kt for the demo app:

```kotlin
object ComposableResourceIDs {
    // Screens
    const val CatSelectionScreen = "catSelectionScreen"
    const val DeepLinkScreen1 = "deepLink1Screen"
    const val DeepLinkScreen2 = "deepLink2Screen"
    const val DeepLinkScreen3 = "deepLinkS3creen"
    const val PetDetailsScreen = "petDetailsScreen"
    const val PetsListScreen = "petsListScreen"
    const val TestScreen = "testScreen"
    const val UnknownDeepLinkScreen = "unknownDeepLinkScreen"

    // Child composables on screens
    const val CatSelection = "catSelection"
    const val DeepLink = "deepLink"
    const val PetDetails = "petDetails"
    const val PetsList = "petsList"
    const val Test = "test"
    const val UnknownDeepLink = "unknownDeepLink"
}
```

In this file we have not only provided constants for the screens but for the children composables hosted on screens. The demo app actually uses two variations of the pet details. One is for portrait mode and the other is for landscape mode. Collectively though, they are still considered **Pet details** and thus given the id **PetDetais**

It should be noted that the naming convention used to identify screen composables is to append the word "Screen" at the end while the name for the child composable is without the word "Screen". This makes it easier to read and manage your composables.

<br />

#### 6. Add Composable Resources to the CRM

Now that you have defined your composable resources, they need to be added to the CRM. This is only done once, during your app's startup. The recommended place to do this is in the class that inherits from Application <u>**after**</u> the call to **initializeJetmagic**:

```kotlin
crm.apply {
    addComposableResources(
        mutableListOf(
            ComposableResource(
                resourceId = ComposableResourceIDs.PetsListScreen,
            ) { composableInstance ->
                // PetsList default screen.
                PetsListScreenHandler(composableInstance)
            },
            ComposableResource(
                resourceId = ComposableResourceIDs.PetsListScreen,
                screenOrientation = ScreenOrientation.Landscape,
                screenSize = ScreenSize.XLarge,
            ) { composableInstance ->
                // PetDetails screen in landscape mode on a tablet.
                PetsListWithDetailsScreenHandler(composableInstance)
            },
            ComposableResource(
                resourceId = ComposableResourceIDs.PetDetailsScreen,
            ) { composableInstance ->
                // PetDetails default screen.
                PetDetailsScreenHandler(composableInstance)
            },

            // ************** List all children resources. **************

            ComposableResource(
                resourceId = ComposableResourceIDs.PetsList,
                viewmodelClass = PetsListViewModel::class.java
            ) { composableInstance ->
                // PetsList default
                PetsListHandler(composableInstance)
            },
            ComposableResource(
                resourceId = ComposableResourceIDs.PetDetails,
                viewmodelClass = PetDetailsViewModel::class.java
            ) { composableInstance ->
                // PetDetails default.
                PetDetailsHandler(composableInstance)
            },
            ComposableResource(
                resourceId = ComposableResourceIDs.PetDetails,
                viewmodelClass = PetDetailsViewModel::class.java,
                screenOrientation = ScreenOrientation.Landscape
            ) { composableInstance ->
                // PetDetails in landscape mode.
                dev.wirespec.sample.ui.screens.petdetails.land.PetDetailsHandler(composableInstance)
            },
            ComposableResource(
                resourceId = ComposableResourceIDs.PetDetails,
                viewmodelClass = PetDetailsViewModel::class.java,
                languageAndRegion = "de",
            ) { composableInstance ->
                // PetDetails in German.
                dev.wirespec.sample.ui.screens.petdetails.de.PetDetailsHandler(composableInstance)
            }
        )
    )
}
```

Composable resources are added by calling **addComposableResources**. Each resource is added by creating an instance of **ComposableResource**. The order in which you add your resources is not important. However, to make it easier to visualize, it is recommended that you add your screen resources first followed by the children resources. Adding a comment between the two to separate them makes it even easier to visualize.

The ComposableResource constructor takes many parameters. At the very minimum, the resourceId and onRender lambda callback must be provided. The constructor has parameters for configuration qualifiiers. Set the qualifiers for a resource when you want the resource to be selected for those specific qualifiers. Whenever you add a resource with a specific id, you must make sure to add at least one resource that has no qualifiers defined. This is Jetmagic's way of ensuring that you provide a default resource. In the demo app, and shown above, two resources have their **resourceId** parameter set to **ComposableResourceIDs.PetDetails**. Because one of these uses the orientation qualifier, the other resource must be added without any qualifiers. To be clear, if you add a composable resource that you only intend on rendering when a certain qualifier is met, you must also provide a resource with the same resourceId that has no qualifiers to act as default.

During runtime, when the CRM determines which composable resource to render, it will call the resource's onRender lambda callback to provide the actual composable that will get rendered. In this callback, simply call the screen composable, or the child composable that you want rendered.

The viewmodel parameter is optional. If you provide a reference to a viewmodel class (not an instance), then the CRM will create the viewmodel and associate it with the composable that gets rendered. Alternatively, if you need control over how the viewmodel is created or if your viewmodel's has a constructor with parameters , you can use the **onCreateViewmodel** property which is a lambda callback. When this callback is called, you return an instance of the viewmodel you want.

<br />

#### 7. Navigate to the Startup Screen

When your app starts, you navigate to the startup screen by calling **navman.goto**. You can do this in your activity's onCreate. Here is how it's done in the demo app:

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navman.activity = this

        if (navman.totalScreensDisplayed == 0) {
            navman.goto(composableResId = ComposableResourceIDs.PetsListScreen)
        }

        setContent {
            SetAppTheme(ColorThemes.DefaultLight) {
                MainHandler()
            }
        }
    }
}
```

In this demo, the call to navman.goto is only done when **navman.totalScreensDisplayed** is equal to zero. If the user terminates the activity (or the operating system does) and the user restarts the activity, the state of the navigation stack is still as it was when the user last used the app. It could be that they were on the pet details screen. Allowing them to return back to the last screen they were on is a nice feature and is done in the demo app to simply demonstrate how the app restores the state of the app from where the user left off. If the last screen was the pet details and the user taps on the Back button, they will return to the previous screen, which is the list screen.

If you always want the user to start from the same screen each time the activity restarts, just leave out the line of code that tests for the condition of **totalScreensDisplayed**.

If you don't call **navman.goto** when your app starts and there are no screens currently on the navigation stack, no screens will be displayed.

<br />

## Further Reading

This setup is the bare minimum needed to create composable resources and display a screen. There is additional documentation available covering more advanced topics.

#### CRM Topics

The CRM documentation covers:

* How to access a viewmodel associated with a composable instance
* How to access a parent viewmodel
* How to access a viewmodel from anywhere
* How to pass data from one screen to another
* How to return data from one screen to a previous screen
* How to add a custom animation for any screen

For details on these topics, see:
[**Composable Resource Manager (CRM)**](../../tree/main/jetmagic/readme-crm.md)![](https://en.wikipedia.org/w/resources/src/mediawiki.skinning/images/external-ltr.svg?59558)
<br /><br />

#### Navigation Topics

The Navigation Manager documentation covers:

* How to navigate forwards
* How to navigate back
* How to navigate to the home screen
* How to get notified of navigation events
* How to prevent navigating back
* How to cache a screen

For details on these topics, see:
[**Navigation Manager**](../../tree/main/jetmagic/readme-navigation.md)![](https://en.wikipedia.org/w/resources/src/mediawiki.skinning/images/external-ltr.svg?59558)
<br /><br />

#### Deep Link Topics

The Deep Link documentation covers:

* Setting up deep links
* Using regluar expressions to match URIs

For details on these topics, see:
[**Deep Linking**](../../tree/main/jetmagic/readme-deep-link.md)![](https://en.wikipedia.org/w/resources/src/mediawiki.skinning/images/external-ltr.svg?59558)

<br /><br />

# License

This software is open source and licensed under the GNU GENERAL PUBLIC LICENSE (Version 3). See:

http://www.gnu.org/licenses/gpl-3.0.html