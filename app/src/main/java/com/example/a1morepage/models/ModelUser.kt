package com.example.a1morepage.models

class ModelUser {
     var id: String = ""
     var bookId: String = ""
     var profileImage: String = ""
     var name: String  = ""
     var uid : String = ""

     constructor()
     constructor(id: String, bookId: String, profileImage: String, userName: String, uid: String) {
          this.id = id
          this.bookId = bookId
          this.profileImage = profileImage
          this.name = userName
          this.uid = uid
     }


}




