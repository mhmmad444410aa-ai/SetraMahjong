package com.mhmmad.setramahjong;
import java.util.*;
public final class Puzzle {
 public static final int CLASSIC=0, ROUTES=1, MEMORY=2;
 public final int mode,rows,cols; public final int[] tiles; public final long seed;
 public int moves=0;
 public Puzzle(int mode,int rows,int cols,long seed){
  if(mode<0||mode>2||rows<2||cols<2||cols%2!=0)throw new IllegalArgumentException();
  this.mode=mode;this.rows=rows;this.cols=cols;this.seed=seed;
  int n=rows*cols; tiles=new int[mode==CLASSIC?2*n:n];Random r=new Random(seed);
  if(mode==MEMORY){List<Integer> a=new ArrayList<>();for(int i=0;i<n/2;i++){a.add(i+1);a.add(i+1);}Collections.shuffle(a,r);for(int i=0;i<n;i++)tiles[i]=a.get(i);}
  else if(mode==CLASSIC){for(int y=0;y<rows;y++)for(int x=0;x<cols/2;x++){int v=1+r.nextInt(12);tiles[y*cols+x]=tiles[y*cols+cols-1-x]=v;}
   for(int y=1;y<rows-1;y++)for(int x=1;x<cols/2;x++){int v=1+r.nextInt(12);tiles[n+y*cols+x]=tiles[n+y*cols+cols-1-x]=v;}}
  else {for(int y=0;y<rows;y++)for(int x=0;x<cols;x+=2){int v=1+r.nextInt(12);tiles[y*cols+x]=tiles[y*cols+x+1]=v;}}
 }
 public int visible(int cell){int n=rows*cols;return mode==CLASSIC&&tiles[n+cell]!=0?n+cell:cell;}
 public boolean free(int i){if(i<0||i>=tiles.length||tiles[i]==0)return false;if(mode!=CLASSIC)return true;
  int n=rows*cols,x=i%cols;if(i<n&&tiles[i+n]!=0)return false;return x==0||tiles[i-1]==0||x==cols-1||tiles[i+1]==0;}
 public boolean matches(int a,int b){if(a==b||!free(a)||!free(b)||tiles[a]!=tiles[b])return false;return mode!=ROUTES||path(a,b);}
 public boolean remove(int a,int b){if(!matches(a,b))return false;tiles[a]=tiles[b]=0;return true;}
 public int remaining(){int n=0;for(int t:tiles)if(t!=0)n++;return n;}
 public int[] hint(){for(int a=0;a<tiles.length;a++)for(int b=a+1;b<tiles.length;b++)if(matches(a,b))return new int[]{a,b};return new int[]{-1,-1};}
 private boolean path(int a,int b){
  int h=rows+2,w=cols+2;int sx=a%cols+1,sy=a/cols+1,tx=b%cols+1,ty=b/cols+1;
  int[][][] best=new int[h][w][4];for(int[][] row:best)for(int[] p:row)Arrays.fill(p,3);
  int[] dx={1,0,-1,0},dy={0,1,0,-1};ArrayDeque<int[]> q=new ArrayDeque<>();
  for(int d=0;d<4;d++){best[sy][sx][d]=0;q.add(new int[]{sx,sy,d,0});}
  while(!q.isEmpty()){int[] p=q.remove();for(int d=0;d<4;d++){int x=p[0]+dx[d],y=p[1]+dy[d],t=p[3]+(d==p[2]?0:1);
   if(x<0||x>=w||y<0||y>=h||t>2||t>=best[y][x][d])continue;
   if(x==tx&&y==ty)return true;
   if(x>0&&x<w-1&&y>0&&y<h-1&&tiles[(y-1)*cols+x-1]!=0)continue;
   best[y][x][d]=t;q.add(new int[]{x,y,d,t});}}
  return false;
 }
}
