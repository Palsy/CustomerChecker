package palsy.customerchecker;

import android.app.Dialog;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by TedPark on 15. 12. 15..
 */
public class CallingService extends Service  {

    public static final String EXTRA_CALL_NUMBER = "call_number";
    protected View rootView;

    @InjectView(R.id.description)
    TextView description;

    String call_number;

    private WindowManager windowManager;
    WindowManager.LayoutParams params;

    @Override
    public IBinder onBind(Intent intent) {

        // Not used
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        rootView = layoutInflater.inflate(R.layout.call_popup_top, null);

        //Display display = windowManager.getDefaultDisplay();
        //int width = (int) (display.getWidth() * 0.9); //Display 사이즈의 90%

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        ButterKnife.inject(this, rootView);
        setDraggable();

        try{
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        }
        catch(Exception e) {
            e.printStackTrace();
            Log.d("TEST", "Exception message : " + e.getLocalizedMessage());
        }
    }

    private void setDraggable() {

        rootView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);

                        if (rootView != null)
                            windowManager.updateViewLayout(rootView, params);
                        return true;
                }
                return false;
            }
        });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        windowManager.addView(rootView, params);

        setExtra(intent);
        description.setText("모르는번호입니다.");

        /*
        if (!TextUtils.isEmpty(call_number)) {
            description.setText(call_number);
        }
        */
        return START_REDELIVER_INTENT;
    }


    private void setExtra(Intent intent) {

        if (intent == null) {
            removePopup();
            return;
        }

        call_number = intent.getStringExtra(EXTRA_CALL_NUMBER);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removePopup();
    }

    @OnClick(R.id.btn_close)
    public void removePopup() {
        if (rootView != null && windowManager != null) {
            windowManager.removeView(rootView);
        }
    }
}