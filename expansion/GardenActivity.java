package com.mhmmad.setramahjong.expansion;

import android.app.Activity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.content.res.ColorStateList;
import android.view.animation.ScaleAnimation;
import android.view.animation.Animation;
import android.os.Bundle;
import android.os.Build;
import android.window.OnBackInvokedDispatcher;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.Arrays;
import java.util.Locale;

public final class GardenActivity extends Activity {
    private final int DARK=0xff101c30, JADE=0xff264c70, GOLD=0xffffd38a, IVORY=0xfffff9e9, MUTE=0xffc4d6ed;
    private final int CORAL=0xffff9a79, SKY=0xff8be1ef;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private static final String[] MARK={"","●","◆","▲","■","✦","✚","○","◇","△","□","×","+","⬟","★","≋","◎"};
    private SharedPreferences prefs;
    private String lang="en-GB",mode=""; private int rows,cols,first=-1,second=-1,moves,hintA=-1,hintB=-1; private long seed;
    private boolean daily,locked,won; private int campaignLevel=0; private int[] board,initial; private LinearLayout body; private GridLayout grid; private TextView status; private int generation;
    private String tr(String en,String ar,String pt){return lang.equals("ar")?ar:lang.equals("pt-BR")?pt:en;}
    private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
    @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("setra_garden_v1",MODE_PRIVATE);
        lang=prefs.getString("lang","en-GB"); getWindow().setStatusBarColor(DARK);getWindow().setNavigationBarColor(DARK);
        if(Build.VERSION.SDK_INT >= 33){
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::navigateBack);
        }
        if(b!=null){mode=b.getString("mode","");board=b.getIntArray("board");initial=b.getIntArray("initial");rows=b.getInt("rows");cols=b.getInt("cols");seed=b.getLong("seed");moves=b.getInt("moves");daily=b.getBoolean("daily");won=b.getBoolean("won");}
        if(board!=null&&!mode.isEmpty())showGame();else home();
    }
    @Override protected void onSaveInstanceState(Bundle b){super.onSaveInstanceState(b);b.putString("mode",mode);b.putIntArray("board",board);b.putIntArray("initial",initial);b.putInt("rows",rows);b.putInt("cols",cols);b.putLong("seed",seed);b.putInt("moves",moves);b.putBoolean("daily",daily);b.putBoolean("won",won);}
    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);super.onDestroy();}
    private void navigateBack(){if(!mode.isEmpty())home();else finish();}
    @SuppressLint("GestureBackNavigation") // Fallback for pre-Android 13: modern gesture callback is registered above.
    @Override public void onBackPressed(){navigateBack();}
    private GradientDrawable panel(int color,int stroke,float radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));if(stroke!=0)d.setStroke(dp(1),stroke);return d;}
    private GradientDrawable gradient(int top,int bottom,float radius){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{top,bottom});d.setCornerRadius(dp(radius));return d;}
    private void entrance(View v){ScaleAnimation a=new ScaleAnimation(.92f,1f,.92f,1f,Animation.RELATIVE_TO_SELF,.5f,Animation.RELATIVE_TO_SELF,.5f);a.setDuration(260);v.startAnimation(a);}
    private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.START);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setIncludeFontPadding(true);return t;}
    private void add(View v){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(10);body.addView(v,p);}
    private Button button(String s,Runnable action){Button b=new Button(this);b.setAllCaps(false);b.setText(s);b.setTextSize(16);b.setTextColor(IVORY);b.setGravity(Gravity.CENTER);b.setMinHeight(dp(52));b.setPadding(dp(12),dp(10),dp(12),dp(10));b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x66ffffff),gradient(JADE,0xff1b3a5a,16),null));b.setElevation(dp(3));b.setOnClickListener(v->action.run());return b;}
    private void frame(String title,String subtitle){
        FrameLayout scene=new FrameLayout(this);
        scene.addView(new GardenBackdrop(this),new FrameLayout.LayoutParams(-1,-1));
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(Color.TRANSPARENT);
        scroll.setVerticalScrollBarEnabled(false);
        body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(22),dp(32),dp(22),dp(40));body.setLayoutDirection(lang.equals("ar")?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);
        scroll.addView(body,new ScrollView.LayoutParams(-1,-2));
        scene.addView(scroll,new FrameLayout.LayoutParams(-1,-1));setContentView(scene);
        TextView brand=text("✦  S E T R A   •   G A R D E N",12,GOLD,true);brand.setLetterSpacing(0.09f);add(brand);
        TextView heading=text(title,30,IVORY,true);heading.setShadowLayer(dp(5),0,dp(3),0xff072b24);add(heading);
        TextView detail=text(subtitle,15,MUTE,false);detail.setPadding(0,0,0,dp(10));add(detail);
    }
    private void home(){generation++;handler.removeCallbacksAndMessages(null);mode="";board=null;initial=null;locked=false;won=false;
        frame(tr("A world of tile puzzles","عالم من ألغاز البلاطات","Um mundo de enigmas"),tr("Explore • Solve • Collect stars","استكشف • حل الألغاز • اجمع النجوم","Explore • Resolva • Colecione estrelas"));
        LinearLayout hero=new LinearLayout(this);hero.setOrientation(LinearLayout.VERTICAL);hero.setPadding(dp(20),dp(20),dp(20),dp(20));hero.setBackground(gradient(0xff694a96,0xff294f82,24));hero.setElevation(dp(7));
        TextView jewel=text("✦    ◆    ◈    ✧",38,GOLD,true);jewel.setGravity(Gravity.CENTER);hero.addView(jewel);
        TextView tagline=text(tr("THE TILE QUEST","رحلة البلاطات","A JORNADA DAS PEÇAS"),22,IVORY,true);tagline.setGravity(Gravity.CENTER);hero.addView(tagline);
        TextView progress=text(tr("Campaign stars: ","نجوم الرحلة: ","Estrelas da jornada: ")+prefs.getInt("campaign-stars",0)+"  ★",16,SKY,true);progress.setGravity(Gravity.CENTER);hero.addView(progress);add(hero);
        rowLang();
        card(tr("Tile Quest • Campaign","رحلة البلاطات • المراحل","Jornada das Peças • Fases"),tr("Unlock challenges and earn stars.","افتح تحديات متدرجة واجمع النجوم.","Desbloqueie desafios e ganhe estrelas."),this::campaign);
        card(tr("Classic Mahjong","ماجونغ الكلاسيكية","Mahjong clássico"),tr("Original layered tile solitaire.","لعبة مطابقة البلاطات الأصلية متعددة الطبقات.","O solitário original com peças em camadas."),()->{Intent i=new Intent();i.setClassName(this,ClassicTarget.NAME);startActivity(i);});
        card(tr("Garden Routes","مسارات الحديقة","Caminhos do jardim"),tr("Link matching stones with up to two turns. Think around obstacles.","اربط الأحجار المتطابقة بمسار لا يتجاوز انعطافين وتجنب العوائق.","Ligue pedras iguais com até duas curvas, contornando obstáculos."),()->choose("routes"));
        card(tr("Memory Grove","بستان الذاكرة","Bosque da memória"),tr("Turn hidden stones and remember their positions.","اقلب الأحجار المخفية وتذكر أماكنها.","Vire pedras ocultas e memorize suas posições."),()->choose("memory"));
        card(tr("Daily seed","لغز اليوم","Enigma do dia"),tr("A new fixed challenge each day, even offline.","تحدٍ ثابت جديد كل يوم حتى دون اتصال.","Um novo desafio fixo por dia, mesmo offline."),()->{long day=System.currentTimeMillis()/86400000L;start(day%2==0?"routes":"memory",4,6,day,true);});
        int total=prefs.getInt("completed",0);
        add(text(tr("Garden puzzles completed: ","ألغاز الحديقة المكتملة: ","Enigmas do jardim concluídos: ")+total,16,GOLD,true));
    }
    private void rowLang(){LinearLayout r=new LinearLayout(this);for(String l:new String[]{"en-GB","ar","pt-BR"}){Button b=button(l.equals("ar")?"العربية":l.equals("pt-BR")?"Português":"English",()->{lang=l;prefs.edit().putString("lang",l).apply();home();});b.setTextSize(12);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.setMargins(dp(2),0,dp(2),0);r.addView(b,p);}add(r);}
    private void card(String title,String detail,Runnable click){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(14),dp(12),dp(14),dp(12));c.setBackground(gradient(0xff2a4a75,0xff203652,20));c.setElevation(dp(4));c.addView(text(title,20,IVORY,true));TextView d=text(detail,14,MUTE,false);d.setPadding(0,dp(5),0,dp(8));c.addView(d);c.addView(button(tr("PLAY  ✦","العب  ✦","JOGAR  ✦"),click));add(c);entrance(c);}
    private void campaign(){mode="campaign";frame(tr("Tile Quest","رحلة البلاطات","Jornada das Peças"),tr("Clear each challenge to unlock the next.","أكمل كل تحدٍ لفتح المرحلة التالية.","Conclua cada desafio para desbloquear o próximo."));
        int unlocked=prefs.getInt("campaign-unlocked",1);
        for(int n=1;n<=12;n++){final int level=n;boolean available=n<=unlocked;String stars=prefs.getBoolean("campaign-win-"+n,false)?"  ★★★":"";
            Button b=button((available?"✦  ":"🔒  ")+tr("Level ","المرحلة ","Fase ")+n+stars,()->{campaignLevel=level;String type=level%3==0?"memory":"routes";int rows=level<4?4:level<9?4:6;int cols=level<4?4:6;start(type,rows,cols,10000L+level,false);});b.setEnabled(available);add(b);
        }
        add(button(tr("Back to home","العودة للرئيسية","Voltar ao início"),this::home));
    }
    private void choose(String type){campaignLevel=0;mode=type;frame(type.equals("routes")?tr("Garden Routes","مسارات الحديقة","Caminhos do jardim"):tr("Memory Grove","بستان الذاكرة","Bosque da memória"),tr("Choose your board.","اختر حجم اللوحة.","Escolha seu tabuleiro."));
        for(int[] shape:new int[][]{{4,4},{4,6},{6,6}}){if(type.equals("memory")&&shape[0]*shape[1]>32)continue;int r=shape[0],c=shape[1];add(button(r+" × "+c,()->start(type,r,c,System.nanoTime(),false)));}
        add(button(tr("Back","رجوع","Voltar"),this::home));
    }
    private void start(String type,int r,int c,long s,boolean isDaily){mode=type;rows=r;cols=c;seed=s;daily=isDaily;moves=0;first=second=hintA=hintB=-1;locked=won=false;
        initial=type.equals("routes")?GardenEngine.routes(r,c,s).tiles:GardenEngine.memory(r*c,s);board=initial.clone();generation++;showGame();}
    private void showGame(){
        boolean routes=mode.equals("routes");
        frame(routes?tr("Garden Routes","مسارات الحديقة","Caminhos do jardim"):tr("Memory Grove","بستان الذاكرة","Bosque da memória"),
            routes?tr("Match equal stones along clear paths with no more than two turns.","طابق الأحجار المتساوية عبر مسارات فارغة بانعطافين على الأكثر.","Ligue pedras iguais por caminhos livres com até duas curvas."):
            tr("Reveal two stones. Remember and match the pairs.","اكشف حجرين وتذكر أماكن الأزواج المتطابقة.","Revele duas pedras. Memorize e combine os pares."));
        status=text("",16,GOLD,true);add(status);grid=new GridLayout(this);grid.setColumnCount(cols);grid.setRowCount(rows);grid.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);add(grid);draw();
        if(routes)add(button(tr("Find a move","ابحث عن حركة","Encontrar jogada"),()->{int[] a=GardenEngine.hint(board,rows,cols);hintA=a[0];hintB=a[1];draw();if(a[0]<0&&!won)status.setText(tr("No moves. Restart this deal.","لا حركات متاحة. أعد هذا اللغز.","Sem jogadas. Reinicie este desafio."));}));
        add(button(tr("Restart this puzzle","أعد هذا اللغز","Reiniciar este enigma"),()->start(mode,rows,cols,seed,daily)));
        add(button(tr("Garden home","الحديقة الرئيسية","Início do jardim"),this::home));
    }
    private void draw(){
        grid.removeAllViews();boolean memory=mode.equals("memory");
        int remaining=0;for(int x:board)if(x!=0)remaining++;
        status.setText(won?tr("Garden complete!","أكملت الحديقة!","Jardim concluído!"):tr("Moves: ","الحركات: ","Jogadas: ")+moves+"   •   "+tr("Pairs left: ","الأزواج المتبقية: ","Pares restantes: ")+remaining/2+(daily?"   ◈":""));
        int width=Math.max(dp(40),(getResources().getDisplayMetrics().widthPixels-dp(36))/cols-dp(6));
        int height=Math.min(dp(70),Math.max(dp(48),width));
        for(int i=0;i<board.length;i++){final int idx=i;int val=board[i];boolean exposed=!memory||i==first||i==second;Button tile=new Button(this);tile.setAllCaps(false);tile.setMinWidth(0);tile.setMinimumWidth(0);tile.setMinHeight(0);tile.setMinimumHeight(0);tile.setPadding(0,0,0,0);tile.setTextSize(cols==6?20:26);tile.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            tile.setText(val==0?"":exposed?MARK[val]:"◆");tile.setTextColor(exposed?JADE:GOLD);
            tile.setBackground(gradient(val==0?DARK:exposed?0xfffff0c5:0xff436fa2,val==0?DARK:exposed?0xffffd99e:0xff2b4d7b,12));
            if(val!=0){tile.setElevation(dp(4));if(i==first||i==second||i==hintA||i==hintB){tile.setBackground(panel(CORAL,GOLD,12));tile.setTextColor(DARK);}}
            tile.setContentDescription(val==0?tr("Empty","فارغ","Vazio"):exposed?tr("Stone ","حجر ","Pedra ")+val:tr("Hidden stone ","حجر مخفي ","Pedra oculta ")+(i+1));
            GridLayout.LayoutParams p=new GridLayout.LayoutParams(GridLayout.spec(i/cols),GridLayout.spec(i%cols,1,1f));p.width=0;p.height=height;p.setMargins(dp(3),dp(3),dp(3),dp(3));grid.addView(tile,p);
            tile.setEnabled(val!=0&&!won);tile.setOnClickListener(v->tap(idx));
        }
    }
    private void tap(int i){if(won||locked||board[i]==0)return;hintA=hintB=-1;
        if(first<0){first=i;draw();return;}if(first==i){first=-1;draw();return;}
        int a=first;second=i;moves++;
        boolean ok=mode.equals("memory")?board[a]==board[i]:GardenEngine.connects(board,rows,cols,a,i);
        if(ok){board[a]=board[i]=0;first=second=-1; if(GardenEngine.empty(board)){won=true;String key=daily?"daily-"+seed:"deal-"+mode+"-"+seed;if(!prefs.getBoolean(key,false)){prefs.edit().putBoolean(key,true).putInt("completed",prefs.getInt("completed",0)+1).apply();}if(campaignLevel>0&&!prefs.getBoolean("campaign-win-"+campaignLevel,false)){int next=Math.min(12,campaignLevel+1);prefs.edit().putBoolean("campaign-win-"+campaignLevel,true).putInt("campaign-unlocked",Math.max(next,prefs.getInt("campaign-unlocked",1))).putInt("campaign-stars",prefs.getInt("campaign-stars",0)+3).apply();}}draw();if(!won&&mode.equals("routes")&&GardenEngine.hint(board,rows,cols)[0]<0)status.setText(tr("No moves. Restart this deal.","لا حركات متاحة. أعد هذا اللغز.","Sem jogadas. Reinicie este desafio."));return;}
        if(mode.equals("memory")){locked=true;draw();final int g=generation;handler.postDelayed(()->{if(g==generation&&!isFinishing()){first=second=-1;locked=false;draw();}},750);}
        else{first=i;second=-1;draw();status.setText(tr("No clear path or different stones. Try another pair.","المسار غير متاح أو الحجران مختلفان. جرّب زوجًا آخر.","Sem caminho livre ou pedras diferentes. Tente outro par."));}
    }
}
