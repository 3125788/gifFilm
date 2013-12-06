package com.wz.gif;

import com.wz.gif.util.Log;
import com.wz.gif.view.MenuView;
import com.wz.gif.view.MenuView.OnMenuItemClickListener;
import com.wz.gif.view.MyListView;
import com.wz.gif.view.PopupModelView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class HomeActivity extends Activity {
    
    private Button btn_add;
    private LinearLayout virtualLayout;
//    private ImageButton  btn_add_normal;
//    private ImageButton btn_add_magic;
    private MyListView listView;
    private ImageView imageDel;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("welcome onCreate");
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.home);
        
        setUpViews();
        setUpListeners();
        
        listView.setDelImage(imageDel);
        listView.init();
    }
    
    //TODO:问题一：当用户想回到主页的时候要重新显示数据
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApp app = (MyApp) getApplication();
        app.removeAllMagicBitmap();
        app.removeAllBitmap();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("onRestart");
        listView.checkChange();
    }

    private void setUpViews() {
        virtualLayout = (LinearLayout)this.findViewById(R.id.virtualLayout);
        btn_add = (Button)this.findViewById(R.id.home_btn_add);
//        btn_add_normal = (ImageButton)findViewById(R.id.home_btn_add_normal);
//        btn_add_magic = (ImageButton)findViewById(R.id.home_btn_add_magic);
        listView = (MyListView)this.findViewById(R.id.home_list);
        imageDel = (ImageView)this.findViewById(R.id.home_image_del);
    }

    private void setUpListeners() {
        btn_add.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                PopupModelView popup = new PopupModelView(HomeActivity.this,virtualLayout);
                popup.show();
            }
        });
        
//        btn_add_magic.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ConstantInfo.magicMode = true;
//                Intent intent = new Intent(HomeActivity.this,CameraPreview.class);
//                startActivity(intent);
//            }
//        });
//        btn_add_normal.setOnClickListener(new View.OnClickListener() {
//            
//            @Override
//            public void onClick(View v) {
//                ConstantInfo.magicMode = false;
//                Intent intent = new Intent(HomeActivity.this,CameraPreview.class);
//                startActivity(intent);
//            }
//        });
//        
        listView.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                   Intent intent  = new Intent(HomeActivity.this,ShareActivity.class);
                   intent.putExtra("index", position);
                   startActivity(intent);
            }
        });
    }


    private MenuView mMenuView;
    private final int[] mImgRes = {R.drawable.common_menu_setting,R.drawable.common_menu_budget,R.drawable.common_menu_cancel};
    
    private void showMenu()
    {
        if (null == mMenuView)
        {
            mMenuView = new MenuView(this);
//          mMenuView.setWidth(300);
            mMenuView.setImageRes(mImgRes);
            mMenuView.setAnimStyle(R.style.popup_in_out);
            mMenuView.setBackgroundResource(R.drawable.bg01);
            mMenuView.setText(getResources().getStringArray(R.array.menu_text));
            mMenuView.setOnMenuItemClickListener(new OnMenuItemClickListener()
            {
                @Override
                public void onMenuItemClick(AdapterView<?> parent, View view,
                        int position)
                {
                    switch(position){
                        case 0:
                            Intent intent = new Intent(HomeActivity.this,SettingActivity.class);
                            startActivity(intent);        
                            break;
                        case 1:
                            Intent intent01 = new Intent(HomeActivity.this,AboutActivity.class);
                            startActivity(intent01);
                            break;
                        case 2:
                            finish();
                            break;
                    }
                }

                @Override
                public void hideMenu()
                {
                    mMenuView.dismiss();
                    mMenuView = null;
                }
                
            });
        }
        mMenuView.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_DOWN)
        {
            switch (keyCode)
            {
            case KeyEvent.KEYCODE_MENU:
                showMenu();
                break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}