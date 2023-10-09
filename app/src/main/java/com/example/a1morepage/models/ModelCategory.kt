package com.example.a1morepage.models

class ModelCategory {
    var id: String =""
    var category: String =""
    var timestamp: String = ""
    var uid: String = ""

    // empty constructor, required by firebase
    constructor()

    //parametrized constructor
    constructor(id: String, category:String, timestamp: String, uid:String){
        this.id = id
        this.category = category
        this.timestamp = timestamp
        this.uid = uid
    }


}