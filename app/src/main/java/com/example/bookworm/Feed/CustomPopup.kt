package com.example.bookworm.Feed

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import com.example.bookworm.Feed.Comments.subactivity_comment
import com.example.bookworm.Feed.items.Feed
import com.example.bookworm.MainActivity
import com.example.bookworm.R
import com.example.bookworm.fragments.fragment_feed
import com.example.bookworm.modules.FBModule

class CustomPopup(context: Context?, anchor: View?) : PopupMenu(context, anchor), PopupMenu.OnMenuItemClickListener {
    var context: Context? = context  //종료할 액티비티의 context , ?이 붙여지면 nullable한 변수
    var context2: Context? = null //
    var fbModule: FBModule? = null
    var layout: Int = R.menu.feed_menu
    var item: Feed? = null

    fun setItems(context: Context, fbModule: FBModule, feed: Feed) {
        this.fbModule = fbModule
        this.context2 = context
        this.item = feed
        this.menuInflater.inflate(layout, this.menu) //레이아웃에 inflate
    }

    override fun onMenuItemClick(p0: MenuItem?): Boolean {
        if (layout == R.menu.feed_menu) {
            var pos: Int = item!!.position
            var ff: fragment_feed? = (context2 as MainActivity).supportFragmentManager.findFragmentByTag("0") as fragment_feed?
            var oldList: ArrayList<Feed>? = ArrayList(ff!!.feedList)
            when (p0?.itemId) {
                R.id.menu_modify -> {
                    //현재 화면이 댓글 화면이라면
                    if (context is subactivity_comment) {
                        var intent: Intent? = Intent(context, subActivity_Feed_Modify::class.java)
                        intent?.putExtra("Feed",item)
                        (context as (subactivity_comment)).startActivityResult.launch(intent)
                    }
                    //현재 화면이 피드 화면이라면
                    else {
                        var intent: Intent? = Intent(context, subActivity_Feed_Modify::class.java)
                        intent?.putExtra("Feed",item)
                        ff.startActivityResult.launch(intent)//이렇게 하면 피드에서 값 세팅은 될지 모르겠지만,,,

                    }
                    return true
                }
                R.id.menu_delete -> {
                    fbModule!!.deleteData(1, item!!.feedID) //삭제
                    oldList?.removeAt(pos)
                    ff.replaceItem(oldList)
                    //만약 댓글을 모아보는 액티비티(subactivity_comment)에 있는 경우, 해당 액티비티를 종료
                    if (context is subactivity_comment) (context as subactivity_comment).finish()
                }
                else -> return true
            }
        }
        return false;
    }

    fun setVisible(boolean: Boolean) {
        this.menu.findItem(R.id.menu_delete).setVisible(boolean)
        this.menu.findItem(R.id.menu_modify).setVisible(boolean)
    }


}