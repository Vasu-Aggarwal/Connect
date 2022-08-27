package com.example.connect

//not using thumbnail image can't get that concept it basically reduces the size of the image

data class User(val name: String, val imageUrl: String, val uid: String,
                val deviceToken: String, val status: String, val onlineStatus: String){

    /*whenever we make data class for firebase always make
    an empty constructor otherwise it will not run
     */

    constructor(): this("", "", "",
        "", "", "")

    constructor(name: String, imageUrl: String,
                uid: String): this(
        name,
        imageUrl,
        uid,
        "",
        "Hey!",
        System.currentTimeMillis().toString()
    )

}
