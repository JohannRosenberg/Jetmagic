package io.github.johannrosenberg.sample.da.web

// This api is no longer available. Replace the address with an api that provides cat data.
// Currently cat data is being loaded from a json file stored in the pets.json file and thumbnail
// images are retrieved from images stored under the drawable folder.

const val APIBaseAddress = "https://api.wirespec.dev/wirespec/adoptpets/"
const val ImageBaseAddress = "https://storage.googleapis.com/wirespec.appspot.com/images/cats/"
const val PetsThumbnailImagesPath = "${ImageBaseAddress}thumbnails/"
const val PetsLargeImagesPath = "${ImageBaseAddress}large/"