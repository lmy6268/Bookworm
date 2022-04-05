package com.example.bookworm.Feed.ViewHolders;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookworm.Feed.items.Comment;
import com.example.bookworm.Feed.items.Feed;
import com.example.bookworm.Feed.items.customDialog;
import com.example.bookworm.Feed.likeCounter;
import com.example.bookworm.R;
import com.example.bookworm.Search.items.Book;
import com.example.bookworm.Search.subActivity.search_fragment_subActivity_result;
import com.example.bookworm.User.UserInfo;
import com.example.bookworm.databinding.LayoutFeedBinding;
import com.example.bookworm.modules.FBModule;
import com.example.bookworm.modules.personalD.PersonalD;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ItemViewHolder extends RecyclerView.ViewHolder {
    LayoutFeedBinding binding;
    UserInfo nowUser;
    ArrayList<String> strings;
    Boolean liked = false;
    int limit = 0;
    Boolean restricted = false;
    Context context;
    customDialog dialog;

    //생성자를 만든다.
    public ItemViewHolder(@NonNull View itemView, Context context) {
        super(itemView);
        binding = LayoutFeedBinding.bind(itemView);
        this.context = context;
        nowUser = new PersonalD(context).getUserInfo();
    }

    //아이템을 세팅하는 메소드
    public void setItem(Feed item) {
        //피드에 삽입한 책
        Book book = item.getBook();
        binding.feedBookAuthor.setText(book.getAuthor());
        Glide.with(itemView).load(book.getImg_url()).into(binding.feedBookThumb); //책 썸네일 설정
        binding.feedBookTitle.setText(book.getTitle());
        binding.llbook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, search_fragment_subActivity_result.class);
                intent.putExtra("itemid", book.getItemId());
                context.startActivity(intent);
            }
        });
        //작성자 UserInfo
        UserInfo user = item.getCreator();
        binding.tvNickname.setText(user.getUsername());
        Glide.with(itemView).load(user.getProfileimg()).circleCrop().into(binding.ivProfileImage);
        //피드 내용

        dialog=new customDialog(context);
        dialog.setView(0);
        //댓글창을 클릭했을때
        binding.llComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.showcustomDialog(item,nowUser);
            }
        });
        //좋아요 수 세팅
        binding.tvLike.setText(String.valueOf(item.getLikeCount()));

        try {
            if (nowUser.getLikedPost().contains(item.getFeedID())) {
                binding.btnLike.setBackground(context.getDrawable(R.drawable.icon_like_red));
                liked = true;
            } else {
                binding.btnLike.setBackground(context.getDrawable(R.drawable.icon_like));
                liked = false;
            }
        } catch (NullPointerException e) {
            binding.btnLike.setBackground(context.getDrawable(R.drawable.icon_like));
            liked = false;
        }
        //좋아요 표시 관리
        binding.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controlLike(item);
            }
        });
        //이미지 뷰 정리
        if (item.getImgurl() != null) {
            Glide.with(itemView).load(item.getImgurl()).into(binding.feedImage);
        }
        binding.tvFeedtext.setText(item.getFeedText());
        setLabel(item.getLabel()); //라벨 세팅
    }

    public void setVisibillity(Boolean check) {
        if (check) binding.feedImage.setVisibility(View.VISIBLE);
        else {
            binding.feedImage.setImageResource(0);
            binding.feedImage.setVisibility(View.INVISIBLE);
        }
    }

    private void controlLike(Feed item) {
        if (limit < 5) {
            limit += 1;
            nowUser = new PersonalD(context).getUserInfo();
            strings = nowUser.getLikedPost();
            Map map = new HashMap();
            Integer likeCount = Integer.parseInt(binding.tvLike.getText().toString());

            if (!liked) {
                //현재 좋아요를 누르지 않은 상태
                likeCount += 1;
                liked = true;
                strings.add(item.getFeedID());
                binding.btnLike.setBackground(context.getDrawable(R.drawable.icon_like_red));
            } else {
                //현재 좋아요를 누른 상태
                likeCount -= 1;
                liked = false;
                strings.removeAll(Arrays.asList(item.getFeedID()));
                strings.remove(item.getFeedID());
                binding.btnLike.setBackground(context.getDrawable(R.drawable.icon_like));
            }
            nowUser.setLikedPost(strings);
            map.put("nowUser", nowUser);
            binding.tvLike.setText(String.valueOf(likeCount));
            map.put("liked", liked);
            new PersonalD(context).saveUserInfo(nowUser);
            new likeCounter().updateCounter(map, item.getFeedID());
        } else {
            new AlertDialog.Builder(context)
                    .setMessage("커뮤니티 활동 보호를 위해 잠시 후에 다시 시도해주세요")
                    .setPositiveButton("네", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
            if (!restricted) {
                restricted = true;
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        limit = 0;
                        restricted = false;
                    }
                }, 10000);
            }
        }
    }

    //라벨을 동적으로 생성
    private void setLabel(ArrayList<String> label) {
        binding.lllabel.removeAllViews(); //기존에 설정된 값을 초기화 시켜줌.
        int idx = label.indexOf("");
        for (int i = 0; i < idx; i++) {
            //뷰 생성
            TextView tv = new TextView(context);
            tv.setText(label.get(i)); //라벨에 텍스트 삽입
            tv.setBackground(context.getDrawable(R.drawable.label_design)); //디자인 적용
            tv.setBackgroundColor(Color.parseColor("#0F000000")); //배경색 적용
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT); //사이즈 설정
            params.setMargins(10, 0, 10, 0); //마진 설정
            tv.setLayoutParams(params); //설정값 뷰에 저장
            binding.lllabel.addView(tv); //레이아웃에 뷰 세팅
        }
    }
}