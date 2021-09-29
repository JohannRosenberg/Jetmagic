# Jetmagic - Composable Resource Manager (CRM)

The [Navigation Manager](../../../tree/main/jetmagic/readme-navigation.md) and Composable Resource Manager are designed to work together. It isn't possible to use either of these on their own. Jetmagic's CRM is responsible for creating composables that are dependent on the current device configuration. The CRM uses "Composable Resources" to create "Composable Instances" which are rendered to the UI. During navigation, the Navigation Manager manages a stack of composable instances that essentially represent the different screens in the app that users navigate to and from.

**Table of Contents**

* How to:
  - [How to access a viewmodel associated with a composable instance](#access_viewmodel)
  - [How to access a parent viewmodel](#access_parent_viewmodel)
  - [How to access a viewmodel from anywhere](#access_viewmodel_from_anywhere)
  - [How to pass data from one screen to another](#passing_data)
  - [How to return data from one screen to a previous screen](#returning_data)
  - [How to add a custom animation for any screen](#custom_animation)
* APIs
  - [ComposableReourceManager](#composable_resource_manager)
  - [ComposableResource](#composable_resource)
  - [ComposableInstance](#composable_instance)
  - [ComposableParams](#composable_params)
  - [ScreenFactoryHandler](#screen_factory_handler)
  - [LocalComposableInstance](#local_composable_instance)

<a name="access_viewmodel" />

<br />

### How to access a viewmodel associated with a composable instance

If you allow Jetmagic to create and manage the viewmodel for your composables, you can easily access them from any composable on the screen without the need to pass the viewmodel as parameters. Passing viewmodels through parameters is a bad practice for various reasons including the lost ability of reusing the composable in a different app. Jetmagic follows the pattern of "state hoisting" to separate the UI component from any state management that provides data to the UI component. It also follows the pattern of *unidirectional data flow*.

In Jetmagic, the composable that provides state hoisting is normally referred to as the "Handler". The root (a.k.a screen) composable as well its children will have their own handlers.

Normally a screen in Jetmagic will consist of a root composable (referred to as the "screen handler") and one or more children composables. Both the root and children composables can have their own viewmodels. Generally speaking, stuff that is common to all the composables on a screen should be placed in a viewmodel associated with the root composable while viewmodels for children are normally meant for the child composable it is associated with and any of its children. However, it is still possible to access any viewmodel on the screen regardless at what level a composable is located at in the hierarchy.

When the CRM renders a composable, it provides it with a ComposableInstance parameter in the composable function. The viewmodel can be accessed as a property of the composable instance. In the demo app, the PetsListHandler looks something like this:

```kotlin
@Composable
fun PetsListHandler(composableInstance: ComposableInstance) {

    val vm = composableInstance.viewmodel as PetsListViewModel
    val petsList = vm.onPetsAvailable.observeAsState().value

    val p = composableInstance.parameters as ComposableParams?
    val modifier = p?.modifier ?: Modifier

    if (!composableInstance.isTerminated && !petsList.isNullOrEmpty()) {
        vm.updatePetDetailsIfPresent(composableInstance = composableInstance, petInfo = petsList[0])
    }

    PetsList(
        modifier = modifier,
        petsList = petsList,
        scrollState = vm.scrollState,
        onItemClick = { petInfo ->
            vm.updateOrGotoPetDetails(composableInstance = composableInstance, petInfo = petInfo)
        }
    )
}
```

<a name="access_parent_viewmodel" />

<br />

### How to access a parent viewmodel

There are two ways to access the parent viewmodel from a child composable. You can either use CompositionLocalProvider or call the CRM API **getParentComposableInstance**. To use **CompositionLocalProvider**, you must first set it in the screen handler. In the child composable, you call **LocalComposableInstance.current**. Note, if your child also uses **CompositionLocalProvider**, you need to access the parent composable instance before calling **CompositionLocalProvider** in your child composable:

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

@Composable
fun PetsListHandler(composableInstance: ComposableInstance) {

    val parentComposableInstance = LocalComposableInstance.current

    CompositionLocalProvider(LocalComposableInstance provides composableInstance) {

        val vmParent = parentComposableInstance.viewmodel as PetsListScreenViewModel

        val vm = composableInstance.viewmodel as PetsListViewModel
        val petsList = vm.onPetsAvailable.observeAsState().value

        val p = composableInstance.parameters as ComposableParams?
        val modifier = p?.modifier ?: Modifier

        if (!composableInstance.isTerminated && !petsList.isNullOrEmpty()) {
            vm.updatePetDetailsIfPresent(composableInstance = composableInstance, petInfo = petsList[0])
        }

        PetsList(
            modifier = modifier,
            petsList = petsList,
            scrollState = vm.scrollState,
            onItemClick = { petInfo ->
                vm.updateOrGotoPetDetails(composableInstance = composableInstance, petInfo = petInfo)
            }
        )
    }
}
```

If you don't want to use CompositionLocalProvider, you can use **getParentComposableInstance** from a child as follows:

```kotlin
@Composable
fun PetsListScreenHandler(composableInstance: ComposableInstance) {

    crm.RenderComposable(
        parentComposableId = composableInstance.id,
        composableResId = ComposableResourceIDs.PetsList,
        childComposableId = "petsList")
}

@Composable
fun PetsListHandler(composableInstance: ComposableInstance) {

    val parentComposableInstance = crm.getParentComposableInstance(composableInstance)
    val vmParent = parentComposableInstance.viewmodel as PetsListScreenViewModel

    // ...
}
```

<a name="access_viewmodel_from_anywhere" />

<br />

### How to access a viewmodel from anywhere

If you want to access a viewmodel of some composable instance from anywhere in your app, you can call **getComposableInstanceById** from the CRM by providing the id of the composable instance that the viewmodel is associated with. In order to know the id, you have to first make a call to the **goto** function in the Navigation Manager and provide the id. By default, when goto is called without the **composableInstanceId** parameter being set, a unique id will be created and assigned to the composable instance:

```kotlin
object ComposableInstanceIDs {
    const val PetOfTheDay = "petOfTheDay"
}

navman.goto(composableInstanceId = ComposableInstanceIDs.PetOfTheDay, composableResId = ComposableResourceIDs.PetDetailsScreen)

// In some composable, get the viewmodel for the composable instance with the id PetOfTheDay
@Composable
fun PetsListHandler(composableInstance: ComposableInstance) {

    val parentComposableInstance = crm.getComposableInstanceById(id = ComposableInstanceIDs.PetOfTheDay)
    val vmParent = parentComposableInstance.viewmodel as PetsListScreenViewModel

    // ...
}
```

Using this approach does mean that only one composable should exist with the id you are provided. You should only use this approach if you can ensure that only one screen or composable instance exists that has that id. If multiple composable instances exist using the same id, then calling **getComposableInstanceById** will return the first one it finds, which may not be what you were expecting.

The id you provide is not limited to a root composable instance. You can use it for children instances as well. However, when using it with a child instance, instead of setting the id with **navman.goto**, you provide the id when calling **RenderComposable** and setting the **childComposableId** parameter:

```kotlin
@Composable
fun PetsListScreenHandler(composableInstance: ComposableInstance) {

    crm.RenderComposable(
        parentComposableId = composableInstance.id,
        composableResId = ComposableResourceIDs.PetsList,
        childComposableId = ComposableInstanceIDs.PetOfTheDay)
}
```

When **getComposableInstanceById** is called and it returns a composable instance, the instance could refer to one that is currently on the navigation stack or one that is in the navigation cache. A cached screen may not necessarily be on the navigation stack (meaning that it is still alive but not visible).

<br />

<a name="passing_data" />

### How to pass data from one screen to another

There are a number of ways to pass data from one screen to another.

#### Using navman.goto

In the demo app, on the test screen there is a button to open up another test screen. It passes the text "Test Screen":

```kotlin
navman.goto(composableResId = ComposableResourceIDs.TestScreen, p = "Test Screen")
```

<br />

#### Using crm.RenderComposable

Screen handlers that need to render children composable instances call **crm.RenderComposable** and can pass parameter data to their children. In the demo app, in PetsListWithDetailsScreenHandler, the details screen is passed an instance of PetDetailsParams:

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

If you want to pass the parameter data from a parent to a child but don't want to use CompositionLocalProvider in the child to access the parent, you can just pass the parent's parameters to the child by setting the child's **p** parameter:

```kotlin
@Composable
fun TestScreenHandler(composableInstance: ComposableInstance) {
        crm.RenderComposable(
            parentComposableId = composableInstance.id,
            composableResId = ComposableResourceIDs.Test,
            childComposableId = "test",
            p = composableInstance.parameters
        )
}
```

<br />

#### Receiving data from another screen

To access the data sent from another screen (or from any composable instance), you can use the **parameter** property on a composable instance:

```kotlin
@Composable
fun TestHandler(composableInstance: ComposableInstance) {

    var screenText = composableInstance.parameters as String?

    // ...
}
```

One of the most common things you'll probably end up doing is passing some standard types of parameters from a parent to a child such as the **Modifier** parameter that composables typically pass down their hierarchy in order to propagate modifier settings that the UI needs at various levels. For this reason, you can use the **ComposableParams** class. It isn't anything special but provides the basic parameters you might find yourself using regularly. It looks like this:

```kotlin
open class ComposableParams(
    var modifier: Modifier = Modifier,
    var data: Any? = null,
    var onReturn: ((data: Any?, canceled: Boolean) -> Unit)? = null
)
```

The modifier parameter lets you set your own custom modifier. The data parameter can be set to any type of data. The onReturn is a callback that a composable instance on one screen can use to return a value to the previous screen. If canceled is set to true, it means that the user canceled whatever action the screen was performing and the previous screen can take action based on whether the screen was canceled or not. In the demo app, the **CatSelectionHandler** retrieves its parameters from the previous screen as follows:

```kotlin
fun CatSelectionHandler(composableInstance: ComposableInstance) {
    val p = composableInstance.parameters as ComposableParams?
}
```

<br />

#### Getting data updates from another screen or composable instance

A composable instance can be notified about data updates. A composable instance has an **onUpdate** property which is a LiveData. When the demo app is run on a tablet in landscape mode, the pets list appears on the left while the selected pet details appear on the right. When you tap on a cat, the pet details gets updated with the selected cat. This is done by observing for updates using the onUpdate property:

```kotlin
@Composable
fun PetDetailsHandler(composableInstance: ComposableInstance) {

    // Get notified of updates.
    composableInstance.onUpdate?.observeAsState()?.value

    // ...
}
```

onUpdate is triggered by the CRM using a random number. The value returned by onUpdate is the random number which itself is not of any value. But the triggering of onUpdate inside the composable causes the composable to recompose. It is the responsibility of the client that is performing the update to provide any updated data. This is generally done by accessing the target composable instance that needs to be updated and modifying its **parameters** property. The client can choose to overwrite the existing parameters or first read the parameters and if it is an object, it can update just those properties of the object that need updating. In the demo app, the pets list updates only the **petsListItemInfo** property in the parameters. A simplified version looks like this:

```kotlin
fun updatePetDetails(composableInstance: ComposableInstance, petInfo: PetListItemInfo) {
    // Get the composable on the screen for the pet details.
    val petDetailsComposableInstance = crm.getChildComposableInstance(
        parentComposableInstance = composableInstance,
        childComposableResourceId = ComposableResourceIDs.PetDetails,
    )

    // Update the petsListItemInfo property with the selected pet.
    val p = petDetailsComposableInstance.parameters as PetDetailsParams
    p.petsListItemInfo = petInfo

    // Notify the composable of the update.
    crm.notifyChildComposableInstanceOfUpdate(
        parentComposableInstance = composableInstance,
        childComposableResourceId = ComposableResourceIDs.PetDetails
    )
}
```

If you choose to update a composable's data by some other means, you can still trigger the target composable instance to recompose by calling **notifyComposableInstanceOfUpdate**:

```kotlin
// Pass in the composable instance that you want to recompose.
crm.notifyComposableInstanceOfUpdate(targetComposableInstance)
```

<a name="returning_data" />

<br />

### How to return data from one screen to a previous screen

Your app may have a screen where a user needs to navigate to, make some selection and then return the selected item back to the previous screen when the screen closes. The easiest way to handle this in Jetmagic is to provide a callback that is part of the parameters you include when you navigate to the screen. When the user makes a selection or even cancels the screen, the callback can be called to provide the selected data or provide whatever information the previous screen requires. In the demo app, the test screen includes a button labeled **Return value from another screen**. When tapped, the user can select a type of cat and then return the selection when they tap on the **Return selection** button:

```kotlin
@Composable
fun Test(
    screenId: Int,
    screenText: String,
    modifier: Modifier = Modifier,
    onBackButtonClick: () -> Unit
) {
// The client navigates to the target screen and includes a callback...
Button(
    modifier = modifier.padding(bottom = 10.dp),
    onClick = {
        val p = ComposableParams() { result, canceled ->
            // Display the selected result...
        }

        navman.goto(composableResId = ComposableResourceIDs.CatSelectionScreen, p = p)

    }) {
        Text(text = "Return value from another screen")
    }
}


// The target screen returns data to the previous screen...
@Composable
fun CatSelectionHandler(composableInstance: ComposableInstance) {

    val p = composableInstance.parameters as ComposableParams?

    CatSelection(
        onSelection = { selectedBreed ->
            p?.onReturn?.invoke(selectedBreed, false)
            navman.goBack()
        })
}
```

How you define your callback is your own choice. The one provided by **ComposableParams** is typical. Processing a canceled screen is optional. It might be useful in a use case where the target screen has a Cancel button that when pressed informs the previous screen that the operation has been canceled and any necessary action can be taken.

<a name="custom_animation" />

<br />

### How to add a custom animation for any screen

Each screen can have a customized animation when becomes visible or is hidden (i.e. removed from the navigation stack). You customize the animation when you add the screen's composable resource with **addComposableResources** and set the **onAnimateVisibility** parameter. In the demo app, the test screen's animation is set like this:

```kotlin
crm.apply {
    addComposableResources(
        mutableListOf(
            ComposableResource(
                resourceId = ComposableResourceIDs.TestScreen,
                onAnimateVisibility = { composableInstance ->
                    AnimatedVisibility(
                        visibleState = composableInstance.animationTransitionState!!,
                        enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(800)),
                        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(800)),
                    ) {
                        RenderComposable(composableInstance = composableInstance)
                    }
                }
            ) { composableInstance ->
                // Test default screen.
                TestScreenHandler(composableInstance)
            }
        )
    )
}
```

The **visibleState** parameter of AnimatedVisibility needs to be set to **composableInstance.animationTransitionState** in order for the animation state to be set correctly. Do not try and maintain the visibility state yourself with **MutableTransitionState** as the state will end up getting destroyed and the animation effects will not work properly. The composable instance keeps track of the transition state and applies it when the composable is rendered.

<a name="composable_resource_manager" />

<br />

## ComposableResourceManager (Class)

The CRM is responsible for determining which composable instance to create that gets rendered to the screen. It does this by interrogating the device's current configuration and selecting a composable resource that is used to create the composable instance. The CRM also reacts to device configuration changes such as changing the orientation from portrait mode to landscape mode. The CRM works closely with the Navigation Manager to coordinate state management of cached composable instances.

During your app's startup, one of the very first things that needs to be done is to call the addComposableResources function. This will provide the CRM the setup for screens and the resources needed to create those screens. A typical place to do this is in a class that inherits from Application - in the onCreate function. After addComposableResources has been called, you can then call navman.goto to navigate to the first screen (the home screen).

| Function / Property                        | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| ------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| addComposableResources                     | ```fun addComposableResources(composables: List<ComposableResource>)```<br /><br />Adds a list of composable resources that the Composable Resource Manager will manage.<br/><br/>**composables:** A list of one or more composable resources. This function should only be called once in your app and the list provided will be the only list used.<br/><br/>For every resource added that has configuration qualifiers defined, a default resource must be provided that contains no qualifiers. For example, if you provide a composable resource with its composableId set to "PetsList" and set its screenOrientation property to ScreenOrientation.Landscape, you must also provide a composable resource with its composableId also set to "PetsList" with none of its qualifiers set.<br/><br/>**exception:** An exception is thrown if a default composable resource is not provided for any resource that specifies qualifiers.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| getChildComposableInstance                 | ```fun getChildComposableInstance(parentComposableInstance: ComposableInstance, childComposableId: String? = null, childComposableResourceId: String): ComposableInstance?```<br /><br />Finds the child composable instance for the specified parent composable instance.<br/><br/>**parentComposableInstance:** The composable instance of the parent.<br/><br/>**childComposableId:** The id of the child composable instance to find. If set to null, the childComposableResourceId parameter will be used to see if a child exists for the parent that has the resource id and that child will be returned.<br/><br/>**childComposableResourceId:** If no child composable instance can be found when a childComposableId is specified, a child is searched for using the resource id of the child.<br/><br/>**returns:** The child composable instance is returned if one exists, otherwise null is returned.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| getComposableInstanceById                  | ```fun getComposableInstanceById(id: String): ComposableInstance?```<br /><br />Returns the composable instance that has the specified id.<br /><br />**id:** The id of the composable instance.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| getComposableResourceForComposableInstance | ```fun getComposableResourceForComposableInstance(composableInstance: ComposableInstance): ComposableResource```<br /><br />Returns the composable resource that will be used for the specified composable.<br/><br/>**composableInstance:** The composable instance to use.<br/><br/>**returns:** Returns the composable resource that would be used if the composable instance were rendered on the<br/>screen for the current device configuration.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| getParentComposableInstance                | ```fun getParentComposableInstance(composableInstance: ComposableInstance): ComposableInstance```<br /><br />Returns the parent composable instance.<br/><br/>**composableInstance:** This should be a reference to a child composable instance. But if it isactually the parent instance itself, this value of this parameter will be returned.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| notifyChildComposableInstanceOfUpdate      | ```fun notifyChildComposableInstanceOfUpdate(parentComposableInstance: ComposableInstance, childComposableId: String? = null, childComposableResourceId: String) :ComposableInstance?```<br /><br />Notifies a child composable instance of any updates.<br/><br/>**parentComposableInstance:** The parent composable instance that the child composable instance belongs to.<br/><br/>**childComposableId:** The id of the child composable instance. If this is provided, a search is first made to see if a child composable instance exists. If the id is not specified or the child with the id cannot be found, the childComposableResourceId will be used to search for the child.<br/><br/>**childComposableResourceId:** The composable resource id of the child composable instance. If multiple children exist on the screen with the same composable resource id and none can be found with the childComposableId, then the first child with the composable resource id will be notified of the update.<br/><br/>**returns:** The child composable instance is returned if it exists, otherwise null is returned.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| notifyComposableInstanceOfUpdate           | ```fun notifyComposableInstanceOfUpdate(composableInstance: ComposableInstance)```<br /><br />Notifies a composable instance of an update.<br/><br/>This function only generates a notification. It is the responsibility of the calling client to have updated any data that the composable instance needs.<br/><br/>**composableInstance:** The composable instance that will be notified.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| onConfigurationChanged                     | ```fun onConfigurationChanged()```<br /><br />Responds to device configuration changes.<br/><br/>This should be called from the main activity's onDestroy function. When a configuration change occurs, all the children composable instances on all screens are removed from their parent composable instance. When the activity is recreated, all the root composable instances (the ones that make up each screen) will be recomposed. This may result in a completely different composable resource being selected for each screen, depending on the type of configuration change and what composable resource (if any) is setup to be used under the new configuration.<br/><br/>When a configuration change occurs, all the children composable instances on each screen is temporarily cached internally. When a screen is recomposed, if the new screen requires a composable resource for one of its children and a composable instance of that type is in the cache and was previously assigned to the same screen, it will be reused, otherwise it will be removed from the cache as soon as the user moves to a different screen. For example, if the current screen is displayed in landscape mode showing a list in a pane on the left and a details pane on the right is then rotated to portrait mode but now only shows just the list, the composable instance for the list will be reused and the composable instance for the details pane will be removed as soon as the user navigates to a different screen. Should the user rotate the device to landscape mode again without navigating to a different screen, the composable instance for the details pane will be reused.                                                                                                                                                                                                                                                     |
| RenderComposable                           | ```fun RenderComposable(parentComposableId: String, composableResId: String? = null, childComposableId: String? = null, p: Any? = null)```<br /><br />Renders a composable instance.<br/><br/>**parentComposableId:** If the composable instance that is to be rendered is the root composable on a screen, this is its id. A root commposable had to have been created previously when the createRootComposableInstance function was called, in which case the ComposableInstance.id property will have been automatically created.<br/><br/>**composableResId:** Identifies the composable resource to use to create a composable instance that will be rendered. Whether a resource will be selected and used to create a composable instance depends on whether the childComposableId parameter is specified and whether a child composable instance already exists for the parent composable instance.<br/><br/>**childComposableId:** If specified, it means that the composable instance that is about to be rendered is a child composable on the screen whose parent is identified by the parentComposableId parameter. A check is first made to see whether a composable instance for the child already exists in a temporary cache and used if it does. Children composable instances are temporarily cached whenever a device configuration change occurs, such as changing the device's orientation. After the configuration change completes and the current screen is recomposed, RenderComposable should be called to recompose the root (parent) composable instance which in turn will recompose all of its children. The temporarily cached children are reused if a screen with the same parent is recomposed. Any unused cached children instances remain in the cache until the user navigates back to a previous screen or to the home screen.<br /><br />**p:** Any data that needs to be passed to the composable instance. |
| RenderComposable                           | ```fun RenderComposable(composableInstance: ComposableInstance)```<br /><br />Renders a composable.<br/><br/>**composableInstance:** If the isTerminated property is set to true, the selectedResourceId will be used to select the composable resource and render the composable instance with that resource. If isTerminated is false, the composable instance is rendered as it normally would be using only the parentComposableId.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| selectComposableResource                   | ```fun selectComposableResource(composableResId: String): ComposableResource```<br /><br />Selects the composable resource that will be used to render a composable instance.<br/><br/>**composableResId:** The composable resource id. Many composable resources can be defined using addComposableResources but the one selected for rendering a composable instance depends on the current device configuration.<br/><br/>**returns:** The selected composable resource will be returned.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| setContext                                 | ```fun setContext(ctx: Context)```<br /><br />Sets the context that the CRM uses.<br/><br/>IMPORTANT: Only set this context using the Application context when the app starts up. Don't use any other context, otherwise you will end up with a memory leak.<br/><br/>**ctx:** The context that the CRM will use.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| updateOrGoto                               | ```fun updateOrGoto(parentComposableInstance: ComposableInstance, childComposableId: String? = null, childComposableResourceId: String,   fullscreenComposableResourceId: String, p: Any?, cacheComposable: Boolean = false)```<br /><br />Updates an existing child composable instance or navigates to a new screen using the specified child composable resource. This is essentially a shortcut call to getChildComposableInstance and createRootComposableInstance. First a call will be made to getChildComposableInstance to retrieve the child if it exists. If it exists, the child will be updated. If no child exists, the NavigationManager.goto function will be called.<br/><br/>**parentComposableInstance:** The parent composable instance that the child may or may not belong to.<br/><br/>**childComposableId:** The id of the child when getChildComposableInstance is called.<br/><br/>**childComposableResourceId:** The id of the composable resource when getChildComposableInstance is called.<br/><br/>**fullscreenComposableResourceId:** If the CRM determines that no child composable instance exists on the current screen, it will navigate to a new screen using the composable resource specified by this parameter.<br/><br/>**p:** If no child composable instance exists for the specified parent, createRootComposableInstance will be called and the p parameter will be passed to the new screen. If a child composable instance exists for the parent, the p parameter is ignored. If the client needs to update a child composable that is on the same screen, it should call getChildComposableInstance first and then update the child's parameters before calling updateOrGoto.<br/><br/>**cacheComposable:** If set to true and the CRM navigates to a new screen, the new screen will be cached if this parameter is set to true.                                                                |

<a name="composable_resource" /><br />

## ComposableResource (Class)

Represents a composable resource.

A composable resource acts as a template similar to how an xml layout works under the older view system. The CRM uses the composable resource when it needs to create a composable instance. Similar to how configuration qualifiers are used with xml layouts, composable resources can also define qualifiers through properties. The CRM will determine which composable resource to use based on the current device configuration. Requests to the CRM can be made to create composable instances from composable resources. A composable instance is rendered on the screen. For details on each qualifier, see:

[https://developer.android.com/guide/topics/resources/providing-resources#AlternativeResources](https://developer.android.com/guide/topics/resources/providing-resources#AlternativeResources)

A composable resource that has not set any of its configuration qualifiers is referred to as the default resource. Multiple composable resources can be defined that use the same resourceId property but one - and only one - must exist that acts as the default. An exception is thrown by addComposableResources if no default is defined for each unique resourceId.

A composable resource can also optionally define a viewmodel. When a composable instance is created, an instance of the viewmodel is created and assigned to the composable instance.

| Function / Property             | Description                                                                                                                                                                                                                                                                                                                                                               |
| ------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| availableHeightInDp             | ```var availableHeightInDp: Int? = null```<br /><br />The available screen height in dp.                                                                                                                                                                                                                                                                                  |
| availableWidthInDp              | ```val availableWidthInDp: Int? = null```<br /><br />The available screen width in dp.                                                                                                                                                                                                                                                                                    |
| highDynamicRange                | ```var highDynamicRange: HighDynamicRange? = nul```<br /><br />Displays with high or low dynamic ranges.                                                                                                                                                                                                                                                                  |
| keyboardAvailability            | ```var keyboardAvailability: KeyboardAvailability? = null```<br /><br />Type of keyboard available.                                                                                                                                                                                                                                                                       |
| languageAndRegion               | ```val languageAndRegion: String? = null```<br /><br />The language and region. Example "en-ca" for english, Canada. Note: BCP 47 language tags are currently not supported.                                                                                                                                                                                              |
| layoutDirection                 | ```var layoutDirection: LayoutDirection? = null```<br /><br />Layout direction is either left-to-right or right-to-left.                                                                                                                                                                                                                                                  |
| mcc                             | ```val mcc: Int? = null```<br /><br />The mobile country code (MCC). If the MNC qualifier is also provided, it will be combined with the MCC qualifier to act as a single qualifier.                                                                                                                                                                                      |
| mnc                             | ```val mnc: Int? = null```<br /><br />The mobile network code (MNC). If the MCC qualifier is not specified, the MNC qualifier will be ignored.                                                                                                                                                                                                                            |
| navigationKeyAvailability       | ```var navigationKeyAvailability: NavigationKeyAvailibility? = null```<br /><br />Whether navigation keys are available.                                                                                                                                                                                                                                                  |
| nightMode                       | ```var nightMode: NightMode? = null```<br /><br />Night mode.                                                                                                                                                                                                                                                                                                             |
| onAnimateVisibility             | ```var onAnimateVisibility: (@Composable (composableInstance: ComposableInstance, isVisible: Boolean) -> Unit)? = null```<br /><br />A callback that will be called to allow the composable to provide animation when made visible or hidden. The invisible parameter will be set to true to indicate that composable is being made visible and set to false when hidden. |
| onRender                        | ```var onRender: @Composable (composableInstance: ComposableInstance) -> Unit```<br /><br />A callback that will be called to render the composable.                                                                                                                                                                                                                      |
| platformVersion                 | ```var platformVersion: Int? = null```<br /><br />The API level supported by the device. Example, 1, 2, 3...30, etc.                                                                                                                                                                                                                                                      |
| primaryNonTouchNavigationMethod | ```var primaryNonTouchNavigationMethod: PrimaryNonTouchNavigationMethod? = null```<br /><br />The primary method used to interact that is non-touch.                                                                                                                                                                                                                      |
| primaryTextInputMethod          | ```var primaryTextInputMethod: PrimaryTextInputMethod? = null```<br /><br />Primary means of entering text.                                                                                                                                                                                                                                                               |
| resourceId                      | ```val resourceId: String```<br /><br />The id provided by the app to identify the resource. This is not unique. The same id can be used for a different set of qualifier configurations.                                                                                                                                                                                 |
| roundScreen                     | ```var roundScreen: RoundScreen? = null```<br /><br />The screen's shape.                                                                                                                                                                                                                                                                                                 |
| screenAspect                    | ```var screenAspect: ScreenAspect? = null```<br /><br />The screen's aspect ratio in a generic format.                                                                                                                                                                                                                                                                    |
| screenOrientation               | ```var screenOrientation: ScreenOrientation? = null```<br /><br />The screen orientation: portrait or landscape.                                                                                                                                                                                                                                                          |
| screenPixelDensityDpi           | ```var screenPixelDensityDpi: Int? = null```<br /><br />The screen's pixel density in dpi. Use constants defined in ScreenPixelDensityDpi or set this property to a dpi value (which is the equivalent of setting the nnndpi qualifier value).                                                                                                                            |
| screenSize                      | ```var screenSize: ScreenSize? = null```<br /><br />The size of the screen using generic approximate sizes.                                                                                                                                                                                                                                                               |
| smallestWidthInDp               | ```var smallestWidthInDp: Int? = null```<br /><br />Smallest screen width specified in dp.                                                                                                                                                                                                                                                                                |
| touchScreenType                 | ```var touchScreenType: TouchScreenType? = null```<br /><br />Whether touch is used on the screen.                                                                                                                                                                                                                                                                        |
| uiMode                          | ```val uiMode: UIMode? = null```<br /><br />Type of device the screen is being displayed on.                                                                                                                                                                                                                                                                              |
| viewmodel                       | ```val viewmodelClass: Class<*>? = null```<br /><br />The class that will be used to create an instance of the viewmodel associated with an instance of the composable. Only a reference to the viewmodel class is to be provided. For example: PetsListViewModel::class.java                                                                                             |
| wideColorGamut                  | ```var wideColorGamut: WideColorGamut? = null```<br /><br />The screen's color gamut.                                                                                                                                                                                                                                                                                     |

<a name="composable_instance" /><br />

## ComposableInstance (Class)

A composable instance encapsulates all the properties needed to render (compose/recompose) a composable based on a device's configuration. A composable instance can also optionally include a viewmodel. A screen can consist of a parent (root) composable instance and multiple children composable instances.

<ins>**Currently there is no support for children composable instances having their own children.**</ins>

When a root composable instance is rendered, the entire screen is rendered. A child composable instance will normally act as both a root and a child depending on the device configuration. For example, on a phone in portrait mode, you could have a list of pets. The root composable instance for this might be called "PetsList". When the user clicks on a list item, the user navigates to a details screen displaying information about the selected pet. This details screen is also a root composable instance and could be referred to as "PetDetails". When the same app is run on a tablet in landscape mode, there is more screen space to show both the list and details side-by-side. This screen would contain a root composable instance that acts a container for the two children composable instances which would be the PetsList and the PetDetails. So on a phone in portrait mode, the PetsList and PetDetails act as root composables but when used on a tablet in landscape mode, they act as children.

When the user hits the Back button or returns directly to the home screen, the current screen's root composable instance is removed from the navigation stack along with its children composable instances.

| Function / Property      | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| ------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| animationTransitionState | ```var animationTransitionState: MutableTransitionState<Boolean>? = null```<br /><br />Indicates the animation transition state when the composable instance is being shown or hidden with animation.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| composableResId          | ```var composableResId: String```<br /><br />This identifies the composable resource that the instance is associated with. A composable resource with the same id can be provided by the client when the addComposableResources function is called. For example, multiple composable resources with the id "PetsList" can be provided to addComposableResources but where each resource has its own unique set of configuration qualifiers.                                                                                                                                                                                                                                                                                                               |
| composables              | ```val composables: MutableList<ComposableInstance> = mutableListOf()```<br /><br />Lists all of the children composables that the composable instance may have. Only a root composable instance can have children. Although a child composable instance will still have this property, it will not be used. Support for deeply nested descendant composable instances is not currently supported (grandchildren, great grandchildren, etc). This means that if you have root composable that contains children instance composables, those children should not have any children of their own.                                                                                                                                                           |
| deepLink                 | ```var deepLink: DeepLink? = null```<br /><br />                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| id                       | ```var id: String```<br /><br />If the composable instance is the root composable for a screen, the id will be generated by the ComposableResourceManager if the client doesn't provide the id. If it's a child composable on a screen, the id will be provided by the client when RenderComposable is called.                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| isRoot                   | ```var isRoot: Boolean = false```<br /><br />Set to true if this composable is the root composable on the screen.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| isTerminated             | ```var isTerminated: Boolean = false```<br /><br />If set to true, the composable is terminated and no longer part of the navigation stack. A call to RenderComposable can still be made with the composable when a screen where the composable is located is being closed. This allows the screen to be recomposed during animation transitions to become invisible.                                                                                                                                                                                                                                                                                                                                                                                     |
| onCloseScreen            | ```var onCloseScreen: LiveData<Boolean>? = null```<br /><br />Used to notify the composable instance that the screen on which they located is being closed. Composable instances should then take action to perform any cleanup they need as well as prevent any processes from being carried out that would normally be executed when the screen is being made visible the first time.<br/><br/>Whenever the LiveData is triggered, the composable instance performs a recompose.                                                                                                                                                                                                                                                                        |
| onUpdate                 | ```var onUpdate: LiveData<Int>? = null```<br /><br />Used to notify the composable instance when it is being updated.<br/><br/>A typical case is when a screen consists of two composable instances - one a list in the left pane and a details pane on the right. When the user clicks on a list item, the details pane needs to be updated. The details pane can be updated if the list calls updateOrGoto. The value sent by LiveData is a random number and has no meaning. It is simply used to trigger the LiveData. The details screen is then responsible to retrieve any updated data through the parameters property of its own composable instance.<br/><br/>Whenever the LiveData is triggered, the composable instance performs a recompose. |
| parameters               | ```var parameters: Any? = null```<br /><br />Contains any parameters that need to be passed to the composable instance.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| parentId                 | ```var parentId: String? = null```<br /><br />If the composable is a child composable, the parent id refers to the composable that it is a child of.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| selectedResourceId       | ```var selectedResourceId: String? = null```<br /><br />When a composable resource is selected to render the composable, the id for the selected composable resource is used here. This id originates from ComposableResource.id which is generated internally by the ComposableResourceManager.                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| viewmodel                | ```var viewmodel: ViewModel? = null```<br /><br />An optional viewmodel that can be assigned to the composable instance.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |

<a name="composable_params" /><br />

## ComposableParams (Class)

General purpose class that can be used to pass data between composables.

| Function / Property | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| ------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| constructor         | ```open class ComposableParams(var modifier: Modifier = Modifier,var data: Any? = null, var onReturn: ((data: Any?, canceled: Boolean) -> Unit)? = null)```<br /><br />**modifier:** Modifiers that can be passed on down through the screen's hierarchy.<br /><br />**data:** Any data that needs to be provided to the target composable.<br /><br />**onReturn:** A callback that can be used to return data from the target composable instance. If canceled is set to true, it means the user canceled whatever action was to be performed on the target screen. |

<a name="screen_factory_handler" /><br />

## ScreenFactoryHandler (Composable)

The Screen Factory is responsible for calling upon the CRM to render all of the screens on the navigation stack. It is triggered through LiveData whenever the Navigation Manager performs a navigation. Although all the screens are recomposed whenever the Screen Factory is triggered, from the user's perspective, the user only sees a new screen being displayed or the current screen being removed (which happens when you hit the Back button). The user does not see all the screens being rendered. However, because each screen is recomposed, the developer must take care to handle recomposition happening multiple times for the same screen. This is the behavior of Jetpack Compose and not something peculiar to Jetmagic.

The Screen Factory will call the CRM to select the correct composable resource for each composable instance (that represents a screen) and use that resource to render the actual composable instance to the screen.

Each screen that is rendered can have its own custom visibility animation. For a list of animations that Jetpack Compose offers, see:

[https://developer.android.com/jetpack/compose/animation](https://developer.android.com/jetpack/compose/animation)

This composable should be one of the topmost composables placed in your app. For example, if your app uses the Scaffold composable, you would place this in the Scaffold's content parameter. For example:

```kotlin
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
```

<a name="local_composable_instance" /><br />

## LocalComposableInstance (Property)

```
val LocalComposableInstance = staticCompositionLocalOf { ComposableInstance(id = "", composableResId = "") }
```

LocalComposableInstance is used with a LocalCompositionProvider to provide composables on a screen access to a composable instance. It is primarily intended to be used in the root composable instance of a screen although children composable instances can use it to provide their own children access to their composable instance at any level further down the hierarchy. On screens where there is a deep hierarchy of composables, some composables need access to certain properties of a composable instance such as the viewmodel. LocalCompositionProvider is designed to avoid having to pass these properties down through the hierarchy through parameters in composables.

Composables should follow the "state hoisting" pattern as described at:

[https://developer.android.com/jetpack/compose/state#state-hoisting](https://developer.android.com/jetpack/compose/state#state-hoisting)

Accessing properties of the composable instance should be done in the hoisted composable. For example, in the demo app, the pet details uses the LocalComposableInstance to provide access to the composable instance to all composables below itself in its hierarchy:

```kotlin
@Composable
fun PetDetailsHandler(composableInstance: ComposableInstance) {

    CompositionLocalProvider(LocalComposableInstance provides composableInstance) {
        val vm = composableInstance.viewmodel as PetDetailsViewModel
        val p = composableInstance.parameters as PetDetailsParams?

        vm.imageManager.initialize(composableInstance = composableInstance)

        val modifier: Modifier = p?.modifier ?: Modifier
        val pet: PetListItemInfo? = p?.petsListItemInfo

        PetDetailsUI(
            modifier = modifier,
            pet = pet,
            scrollState = vm.scrollState,
            onAdoptClick = {

            },
            onBackButtonClick = {
                navman.goBack()
            })
    }
}
```

The image gallery on the pet details screen needs access to viewmodel in order to have access to the Image Manager:

```kotlin
@Composable
fun PetImageGalleryHandler(
    pet: PetListItemInfo,
    modifier: Modifier = Modifier
) {
    val imageManager = (LocalComposableInstance.current.viewmodel as IImageManager).imageManager

    PetImageGallery(
        pet = pet,
        modifier = modifier,
        onThumbnailClick = { petId, selectedThumbnailNumber ->
            val pathToLargeImage = getGalleryLargeImagePath(petId = petId, imageIndex = selectedThumbnailNumber)
            imageManager.updateState(id = "large", imagePath = pathToLargeImage, animate = true)
        })
}
```