# Jetmagic - Deep Linking

Deep linking is the process of transforming a uri to one or more screens. A deep link is triggered through an activity's intent filter. Jetmagic's deep linking can be used to launch a single screen or several screens in succession. An activity need not be running for a deep link to launch the activity and navigate to the target screen. Deep linking is an integral part of the Composable Resource Manager and the Navigation Manager. This means that the screens that get rendered will be selected based on the device's current configuration.

In a use case scenario where you want several screens to launch in succession, each screen can decide whether to proceed to the next screen or terminate the navigation to any further screens. This is useful in apps where it might not make sense to navigate to another screen until some precondition is first achieved. For example, in a banking app, if the user launches a deep link to view their account status, they would normally need to login. Therefore, you might first launch the login screen and check whether their session hasn't timed out. If their session has timed out, you can terminate the deep link navigation to the next screen and display the login screen where they must first sign in to proceed to the next screen. If the session hasn't timed out, the login screen might pack some data into the deep link navigation that the accounts status screen might need.

#### Table of Contents

* How to:
  - [How to set up deep links](#setting_up_deep_links)
  - [How to use regluar expressions to match URIs](#regular_expressions)
* APIs
  - [DeepLink](#deep_link)
  - [DeepLinkMap](#deep_link_map)

<a name="setting_up_deep_links" />

<br />

### Setting up deep links

Deep links require that you setup an intent filter in your AndroidManifest.xml. The intent filter is what Android uses to intercept URIs and launch an activity that can handle the URI. The demo app uses the following intent filter:

```xml
<manifest>
    <application>
        <activity
            android:name=".ui.screens.MainActivity"
            android:launchMode="singleInstance">
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data
                    android:host="www.wirespec.dev"
                    android:pathPattern="/jetmagic/.*"
                    android:scheme="http" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

Any URIs launched with http://www.wirespec.dev/jetmagic/... will trigger a deep link. For details on how to use the ```<data>``` element, see:
[https://developer.android.com/guide/topics/manifest/data-element](https://developer.android.com/guide/topics/manifest/data-element)

To CRM needs to be setup to recognize the deep link. This is typically done in the same location where you setup composable resources. In the demo app it looks like this:

```kotlin
navman.addDeepLinks(
    map = mutableListOf(
        DeepLinkMap(paths = mutableListOf(DeepLinkPaths.root)) { uri, queryKeyValues ->
            mutableListOf(ComposableResourceIDs.PetsListScreen)
        },
        DeepLinkMap(paths = mutableListOf(DeepLinkPaths.petInfo)) { uri, queryKeyValues ->
            mutableListOf(ComposableResourceIDs.PetDetailsScreen)
        },
        DeepLinkMap(paths = mutableListOf(DeepLinkPaths.deepLink), displayLastScreenOnly = false) { uri, queryKeyValues ->
            mutableListOf(ComposableResourceIDs.DeepLinkScreen1, ComposableResourceIDs.DeepLinkScreen2, ComposableResourceIDs.DeepLinkScreen3)
        },
    )
) { uri, queryKeyValues ->
	mutableListOf(ComposableResourceIDs.UnknownDeepLinkScreen)
}
```

**adDeepLinks** takes a list of one or more **DeepLinkMap** objects. Each DeepLinkMap can provide one or more URIs that it will handle. The **onRequestForComposableResourceIds** parameter is a lambda callback that will be called if one or more of the mapped URIs matches the URI that triggered the deep link. When **onRequestForComposableResourceIds** is called, the URI that triggered the deep link will be provided. If the URI had a query string, the key/values will also be provided. **onRequestForComposableResourceIds** must return one or more composable resource ids. Note: This is done by a callback instead of providing it as a list because the callback will be called where you can inspect the URI and query parameters and dynamically return the appropriate composable resource ids accordingly. For each id provided, a screen for that resource will be created. The screens are created in the order in which the ids are specified.

There may be cases where you would like to launch several screens in succession but you only want the last screen to be displayed. It could be a situation where you cannot go directly to the last screen without first proceeding through some higher priority screens. For example, in the banking app mentioned earlier, you may need to first verify that the user is signed in. if the user is signed in, you could proceed directly to their accounts screen. Without using multiple screens, your target screen would end up having to duplicate a lot of the code needed to check verification and possibly make other API calls before it can even display the accounts screen. But why duplicate this code if it can already be executed on the previous screens.

The only thing that you maybe don't want is that once the user ends up on the target screen is for the previous screens to be on the navigation stack. It might make more sense to just have them exit the app when they tap on the Back button. Whatever the reason may be, if there are situations where you don't want the previous screens to be displayed, you can set the **displayLastScreenOnly** parameter of DeepLinkMap to true. When set to true, all the screens will be rendered but each screen needs to inspect the **removeScreenFromNavigationStack** property of the DeepLink object which is a property of a composable instance. If **removeScreenFromNavigationStack** is set to true, the composable instance should not render its UI. However, it is up to the composable instance to make that decision. If, as mentioned earlier, the banking app's verification screen noticed that the user's session has expired, that screen should terminate navigating to any further deep links and cause its UI to be rendered so that the user can sign in.

In the event that no map can be found to handle the URI, the **onDeepLinkMatchNotFound** parameter - which is a lambda callback - will be called if one is provided. This lambda is the last parameter of  **addDeepLinks**. This callback can provide one or more composable resource ids. For each id, a screen will be created.

The activity needs to be setup to intercept the intent. In the demo app it looks like this:

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navman.activity = this

        if (intent.dataString != null) {
            navman.gotoDeepLink(url = intent.dataString!!)
        } else {
            if (navman.totalScreensDisplayed == 0) {
                navman.goto(composableResId = ComposableResourceIDs.PetsListScreen)
            }
        }

        // ...

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent?.dataString != null) {
            navman.gotoDeepLink(url = intent.dataString!!)
        }
    }
}
```

If the activity has not been started and a deep link is triggered, then the call to **navman.gotoDeepLink** is called if **intent.dataString** is not null - otherwise, the home screen is launched if no other screens are currently on the navigation stack.

If the activity is a single instance, you also need to implement **onNewIntent**. This will get called if the activity is already running an deep link is triggered.

When one or more screens are launched when a deep link is triggered, the composable instances that make up those screens need to properly handle the deep link in order for the target screen to be displayed or for the navigation to be terminated. In the demo app, the deep link handler does it like this:

```kotlin
@Composable
fun DeepLinkHandler(composableInstance: ComposableInstance) {

    val parentComposableInstance = LocalComposableInstance.current

    // ...

    val deepLink = navman.getDeepLinkForComposableInstance(composableInstance = composableInstance)

    if (deepLink != null) {
        navman.gotoNextDeepLink(composableInstance = composableInstance, p = nextScreenText)
    }

    if (parentComposableInstance.deepLink?.removeScreenFromNavigationStack == true) {
        return
    }

    // ...
}
```

Internally, the Navigation Manager keeps a stack of deep links with the top of the stack referring to the current deep link that needs to be prcoessed. If for example, there are three screens that will be launched in succession - A, B, C - then screen A will be the first deep link to be processed on the stack. If screen A has no objection, it will then tell the Navigation Manager to move on to the next deep link in the stack which is Screen B. If screen B has no objection, it moves on to screen C. If either screen A or B object to moving on to the next screen, the navigation terminates. 

A call to **getDeepLinkForComposableInstance** needs to be made with a composable instance as it's parameter. This can be either the root composable instance or one of its children. If getDeepLinkForComposableInstance returns a deep link object, it means that the screen that the composable instance is on is due to a deep link having been triggered. When the composable instance is ready to move on to the next screen, it must call **navman.gotoNextDeepLink** which will result in the next screen being created.

If the screen objects to moving on to the next screen, it needs to terminate the deep linking by calling **navman.clearDeepLinks**.

As mentioned previously, if three screens are launched as a result of a deep link being triggered and all of them except the last one should be hidden, the composable instances should honor this by preventing themselves from being rendered. In the **DeepLinkHandler** shown above, if **removeScreenFromNavigationStack** is set to true, then a return is made, preventing the composable from being rendered. 

Note: **removeScreenFromNavigationStack** in the code above is referenced from the composable instance and not from **getDeepLinkForComposableInstance**. In reality, the deepLink returned by **getDeepLinkForComposableInstance** is the same object instance as the one returned by the deepLink property of the composable instance.

Why must you interrogate the one from the composable instance instead of the one returned by **getDeepLinkForComposableInstance**? There are two things that happen that are not obvious. When a call to **gotoNextDeepLink** is called, the deepLink returned by **getDeepLinkForComposableInstance** gets removed from the deep link stack and is no longer available. It will however still be available through the composable instance's deepLink property. The composable instance can and most likely will get recomposed again at some time. When it does get recomposed, a call to getDeepLinkForComposableInstance will return null, meaning that there is no deep link. It was already processed previously and there is no need to launch the next screen again. But the composable instance still needs to know whether to show itself or be hidden and it can only do this from the **removeScreenFromNavigationStack** property.

Keeping a copy of the deep link in the composable instance's deepLink property isn't just for knowing the state of **removeScreenFromNavigationStack**. The deep link includes information about the URI that launched the deep link as well as the query string key/values and the composable instance may need access to these in order to properly render the composable.

<a name="regular_expressions" />

<br />

### Using regluar expressions to match URIs

Jetmagic supports the use of regular expressions to define the URIs that you want to handle. To use a regular expression, the entire path must be enclosed inside [%  %]  For example, the demo app includes this regular expression:

```kotlin
navman.addDeepLinks(
    map = mutableListOf(
        DeepLinkMap(paths = mutableListOf("[%/jetmagic/sample/category/[d-m]/details%]")) { uri, queryKeyValues ->
            mutableListOf(ComposableResourceIDs.DeepLinkScreen1, ComposableResourceIDs.DeepLinkScreen2, ComposableResourceIDs.DeepLinkScreen3)
        },
    )
) { uri, queryKeyValues ->
	mutableListOf(ComposableResourceIDs.UnknownDeepLinkScreen)
}
```

In a terminal if you execute this:

```
adb shell am start -W -a android.intent.action.VIEW -d "http://www.wirespec.dev/jetmagic/sample/category/d/details" dev.wirespec.jetmagic
```

the three deep link test screens will be displayed. This regular expression will match any sub folder after the "category" folder that is a letter from "d" to "m" and followed by the folder "details".

<a name="deep_link" />

<br />

## DeepLink (Data Class)

Provides information about a deep link to composable instances when the app is activated by a deep link.

| Function / Property             | Description                                                                                                                                                                                                                                                                                                                                            |
| ------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| composableInstanceId            | ```val composableInstanceId: String```<br /><br />The id of the composable instance that the deep link is associated with.                                                                                                                                                                                                                             |
| composableResourceId            | ```val composableResourceId: String```<br /><br />The id of the composable resource that was used to render the composable instance.                                                                                                                                                                                                                   |
| navigateToComposableResourceId  | ```var navigateToComposableResourceId: String? = null```<br /><br />If set, this indicates the composable resource that will be used to navigate to the next screen in the deep link. The composable resource ids are defined when NavigationManager.addDeepLinks is called to setup the deep links.                                                   |
| path                            | ```val path: String```<br /><br />The path of the url that was used to launch the deep link. This excludes the protocol and domain. For example, if the url that was used to launch the deep link was https://dev.wirespec.dev/jetmagic/sample/get_pet?id=123, the path will be /jetmagic/sample/get_pet without the protocol, domain or query string. |
| queryKeyValues                  | ```val queryKeyValues: Map<String, String>```<br /><br />A collection of key/values where the key is the name of the query string parameter and the value is the value the parameter is set to.                                                                                                                                                        |
| removeScreenFromNavigationStack | ```var removeScreenFromNavigationStack: Boolean = false```<br /><br />                                                                                                                                                                                                                                                                                 |
| url                             | ```val url: String```<br /><br />The url (or uri) that was used to launch the deep link.                                                                                                                                                                                                                                                               |

<a name="deep_link_map" /><br />

## DeepLinkMap (Data Class)

Used to define a deep link.

| Function / Property               | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| --------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| displayLastScreenOnly             | ```val displayLastScreenOnly: Boolean = false```<br /><br />If set to true and multiple screens are defined by the onRequestForComposableResourceIds list, only the last screen in the list will be shown. The other screens will be rendered but it is the responsibility of each screen to decide whether to display its UI or not. When displayLastScreenOnly is set to true, **DeepLink.removeScreenFromNavigationStack** will be set to true in the composable instance for all the screens except the last screen.<br/><br/>Setting **displayLastScreenOnly** to true is useful when you need the target screen that is displayed is dependent on other screens and those other screens should be launched prior to the target screen being displayed. It is also useful if the target screen doesn't want to have to obtain its data when it could receive the data from a previous screen. An example is the sample app where the pet details screen receives the pet details from the pets list screen. Without receiving this data, the pet details screen would have to parse the deep link url and retrieve the data from the repository based on the query parameters. |
| onRequestForComposableResourceIds | ```val onRequestForComposableResourceIds: (uri: URI, queryKeyValues: Map<String, String>) -> List<String>?```<br /><br />                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| paths                             | ```val paths: List<String>```<br /><br />A collection of paths that the deep link will respond to. A path can also be a regular expression. These are paths without the protocol or domain. Also, don't include any query string parameters at the end of the path.<br/><br/>To indicate that a path is a regular expression, it must be enclosed in [% %]. For example, this regular expression:<br/><br/>```[%/jetmagic/sample/[4-6]/animals%]```<br /><br />will match<br /><br />```/jetmagic/sample/4/animals```<br />```/jetmagic/sample/5/animals```<br />```/jetmagic/sample/6/animals```<br /><br />but will not match any sub path that is not 4, 5 or 6.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |