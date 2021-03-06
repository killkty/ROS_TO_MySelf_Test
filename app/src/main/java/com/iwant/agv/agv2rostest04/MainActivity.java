package com.iwant.agv.agv2rostest04;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.helper.NavHelper;
import com.jilk.ros.ROSClient;
import com.jilk.ros.rosbridge.ROSBridgeClient;
import com.map.WayPointUtil;
import com.model.InitPoseResult;
import com.nav.NavPublich;
import com.nav.TMove_Base_Goal;
import com.service.PostionMonitorService;

import net.whsgzcy.rosclient.RCApplication;
import net.whsgzcy.rosclient.entity.PublishEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "whsgzcy";

    private EditText mIPEdit;
    private Button mConnectBtn;
    private Button mUpBtn, mDownBtn, mLeftBtn, mRightBtn, mStopBtn;
    private Button mTestBtn;
    private Button mPowerBtn;
    private Button mUnPowerBtn;

    private ROSBridgeClient client;

    private String mWSURL;

    private Button mState;
    private TextView mStateTextView;

    // 导航publish
    private NavPublich mNavPublich = new NavPublich();

    private PostionMonitorService.MyBinder binder;
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (PostionMonitorService.MyBinder) service;
            binder.getPoi(client);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 2:
                    break;
                // 连接成功 获取设备信息
                case 1:
                    break;
                case 3:
                    mNavPointName = "map_4_A_400";
                    mNavPointState = 1;
                    client.send(new Gson().toJson(mNavPublich.getNavPublishHashMap().get("map_4_A_400")));
                    mPointName = "map_4_A_400";
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);

        mIPEdit = (EditText) findViewById(R.id.ip_edit);
        mConnectBtn = (Button) findViewById(R.id.connect_btn);
        mConnectBtn.setOnClickListener(this);

        mUpBtn = (Button) findViewById(R.id.up_btn);
        mDownBtn = (Button) findViewById(R.id.down_btn);
        mLeftBtn = (Button) findViewById(R.id.left_btn);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mStopBtn = (Button) findViewById(R.id.stay_btn);

        mTestBtn = (Button) findViewById(R.id.ansy_data);
        mTestBtn.setOnClickListener(this);

        // 获取电量按钮
        mPowerBtn = (Button) findViewById(R.id.power);
        mPowerBtn.setOnClickListener(this);

        mUnPowerBtn = (Button) findViewById(R.id.unpower);
        mUnPowerBtn.setOnClickListener(this);

        mIPEdit.setText("ws://192.168.0.9:9090");

        mUpBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        processMoveTopic(1, 1);
                        break;
                    case MotionEvent.ACTION_UP:
                        processStopTopic();
                        break;
                }
                return false;
            }
        });

        mDownBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        client.send("{\"op\":\"publish\",\"topic\":\"/cmd_vel\",\"msg\":" +
                                "{\"linear\":{\"x\":" + -1 + ",\"y\":0,\"z\":0},\"angular\":{\"x\":0,\"y\":0,\"z\":" + -1 + "}}}");
                        break;
                    case MotionEvent.ACTION_UP:
                        processStopTopic();
                        break;
                }
                return false;
            }
        });

        mLeftBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        processMoveTopic(0, 1);
                        break;
                    case MotionEvent.ACTION_UP:
                        processStopTopic();
                        break;
                }
                return false;
            }
        });

        mRightBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        client.send("{\"op\":\"publish\",\"topic\":\"/cmd_vel\",\"msg\":" +
                                "{\"linear\":{\"x\":" + 0 + ",\"y\":0,\"z\":0},\"angular\":{\"x\":-1,\"y\":0,\"z\":" + -1 + "}}}");
                        break;
                    case MotionEvent.ACTION_UP:
                        processStopTopic();
                        break;
                }
                return false;
            }
        });

        mStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processStopTopic();
            }
        });


        Button ceshi = (Button) findViewById(R.id.ceshi);
        ceshi.setOnClickListener(this);

        Button cmd_vel = (Button) findViewById(R.id.cmd_vel);
        cmd_vel.setOnClickListener(this);

        Button nav_ctrl0 = (Button) findViewById(R.id.nav_ctrl_0);
        nav_ctrl0.setOnClickListener(this);
        Button nav_ctrl1 = (Button) findViewById(R.id.nav_ctrl_1);
        nav_ctrl1.setOnClickListener(this);
        Button nav_ctrl2 = (Button) findViewById(R.id.nav_ctrl_2);
        nav_ctrl2.setOnClickListener(this);
        // 导航状态
        mState = (Button) findViewById(R.id.nav_state);
        mState.setOnClickListener(this);
        mStateTextView = (TextView) findViewById(R.id.nav_state_text);

        // 暂停接收导航状态
        Button stop_nav_state_btn = (Button) findViewById(R.id.stop_nav_state);
        stop_nav_state_btn.setOnClickListener(this);

        // 导航
        Button n_400 = (Button) findViewById(R.id.nav_400_go);
        Button n_400_ = (Button) findViewById(R.id.nav_400_go_go);
        Button n_400_init = (Button) findViewById(R.id.nav_400_init);
        Button n_400_init_ = (Button) findViewById(R.id.nav_400_init_);
        n_400.setOnClickListener(this);
        n_400_.setOnClickListener(this);
        n_400_init.setOnClickListener(this);
        n_400_init_.setOnClickListener(this);

        // 导航
        Button n_401 = (Button) findViewById(R.id.nav_401_go);
        Button n_401_ = (Button) findViewById(R.id.nav_401_go_go);
        Button n_401_init = (Button) findViewById(R.id.nav_401_init);
        Button n_401_init_ = (Button) findViewById(R.id.nav_401_init_);
        n_401.setOnClickListener(this);
        n_401_.setOnClickListener(this);
        n_401_init.setOnClickListener(this);
        n_401_init_.setOnClickListener(this);

        Button n_402 = (Button) findViewById(R.id.nav_402_go);
        Button n_402_ = (Button) findViewById(R.id.nav_402_go_go);
        Button n_402_init = (Button) findViewById(R.id.nav_402_init);
        Button n_402_init_ = (Button) findViewById(R.id.nav_402_init_);
        n_402.setOnClickListener(this);
        n_402_.setOnClickListener(this);
        n_402_init.setOnClickListener(this);
        n_402_init_.setOnClickListener(this);

        Button n_403 = (Button) findViewById(R.id.nav_403_go);
        Button n_403_ = (Button) findViewById(R.id.nav_403_go_go);
        Button n_403_init = (Button) findViewById(R.id.nav_403_init);
        Button n_403_init_ = (Button) findViewById(R.id.nav_403_init_);
        n_403.setOnClickListener(this);
        n_403_.setOnClickListener(this);
        n_403_init.setOnClickListener(this);
        n_403_init_.setOnClickListener(this);

        Button n_404 = (Button) findViewById(R.id.nav_404_go);
        Button n_404_ = (Button) findViewById(R.id.nav_404_go_go);
        Button n_404_init = (Button) findViewById(R.id.nav_404_init);
        Button n_404_init_ = (Button) findViewById(R.id.nav_404_init_);
        n_404.setOnClickListener(this);
        n_404_.setOnClickListener(this);
        n_404_init.setOnClickListener(this);
        n_404_init_.setOnClickListener(this);

        Button charge = (Button) findViewById(R.id.test_navi_f);
        charge.setOnClickListener(this);

        Button poiBtn = (Button) findViewById(R.id.nav_poi);
        poiBtn.setOnClickListener(this);

        Button masterBtn = (Button) findViewById(R.id.map_master);
        masterBtn.setOnClickListener(this);

        Button yuBtn = (Button) findViewById(R.id.map_yu);
        yuBtn.setOnClickListener(this);

        Button initPose = (Button) findViewById(R.id.initpose);
        initPose.setOnClickListener(this);

//        Button poiCancelBtn = (Button) findViewById(R.id.nav_poi_cancel);
//        poiCancelBtn.setOnClickListener(this);

        Button gmapping_pose = (Button) findViewById(R.id.gmapping_pose);
        gmapping_pose.setOnClickListener(this);

        Button save_map = (Button) findViewById(R.id.save_map);
        save_map.setOnClickListener(this);

        Button load_map = (Button) findViewById(R.id.load_map);
        load_map.setOnClickListener(this);

        Button save_map_edit = (Button) findViewById(R.id.save_map_edit);
        save_map_edit.setOnClickListener(this);

        // 注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.iwant.action");
        registerReceiver(mBroadCastReceive, intentFilter);
    }

    String mPointName = null;

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            // 连接 IP
            case R.id.connect_btn:
                mWSURL = mIPEdit.getText().toString().trim();
                connect(mWSURL);
                break;
            case R.id.nav_poi:
                mNavPointName = "map_4_A_400";
                mNavPointState = 1;
                mPointName = "map_4_A_400";
                client.send(new Gson().toJson(mNavPublich.getNavPublishHashMap().get("map_4_A_400")));
                client.send(NavHelper.nav(1, "map_4_A_400"));
//                if (binder == null) {
//                    // 开始导航
//                    Intent intent = new Intent(this, PostionMonitorService.class);
//                    bindService(intent, conn, BIND_AUTO_CREATE);
//                }
                break;
//            case R.id.nav_poi_cancel:
//                unbindService(conn);
//                break;
            case R.id.nav_400_go:
                mNavPointName = "map_4_A_400";
                mNavPointState = 1;
                mPointName = "map_4_A_400";
                client.send(new Gson().toJson(mNavPublich.getNavPublishHashMap().get("map_4_A_400")));
                client.send(NavHelper.nav(1, "map_4_A_400"));
                break;
            case R.id.nav_400_go_go:
                mNavPointName = "map_4_A_400_map";
                mNavPointState = 1;
                mPointName = "map_4_A_400_map";
                client.send(new Gson().toJson(mNavPublich.getNavPublishHashMap().get("map_4_A_400_map")));
                client.send(NavHelper.nav(1, "map_4_A_400_map"));
                break;
            case R.id.nav_400_init:
                TMove_Base_Goal o = mNavPublich.getNavPublishHashMap().get("map_4_A_400");
                InitPoseResult oi = new InitPoseResult();

                InitPoseResult.MsgBean msgbean = new InitPoseResult.MsgBean();

                InitPoseResult.MsgBean.PoseBeanX p = new InitPoseResult.MsgBean.PoseBeanX();
                p.setCovariance();

                InitPoseResult.MsgBean.PoseBeanX.PoseBean pp = new InitPoseResult.MsgBean.PoseBeanX.PoseBean();

                InitPoseResult.MsgBean.PoseBeanX.PoseBean.PositionBean ppp = new InitPoseResult.MsgBean.PoseBeanX.PoseBean.PositionBean();

                ppp.setX(o.getMsg().getGoal().getTarget_pose().getPose().getPosition().getX());
                ppp.setY(o.getMsg().getGoal().getTarget_pose().getPose().getPosition().getY());
                ppp.setZ(o.getMsg().getGoal().getTarget_pose().getPose().getPosition().getZ());

                InitPoseResult.MsgBean.PoseBeanX.PoseBean.OrientationBean ppo = new InitPoseResult.MsgBean.PoseBeanX.PoseBean.OrientationBean();

                ppo.setW(o.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getW());
                ppo.setX(o.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getX());
                ppo.setY(o.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getY());
                ppo.setZ(o.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getZ());

                pp.setPosition(ppp);
                pp.setOrientation(ppo);
                p.setPose(pp);

                msgbean.setPose(p);
                oi.setMsg(msgbean);

                client.send(new Gson().toJson(oi));
                break;
            case R.id.nav_400_init_:
                TMove_Base_Goal o_ = mNavPublich.getNavPublishHashMap().get("map_4_A_400_map");
                InitPoseResult oi_ = new InitPoseResult();
                InitPoseResult.MsgBean msgbean_ = new InitPoseResult.MsgBean();
                InitPoseResult.MsgBean.PoseBeanX p_ = new InitPoseResult.MsgBean.PoseBeanX();
                p_.setCovariance();
                InitPoseResult.MsgBean.PoseBeanX.PoseBean pp_ = new InitPoseResult.MsgBean.PoseBeanX.PoseBean();
                InitPoseResult.MsgBean.PoseBeanX.PoseBean.PositionBean ppp_ = new InitPoseResult.MsgBean.PoseBeanX.PoseBean.PositionBean();
                ppp_.setX(o_.getMsg().getGoal().getTarget_pose().getPose().getPosition().getX());
                ppp_.setY(o_.getMsg().getGoal().getTarget_pose().getPose().getPosition().getY());
                ppp_.setZ(o_.getMsg().getGoal().getTarget_pose().getPose().getPosition().getZ());
                InitPoseResult.MsgBean.PoseBeanX.PoseBean.OrientationBean ppo_ = new InitPoseResult.MsgBean.PoseBeanX.PoseBean.OrientationBean();
                ppo_.setW(o_.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getW());
                ppo_.setX(o_.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getX());
                ppo_.setY(o_.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getY());
                ppo_.setZ(o_.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getZ());
                pp_.setPosition(ppp_);
                pp_.setOrientation(ppo_);
                p_.setPose(pp_);
                msgbean_.setPose(p_);
                oi_.setMsg(msgbean_);
                client.send(new Gson().toJson(oi_));
                break;
            case R.id.nav_401_go:
//                mNavPointName = "map_4_A_401";
//                mNavPointState = 1;
//                mPointName = "map_4_A_401";
//                client.send(new Gson().toJson(mNavPublich.getNavPublishHashMap().get("map_4_A_401")));
                client.send(NavHelper.nav(1, "map_A"));
                break;
            case R.id.nav_401_go_go:
                mNavPointName = "map_4_A_401_map";
                mNavPointState = 1;
                mPointName = "map_4_A_401_map";
                client.send(new Gson().toJson(mNavPublich.getNavPublishHashMap().get("map_4_A_401_map")));
                client.send(NavHelper.nav(1, "map_4_A_401_map"));
                break;
            case R.id.nav_401_init:
                TMove_Base_Goal ao = mNavPublich.getNavPublishHashMap().get("map_4_A_401");
                InitPoseResult aoi = new InitPoseResult();
                InitPoseResult.MsgBean amsgbean = new InitPoseResult.MsgBean();
                InitPoseResult.MsgBean.PoseBeanX ap = new InitPoseResult.MsgBean.PoseBeanX();
                ap.setCovariance();
                InitPoseResult.MsgBean.PoseBeanX.PoseBean app = new InitPoseResult.MsgBean.PoseBeanX.PoseBean();
                InitPoseResult.MsgBean.PoseBeanX.PoseBean.PositionBean appp = new InitPoseResult.MsgBean.PoseBeanX.PoseBean.PositionBean();
                appp.setX(ao.getMsg().getGoal().getTarget_pose().getPose().getPosition().getX());
                appp.setY(ao.getMsg().getGoal().getTarget_pose().getPose().getPosition().getY());
                appp.setZ(ao.getMsg().getGoal().getTarget_pose().getPose().getPosition().getZ());
                InitPoseResult.MsgBean.PoseBeanX.PoseBean.OrientationBean appo = new InitPoseResult.MsgBean.PoseBeanX.PoseBean.OrientationBean();
                appo.setW(ao.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getW());
                appo.setX(ao.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getX());
                appo.setY(ao.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getY());
                appo.setZ(ao.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getZ());
                app.setPosition(appp);
                app.setOrientation(appo);
                ap.setPose(app);
                amsgbean.setPose(ap);
                aoi.setMsg(amsgbean);
                client.send(new Gson().toJson(aoi));
                break;
            case R.id.nav_401_init_:
                TMove_Base_Goal ao_ = mNavPublich.getNavPublishHashMap().get("map_4_A_401_map");
                InitPoseResult aoi_ = new InitPoseResult();
                InitPoseResult.MsgBean amsgbean_ = new InitPoseResult.MsgBean();
                InitPoseResult.MsgBean.PoseBeanX ap_ = new InitPoseResult.MsgBean.PoseBeanX();
                ap_.setCovariance();
                InitPoseResult.MsgBean.PoseBeanX.PoseBean app_ = new InitPoseResult.MsgBean.PoseBeanX.PoseBean();
                InitPoseResult.MsgBean.PoseBeanX.PoseBean.PositionBean appp_ = new InitPoseResult.MsgBean.PoseBeanX.PoseBean.PositionBean();
                appp_.setX(ao_.getMsg().getGoal().getTarget_pose().getPose().getPosition().getX());
                appp_.setY(ao_.getMsg().getGoal().getTarget_pose().getPose().getPosition().getY());
                appp_.setZ(ao_.getMsg().getGoal().getTarget_pose().getPose().getPosition().getZ());
                InitPoseResult.MsgBean.PoseBeanX.PoseBean.OrientationBean appo_ = new InitPoseResult.MsgBean.PoseBeanX.PoseBean.OrientationBean();
                appo_.setW(ao_.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getW());
                appo_.setX(ao_.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getX());
                appo_.setY(ao_.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getY());
                appo_.setZ(ao_.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getZ());
                app_.setOrientation(appo_);
                ap_.setPose(app_);
                amsgbean_.setPose(ap_);
                aoi_.setMsg(amsgbean_);
                client.send(new Gson().toJson(aoi_));
                break;
            case R.id.nav_402_go:
//                mNavPointName = "map_4_A_402";
//                mNavPointState = 1;
//                mPointName = "map_4_A_402";
//                client.send(new Gson().toJson(mNavPublich.getNavPublishHashMap().get("map_4_A_402")));
                client.send(NavHelper.nav(1, "map_B"));
                break;
            case R.id.nav_402_go_go:
                mNavPointName = "map_4_A_402_map";
                mNavPointState = 1;
                mPointName = "map_4_A_402_map";
                client.send(new Gson().toJson(mNavPublich.getNavPublishHashMap().get("map_4_A_402_map")));
                client.send(NavHelper.nav(1, "map_4_A_402_map"));
                break;
            case R.id.nav_402_init:
                TMove_Base_Goal bo = mNavPublich.getNavPublishHashMap().get("map_4_A_402");
                InitPoseResult boi = new InitPoseResult();
                InitPoseResult.MsgBean bmsgbean = new InitPoseResult.MsgBean();
                InitPoseResult.MsgBean.PoseBeanX bp = new InitPoseResult.MsgBean.PoseBeanX();
                InitPoseResult.MsgBean.PoseBeanX.PoseBean bpp = new InitPoseResult.MsgBean.PoseBeanX.PoseBean();
                InitPoseResult.MsgBean.PoseBeanX.PoseBean.PositionBean bppp = new InitPoseResult.MsgBean.PoseBeanX.PoseBean.PositionBean();
                InitPoseResult.MsgBean.PoseBeanX.PoseBean.OrientationBean bppo = new InitPoseResult.MsgBean.PoseBeanX.PoseBean.OrientationBean();
                bp.setCovariance();
                bppp.setX(bo.getMsg().getGoal().getTarget_pose().getPose().getPosition().getX());
                bppp.setY(bo.getMsg().getGoal().getTarget_pose().getPose().getPosition().getY());
                bppp.setZ(bo.getMsg().getGoal().getTarget_pose().getPose().getPosition().getZ());
                bppo.setW(bo.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getW());
                bppo.setX(bo.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getX());
                bppo.setY(bo.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getY());
                bppo.setZ(bo.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getZ());
                bpp.setPosition(bppp);
                bpp.setOrientation(bppo);
                bp.setPose(bpp);
                bmsgbean.setPose(bp);
                boi.setMsg(bmsgbean);
                client.send(new Gson().toJson(boi));
                break;
            case R.id.nav_402_init_:
                TMove_Base_Goal bo_ = mNavPublich.getNavPublishHashMap().get("map_4_A_402_map");
                InitPoseResult boi_ = new InitPoseResult();
                InitPoseResult.MsgBean bmsgbean_ = new InitPoseResult.MsgBean();
                InitPoseResult.MsgBean.PoseBeanX bp_ = new InitPoseResult.MsgBean.PoseBeanX();
                InitPoseResult.MsgBean.PoseBeanX.PoseBean bpp_ = new InitPoseResult.MsgBean.PoseBeanX.PoseBean();
                InitPoseResult.MsgBean.PoseBeanX.PoseBean.PositionBean bppp_ = new InitPoseResult.MsgBean.PoseBeanX.PoseBean.PositionBean();
                InitPoseResult.MsgBean.PoseBeanX.PoseBean.OrientationBean bppo_ = new InitPoseResult.MsgBean.PoseBeanX.PoseBean.OrientationBean();
                bp_.setCovariance();
                bppp_.setX(bo_.getMsg().getGoal().getTarget_pose().getPose().getPosition().getX());
                bppp_.setY(bo_.getMsg().getGoal().getTarget_pose().getPose().getPosition().getY());
                bppp_.setZ(bo_.getMsg().getGoal().getTarget_pose().getPose().getPosition().getZ());
                bppo_.setW(bo_.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getW());
                bppo_.setX(bo_.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getX());
                bppo_.setY(bo_.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getY());
                bppo_.setZ(bo_.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getZ());
                bpp_.setPosition(bppp_);
                bpp_.setOrientation(bppo_);
                bp_.setPose(bpp_);
                bmsgbean_.setPose(bp_);
                boi_.setMsg(bmsgbean_);
                client.send(new Gson().toJson(boi_));
                break;
            case R.id.nav_403_go:
                mNavPointName = "map_4_A_403";
                mNavPointState = 1;
                mPointName = "map_4_A_403";
                client.send(new Gson().toJson(mNavPublich.getNavPublishHashMap().get("map_4_A_403")));
                client.send(NavHelper.nav(1, "map_4_A_403"));
                break;
            case R.id.nav_403_go_go:
                mNavPointName = "map_4_A_403_map";
                mNavPointState = 1;
                mPointName = "map_4_A_403_map";
                client.send(new Gson().toJson(mNavPublich.getNavPublishHashMap().get("map_4_A_403_map")));
                client.send(NavHelper.nav(1, "map_4_A_403_map"));
                break;
            case R.id.nav_403_init:
                TMove_Base_Goal co = mNavPublich.getNavPublishHashMap().get("map_4_A_403");
                InitPoseResult coi = new InitPoseResult();
                InitPoseResult.MsgBean cmsgbean = new InitPoseResult.MsgBean();
                InitPoseResult.MsgBean.PoseBeanX cp = new InitPoseResult.MsgBean.PoseBeanX();
                InitPoseResult.MsgBean.PoseBeanX.PoseBean cpp = new InitPoseResult.MsgBean.PoseBeanX.PoseBean();
                InitPoseResult.MsgBean.PoseBeanX.PoseBean.PositionBean cppp = new InitPoseResult.MsgBean.PoseBeanX.PoseBean.PositionBean();
                InitPoseResult.MsgBean.PoseBeanX.PoseBean.OrientationBean cppo = new InitPoseResult.MsgBean.PoseBeanX.PoseBean.OrientationBean();
                cp.setCovariance();
                cppp.setX(co.getMsg().getGoal().getTarget_pose().getPose().getPosition().getX());
                cppp.setY(co.getMsg().getGoal().getTarget_pose().getPose().getPosition().getY());
                cppp.setZ(co.getMsg().getGoal().getTarget_pose().getPose().getPosition().getZ());
                cppo.setW(co.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getW());
                cppo.setX(co.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getX());
                cppo.setY(co.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getY());
                cppo.setZ(co.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getZ());
                cpp.setPosition(cppp);
                cpp.setOrientation(cppo);
                cp.setPose(cpp);
                cmsgbean.setPose(cp);
                coi.setMsg(cmsgbean);
                client.send(new Gson().toJson(coi));
                break;
            case R.id.nav_403_init_:
                TMove_Base_Goal co_ = mNavPublich.getNavPublishHashMap().get("map_4_A_403");
                InitPoseResult coi_ = new InitPoseResult();
                InitPoseResult.MsgBean cmsgbean_ = new InitPoseResult.MsgBean();
                InitPoseResult.MsgBean.PoseBeanX cp_ = new InitPoseResult.MsgBean.PoseBeanX();
                InitPoseResult.MsgBean.PoseBeanX.PoseBean cpp_ = new InitPoseResult.MsgBean.PoseBeanX.PoseBean();
                InitPoseResult.MsgBean.PoseBeanX.PoseBean.PositionBean cppp_ = new InitPoseResult.MsgBean.PoseBeanX.PoseBean.PositionBean();
                InitPoseResult.MsgBean.PoseBeanX.PoseBean.OrientationBean cppo_ = new InitPoseResult.MsgBean.PoseBeanX.PoseBean.OrientationBean();
                cp_.setCovariance();
                cppp_.setX(co_.getMsg().getGoal().getTarget_pose().getPose().getPosition().getX());
                cppp_.setY(co_.getMsg().getGoal().getTarget_pose().getPose().getPosition().getY());
                cppp_.setZ(co_.getMsg().getGoal().getTarget_pose().getPose().getPosition().getZ());
                cppo_.setW(co_.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getW());
                cppo_.setX(co_.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getX());
                cppo_.setY(co_.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getY());
                cppo_.setZ(co_.getMsg().getGoal().getTarget_pose().getPose().getOrientation().getZ());
                cpp_.setPosition(cppp_);
                cpp_.setOrientation(cppo_);
                cp_.setPose(cpp_);
                cmsgbean_.setPose(cp_);
                coi_.setMsg(cmsgbean_);
                client.send(new Gson().toJson(coi_));
                break;
            case R.id.nav_404_go:
                mNavPointName = "map_4_A_404";
                mNavPointState = 1;
                client.send(new Gson().toJson(mNavPublich.getNavPublishHashMap().get("map_4_A_404")));
                mPointName = "map_4_A_404";
                break;
            case R.id.nav_404_go_go:
                mNavPointName = "map_4_A_404_map";
                mNavPointState = 1;
                client.send(new Gson().toJson(mNavPublich.getNavPublishHashMap().get("map_4_A_404_map")));
                mPointName = "map_4_A_404_map";
                break;
            case R.id.nav_404_init:
                break;
            case R.id.nav_404_init_:
                break;
            case R.id.map_master:
                client.send("{\n" +
                        "\t\"op\": \"publish\",\n" +
                        "\t\"topic\": \"/cmd_string\",\n" +
                        "\t\"msg\": {\n" +
                        "\t\t\"data\": \"dbparam-update:master\"\n" +
                        "\t}\n" +
                        "}");
                break;
            case R.id.map_yu:
                client.send("{\n" +
                        "\t\"op\": \"publish\",\n" +
                        "\t\"topic\": \"/cmd_string\",\n" +
                        "\t\"msg\": {\n" +
                        "\t\t\"data\": \"dbparam-update:yu\"\n" +
                        "\t}\n" +
                        "}");
                break;
            case R.id.initpose:
                client.send("{\n" +
                        "    \"op\": \"publish\",\n" +
                        "    \"topic\": \"/initialpose\",\n" +
                        "    \"msg\": {\n" +
                        "        \"header\": {\n" +
                        "            \"frame_id\": \"map\"\n" +
                        "        },\n" +
                        "        \"pose\": {\n" +
                        "            \"pose\": {\n" +
                        "                \"position\": {\n" +
                        "                    \"x\": 0,\n" +
                        "                    \"y\": 0,\n" +
                        "                    \"z\": 0\n" +
                        "                },\n" +
                        "                \"orientation\": {\n" +
                        "                    \"x\": 0,\n" +
                        "                    \"y\": 0,\n" +
                        "                    \"z\": 0,\n" +
                        "                    \"w\": 1\n" +
                        "                }\n" +
                        "            },\n" +
                        "            \"covariance\": [\n" +
                        "                0.25,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0.25,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0,\n" +
                        "                0.06853891945200942\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    }\n" +
                        "}");
                break;
            case R.id.test_navi_f:
//                if (binder != null) binder.startPoi();
                client.send("{" + "\"op\": \"publish\"," + "\"topic\": \"/rosnodejs/charge_ctrl\"," + "\"msg\": {" + "\"data\": \"charge\"" + "}" + "}");
                break;
            case R.id.unpower:
                client.send("{" + "\"op\": \"publish\"," + "\"topic\": \"/rosnodejs/charge_ctrl\"," + "\"msg\": {" + "\"data\": \"uncharge\"" + "}" + "}");
                break;
            case R.id.power:
                String msg = "{" + "\"op\": \"subscribe\"," + "\"topic\": \"/rosnodejs/robot_status\"," + "\"throttle_rate\": 3000" + "}";
                client.send(msg);
                break;
            case R.id.nav_state:
//                client.send("{" + "\"op\": \"subscribe\"," + "\"topic\": \"/move_base/status\"," + "\"throttle_rate\": 1888" + "}");
                client.send("{\"op\":\"subscribe\",\"topic\":\"/nav_ctrl_status\"}");
                break;
            case R.id.stop_nav_state:
                Log.d("click", NavHelper.getTime());
                client.send("{" + "\"op\": \"unsubscribe\"," + "\"topic\": \"/move_base/status\"" + "}");
                Log.d("click", NavHelper.getTime());
                break;
            // 同步站点数据
            case R.id.ansy_data:
                client.send("{ \"op\": \"subscribe\", \"topic\": \"/waypoints\"}");
                break;
            case R.id.ceshi:
                client.send("{\"op\":\"publish\",\"topic\":\"/cmd_string\",\"msg\":{\"data\":\"cancel\"}}");
                break;
            case R.id.cmd_vel:
                client.send("{\"op\":\"publish\",\"topic\":\"/cmd_vel\",\"msg\":{\"linear\":{\"x\":0,\"y\":0,\"z\":0},\"angular\":{\"x\":0,\"y\":0,\"z\":0}}}");
                break;
            case R.id.nav_ctrl_0:
//                client.send("{\"op\":\"publish\",\"topic\":\"/nav_ctrl\",\"msg\":{\"control\":0,\"goal_name\":\"\"}}");
                client.send("{\"op\": \"publish\",\"topic\": \"/nav_ctrl\",\"msg\": {\"control\": 0,\"goal_name\": \"\"}}");
                break;
            case R.id.nav_ctrl_1:
                client.send("{\"op\": \"publish\",\"topic\": \"/nav_ctrl\",\"msg\": {\"control\": 1,\"goal_name\": \"\"}}");
                break;
            case R.id.nav_ctrl_2:
                client.send("{\"op\": \"publish\",\"topic\": \"/nav_ctrl\",\"msg\": {\"control\": 2,\"goal_name\": \"\"}}");
                break;
            case R.id.gmapping_pose:
                client.send("{\"op\":\"publish\",\"topic\":\"/cmd_string\",\"msg\":{\"data\":\"gmapping_pose\"}}");
                client.send("{\"op\":\"publish\",\"topic\":\"/cmd_string\",\"msg\":{\"data\":\"save_map\"}}");
                client.send("{\"op\":\"publish\",\"topic\":\"/cmd_string\",\"msg\":{\"data\":\"load_map\"}}");
                client.send("{\"op\":\"publish\",\"topic\":\"/cmd_string\",\"msg\":{\"data\":\"save_map_edit\"}}");
                break;
            case R.id.save_map:
                client.send("{\"op\":\"publish\",\"topic\":\"/cmd_string\",\"msg\":{\"data\":\"save_map\"}}");
                break;
            case R.id.load_map:
                client.send("{\"op\":\"publish\",\"topic\":\"/cmd_string\",\"msg\":{\"data\":\"load_map\"}}");
                break;
            case R.id.save_map_edit:
                client.send("{\"op\":\"publish\",\"topic\":\"/cmd_string\",\"msg\":{\"data\":\"save_map_edit\"}}");
                break;
        }
    }

    BroadcastReceiver mBroadCastReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String state = intent.getStringExtra("action");
            // 取消底盘
            if (state.equals("cancel")) {
                Log.d("yu", "i'm have receviced");
//                client.send("{\"op\":\"publish\",\"topic\":\"/cmd_string\",\"msg\":{\"data\":\"cancel\"}}");
            }
        }
    };

    //    Add TouchListener on log TextView
    private void processMoveTopic(float linearX, float angularZ) {
        client.send("{\"op\":\"publish\",\"topic\":\"/cmd_vel\",\"msg\":{\"linear\":{\"x\":" + linearX + ",\"y\":0,\"z\":0},\"angular\":{\"x\":0,\"y\":0,\"z\":" + angularZ + "}}}");
        Log.d(TAG, "send cmd_vel msg:x:" + linearX + " z:" + angularZ);
    }

    //    Add TouchListener on log TextView
    private void processStopTopic() {
        client.send("{\"op\":\"publish\",\"topic\":\"/cmd_vel\",\"msg\":{\"linear\":{\"x\":" + 0 + ",\"y\":0,\"z\":0},\"angular\":{\"x\":0,\"y\":0,\"z\":" + 0 + "}}}");
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (client != null) {
                processStopTopic();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void connect(String wsUrl) {
        Log.d(TAG, "connect()");
        if (client != null) {
            // 节省内存
            if (client.connect()) return;
        }
        final Message message = new Message();
        client = new ROSBridgeClient(wsUrl);
        client.connect(new ROSClient.ConnectionStatusListener() {
            @Override
            public void onConnect() {
                client.setDebug(true);
                ((RCApplication) getApplication()).setRosClient(client);
                Log.d("chuan", "Connect ROS success");
                message.what = 1;
                mHandler.sendMessage(message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onDisconnect(boolean normal, String reason, int code) {
                Log.d("chuan", "ROS disconnect");
                message.what = 2;
                mHandler.sendMessage(message);
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
                message.what = 2;
                mHandler.sendMessage(message);
                Log.d("chuan", "ROS communication error");
            }
        });
    }

    //Receive data from ROS server, send from ROSBridgeWebSocketClient onMessage()
    String mNavPointName;
    // 记录导航命令的下达 1是 0否
    int mNavPointState = 0;
    //
    int FLAG = 0;
    String mCurrentPointName = null;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMain(final PublishEvent event) {
        if ("/map".equals(event.name)) {
            return;
        }
        Log.d(TAG, "MainAcitvity response = " + event.op);
        Log.d(TAG, "MainAcitvity response = " + event.id);
        Log.d(TAG, "MainAcitvity response = " + event.name);
        Log.d(TAG, "MainAcitvity response = " + event.msg);


        if (event.name.equals("/nav_ctrl_status")) {
            mStateTextView.setText(event.msg);
            Log.d("yuu", event.msg);
        }

        // 事实的数据 为单点导航使用
        if (event.name.equals("/move_base/status")) {
            Log.d("moved", "MainAcitvity response = " + event.msg);
            mStateTextView.setText(event.msg);
//            Move_Base_Status mbs = new Gson().fromJson(event.msg, Move_Base_Status.class);
//            if (mbs == null) return;
//            if (mbs.getStatus_list().size() == 0) return;
//            for (int i = 0; i < mbs.getStatus_list().size(); i++) {
//                String name = mbs.getStatus_list().get(i).getGoal_id().getId();
//                Log.d("move", "MainAcitvity response = " + event.msg);
//                Log.d("move", "MainAcitvity name = " + name);
//                if (name.contains("map_") || name.contains("_map")) {
//                    // 导航为1,正在运行
//                    if (mbs.getStatus_list().get(i).getStatus() == 1) {
//                        mPointName = name.replace("_map", "");
//                        mCurrentPointName = name;
//                        Log.d("move", "/n");
//                        Log.d("move", "MainAcitvity getStatus = " + 1);
//                        Log.d("move", "MainAcitvity mPointName = " + mPointName);
//                        Log.d("move", "MainAcitvity mCurrentPointName = " + mCurrentPointName);
//                    }
//                    // 导航为2,取消所有运动
//                    if (mbs.getStatus_list().get(i).getStatus() == 2) {
//                        Log.d("move", "/n");
//                        Log.d("move", "MainAcitvity getStatus = " + 2);
//                        Log.d("move", "MainAcitvity mPointName = " + mPointName);
//                        Log.d("move", "MainAcitvity mCurrentPointName = " + mCurrentPointName);
//                        if (mPointName == null) return;
//                        if (mCurrentPointName == null) return;
//                        if (!mCurrentPointName.equals(name)) return;
//                        // 如果站点名称包含当前导航的名称
//                        if (name.equals(mPointName)) {
//                            client.send(new Gson().toJson(mNavPublich.getNavPublishHashMap().get(mPointName + "_map")));
//                            mCurrentPointName = mPointName + "_map";
//                        }
//                        if (name.contains("_map") && name.contains(mPointName)) {
//                            client.send(new Gson().toJson(mNavPublich.getNavPublishHashMap().get(mPointName)));
//                            mCurrentPointName = mPointName;
//                        }
//                    }
//                    // 导航为3,到达
//                    if (mbs.getStatus_list().get(i).getStatus() == 3) {
//                        if (mPointName != null) {
//                            if (name.equals(mPointName) || name.equals(mPointName + "_map")) {
//                                Log.d("move", "/n");
//                                Log.d("move", "MainAcitvity getStatus = " + 3);
//                                Log.d("move", "MainAcitvity mPointName = " + mPointName);
//                                Log.d("move", "MainAcitvity mCurrentPointName = " + mCurrentPointName);
//                                mPointName = null;
//                                mCurrentPointName = null;
//                                Log.d("move", "MainAcitvity getStatus = " + 3);
//                                Log.d("move", "MainAcitvity mPointName = " + mPointName);
//                                Log.d("move", "MainAcitvity mCurrentPointName = " + mCurrentPointName);
//                            }
//                        }
//                    }
//                    // 进入4 当前点被放弃
//                    if (mbs.getStatus_list().get(i).getStatus() == 4) {
//                        Log.d("move", "/n");
//                        Log.d("move", "MainAcitvity getStatus = " + 4);
//                        Log.d("move", "MainAcitvity mPointName = " + mPointName);
//                        Log.d("move", "MainAcitvity mCurrentPointName = " + mCurrentPointName);
//                        if (mPointName == null) return;
//                        if (mCurrentPointName == null) return;
//                        if (!mCurrentPointName.equals(name)) return;
//                        // 如果站点名称包含当前导航的名称
//                        if (name.equals(mPointName)) {
//                            client.send(new Gson().toJson(mNavPublich.getNavPublishHashMap().get(mPointName + "_map")));
//                            mCurrentPointName = mPointName + "_map";
//                        }
//                        if (name.contains("_map") && name.contains(mPointName)) {
//                            client.send(new Gson().toJson(mNavPublich.getNavPublishHashMap().get(mPointName)));
//                            mCurrentPointName = mPointName;
//                        }
//                    }
//                }
//            }
        }

        // 获取所有站点并存入到HashMap集合中去
        if (event.name.equals("/waypoints")) {
            /****第一种方式***/

            /****第二种方式***/

            // 将返回的数据生成实体类
            WayPointUtil wayPointUtil = new Gson().fromJson(event.msg, WayPointUtil.class);
            // mNavPublich 赋值
            List<String> mWayPointsNamesList = new ArrayList<String>();
            HashMap<String, TMove_Base_Goal> mNavPublishHashMap = new HashMap<String, TMove_Base_Goal>();

            for (int i = 0; i < wayPointUtil.getWaypoints().size(); i++) {

                String wayPointName = wayPointUtil.getWaypoints().get(i).getName();
                mWayPointsNamesList.add(wayPointName);

                /******************************************** 拼接导航对象 ****************************************/
                TMove_Base_Goal mbg = new TMove_Base_Goal();

                // 设置msg
                TMove_Base_Goal.MsgBean mgb_msg = new TMove_Base_Goal.MsgBean();
                TMove_Base_Goal.MsgBean.HeaderBean mbg_msg_header = new TMove_Base_Goal.MsgBean.HeaderBean();
                TMove_Base_Goal.MsgBean.HeaderBean.StampBean mbg_msg_header_stamp = new TMove_Base_Goal.MsgBean.HeaderBean.StampBean();
                mbg_msg_header.setStamp(mbg_msg_header_stamp);
                mgb_msg.setHeader(mbg_msg_header);

                // 设置 goal
                TMove_Base_Goal.MsgBean.GoalIdBean mbg_msg_gid = new TMove_Base_Goal.MsgBean.GoalIdBean();
                TMove_Base_Goal.MsgBean.GoalIdBean.StampBeanX mbg_msg_gid_stamp = new TMove_Base_Goal.MsgBean.GoalIdBean.StampBeanX();
                mbg_msg_gid.setStamp(mbg_msg_gid_stamp);
                mbg_msg_gid.setId(wayPointName);
                mgb_msg.setGoal_id(mbg_msg_gid);

                // 设置 goal
                TMove_Base_Goal.MsgBean.GoalBean mbg_msg_goal = new TMove_Base_Goal.MsgBean.GoalBean();
                // 设置 goal ------> target_pose -------->header
                TMove_Base_Goal.MsgBean.GoalBean.TargetPoseBean mbg_msg_goal_targetpose = new TMove_Base_Goal.MsgBean.GoalBean.TargetPoseBean();
                TMove_Base_Goal.MsgBean.GoalBean.TargetPoseBean.HeaderBeanX mbg_msg_goal_targetpose_hearder = new TMove_Base_Goal.MsgBean.GoalBean.TargetPoseBean.HeaderBeanX();
                TMove_Base_Goal.MsgBean.GoalBean.TargetPoseBean.HeaderBeanX.StampBeanXX mbg_msg_goal_targetpose_hearder_stamp = new TMove_Base_Goal.MsgBean.GoalBean.TargetPoseBean.HeaderBeanX.StampBeanXX();
                mbg_msg_goal_targetpose_hearder.setStamp(mbg_msg_goal_targetpose_hearder_stamp);
                mbg_msg_goal_targetpose.setHeader(mbg_msg_goal_targetpose_hearder);
                // 设置 goal ------> target_pose -------->pose
                TMove_Base_Goal.MsgBean.GoalBean.TargetPoseBean.PoseBean mbg_msg_goal_targetpose_pose = new TMove_Base_Goal.MsgBean.GoalBean.TargetPoseBean.PoseBean();
                TMove_Base_Goal.MsgBean.GoalBean.TargetPoseBean.PoseBean.PositionBean mbg_msg_goal_targetpose_pose_position = new TMove_Base_Goal.MsgBean.GoalBean.TargetPoseBean.PoseBean.PositionBean();
                mbg_msg_goal_targetpose_pose_position.setZ(wayPointUtil.getWaypoints().get(i).getPose().getPosition().getZ());
                mbg_msg_goal_targetpose_pose_position.setX(wayPointUtil.getWaypoints().get(i).getPose().getPosition().getX());
                mbg_msg_goal_targetpose_pose_position.setY(wayPointUtil.getWaypoints().get(i).getPose().getPosition().getY());
                mbg_msg_goal_targetpose_pose.setPosition(mbg_msg_goal_targetpose_pose_position);

                TMove_Base_Goal.MsgBean.GoalBean.TargetPoseBean.PoseBean.OrientationBean mbg_msg_goal_targetpose_pose_orient = new TMove_Base_Goal.MsgBean.GoalBean.TargetPoseBean.PoseBean.OrientationBean();
                mbg_msg_goal_targetpose_pose_orient.setZ(wayPointUtil.getWaypoints().get(i).getPose().getOrientation().getZ());
                mbg_msg_goal_targetpose_pose_orient.setX(wayPointUtil.getWaypoints().get(i).getPose().getOrientation().getX());
                mbg_msg_goal_targetpose_pose_orient.setW(wayPointUtil.getWaypoints().get(i).getPose().getOrientation().getW());
                mbg_msg_goal_targetpose_pose_orient.setY(wayPointUtil.getWaypoints().get(i).getPose().getOrientation().getY());
                mbg_msg_goal_targetpose_pose.setOrientation(mbg_msg_goal_targetpose_pose_orient);

                mbg_msg_goal_targetpose.setPose(mbg_msg_goal_targetpose_pose);

                mbg_msg_goal.setTarget_pose(mbg_msg_goal_targetpose);

                mgb_msg.setGoal(mbg_msg_goal);
                // 数据汇总
                mbg.setMsg(mgb_msg);
                /******************************************** 抛出 mbg ****************************************/
                mNavPublishHashMap.put(wayPointName, mbg);
            }
            // 清除数据缓存
            mNavPublich.clear();
            // 复制数据至新的集合
            mNavPublich.setWayPointsNames(mWayPointsNamesList);
            mNavPublich.setNavPublishHashMap(mNavPublishHashMap);
            Toast.makeText(this, " 数据同步成功 ", Toast.LENGTH_SHORT).show();
        }
    }
}