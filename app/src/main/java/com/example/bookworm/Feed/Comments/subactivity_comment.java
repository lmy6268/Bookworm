package com.example.bookworm.Feed.Comments;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.bookworm.Feed.items.Feed;
import com.example.bookworm.Search.items.Book;
import com.example.bookworm.User.UserInfo;
import com.example.bookworm.databinding.SubactivityCommentBinding;
import com.example.bookworm.modules.FBModule;
import com.example.bookworm.modules.personalD.PersonalD;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class subactivity_comment extends AppCompatActivity {
    SubactivityCommentBinding binding;
    Context context;
    Feed item;
    UserInfo nowUser;
    final int LIMIT = 10;
    int page = 1;
    FBModule fbModule;
    private Map map;
    CommentAdapter commentAdapter;
    ArrayList<Comment> commentList;
    private Boolean isLoading = false, canLoad = true;
    DocumentSnapshot lastVisible = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = SubactivityCommentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        item = (Feed) getIntent().getSerializableExtra("item");
        nowUser = new PersonalD(this).getUserInfo();
        fbModule = new FBModule(context);
        setItems();
        binding.btnWriteComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addComment();
            }
        });
    }

    private void setItems() {
        initComment();
//        //책 표시
//        Book book = item.getBook();
//        binding.feedBookTitle.setText(book.getTitle());
//        Glide.with(this).load(book.getImg_url()).into(binding.feedBookThumb);
//        binding.feedBookAuthor.setText(book.getAuthor());
        //피드 요약
//        if (item.getImgurl() != "") Glide.with(this).load(item.getImgurl()).into(binding.feedImage);
//        else  binding.feedImage.setVisibility(View.INVISIBLE);
        binding.tvFeedtext.setText(item.getFeedText());
        //댓글 표시
        loadData();
    }
    //피드 초기화
    private void initComment() {
        isLoading = true;
        page = 1;
        canLoad = false;
        lastVisible = null;
        commentList = new ArrayList<>(); //댓글을 담는 리스트 생성
    }

    //리사이클러뷰 스크롤 초기화
    private void initScrollListener() {
        binding.mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
                if (!isLoading) {
                    try {
                        if (layoutManager != null && lastVisibleItemPosition == commentAdapter.getItemCount() - 1) {
                            commentAdapter.deleteLoading();

                            //이전에 가져왔던 자료를 인자로 보내주어 그 다음 자료부터 조회한다.
                            if (lastVisible != null) {
                                map.put("lastVisible", lastVisible);
                                fbModule.readData(1, map, null);   //쿼리를 보내어, 데이터를 조회한다.
                            }
                            isLoading = true;//현재 로딩을 끝냄을 알린다.
                        }
                    } catch (NullPointerException e) {

                    }
                }
            }
        });

    }

    //리사이클러뷰를 초기화
    private void initRecyclerView() {
        binding.mRecyclerView.setAdapter(commentAdapter);
        initScrollListener(); //무한스크롤
    }

    private void initAdapter() {
        commentAdapter = new CommentAdapter(commentList, context);
    }

    private void replaceItem(ArrayList newthings) {
        DiffUtilCallback callback = new DiffUtilCallback(commentList, newthings);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(callback);
        commentList.clear();
        commentList.addAll(newthings);
        diffResult.dispatchUpdatesTo(commentAdapter);
    }

    private void addComment() {
        Map<String, Object> data = new HashMap<>();
        //유저정보, 댓글내용, 작성시간
        Comment comment = new Comment();
        comment.getData(nowUser, binding.edtComment.getText().toString(), System.currentTimeMillis());
        data.put("comment", comment);
        //입력한 댓글 화면에 표시하기
        ArrayList<Comment> a = new ArrayList<>(commentList);
        a.add(0, comment);
        replaceItem(a);
        new commentsCounter().updateCounter(data, context, item.getFeedID());
        //키보드 내리기
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.edtComment.getWindowToken(), 0);
        binding.edtComment.clearFocus();
        binding.edtComment.setText(null);
    }





    public void setLastVisible(DocumentSnapshot a) {
        this.lastVisible = a;
    }

    private void loadData() {
        map = new HashMap();
        if (map.get("lastVisible") != null) map.remove("lastVisible");
        map.put("FeedID", item.getFeedID());
        fbModule.setLIMIT(LIMIT);
        fbModule.readData(1, map, null);
    }

    public void moduleUpdated(List<DocumentSnapshot> a) {
        ArrayList<Comment> newList = new ArrayList(commentList);
        if (a == null) {
            if (page == 1) {
                initComment();
                initAdapter();
            }
            else{
                canLoad=false;
                isLoading=true;
            }
        } else {
            if (page == 1) {
                isLoading = false;
                commentList = new ArrayList<>(); //챌린지를 담는 리스트 생성
            }
            //가져온 데이터를 for문을 이용하여, feed리스트에 차곡차곡 담는다.
            try {
                for (DocumentSnapshot snapshot : a) {
                    Map data = snapshot.getData();
                    Comment item = new Comment();
                    item.setData(data);
                    newList.add(item);
                }
                //가져온 값의 마지막 snapshot부터 이어서 가져올 수 있도록 하기 위함.
                lastVisible = a.get(a.size() - 1);
                //리사이클러뷰에서 튕김현상이 발생하여 넣어준 코드
                //현재 불러오는 값의 크기(a.size())가 페이징 제한 값(LIMIT)보다 작은 경우 => 더이상 불러오지 않게 함.
                canLoad=true;
                if (a.size() < LIMIT ) {
                    canLoad = false;
                }
            } catch (NullPointerException e) {
                canLoad = false;
            }
        }
        //만약 더이상 불러오지 못 할 경우
        if (canLoad == false) {
            isLoading = true;
            if (page > 1) {
                replaceItem(newList); //이미 불러온 데이터가 있는 경우엔 가져온 데이터 만큼의 범위를 늘려준다.
            }

            else { //없는 경우엔 새로운 어댑터에 데이터를 담아서 띄워준다.
                initAdapter(); //어댑터 초기화
                replaceItem(newList);//데이터 범위 변경
                initRecyclerView(); //리사이클러뷰에 띄워주기
            }
       }
        //더 불러올 데이터가 있는 경우
        else {
            newList.add(new Comment()); //로딩바 표시를 위한 빈 값
            if (page > 1) {
                isLoading = false;
                replaceItem(newList);//데이터 범위 변경
            } else {
                initAdapter();//어댑터 초기화
                replaceItem(newList);//데이터 범위 변경
                initRecyclerView(); //리사이클러뷰에 띄워주기
            }
            page++; //로딩을 다하면 그다음 페이지로 넘어간다.
        }
    }
}
