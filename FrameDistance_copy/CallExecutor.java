import java.io.IOException;
import java.io.*;

public class CallExecutor{
  public static void main(String[] args){

    int[] min = {10,9,8,7};
    double[] demand = {1.00,1.05,1.10,1.15};

    for(int i = 0; i < min.length; i++){
      for(int j = 0; j < demand.length; j++){
        String s_min = String.valueOf(min[i]);
        String s_demand = String.valueOf(demand[j]);
        String filename = "test_" + s_min + "_" + s_demand + ".csv";

        try{
          FileWriter fiw = new FileWriter(new File(filename));
          fiw.write("");
          fiw.close();
        }catch(IOException e){
          e.printStackTrace();
        }

        String result;
        for(int i = 0; i < 100; i++){
          try{
            Runtime rt = Runtime.getRuntime();
            Process p = rt.exec("java Executor " + s_min + " " + s_demand);
            InputStream is = p.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            while((result = br.readLine()) != null){
              System.out.println(result);
              FileWriter fw = new FileWriter(new File(filename), true);
              fw.write(result + "\n");
              fw.close();
            }

          }catch(IOException e){
            e.printStackTrace();
          }
        }
      }
    }



/*

    final String filename = "test_9_1-05.csv";

    try{
      FileWriter fiw = new FileWriter(new File(filename));
      fiw.write("");
      fiw.close();
    }catch(IOException e){
      e.printStackTrace();
    }

    String result;
    for(int i = 0; i < 100; i++){
      try{
        Runtime rt = Runtime.getRuntime();
        Process p = rt.exec("java Executor");
        InputStream is = p.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        while((result = br.readLine()) != null){
          System.out.println(result);
          FileWriter fw = new FileWriter(new File(filename), true);
          fw.write(result + "\n");
          fw.close();
        }

      }catch(IOException e){
        e.printStackTrace();
      }
    }

*/
  }
}
