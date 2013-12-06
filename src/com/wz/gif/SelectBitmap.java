package com.wz.gif;

import com.wz.gif.util.ConstantInfo;
import com.wz.gif.util.Log;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * 选择需要制作的图片
 * @author wuzhi
 *
 */
public class SelectBitmap extends BaseActivity{

    private Button btn_ok;
    private Button btn_select_all;
    private GridView gridView;
    
    private MyApp app ;
    private ArrayList<Bitmap> bitmaps;
    private GridAdapter adapter;
    
    private int currentNum = -1;
    
    //数组用来保存哪个选择了哪张图片
    private int[] selectArray ;
    //记录选择了多少张图
    private int imageCount = 0;
    private boolean isAllSelect = false;
    
    @Override
    public void _onCreate() {
        app = (MyApp)getApplication();
        bitmaps = app.bitmaps;
        Log.d("bitmap has "+ bitmaps.size());
        adapter = new GridAdapter(this);
        selectArray = new int[bitmaps.size()];
        gridView.setAdapter(adapter);
    }
    
    @Override
    public int getContentViewId() {
        return R.layout.select_bitmap;
    }

    @Override
    public void setUpViews() {
        btn_ok = (Button)this.findViewById(R.id.btn_ok);
        btn_select_all =(Button)this.findViewById(R.id.btn_all_selected);
        gridView = (GridView)this.findViewById(R.id.select_gridview);
    }

    @Override
    public void setUpListeners() {
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageCount <= 0){
                    Toast.makeText(SelectBitmap.this, "您还没有选择要制作图片", Toast.LENGTH_SHORT).show();
                }else{
                    app.setSelectArray(selectArray);
//                    for(int i = 0;i<selectArray.length;i++){
//                        if(selectArray[i] == 0){
//                            Log.d("reomve "+ i);
//                            app.removeBitmap(i);
//                        }
//                    }
                    if(!ConstantInfo.magicMode){
                        Intent intent = new Intent(SelectBitmap.this,GifMaker.class);
                        startActivity(intent);
                    }else{
                        //如果选择了制作GIF魔术照片的话，这里应该首先制作GIF图，在后面gif图片和背景叠加的时候效果可以马上展现给用户
                        makeGif();
                        Intent intent = new Intent(SelectBitmap.this,MagicGifReview.class);
                        startActivity(intent);
                    }
                    //TODO：这里要不要finish,是选择结束这个activity还是用户可以重新回来选择图片
                }
            }
        });
        
        btn_select_all.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if(!isAllSelect){
                    for(int i = 0;i<selectArray.length;i++){
                        selectArray[i] = 1;
                    }
                    imageCount = selectArray.length;
                }
                else{
                    for(int i = 0;i<selectArray.length;i++){
                        selectArray[i] = 0;
                    }
                    imageCount = 0;
                }
                isAllSelect = !isAllSelect;
                adapter.notifyDataSetChanged();
            }
        });
        
        gridView.setOnItemClickListener(new OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(selectArray[position] == 0){
//                    Log.d("image "+position +" selected");
                    selectArray[position] = 1;
                    imageCount++;
                }else{
                    selectArray[position] = 0;
//                    Log.d("image "+position +" did not selected");
                    imageCount--;
                }
                currentNum = position;
                adapter.notifyDataSetChanged();
            }
            
        });
    }
    
    /**
     * 制作GIF图片
     */
    protected void makeGif() {
        
    }

    class GridAdapter extends BaseAdapter{

        
        private Context context;
        private LayoutInflater inflater;
        
        public GridAdapter(Context cxt){
            this.context = cxt;
            inflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount() {
            return bitmaps.size();
        }

        @Override
        public Object getItem(int position) {
            return bitmaps.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            //如果用传统推荐的写法虽然效率上提高了，但是不能满足我们的要求，因为它会重用
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.grid_item_select, null);
            holder.itemImage = (ImageView)convertView.findViewById(R.id.grid_item_image);
            holder.itemSelect = (ImageView)convertView.findViewById(R.id.grid_item_select);
            if(selectArray[position] == 1){
                Log.d(currentNum + " ");
                holder.itemSelect.setImageResource(R.drawable.btn_check_on_normal);
            }else{
                holder.itemSelect.setImageResource(R.drawable.btn_check_off_normal);
            }
            holder.itemImage.setImageBitmap(bitmaps.get(position));
            return convertView;
        }
        class ViewHolder{
            ImageView itemSelect;
            ImageView itemImage;
        }
        
    }
}
