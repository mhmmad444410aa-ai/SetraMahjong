package com.mhmmad.setramahjong.expansion;

import java.util.*;

public final class GardenEngine {
    private GardenEngine() {}
    public static final class Deal {
        public final int[] tiles, solution;
        Deal(int[] t,int[] s){tiles=t;solution=s;}
    }
    // Paths may travel through empty cells and a one-cell border; at most two bends.
    public static boolean connects(int[] board,int rows,int cols,int a,int b) {
        return a>=0 && b>=0 && a<board.length && b<board.length && a!=b &&
            board[a]>0 && board[a]==board[b] && path(board,rows,cols,a,b);
    }
    static boolean path(int[] board,int rows,int cols,int a,int b) {
        int h=rows+2,w=cols+2, sr=a/cols+1, sc=a%cols+1,tr=b/cols+1,tc=b%cols+1;
        int[][][] best=new int[h][w][4];
        for(int[][] rr:best) for(int[] cc:rr) Arrays.fill(cc,3);
        ArrayDeque<int[]> q=new ArrayDeque<>();
        int[] dr={-1,0,1,0},dc={0,1,0,-1};
        for(int d=0;d<4;d++){best[sr][sc][d]=0;q.add(new int[]{sr,sc,d,0});}
        while(!q.isEmpty()){
            int[] p=q.remove(); if(p[3]!=best[p[0]][p[1]][p[2]])continue;
            for(int d=0;d<4;d++){
                int r=p[0]+dr[d],c=p[1]+dc[d],n=p[3]+(d==p[2]?0:1);
                if(r<0||r>=h||c<0||c>=w||n>2||n>=best[r][c][d])continue;
                if(r==tr&&c==tc)return true;
                if(r>0&&r<h-1&&c>0&&c<w-1&&board[(r-1)*cols+c-1]!=0)continue;
                best[r][c][d]=n;q.add(new int[]{r,c,d,n});
            }
        }
        return false;
    }
    public static Deal routes(int rows,int cols,long seed) {
        if(rows<2||cols<2||rows*cols%2!=0||rows*cols>48)throw new IllegalArgumentException();
        Random random=new Random(seed); int size=rows*cols;
        // Remove legal pairs from a full occupancy grid, assigning symbols in removal order.
        // The recorded solution is verified by tests; retry a dead-ended construction.
        for(int attempt=0;attempt<32;attempt++){
            int[] occupied=new int[size],tiles=new int[size],solution=new int[size];
            Arrays.fill(occupied,1); int step=0;
            while(step<size){
                List<Integer> live=new ArrayList<>();
                for(int i=0;i<size;i++)if(occupied[i]!=0)live.add(i);
                Collections.shuffle(live,random); int a=-1,b=-1;
                outer:for(int i=0;i<live.size();i++)for(int j=i+1;j<live.size();j++)
                    if(path(occupied,rows,cols,live.get(i),live.get(j))){a=live.get(i);b=live.get(j);break outer;}
                if(a<0)break;
                int symbol=1+random.nextInt(Math.min(16,size/2));
                tiles[a]=tiles[b]=symbol;occupied[a]=occupied[b]=0;solution[step++]=a;solution[step++]=b;
            }
            if(step==size)return new Deal(tiles,solution);
        }
        // Guaranteed fallback: pair adjacent cells in a serpentine traversal.
        int[] t=new int[size],s=new int[size];
        int k=0;for(int r=0;r<rows;r++)for(int j=0;j<cols;j++)s[k++]=r*cols+(r%2==0?j:cols-1-j);
        for(int i=0;i<size;i+=2)t[s[i]]=t[s[i+1]]=1+random.nextInt(Math.min(16,size/2));
        return new Deal(t,s);
    }
    public static int[] memory(int size,long seed){
        if(size<4||size>32||size%2!=0)throw new IllegalArgumentException();
        List<Integer> v=new ArrayList<>();for(int i=1;i<=size/2;i++){v.add(i);v.add(i);}
        Collections.shuffle(v,new Random(seed));int[] a=new int[size];for(int i=0;i<size;i++)a[i]=v.get(i);return a;
    }
    public static int[] hint(int[] board,int rows,int cols){
        for(int i=0;i<board.length;i++)for(int j=i+1;j<board.length;j++)if(connects(board,rows,cols,i,j))return new int[]{i,j};
        return new int[]{-1,-1};
    }
    public static boolean empty(int[] a){for(int x:a)if(x!=0)return false;return true;}
}
