package com.example.bookworm.Feed.ViewHolders

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.example.bookworm.Core.Internet.FBModule
import com.example.bookworm.Core.MainActivity
import com.example.bookworm.Core.UserData.Interface.UserContract
import com.example.bookworm.Core.UserData.Modules.LoadUser
import com.example.bookworm.Core.UserData.PersonalD
import com.example.bookworm.Core.UserData.UserInfo
import com.example.bookworm.Feed.Comments.Comment
import com.example.bookworm.Feed.Comments.CommentsCounter
import com.example.bookworm.Feed.Comments.subactivity_comment
import com.example.bookworm.Feed.CustomPopup
import com.example.bookworm.Feed.items.Feed
import com.example.bookworm.Feed.likeCounter
import com.example.bookworm.Profile.ProfileInfoActivity
import com.example.bookworm.R
import com.example.bookworm.Search.subActivity.search_fragment_subActivity_result
import com.example.bookworm.databinding.FragmentFeedItemBinding
import java.util.*
import kotlin.collections.HashMap

class TestVIewHolder(itemView: View, context: Context?) : RecyclerView.ViewHolder(itemView),
    UserContract.View {
    var binding: FragmentFeedItemBinding? = null
    var nowUser: UserInfo? = null
    var strings: ArrayList<String>? = null
    var liked = false
    var limit = 0
    var restricted = false
    var context: Context? = null
    var fbModule = FBModule(context)
    var Count: Long = 0
    var loadUser1: LoadUser? = null
    var loadUser2: LoadUser? = null
    //생성자를 만든다.
    init {
        binding = FragmentFeedItemBinding.bind(itemView)
        this.context = context
        nowUser = PersonalD(context).userInfo //현재 사용자
        loadUser1= LoadUser(this)
        loadUser2= LoadUser(this)
    }

    //아이템을 세팅하는 메소드
    fun setItem(item: Feed) {
        //피드에 삽입한 책
        val book = item.book
        binding!!.feedBookAuthor.setText(book.author)
        Glide.with(itemView).load(book.img_url)
            .into(binding!!.feedBookThumb) //책 썸네일 설정
        binding!!.feedBookTitle.setText(book.title)
        binding!!.llbook.setOnClickListener({
            val intent = Intent(context, search_fragment_subActivity_result::class.java)
            intent.putExtra("itemid", book.itemId)
            context!!.startActivity(intent)
        })
        //작성자 UserInfo
        loadUser1!!.getData(item.userToken, false)
        //피드 내용

        //댓글 창 세팅
        Count = item.commentCount
        if (Count > 0) setComment(item.comment)
        else {
            setViewV(false)
            binding!!.llCommentInfo.visibility = View.GONE //댓글이 0개인 경우, 몇개 더보기 지움.
        }

        //댓글창을 클릭했을때
        binding!!.llComments.setOnClickListener({
            if (binding!!.tvCommentNickname.visibility != View.GONE) {
                val intent = Intent(context, subactivity_comment::class.java)
                intent.putExtra("item", item)
                intent.putExtra("position", getAdapterPosition())
                context!!.startActivity(intent)
            }
        })
        //댓글 아이콘을 눌렀을 때
        binding!!.btnComment.setOnClickListener({
            val intent = Intent(context, subactivity_comment::class.java)
            intent.putExtra("item", item)
            intent.putExtra("position", getAdapterPosition())
            context!!.startActivity(intent)
        })
        //댓글 빠르게 달기
        binding!!.btnWriteComment.setOnClickListener(View.OnClickListener {
            val userComment: String = binding!!.edtComment.getText().toString()
            if (userComment != "" && userComment != null) {
                Count++
                setComment(addComment(item.feedID))
            }
        })
        binding!!.tvCommentCount.setText(item.commentCount.toString()) //댓글 수 세팅
        //좋아요 수 세팅
        binding!!.tvLike.setText(item.likeCount.toString())
        liked = try {
            if (nowUser!!.likedPost.contains(item.feedID)) {
                binding!!.btnLike.setBackground(context!!.getDrawable(R.drawable.icon_like_red))
                true
            } else {
                binding!!.btnLike.setBackground(context!!.getDrawable(R.drawable.icon_like))
                false
            }
        } catch (e: NullPointerException) {
            binding!!.btnLike.setBackground(context!!.getDrawable(R.drawable.icon_like))
            false
        }
        //좋아요 표시 관리

        binding!!.lllike.setOnClickListener({ controlLike(item) })
        //이미지 뷰 정리
        if (item.imgurl != null) {
            Glide.with(itemView).load(item.imgurl).into(binding!!.feedImage)
        }
        binding!!.tvFeedtext.setText(item.feedText)
        //라벨 세팅
        if (!item.label.get(0).equals("")) setLabel(item.label)
        else binding!!.lllabel.visibility = View.GONE

        //프로필을 눌렀을때 그 사람의 프로필 정보 화면으로 이동
        binding!!.llProfile.setOnClickListener(View.OnClickListener {
            val intent = Intent(context, ProfileInfoActivity::class.java)
            intent.putExtra("userID", item.userToken)
            context!!.startActivity(intent)
        })
        binding!!.btnFeedMenu.setOnClickListener(View.OnClickListener { view ->
            item.position = getAdapterPosition()
            val popup1 = CustomPopup(context, view)
            popup1.setItems(context!!, fbModule, item)
            popup1.setOnMenuItemClickListener(popup1)
            popup1.setVisible(nowUser!!.token == item.userToken)
            popup1.show()
        })
    }

    private fun addComment(FeedID: String): Comment? {
        val data: MutableMap<String?, Any?> = HashMap()
        //유저정보, 댓글내용, 작성시간
        val comment = Comment()
        comment.getData(
            nowUser!!.token,
            binding!!.edtComment.getText().toString(),
            System.currentTimeMillis()
        )
        data["comment"] = comment
        //입력한 댓글 화면에 표시하기
        if (binding!!.llCommentInfo.visibility == View.GONE) binding!!.llCommentInfo.visibility =
            View.VISIBLE
        CommentsCounter().addCounter(data, context, FeedID)
        //키보드 내리기
        val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding!!.edtComment.getWindowToken(), 0)
        binding!!.edtComment.clearFocus()
        binding!!.edtComment.setText(null)
        return comment
    }

    fun setVisibillity(check: Boolean) {
        if (check) binding!!.feedImage.setVisibility(View.VISIBLE) else {
            binding!!.feedImage.setImageResource(0)
            binding!!.feedImage.setVisibility(View.GONE)
        }
    }

    private fun controlLike(item: Feed) {
        if (limit < 5) {
            limit += 1
            nowUser = PersonalD(context).userInfo
            strings = nowUser!!.getLikedPost()
            val map = HashMap<Any, Any>()
            var likeCount: Int = binding!!.tvLike.getText().toString().toInt()
            if (!liked) {
                //현재 좋아요를 누르지 않은 상태
                likeCount += 1
                liked = true
                strings!!.add(item.feedID)
                binding!!.btnLike.setBackground(context!!.getDrawable(R.drawable.icon_like_red))
            } else {
                //현재 좋아요를 누른 상태
                likeCount -= 1
                liked = false
                strings!!.removeAll(Arrays.asList(item.feedID))
                strings!!.remove(item.feedID)
                binding!!.btnLike.setBackground(context!!.getDrawable(R.drawable.icon_like))
            }
            nowUser!!.setLikedPost(strings)
            map["nowUser"] = nowUser!!
            binding!!.tvLike.setText(likeCount.toString())
            map["liked"] = liked
            PersonalD(context).saveUserInfo(nowUser)
            likeCounter().updateCounter(map, item.feedID)
        } else {
            AlertDialog.Builder(context)
                .setMessage("커뮤니티 활동 보호를 위해 잠시 후에 다시 시도해주세요")
                .setPositiveButton(
                    "네"
                ) { dialog, which -> dialog.dismiss() }.show()
            if (!restricted) {
                restricted = true
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    limit = 0
                    restricted = false
                }, 10000)
            }
        }
    }

    fun setComment(comment: Comment?) {
        if (comment != null) {
            setViewV(true)
            binding!!.tvCommentCount.setText(Count.toString())
            binding!!.tvCommentContent.setText(comment.contents)
            loadUser2!!.getData(comment.userToken, true)
            binding!!.tvCommentDate.setText(comment.madeDate)

        } else setViewV(false)
    }

    fun setViewV(bool: Boolean) {
        val value = if (bool) View.VISIBLE else View.GONE
        binding!!.llCommentInfo.visibility = value
        binding!!.tvCommentDate.setVisibility(value)
        binding!!.tvCommentNickname.setVisibility(value)
        binding!!.tvCommentContent.setVisibility(value)
        binding!!.ivCommentProfileImage.setVisibility(value)
    }

    //라벨을 동적으로 생성
    private fun setLabel(label: ArrayList<String>) {
        binding!!.lllabel.removeAllViews() //기존에 설정된 값을 초기화 시켜줌.
        val idx = label.indexOf("")
        for (i in 0 until idx) {
            //뷰 생성
            val tv = TextView(context)
            tv.text = label[i] //라벨에 텍스트 삽입
            tv.background = context!!.getDrawable(R.drawable.label_design) //디자인 적용
//            tv.setBackgroundColor(Color.parseColor("#0F000000")) //배경색 적용
            tv.setBackgroundColor((context as MainActivity).getColor(R.color.subcolor_2)) //배경색 적용
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ) //사이즈 설정
            params.setMargins(10, 0, 10, 0) //마진 설정
            tv.layoutParams = params //설정값 뷰에 저장
            binding!!.lllabel.addView(tv) //레이아웃에 뷰 세팅
        }
    }

    override fun showProfile(userInfo: UserInfo, bool: Boolean?) {

        if (bool==false) {
            binding!!.tvNickname.setText(userInfo.username)
            Glide.with(itemView).load(userInfo.profileimg).circleCrop()
                .into(binding!!.ivProfileImage)
        } else if (bool==true) {
            binding!!.tvCommentNickname.setText(userInfo.username)
            Glide.with(binding!!.getRoot()).load(userInfo.profileimg).circleCrop()
                .into(binding!!.ivCommentProfileImage)
        }
    }

}