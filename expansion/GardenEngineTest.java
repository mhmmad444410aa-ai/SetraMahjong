import com.mhmmad.setramahjong.expansion.GardenEngine;
import java.util.*;
public final class GardenEngineTest {
 static void check(boolean value,String detail){if(!value)throw new AssertionError(detail);}
 public static void main(String[] args){
  check(GardenEngine.connects(new int[]{1,2,1,0,0,0},2,3,0,2),"border path");
  check(!GardenEngine.connects(new int[]{1,2,3,4},2,2,0,1),"different symbols");
  int[] blocked={2,2,2,2,1,2,1,2,2,2,2,2,2,2,2,2};
  check(!GardenEngine.connects(blocked,4,4,4,6),"blocked interior target");
  for(int[] s:new int[][]{{4,4},{4,6},{6,6}})for(int seed=0;seed<100;seed++){
    GardenEngine.Deal d=GardenEngine.routes(s[0],s[1],seed);int[] board=d.tiles.clone();
    check(Arrays.equals(board,GardenEngine.routes(s[0],s[1],seed).tiles),"deterministic deal");
    for(int i=0;i<d.solution.length;i+=2){int a=d.solution[i],b=d.solution[i+1];check(GardenEngine.connects(board,s[0],s[1],a,b),"invalid solution seed "+seed);board[a]=board[b]=0;}
    check(GardenEngine.empty(board),"uncleared deal");
  }
  for(int n:new int[]{16,24})for(int seed=0;seed<100;seed++){int[] m=GardenEngine.memory(n,seed);int[] count=new int[17];for(int v:m)count[v]++;for(int i=1;i<=n/2;i++)check(count[i]==2,"memory pairs");}
  System.out.println("PASS: 300 solvable deterministic route deals and 200 memory deals");
 }
}
