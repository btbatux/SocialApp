package com.hicome.loveday;

import android.app.Application;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class Viewholder_Question extends RecyclerView.ViewHolder {

    ImageView imageView;
    TextView time_result,name_result,question_result,deletebtn,replybtn,replybtn1;
    ImageButton fvrt_btn;
    DatabaseReference favouriteref,blockref;
    CardView cv_question;
    LinearLayout ll_question;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();



    public Viewholder_Question(@NonNull View itemView) {
        super(itemView);
    }

   public void setitem(FragmentActivity activity,String name,String url,String userid,String key,String question,String privacy,
                  String time,String category){

        time_result = itemView.findViewById(R.id.time_que_item_tv);
        name_result = itemView.findViewById(R.id.name_que_item_tv);
        question_result = itemView.findViewById(R.id.que_item_tv);
        imageView = itemView.findViewById(R.id.iv_que_item);
         replybtn = itemView.findViewById(R.id.reply_item_que);
         cv_question = itemView.findViewById(R.id.cv_question);
         ll_question = itemView.findViewById(R.id.ll_questions);

       FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
       String currentuid = user.getUid();
       DocumentReference userRef = db.collection("user").document(userid); // "Users" koleksiyonunuzun adı ve kullanıcının ID'si

       blockref = database.getReference("Block users").child(currentuid);

       blockref.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {

               if (snapshot.hasChild(userid)){
                   cv_question.setVisibility(View.GONE);
                   ll_question.setVisibility(View.GONE);
               }else {
                   userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                       @Override
                       public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                           if (task.isSuccessful()) {
                               DocumentSnapshot document = task.getResult();
                               if (document != null && document.exists()) {
                                   // Kullanıcının profil resmi URL'sini al
                                   String profileImageUrl = document.getString("url"); // "profileImageUrl" alan adınız
                                   if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                       // Picasso ile resmi ImageView'e yükle
                                       Picasso.get().load(profileImageUrl).into(imageView);
                                   }
                               } else {
                                   // Doküman yoksa veya başka bir hata oluştuysa
                                   // Varsayılan bir resim kullanabilirsiniz veya hata yönetimi yapabilirsiniz
                                   imageView.setImageResource(R.drawable.usericon); // Varsayılan avatar resmi
                               }
                           } else {
                               // Sorgu başarısız olduysa
                               Log.d("Firestore", "Error getting documents: ", task.getException());
                           }
                       }
                   });
                   name_result.setText(name);
                   question_result.setText(question);
                   time_result.setText(time);
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });




   }
    public void favouriteChecker(final String postkey) {
        fvrt_btn = itemView.findViewById(R.id.fvrt_f2_item);


        favouriteref = database.getReference("favourites");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();

        favouriteref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child(postkey).hasChild(uid)){
                    fvrt_btn.setImageResource(R.drawable.ic_baseline_turned_in_24);
                }else {
                    fvrt_btn.setImageResource(R.drawable.ic_baseline_turned_in_not_24);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    public void setitemRelated(Application activity, String name, String url, String userid, String key, String question, String privacy,
                               String time){

       TextView timetv = itemView.findViewById(R.id.related_time_que_item_tv);
       ImageView imageView = itemView.findViewById(R.id.related_iv_que_item);
       TextView nametv = itemView.findViewById(R.id.related_name_que_item_tv);
       TextView quetv = itemView.findViewById(R.id.related_que_item_tv);
        replybtn1= itemView.findViewById(R.id.related_reply_item_que);

       Picasso.get().load(url).into(imageView);
       nametv.setText(name);
       timetv.setText(time);
       quetv.setText(question);



    }


    public void setitemdelete(Application activity, String name, String url, String userid, String key, String question, String privacy,
                               String time){


        TextView timetv = itemView.findViewById(R.id.del_time_que_item_tv);
        ImageView imageView = itemView.findViewById(R.id.delete_iv_que_item);
        TextView nametv = itemView.findViewById(R.id.del_name_que_item_tv);
        TextView quetv = itemView.findViewById(R.id.del_que_item_tv);
         deletebtn= itemView.findViewById(R.id.delete_item_que_tv);

        Picasso.get().load(url).into(imageView);
        nametv.setText(name);
        timetv.setText(time);
        quetv.setText(question);



    }
}














