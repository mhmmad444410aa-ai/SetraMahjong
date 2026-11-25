import com.mhmmad.setramahjong.Puzzle;
public class PuzzleTest {
 static void check(boolean x){if(!x)throw new AssertionError();}
 public static void main(String[] args){for(int seed=0;seed<100;seed++){for(int m=0;m<3;m++){Puzzle p=new Puzzle(m,4,6,seed);check(p.remaining()>0);check(!p.matches(0,0));if(m==0){for(int z=1;z>=0;z--)for(int y=0;y<4;y++)for(int x=0;x<3;x++){int a=z*24+y*6+x,b=z*24+y*6+5-x;if(p.tiles[a]!=0)check(p.remove(a,b));}}else if(m==1){for(int y=0;y<4;y++)for(int x=0;x<6;x+=2)check(p.remove(y*6+x,y*6+x+1));}else{while(p.remaining()>0){int[] h=p.hint();check(h[0]>=0&&p.remove(h[0],h[1]));}}check(p.remaining()==0);}}System.out.println("300 generated puzzles verified");}
}
