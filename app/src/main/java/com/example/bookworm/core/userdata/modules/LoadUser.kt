package com.example.bookworm.core.userdata.modules

import com.example.bookworm.core.userdata.interfaces.UserContract
import com.example.bookworm.core.userdata.UserInfo
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.NullPointerException

class LoadUser(val view:UserContract.View):UserContract.Presenter {
    var token: String? = null
    var reference: CollectionReference? =null
    var task: Task<DocumentSnapshot>? =null
    var boolean:Boolean?=null
    override fun getData(token: String,boolean: Boolean?) {
        this.token =token
        reference= FirebaseFirestore.getInstance().collection("users")
        task=reference!!.document(token).get()
        if(boolean!=null)this.boolean=boolean
        task!!.addOnCompleteListener({
            setProfile(it.result)
        })
    }

    override fun setProfile(document: DocumentSnapshot) {
        var userData=UserInfo()
        try{
            var map= document.data!!["UserInfo"] as Map<String,String>
            userData.add(map)
        }catch (e:NullPointerException){
        }

        view.showProfile(userData,boolean)
    }
}