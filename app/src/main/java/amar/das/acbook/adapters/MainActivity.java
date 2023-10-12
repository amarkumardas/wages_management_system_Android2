package amar.das.acbook.adapters;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import amar.das.acbook.activity.InsertPersonDetailsActivity;
import amar.das.acbook.R;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    //https://www.youtube.com/watch?v=s1fW7CpiB9c
    //popupmenu will appear when we click vertical dots
    public void verticleDotsClick(View view) {
        PopupMenu popup =new PopupMenu(this,view);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.popuo_menu);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch(item.getItemId()){
            case R.id.insert_new:{
                        Intent intent = new Intent(MainActivity.this, InsertPersonDetailsActivity.class);
                        startActivity(intent);
                        break;
                       }
            case R.id.all_invoice:{
                             Toast.makeText(MainActivity.this, "Update button clicked", Toast.LENGTH_SHORT).show();
                             break;
                              }
        }
        return true;
    }
}