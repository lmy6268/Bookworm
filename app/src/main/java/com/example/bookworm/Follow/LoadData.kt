package com.example.bookworm.Follow

import android.util.Log
import com.example.bookworm.User.UserInfo
import com.google.firebase.firestore.*

//팔로우 하는 사람 또는 팔로워의 데이터를 가져온다
class LoadData(val view: Contract.View, val isfollewer: Boolean,val nowUserInfo: UserInfo) : LoadInfoCallback, Contract.Presenter {
    public val LIMIT:Long = 10//최대 10명씩 불러오도록 함
    var token: String? = null
    var lastVisible: String? = null //이어서 가져오는 경우 필요함
    var reference: CollectionReference? =null
    var query: Query? =null

    //데이터를 가져오는 부분
    fun getData(token: String) {
        this.token = token
         reference = FirebaseFirestore.getInstance().collection("users")
                .document(token!!).collection(if (isfollewer) "follower" else "following")
        query = reference!!.orderBy("token")
        if(lastVisible!=null)query=query!!.startAfter(lastVisible)
        query=query!!.limit(LIMIT)
        query!!.get().addOnCompleteListener(
                {
                    if (it.isSuccessful) {
                        isFollowed(it.result) //팔로잉 여부 체크
                    } else    onDataNotAvailable()
                }
        )
    }

    //데이터를 뷰에 세팅하도록 함
    override fun setInfo(info: ArrayList<UserInfo>?) {
        view.showInfo(info)
    }


    //이 사람을 내가 팔로우 하고 있는지 확인
    override fun isFollowed(data: QuerySnapshot) {
        val result: List<DocumentSnapshot> = data.documents
        if (result.size>0) {
            var idList: ArrayList<String> = ArrayList()
            var followerList: ArrayList<UserInfo> = ArrayList()
            for (doc: DocumentSnapshot in result) {
                idList.add(doc.id)
                var userInfo = UserInfo()
                userInfo.add(doc.getData())
                followerList.add(userInfo)
            }
            //현재 가장 마지막 데이터를 저장해두어, 다음에 조회시 이어서 받아 올 수 있도록 함.
            lastVisible=followerList.get(followerList.size-1).token
            //팔로잉 여부를 확인하는 쿼리 만들기
            var myref=FirebaseFirestore.getInstance().collection("users")
                    .document(nowUserInfo.token).collection("following")
            var q = myref.whereIn("token", idList)
            //파이어베이스에 쿼리 보내고 응답받기
            q.get().addOnCompleteListener({
                if (it.isSuccessful) onCheckedInfoLoaded(it.result, followerList)
                else onDataNotAvailable()
            })
        }
        //팔로잉 하는 사람이 없는 경우 null을 뷰로 전송
        else  setInfo(null)
    }

    //팔로우 한 사람들만 팔로잉 중 버튼 세팅 후 해당 데이터를 뷰로 전송
    override fun onCheckedInfoLoaded(data: QuerySnapshot, userList: ArrayList<UserInfo>) {
        val result: List<DocumentSnapshot> = data.documents
        val idList: ArrayList<String> = ArrayList()
        for (doc: DocumentSnapshot in result) idList.add(doc.id)

        for (i in 0..userList.size-1) {
            var user = userList.get(i)
            if (idList.contains(user.token)) {
                user.setFollowed(true)
                userList.set(i, user)
            }
        }
        //팔로잉 여부가 확인된 UserList를 뷰로 전송
        setInfo(userList)
    }


    //서버에서 데이터를 받지 못한 경우
    override fun onDataNotAvailable() {
        Log.d("유저 정보 오류", "파이어베이스에서 유저정보를 받아올 수 없습니다")
    }


}