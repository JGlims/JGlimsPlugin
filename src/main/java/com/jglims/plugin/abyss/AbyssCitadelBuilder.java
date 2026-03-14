package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import java.util.Random;

public class AbyssCitadelBuilder {
    private final JGlimsPlugin plugin;
    private final World world;
    private final Random rng = new Random(42);
    private static final Material W1 = Material.DEEPSLATE_BRICKS;
    private static final Material W2 = Material.DEEPSLATE_TILES;
    private static final Material WA = Material.POLISHED_DEEPSLATE;
    private static final Material FP = Material.DEEPSLATE_TILES;
    private static final Material FA = Material.POLISHED_BLACKSTONE;
    private static final Material PIL = Material.POLISHED_DEEPSLATE;
    private static final Material STR = Material.DEEPSLATE_BRICK_STAIRS;
    private static final Material OBS = Material.OBSIDIAN;
    private static final Material CRY = Material.CRYING_OBSIDIAN;
    private static final Material AME = Material.AMETHYST_BLOCK;
    private static final Material IB = Material.IRON_BARS;
    private static final Material SL = Material.SOUL_LANTERN;
    private static final Material SF = Material.SOUL_CAMPFIRE;
    private static final Material ES = Material.END_STONE_BRICKS;
    private static final Material BK = Material.BEDROCK;
    private static final Material BA = Material.BARRIER;
    private static final Material CH;
    static { Material r; try{r=Material.valueOf("CHAIN");}catch(Exception e){r=Material.IRON_BARS;} CH=r; }
    private static final int OH = 70, OWH = 25, KH = 30, KWH = 35, KF = 4, FH = 7, TS = 9, TH = 45, AR = 30, AD = 20;
    private int sY;

    public AbyssCitadelBuilder(JGlimsPlugin plugin, World world) { this.plugin = plugin; this.world = world; }

    public void build() {
        long start = System.currentTimeMillis(); sY = findSurface();
        plugin.getLogger().info("[Citadel] Building at Y=" + sY);
        buildFoundation(); buildOuterWalls(); buildCornerTowers(); buildGrandSouthGate();
        buildCourtyard(); buildInnerKeep(); buildKeepFloors(); buildGrandStaircase();
        buildArena(); buildArenaStairwell(); buildArenaBarriers();
        buildWingCorridors(); buildWingRooms(); buildDecorations(); buildApproachPath();
        plugin.getLogger().info("[Citadel] Complete in " + (System.currentTimeMillis()-start) + "ms");
    }

    private void buildFoundation() {
        plugin.getLogger().info("[Citadel] Laying foundation...");
        for(int x=-OH-2;x<=OH+2;x++)for(int z=-OH-2;z<=OH+2;z++){
            int ty=findTerrainY(x,z);
            for(int y=ty;y<=sY;y++) s(x,y,z,y==sY?FP:W1);
            for(int y=sY+1;y<=sY+KWH+15;y++){Block b=world.getBlockAt(x,y,z);if(b.getType().isSolid()&&b.getType()!=BK)b.setType(Material.AIR);}
        }
    }

    private void buildOuterWalls() {
        plugin.getLogger().info("[Citadel] Building outer walls...");
        int top=sY+OWH;
        for(int y=sY;y<=top;y++){Material m=y%3==0?WA:W1;for(int x=-OH;x<=OH;x++){s(x,y,-OH,m);s(x,y,OH,m);}for(int z=-OH;z<=OH;z++){s(-OH,y,z,m);s(OH,y,z,m);}}
        for(int x=-OH;x<=OH;x+=2){s(x,top+1,-OH,W2);s(x,top+1,OH,W2);}
        for(int z=-OH;z<=OH;z+=2){s(-OH,top+1,z,W2);s(OH,top+1,z,W2);}
        for(int i=-OH+7;i<=OH-7;i+=14){bb(i,sY,-OH,0,-1);bb(i,sY,OH,0,1);bb(-OH,sY,i,-1,0);bb(OH,sY,i,1,0);}
    }
    private void bb(int bx,int by,int bz,int dx,int dz){for(int y=0;y<OWH-2;y++){int d=Math.max(1,(OWH-2-y)/5);for(int i=1;i<=d;i++)s(bx+dx*i,by+y,bz+dz*i,W1);}}

    private void buildCornerTowers() {
        plugin.getLogger().info("[Citadel] Building corner towers...");
        bt(-OH,sY,-OH);bt(OH-TS+1,sY,-OH);bt(-OH,sY,OH-TS+1);bt(OH-TS+1,sY,OH-TS+1);
    }
    private void bt(int tx,int ty,int tz){
        for(int y=0;y<TH;y++){Material m=y%4==0?WA:W1;for(int x=0;x<TS;x++)for(int z=0;z<TS;z++){boolean e=x==0||x==TS-1||z==0||z==TS-1;if(e)s(tx+x,ty+y,tz+z,m);else if(y%FH==0)s(tx+x,ty+y,tz+z,FP);}}
        for(int r=TS/2;r>=0;r--){int y=ty+TH+(TS/2-r);int cx=tx+TS/2,cz=tz+TS/2;for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++)s(cx+x,y,cz+z,W2);}
        s(tx+TS/2,ty+TH+TS/2+1,tz+TS/2,SL);
        for(int y=3;y<TH-2;y+=FH){s(tx+TS/2,ty+y,tz,Material.AIR);s(tx+TS/2,ty+y,tz+TS-1,Material.AIR);s(tx,ty+y,tz+TS/2,Material.AIR);s(tx+TS-1,ty+y,tz+TS/2,Material.AIR);}
    }

    private void buildGrandSouthGate() {
        plugin.getLogger().info("[Citadel] Building grand south gate...");
        int gw=5,gz=OH;
        for(int x=-gw+1;x<=gw-1;x++)for(int y=sY+1;y<sY+15;y++)s(x,y,gz,Material.AIR);
        for(int y=sY;y<=sY+18;y++){Material m=y%3==0?OBS:PIL;for(int dx=0;dx<3;dx++){s(-gw-1+dx,y,gz,m);s(-gw-1+dx,y,gz+1,m);s(gw-1+dx,y,gz,m);s(gw-1+dx,y,gz+1,m);}}
        for(int x=-gw+1;x<=gw-1;x++)for(int dy=0;dy<3;dy++)s(x,sY+15+dy,gz,WA);
        for(int x=-gw+1;x<=gw-1;x++){s(x,sY+14,gz,IB);s(x,sY+13,gz,IB);}
        s(-gw-2,sY+19,gz,SF);s(gw+2,sY+19,gz,SF);
        for(int x=-gw+2;x<=gw-2;x+=3)for(int dy=1;dy<=3;dy++)s(x,sY+13-dy,gz,CH);
        for(int dy=0;dy<4;dy++){s(-gw-1,sY+15-dy,gz-1,Material.PURPLE_WOOL);s(gw+1,sY+15-dy,gz-1,Material.PURPLE_WOOL);}
        for(int x=-gw;x<=gw;x++)for(int dz=-2;dz<=2;dz++)s(x,sY,gz+dz,FA);
    }

    private void buildCourtyard() {
        plugin.getLogger().info("[Citadel] Building courtyard...");
        for(int x=-OH+2;x<=OH-2;x++)for(int z=-OH+2;z<=OH-2;z++){if(Math.abs(x)<=KH+2&&Math.abs(z)<=KH+2)continue;Material m;int d=Math.abs(x)+Math.abs(z);if(d%7==0)m=CRY;else if((x+z)%3==0)m=FA;else m=FP;s(x,sY,z,m);for(int y=sY+1;y<=sY+4;y++){Block b=world.getBlockAt(x,y,z);if(b.getType()!=Material.AIR)b.setType(Material.AIR);}}
        for(int x=-OH+10;x<=OH-10;x+=15)for(int z=-OH+10;z<=OH-10;z+=15){if(Math.abs(x)<=KH+5&&Math.abs(z)<=KH+5)continue;for(int y=0;y<=5;y++)s(x,sY+y,z,PIL);s(x,sY+6,z,SL);}
    }

    private void buildInnerKeep() {
        plugin.getLogger().info("[Citadel] Building inner keep...");
        int top=sY+KWH;
        for(int y=sY;y<=top;y++){Material m=y%4==0?WA:W1;for(int x=-KH;x<=KH;x++){s(x,y,-KH,m);s(x,y,KH,m);}for(int z=-KH;z<=KH;z++){s(-KH,y,z,m);s(KH,y,z,m);}}
        for(int x=-KH;x<=KH;x+=2){s(x,top+1,-KH,W2);s(x,top+1,KH,W2);}
        for(int z=-KH;z<=KH;z+=2){s(-KH,top+1,z,W2);s(KH,top+1,z,W2);}
        for(int x=-2;x<=2;x++)for(int y=sY+1;y<=sY+7;y++)s(x,y,KH,Material.AIR);
        for(int x=-3;x<=3;x++)s(x,sY+8,KH,WA);
    }

    private void buildKeepFloors() {
        plugin.getLogger().info("[Citadel] Building keep floors...");
        for(int fl=0;fl<KF;fl++){int fy=sY+(fl*FH);
            for(int x=-KH+1;x<=KH-1;x++)for(int z=-KH+1;z<=KH-1;z++){s(x,fy,z,(x+z)%2==0?FP:FA);for(int y=fy+1;y<fy+FH;y++)s(x,y,z,Material.AIR);}
            for(int x=-KH+5;x<=KH-5;x+=10)for(int z=-KH+5;z<=KH-5;z+=10)for(int y=fy;y<fy+FH;y++)s(x,y,z,PIL);
            if(fl<KF-1)for(int x=-KH+1;x<=KH-1;x+=5)for(int z=-KH+1;z<=KH-1;z+=5)s(x,fy+FH-1,z,SL);
            bfr(fl,fy);
        }
    }
    private void bfr(int fl,int fy){int ty=fy+FH-1;switch(fl){case 0:br(-KH+2,fy,-KH+2,KH-2,ty,-2,true);br(-KH+2,fy,3,-5,ty,KH-2,true);br(5,fy,3,KH-2,ty,KH-2,true);break;case 1:br(-KH+2,fy,-KH+2,-2,ty,KH-2,true);br(2,fy,-KH+2,KH-2,ty,KH-2,true);break;case 2:br(-KH+2,fy,-KH+2,KH-2,ty,KH-2,false);s(0,fy+1,-KH+4,Material.POLISHED_BLACKSTONE_STAIRS);s(-1,fy+1,-KH+4,OBS);s(1,fy+1,-KH+4,OBS);for(int dy=1;dy<=4;dy++){s(-2,fy+dy,-KH+4,PIL);s(2,fy+dy,-KH+4,PIL);}break;case 3:br(-KH+2,fy,-KH+2,KH-2,ty,KH-2,false);s(0,fy+1,0,AME);s(0,fy+2,0,AME);s(0,fy+3,0,Material.END_ROD);s(-1,fy+1,-1,AME);s(1,fy+1,-1,AME);s(-1,fy+1,1,AME);s(1,fy+1,1,AME);break;}}
    private void br(int x1,int y1,int z1,int x2,int y2,int z2,boolean loot){
        for(int y=y1+1;y<=y2;y++){for(int x=x1;x<=x2;x++){s(x,y,z1,W2);s(x,y,z2,W2);}for(int z=z1;z<=z2;z++){s(x1,y,z,W2);s(x2,y,z,W2);}}
        int mx=(x1+x2)/2,mz=(z1+z2)/2;for(int dy=1;dy<=3;dy++){s(mx,y1+dy,z2,Material.AIR);if(mx+1<=x2)s(mx+1,y1+dy,z2,Material.AIR);s(x1,y1+dy,mz,Material.AIR);s(x1,y1+dy,mz+1,Material.AIR);}
        s(mx,y2-1,mz,SL);if(loot){s(x1+2,y1+1,z1+2,Material.CHEST);s(x2-2,y1+1,z2-2,Material.CHEST);}
    }

    private void buildGrandStaircase() {
        plugin.getLogger().info("[Citadel] Building grand staircase...");
        int sx=KH-6;for(int fl=0;fl<KF-1;fl++){int sy=sY+(fl*FH);for(int st=0;st<FH;st++){int z=-3+st;s(sx,sy+st+1,z,STR);s(sx+1,sy+st+1,z,STR);s(sx-1,sy+st+2,z,IB);for(int dy=1;dy<=3;dy++){s(sx,sy+st+1+dy,z,Material.AIR);s(sx+1,sy+st+1+dy,z,Material.AIR);}}}
    }

    private void buildArena() {
        plugin.getLogger().info("[Citadel] Building arena...");
        int ay=sY-AD;
        for(int x=-AR;x<=AR;x++)for(int z=-AR;z<=AR;z++){if(x*x+z*z>AR*AR)continue;s(x,ay,z,BK);for(int y=ay+1;y<sY;y++)s(x,y,z,Material.AIR);}
        for(int y=ay;y<=ay+AD+5;y++)for(int a=0;a<360;a++){double r=Math.toRadians(a);s((int)Math.round(AR*Math.cos(r)),y,(int)Math.round(AR*Math.sin(r)),BK);}
        for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)if(x*x+z*z<=9)s(x,ay+1,z,OBS);
        s(0,ay+2,0,Material.LODESTONE);
        for(int a=0;a<360;a+=20){double r=Math.toRadians(a);s((int)Math.round((AR-2)*Math.cos(r)),ay+1,(int)Math.round((AR-2)*Math.sin(r)),SF);}
        for(int x=-AR+2;x<=AR-2;x+=5)for(int z=-AR+2;z<=AR-2;z+=5)if(x*x+z*z<(AR-2)*(AR-2))s(x,ay,z,CRY);
    }

    private void buildArenaBarriers() {
        plugin.getLogger().info("[Citadel] Building arena barriers...");
        int ay=sY-AD;
        for(int y=ay+AD+6;y<=ay+AD+25;y++)for(int a=0;a<360;a++){double r=Math.toRadians(a);s((int)Math.round((AR+1)*Math.cos(r)),y,(int)Math.round((AR+1)*Math.sin(r)),BA);}
        for(int x=-AR-1;x<=AR+1;x++)for(int z=-AR-1;z<=AR+1;z++)if(x*x+z*z<=(AR+1)*(AR+1))s(x,ay+AD+25,z,BA);
        for(int a=0;a<360;a+=15){double r=Math.toRadians(a);int x=(int)Math.round((AR-1)*Math.cos(r)),z=(int)Math.round((AR-1)*Math.sin(r));int h=3+rng.nextInt(4);for(int dy=1;dy<=h;dy++)s(x,ay+dy,z,ES);s(x,ay+h+1,z,Material.POINTED_DRIPSTONE);}
    }

    private void buildArenaStairwell() {
        plugin.getLogger().info("[Citadel] Building arena stairwell...");
        int ay=sY-AD,sx=-2,ex=2,sz=-KH+3;
        for(int x=sx;x<=ex;x++)for(int z=sz;z<=sz+3;z++)s(x,sY,z,Material.AIR);
        for(int y=sY;y>ay;y--){int st=sY-y,zo=st%8;boolean gn=(st/8)%2==0;int tz=gn?(sz+zo):(sz+7-zo);for(int x=sx;x<=ex;x++){s(x,y,tz,STR);s(sx-1,y,tz,W1);s(ex+1,y,tz,W1);for(int dy=1;dy<=3;dy++)s(x,y+dy,tz,Material.AIR);}if(st%4==0)s(sx-1,y+2,tz,SL);}
        for(int x=sx;x<=ex;x++)for(int dy=1;dy<=4;dy++)s(x,ay+dy,AR-1,Material.AIR);
    }

    private void buildWingCorridors() {
        plugin.getLogger().info("[Citadel] Building wing corridors...");
        int ch=5;
        for(int z=-KH+5;z<=KH-5;z++){for(int y=sY;y<=sY+ch;y++){s(KH+1,y,z,W1);s(KH+5,y,z,W1);if(y==sY)for(int x=KH+2;x<=KH+4;x++)s(x,y,z,FP);else for(int x=KH+2;x<=KH+4;x++)s(x,y,z,Material.AIR);}for(int x=KH+1;x<=KH+5;x++)s(x,sY+ch+1,z,W2);}
        for(int z=-KH+5;z<=KH-5;z++){for(int y=sY;y<=sY+ch;y++){s(-KH-1,y,z,W1);s(-KH-5,y,z,W1);if(y==sY)for(int x=-KH-4;x<=-KH-2;x++)s(x,y,z,FP);else for(int x=-KH-4;x<=-KH-2;x++)s(x,y,z,Material.AIR);}for(int x=-KH-5;x<=-KH-1;x++)s(x,sY+ch+1,z,W2);}
        for(int dy=1;dy<=3;dy++){s(KH,sY+dy,0,Material.AIR);s(-KH,sY+dy,0,Material.AIR);}
        for(int z=-KH+7;z<=KH-7;z+=5){s(KH+3,sY+ch,z,SL);s(-KH-3,sY+ch,z,SL);}
    }

    private void buildWingRooms() {
        plugin.getLogger().info("[Citadel] Building wing rooms...");
        for(int i=0;i<4;i++){int rz=-KH+8+(i*12),rx=KH+6;bwr(rx,sY,rz,rx+10,sY+5,rz+10);for(int dy=1;dy<=3;dy++)s(KH+5,sY+dy,rz+5,Material.AIR);}
        for(int i=0;i<4;i++){int rz=-KH+8+(i*12),rx=-KH-16;bwr(rx,sY,rz,rx+10,sY+5,rz+10);for(int dy=1;dy<=3;dy++)s(-KH-5,sY+dy,rz+5,Material.AIR);}
    }
    private void bwr(int x1,int y1,int z1,int x2,int y2,int z2){
        for(int y=y1;y<=y2;y++){for(int x=x1;x<=x2;x++){s(x,y,z1,W1);s(x,y,z2,W1);}for(int z=z1;z<=z2;z++){s(x1,y,z,W1);s(x2,y,z,W1);}}
        for(int x=x1+1;x<x2;x++)for(int z=z1+1;z<z2;z++){s(x,y1,z,FP);for(int y=y1+1;y<y2;y++)s(x,y,z,Material.AIR);}
        for(int x=x1;x<=x2;x++)for(int z=z1;z<=z2;z++)s(x,y2,z,W2);
        s((x1+x2)/2,y2-1,(z1+z2)/2,SL);s(x1+2,y1+1,z1+2,Material.CHEST);s(x2-2,y1+1,z2-2,Material.CHEST);
    }

    private void buildDecorations() {
        plugin.getLogger().info("[Citadel] Adding decorations...");
        int[][] kc={{-KH,KH},{KH,KH},{-KH,-KH},{KH,-KH}};for(int[] p:kc)for(int dy=0;dy<3;dy++)s(p[0],sY+KWH+2+dy,p[1],Material.END_ROD);
        for(int i=0;i<20;i++){int x=-OH+5+rng.nextInt(OH*2-10),z=-OH+5+rng.nextInt(OH*2-10);if(Math.abs(x)<KH+5&&Math.abs(z)<KH+5)continue;s(x,sY+1,z,Material.AMETHYST_CLUSTER);}
        for(int x=-OH+7;x<=OH-7;x+=14)for(int dy=0;dy<3;dy++){s(x,sY+OWH-dy,-OH+1,Material.PURPLE_WOOL);s(x,sY+OWH-dy,OH-1,Material.PURPLE_WOOL);}
    }

    private void buildApproachPath() {
        plugin.getLogger().info("[Citadel] Building approach path...");
        for(int z=OH;z<=90;z++)for(int x=-3;x<=3;x++){int ty=findTerrainY(x,z);for(int y=ty;y<=sY;y++)s(x,y,z,Math.abs(x)<=1?FA:FP);if(Math.abs(x)==3&&z%8==0){for(int dy=1;dy<=4;dy++)s(x,sY+dy,z,PIL);s(x,sY+5,z,SL);}}
    }

    private void s(int x,int y,int z,Material m){world.getBlockAt(x,y,z).setType(m);}
    private int findSurface(){for(int y=120;y>1;y--)if(world.getBlockAt(0,y,0).getType().isSolid())return y;return 55;}
    private int findTerrainY(int x,int z){for(int y=120;y>1;y--)if(world.getBlockAt(x,y,z).getType().isSolid())return y;return 50;}
}