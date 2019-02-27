package com.vivo.common.autobrightness;

import com.vivo.common.provider.Calendar.Events;
import java.io.PrintWriter;

public class AppClassify {
    private static String[] mGame = new String[]{"com.happyelements.AndroidAnimal", "com.tencent.tmgp.ylm", "com.ztgame.bob", "com.netease.dhxy.vivo", "com.qqgame.hlddz", "com.tencent.tmgp.zhuxian", "com.tencent.qt.qtl", "com.netease.dhxy.sougou", "com.netease.dhxy", "com.tencent.pao", "com.tencent.peng", "com.wepie.snake.vivo", "com.mfp.jelly.vivo", "com.mojang.minecraftpe"};
    private static String[] mMap = new String[]{"com.autonavi.minimap", "com.baidu.BaiduMap", "com.tencent.map", "com.sogou.map.android.maps", "com.autonavi.cmccmap", "com.google.android.apps.maps"};
    private static String[] mMobaGame = new String[]{"com.tencent.tmgp.sgame"};
    private static String[] mNews = new String[]{"com.tencent.news", "com.ss.android.article.news", "com.tencent.reading", "com.tencent.qt.qtl", "com.sina.news", "com.sohu.newsclient", "com.cubic.autohome", "com.netease.newsreader.activity", "com.zhihu.android", "com.ifeng.news2"};
    private static Pare[] mPareList = new Pare[]{new Pare(AppType.TYPE_VIDEO, mVideo), new Pare(AppType.TYPE_GAME, mGame), new Pare(AppType.TYPE_MOBA_GAME, mMobaGame), new Pare(AppType.TYPE_PUBG_GAME, mPubgGame)};
    private static String[] mPubgGame = new String[]{"com.tencent.tmgp.pubgmhd", "com.tencent.tmgp.pubgm", "com.netease.dwrg", "com.netease.dwrg5.vivo", "com.netease.hyxd", "com.netease.hyxd.vivo", "com.ak.mi", "com.wali.ak.vivo", "com.tencent.tmgp.cf"};
    private static String[] mReading = new String[]{"com.chaozh.iReader", "com.qq.reader", "com.shuqi.controller", "com.esbook.reader", "com.ophone.reader.ui", "com.chaozh.iReaderFree", "com.ushaqi.zhuishushenqi", "com.sogou.novel", "cn.htjyb.reader", "com.lianzainovel"};
    private static String[] mShopping = new String[]{"com.sankuai.meituan", "com.taobao.taobao", "com.achievo.vipshop", "com.dianping.v1", "com.jingdong.app.mall", "com.nuomi", "com.mogujie", "com.tmall.wireless", "com.husor.beibei", "me.ele", "com.baidu.lbs.waimai", "com.sankuai.meituan.takeoutnew"};
    private static String[] mSns = new String[]{"com.tencent.mm", "com.tencent.mobileqq", "com.sina.weibo", "com.qzone", "com.immomo.momo", "com.baidu.tieba", "cn.j.hers", "com.yx", "com.alibaba.android.rimet", "com.taobao.qianniu"};
    private static String[] mVgFlagList = new String[]{AppType.TYPE_VIDEO, AppType.TYPE_GAME, AppType.TYPE_MOBA_GAME, AppType.TYPE_PUBG_GAME};
    private static String[] mVideo = new String[]{"com.sohu.sohuvideo", "com.tencent.qqlive", "com.qiyi.video", "com.ss.android.ugc.aweme", "com.smile.gifmaker", "com.youku.phone", "com.ss.android.ugc.live", "com.ss.android.article.video", "com.baidu.haokan", "com.baidu.video", "com.tudou.android", "com.tencent.weishi", "com.bobo.splayer", "com.panda.videoliveplatform", "com.hunantv.imgo.activity", "com.longzhu.tga", "com.le123.ysdq", "com.android.VideoPlayer", "com.duowan.kiwi", "com.cinema2345", "air.tv.douyu.android", "com.chengzivr.android", "com.meitu.meiyancamera", "com.letv.android.client", "dopool.player", "tv.pps.mobile", "tv.danmaku.bili", "tv.acfundanmaku.video", "com.netease.cc", "com.zhongduomei.rrmj.society", "com.babycloud.hanju", "com.qiyi.video.pad", "com.mt.mtxx.mtxx"};

    public static final class AppType {
        public static String TYPE_GAME = "game";
        public static String TYPE_MAP = "map";
        public static String TYPE_MOBA_GAME = "mobagame";
        public static String TYPE_NEWS = "news";
        public static String TYPE_PUBG_GAME = "pubggame";
        public static String TYPE_READING = "reading";
        public static String TYPE_SHOPING = "shopping";
        public static String TYPE_SNS = "sns";
        public static String TYPE_UNKOWN = "unkown";
        public static String TYPE_VIDEO = "video";
    }

    public static class Pare {
        public String[] list;
        public String type;

        public Pare(String type, String[] list) {
            this.type = type;
            this.list = list;
        }
    }

    public static String getAppType(String pkg) {
        if (pkg == null || Events.DEFAULT_SORT_ORDER.equals(pkg)) {
            return AppType.TYPE_UNKOWN;
        }
        String ret = AppType.TYPE_UNKOWN;
        for (Pare x : mPareList) {
            String type = x.type;
            for (String p : x.list) {
                if (pkg.equals(p)) {
                    ret = type;
                    break;
                }
            }
            if (!ret.equals(AppType.TYPE_UNKOWN)) {
                break;
            }
        }
        return ret;
    }

    public static void updatePareList(Pare[] list) {
        mPareList = list;
    }

    public static boolean getVideoGameFlag(String typ) {
        boolean ret = false;
        if (typ == null || mVgFlagList == null || AppType.TYPE_UNKOWN.equals(typ)) {
            return false;
        }
        for (String x : mVgFlagList) {
            if (typ.equals(x)) {
                ret = true;
            }
        }
        return ret;
    }

    public static void updateVgFlagList(String[] list) {
        mVgFlagList = list;
    }

    public static void dump(PrintWriter pw) {
        int i = 0;
        pw.println("  ");
        pw.println("  PareList");
        for (Pare pare : mPareList) {
            pw.println("    type:" + pare.type);
            String pkgs = "[";
            for (String pkg : pare.list) {
                pkgs = pkgs + pkg + ",";
            }
            pw.println("    pkgs:" + (pkgs + "]"));
        }
        pw.println("  VgFlagList");
        String typs = "[";
        String[] strArr = mVgFlagList;
        while (i < strArr.length) {
            typs = typs + strArr[i] + ",";
            i++;
        }
        pw.println("    typs:" + (typs + "]"));
    }
}
