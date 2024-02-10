package com.hicome.loveday;

import android.app.ApplicationErrorReport;
import android.net.Uri;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Collections;

public class PostViewholder extends RecyclerView.ViewHolder {

    ImageView imageViewprofile,iv_post;
    TextView tv_name,tv_desc,tv_likes,tv_comment,tv_time,tv_nameprofile;
    ImageButton likebtn,menuoptions,commentbtn;
    DatabaseReference likesref,commentref,blockref;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    int likescount =0 ,commentcount;
    CardView cardView;
    LinearLayout linearLayout;
    boolean isLikeProcessing = false;


    public PostViewholder(@NonNull View itemView) {
        super(itemView);
    }

    public void SetPost(FragmentActivity activity, String name, String url,String postUri,String time,
                        String uid,String type,String desc){

        imageViewprofile = itemView.findViewById(R.id.ivprofile_item);
        iv_post = itemView.findViewById(R.id.iv_post_item);
        tv_desc = itemView.findViewById(R.id.tv_desc_post);
        commentbtn = itemView.findViewById(R.id.commentbutton_posts);
        likebtn = itemView.findViewById(R.id.likebutton_posts);
        tv_likes = itemView.findViewById(R.id.tv_likes_post);
        menuoptions = itemView.findViewById(R.id.morebutton_posts);
        tv_time = itemView.findViewById(R.id.tv_time_post);
        tv_nameprofile = itemView.findViewById(R.id.tv_name_post);
        cardView = itemView.findViewById(R.id.cv_post);
        linearLayout = itemView.findViewById(R.id.ll_post);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentuid = user.getUid();

        blockref = database.getReference("Block users").child(currentuid);

        blockref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChild(uid)){
                    cardView.setVisibility(View.GONE);
                    linearLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        if (type.equals("iv")){

            Picasso.get().load(url).into(imageViewprofile);
            Picasso.get().load(postUri).into(iv_post);
            tv_desc.setText(desc);
            tv_time.setText(time);
            tv_nameprofile.setText(name);
        }
    }

    public void likeschecker(final String postkey) {
        likebtn = itemView.findViewById(R.id.likebutton_posts);


        likesref = database.getReference("post likes");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();

        likesref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child(postkey).hasChild(uid)) {
                    likebtn.setImageResource(R.drawable.ic_like);
                    likescount = (int) snapshot.child(postkey).getChildrenCount();
                    tv_likes.setText(Integer.toString(likescount) + " likes");
                } else {
                    likebtn.setImageResource(R.drawable.ic_dislike);
                    likescount = (int) snapshot.child(postkey).getChildrenCount();
                    tv_likes.setText(Integer.toString(likescount) + " likes");

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }




    public void commentchecker(final String postkey) {

        tv_comment = itemView.findViewById(R.id.tv_comment_post);


        commentref = database.getReference("All Posts").child(postkey).child("Comments");

        commentref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                commentcount = (int) snapshot.getChildrenCount();
                tv_comment.setText(Integer.toString(commentcount)+" Comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {


            }
        });

    }

}
