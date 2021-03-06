package hk.edu.cuhk.ie.iems5722.group20.adapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.viewpager.widget.PagerAdapter;
import com.example.im.R;
import hk.edu.cuhk.ie.iems5722.group20.activity.ChatActivity;
import hk.edu.cuhk.ie.iems5722.group20.activity.CircleActivity;
import hk.edu.cuhk.ie.iems5722.group20.activity.LoginActivity;
import hk.edu.cuhk.ie.iems5722.group20.asynctask.HttpGetTask;
import hk.edu.cuhk.ie.iems5722.group20.asynctask.AddFriendRequest;
import hk.edu.cuhk.ie.iems5722.group20.entity.ChatRoom;
import hk.edu.cuhk.ie.iems5722.group20.entity.NewContacter;
import hk.edu.cuhk.ie.iems5722.group20.entity.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MyPagerAdapter extends PagerAdapter {

    private Context context;
    private List<String> contacters = new ArrayList<>();
    private List<NewContacter> newContacters = new ArrayList<>();
    private static UserInfo userInfo ;
    private static ArrayList<ChatRoom> chatRoomList = new ArrayList<>();


    List<View> lsViews = new ArrayList<>();//存储ViewPager需要的View

    //构造方法，用来传递View列表
    public MyPagerAdapter(List<View> lsViews,Context context) {

        this.lsViews = lsViews;
        this.context = context;
        userInfo = (UserInfo) context.getApplicationContext();
    }

    //更新视图数据
    public void Update(List<View> lsViews) {
        this.lsViews = lsViews;
    }

    //获得视图数量
    @Override
    public int getCount() {
        return lsViews.size();
    }

    //用来判断当前视图是否需要缓存
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    //翻页的时候移除之前的视图
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(lsViews.get(position));
    }

    //翻页的时候加载新的视图
    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        // init circles
        switch (position){
            case 0:
                View view_0 = lsViews.get(0);
                final ChatRoomAdapter chatRoomAdapter = new ChatRoomAdapter(context,R.layout.chatroom_list,chatRoomList);
                ListView listView = view_0.findViewById(R.id.chatroom_listview);
                listView.setAdapter(chatRoomAdapter);
                String url = "http://34.238.52.71/api/project/rooms?user_id="+userInfo.id;
                HttpGetTask httpGetTask = new HttpGetTask();
                try {
                    String s = httpGetTask.execute(url).get();
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    if(status.equals("ok")){
                        //continue
                        JSONArray array = jsonObject.getJSONArray("data");
                        chatRoomList.clear();
                        for(int i =0;i<array.length();i++){
                            int id = array.getJSONObject(i).getInt("user_id");
                            String nickname = array.getJSONObject(i).getString("nickname");
                            ChatRoom chatRoom = new ChatRoom(id,nickname);
                            chatRoomList.add(chatRoom);
                        }
                    }else{
                        System.out.println("错误");
                    }

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // 设置跳转到对应的ChatRoom
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ChatRoom chatRoom = (ChatRoom) parent.getItemAtPosition(position);

                        Intent i = new Intent(context, ChatActivity.class);
                        i.putExtra("target_user_id",String.valueOf(chatRoom.getId()));
                        i.putExtra("target_user_name",chatRoom.getChatroom());
                        context.startActivity(i);
                    }
                });



            case 1:

                View view_contact = lsViews.get(1);
                init_contact(view_contact);


                break;
                //
            case 2:
                View view = lsViews.get(2);

                Button button = view.findViewById(R.id.friends);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(context,CircleActivity.class);
                        context.startActivity(i);
                    }
                });
                break;
            case 3:
                View view1 = lsViews.get(3);
                TextView userName = view1.findViewById(R.id.nickname);
                ImageView portrait = view1.findViewById(R.id.protrait);
                portrait.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(context,"修改头像",Toast.LENGTH_SHORT).show();
                    }
                });
                userInfo = (UserInfo) context.getApplicationContext();
                userName.setText(userInfo.nickName);
                Button logout = view1.findViewById(R.id.logout);
                logout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(context, LoginActivity.class);
                        context.startActivity(i);
                    }
                });

            default:
                break;


        }
        container.addView(lsViews.get(position));

        return lsViews.get(position);
    }

    private void init_contact(View view_contact){

        // 初始化 新增列表


        NewContacterAdapter newContacterAdapter = new NewContacterAdapter(context,R.layout.item_new_friend_notification,newContacters);
        ListView listView = view_contact.findViewById(R.id.new_contact_list);
        listView.setAdapter(newContacterAdapter);

        // 添加好友

        ImageView add_friend = view_contact.findViewById(R.id.add_friend);
        add_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText added_user = new EditText(context);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("输入要添加的好友昵称").setIcon(android.R.drawable.ic_dialog_info).setView(added_user)
                        .setNegativeButton("Cancel", null);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        String resultMessage = "请求失败";

                        String receive_user = added_user.getText().toString();
                        if(!"".equals(receive_user)){
                            resultMessage = sendFriendRequest(receive_user);
                        }

                        AlertDialog.Builder response = new AlertDialog.Builder(context);
                        response.setTitle(resultMessage).setNegativeButton("OK",null);
                        response.show();
//                        System.out.println(added_user.getText().toString());
                        // 获得要添加的好友昵称。

                    }
                });
                builder.show();
            }
        });



        // 好友请求列表
        userInfo = (UserInfo) context.getApplicationContext();

        String url = "http://34.238.52.71/api/project/get_friend_request?user_id="+userInfo.id;
        HttpGetTask getFriendsRequest = new HttpGetTask();

        try {
            String s = getFriendsRequest.execute(url).get();

            JSONObject jsonObject = new JSONObject(s);
            String status = jsonObject.getString("status");
            if("ok".equals(status)){

                JSONArray data = jsonObject.getJSONArray("data");
                newContacters.clear();
                for(int i=0;i<data.length();i++){
                    newContacters.add(new NewContacter(data.getJSONObject(i).getString("nickname")));
                }

            }else {
                System.out.println("获取数据错误");
            }

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        // 初始化联系人列表

        url = "http://34.238.52.71/api/project/get_friends?user_id="+userInfo.id;
        HttpGetTask getFriends = new HttpGetTask();
        try {
            String s = getFriends.execute(url).get();

            JSONObject jsonObject = new JSONObject(s);
            String status = jsonObject.getString("status");
            if("ok".equals(status)){

                JSONArray data = jsonObject.getJSONArray("data");
                contacters.clear();
                for(int i=0;i<data.length();i++){
                    contacters.add(data.getJSONObject(i).getString("nickname"));
                }

            }else {
                System.out.println("获取数据错误");
            }

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        ListView listView2 =  view_contact.findViewById(R.id.contacter);//在视图中找到ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,contacters);//新建并配置ArrayAapeter
        listView2.setAdapter(adapter);

    }

//    private void openAlbum(){
//        Intent intent = new Intent("android.intent.action.GET_CONTENT");
//        intent.setType("image/*");
//        context.startActivityFor;
//    }

    private String sendFriendRequest(String receive_nickname){
        String URLString = "http://34.238.52.71/api/project/add_friend";
        String[] parameters = new String[]{URLString,String.valueOf(userInfo.id),receive_nickname};
        String result="";
        AddFriendRequest newFriend = new AddFriendRequest();
        try {
            String s = newFriend.execute(parameters).get();
            JSONObject jsonObject = new JSONObject(s);
            String status = jsonObject.getString("status");
            switch (status){
                case "0":
                    result = "没有该用户";
                    break;
                case "1":
                    result = "已发送请求";
                    break;
                case "2":
                    result = "TA已是您的好友";
                    break;
                default:
                    break;
            }


            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        return result;




    }



}
